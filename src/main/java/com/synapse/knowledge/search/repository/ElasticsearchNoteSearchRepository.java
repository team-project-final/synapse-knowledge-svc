package com.synapse.knowledge.search.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.analysis.NoriDecompoundMode;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.util.NamedValue;
import com.synapse.knowledge.search.entity.NoteSearchDocument;
import com.synapse.knowledge.search.config.SearchProperties;
import com.synapse.knowledge.search.dto.SearchPageResponse;
import com.synapse.knowledge.search.dto.SearchRequest;
import com.synapse.knowledge.search.dto.SearchResultResponse;
import com.synapse.knowledge.search.service.support.SearchCandidate;
import com.synapse.knowledge.search.service.support.SearchCursorCodec;
import com.synapse.knowledge.search.service.support.SearchCursorCodec.CursorPayload;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ElasticsearchNoteSearchRepository implements NoteSearchRepository {

    static final String INDEX_NAME = "notes-v1";

    private final ElasticsearchClient elasticsearchClient;
    private final SearchCursorCodec searchCursorCodec;
    private final SearchProperties searchProperties;
    private final Object indexInitializationMonitor = new Object();
    private volatile boolean indexEnsured;

    @Override
    public SearchPageResponse searchKeyword(Long userId, SearchRequest request) {
        try {
            ensureIndex();

            SearchResponse<NoteSearchDocument> response = elasticsearchClient.search(buildSearchRequest(
                userId,
                request.query(),
                request.limit() + 1,
                request.tags(),
                request.cursor()
            ), NoteSearchDocument.class);

            return toPageResponse(response, request.limit());
        } catch (IOException ex) {
            throw new IllegalStateException("Elasticsearch 검색 요청에 실패했습니다", ex);
        }
    }

    @Override
    public List<SearchCandidate> searchKeywordCandidates(Long userId, String query, int limit, List<String> tags) {
        try {
            ensureIndex();
            SearchResponse<NoteSearchDocument> response = elasticsearchClient.search(
                buildSearchRequest(userId, query, limit, tags, null),
                NoteSearchDocument.class
            );
            return response.hits().hits().stream()
                .map(this::toSearchCandidate)
                .toList();
        } catch (IOException ex) {
            throw new IllegalStateException("Elasticsearch 후보 검색 요청에 실패했습니다", ex);
        }
    }

    @Override
    public void upsert(NoteSearchDocument document) {
        try {
            ensureIndex();
            elasticsearchClient.index(IndexRequest.of(index -> index
                .index(INDEX_NAME)
                .id(document.noteId().toString())
                .document(new NoteSearchDocument(
                    document.noteId(),
                    document.externalNoteId(),
                    document.tenantId(),
                    document.userId(),
                    document.title(),
                    document.content(),
                    document.tags(),
                    document.updatedAt() == null ? Instant.now() : document.updatedAt()
                ))
            ));
        } catch (IOException ex) {
            throw new IllegalStateException("노트 검색 인덱싱에 실패했습니다", ex);
        }
    }

    @Override
    public void deleteByNoteId(Long noteId) {
        try {
            if (!indexEnsured) {
                return;
            }
            elasticsearchClient.delete(DeleteRequest.of(delete -> delete.index(INDEX_NAME).id(noteId.toString())));
        } catch (IOException ex) {
            throw new IllegalStateException("노트 검색 인덱스 삭제에 실패했습니다", ex);
        }
    }

    private SearchPageResponse toPageResponse(SearchResponse<NoteSearchDocument> response, int limit) {
        List<Hit<NoteSearchDocument>> hits = response.hits().hits();
        boolean hasNext = hits.size() > limit;
        List<Hit<NoteSearchDocument>> pageHits = hasNext ? hits.subList(0, limit) : hits;

        List<SearchResultResponse> results = pageHits.stream()
            .map(this::toSearchCandidate)
            .map(candidate -> new SearchResultResponse(
                candidate.noteId(),
                candidate.title(),
                candidate.highlights(),
                candidate.keywordScore() == null ? 0.0f : candidate.keywordScore()
            ))
            .toList();

        String nextCursor = null;
        if (hasNext && !pageHits.isEmpty()) {
            Hit<NoteSearchDocument> lastHit = pageHits.get(pageHits.size() - 1);
            double score = lastHit.score() == null ? 0.0d : lastHit.score();
            long noteId = lastHit.source() == null ? 0L : lastHit.source().noteId();
            nextCursor = searchCursorCodec.encode(score, noteId);
        }

        long totalCount = response.hits().total() == null ? results.size() : response.hits().total().value();
        return new SearchPageResponse(results, totalCount, nextCursor, hasNext);
    }

    private SearchCandidate toSearchCandidate(Hit<NoteSearchDocument> hit) {
        NoteSearchDocument source = hit.source();
        List<String> highlights = new ArrayList<>();
        Map<String, List<String>> highlightMap = hit.highlight();
        if (highlightMap != null) {
            highlights = highlightMap.values().stream()
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());
        }

        String snippet = highlights.isEmpty()
            ? abbreviate(source == null ? null : source.content(), 180)
            : highlights.get(0);

        return new SearchCandidate(
            source == null ? null : source.noteId(),
            source == null ? null : source.externalNoteId(),
            source == null ? null : source.title(),
            highlights,
            snippet,
            hit.score() == null ? 0.0f : hit.score().floatValue(),
            null
        );
    }

    private java.util.function.Function<co.elastic.clients.elasticsearch.core.SearchRequest.Builder, co.elastic.clients.util.ObjectBuilder<co.elastic.clients.elasticsearch.core.SearchRequest>>
    buildSearchRequest(Long userId, String query, int size, List<String> tags, String cursor) {
        return search -> {
            var builder = search
                .index(INDEX_NAME)
                .size(size)
                .query(searchQuery -> searchQuery.bool(bool -> {
                    bool.must(must -> must.multiMatch(multiMatch -> multiMatch
                        .query(query)
                        .fields(
                            "title^" + searchProperties.bm25().titleBoost(),
                            "content^" + searchProperties.bm25().contentBoost(),
                            "tags^" + searchProperties.bm25().tagBoost()
                        )
                        .operator(Operator.Or)
                        .minimumShouldMatch(searchProperties.bm25().minimumShouldMatch())
                    ));
                    bool.filter(filter -> filter.term(term -> term.field("userId").value(userId)));

                    if (tags != null && !tags.isEmpty()) {
                        bool.filter(filter -> filter.terms(terms -> terms
                            .field("tags.keyword")
                            .terms(values -> values.value(tags.stream()
                                .map(FieldValue::of)
                                .toList()))
                        ));
                    }

                    return bool;
                }))
                .highlight(highlight -> highlight
                    .preTags("<mark>")
                    .postTags("</mark>")
                    .fields(List.of(
                        NamedValue.of("title", HighlightField.of(field -> field.numberOfFragments(0))),
                        NamedValue.of("content", HighlightField.of(field -> field.fragmentSize(150).numberOfFragments(3)))
                    ))
                )
                .sort(sort -> sort.score(score -> score.order(SortOrder.Desc)))
                .sort(sort -> sort.field(field -> field.field("noteId").order(SortOrder.Desc)));

            if (cursor != null && !cursor.isBlank()) {
                CursorPayload payload = searchCursorCodec.decode(cursor);
                builder.searchAfter(List.of(FieldValue.of(payload.score()), FieldValue.of(payload.noteId())));
            }

            return builder;
        };
    }

    private String abbreviate(String content, int maxLength) {
        if (content == null || content.isBlank()) {
            return null;
        }
        return content.length() <= maxLength ? content : content.substring(0, maxLength) + "...";
    }

    private void ensureIndex() throws IOException {
        if (indexEnsured) {
            return;
        }

        synchronized (indexInitializationMonitor) {
            if (indexEnsured) {
                return;
            }

            boolean exists = elasticsearchClient.indices().exists(existsRequest -> existsRequest.index(INDEX_NAME)).value();
            if (!exists) {
                try {
                    elasticsearchClient.indices().create(create -> create
                        .index(INDEX_NAME)
                        .settings(settings -> settings
                            .similarity("bm25_tuned", similarity -> similarity
                                .bm25(bm25 -> bm25
                                    .k1(searchProperties.bm25().k1())
                                    .b(searchProperties.bm25().b())
                                )
                            )
                            .analysis(analysis -> analysis
                                .tokenizer("korean_nori_tokenizer", tokenizer -> tokenizer
                                    .definition(definition -> definition.noriTokenizer(nori -> nori.decompoundMode(NoriDecompoundMode.Mixed)))
                                )
                                .filter("korean_nori_pos_filter", filter -> filter
                                    .definition(definition -> definition.noriPartOfSpeech(pos -> pos
                                        .stoptags("EP", "EF", "EC", "ETN", "ETM", "IC", "JKS", "JKC", "JKG", "JKO", "JKB", "JKV", "JKQ", "JX", "JC", "MAG", "MAJ", "MM", "SP", "SSC", "SSO", "SC", "SE", "XPN", "XSA", "XSN", "XSV", "UNA", "NA", "VSV")
                                    ))
                                )
                                .analyzer("korean_nori", analyzer -> analyzer
                                    .custom(custom -> custom
                                        .tokenizer("korean_nori_tokenizer")
                                        .filter("lowercase", "korean_nori_pos_filter")
                                    )
                                )
                            )
                        )
                        .mappings(mappings -> mappings
                            .properties("noteId", Property.of(property -> property.long_(field -> field)))
                            .properties("externalNoteId", Property.of(property -> property.keyword(field -> field)))
                            .properties("tenantId", Property.of(property -> property.keyword(field -> field)))
                            .properties("userId", Property.of(property -> property.long_(field -> field)))
                            .properties("title", Property.of(property -> property.text(field -> field
                                .analyzer("korean_nori")
                                .similarity("bm25_tuned")
                                .fields("keyword", sub -> sub.keyword(keyword -> keyword))
                            )))
                            .properties("content", Property.of(property -> property.text(field -> field
                                .analyzer("korean_nori")
                                .similarity("bm25_tuned")
                            )))
                            .properties("tags", Property.of(property -> property.text(field -> field
                                .analyzer("korean_nori")
                                .similarity("bm25_tuned")
                                .fields("keyword", sub -> sub.keyword(keyword -> keyword))
                            )))
                            .properties("updatedAt", Property.of(property -> property.date(field -> field)))
                        )
                    );
                } catch (ElasticsearchException ex) {
                    if (!isResourceAlreadyExists(ex)) {
                        throw ex;
                    }
                }
            }

            indexEnsured = true;
        }
    }

    private boolean isResourceAlreadyExists(ElasticsearchException ex) {
        return ex.error() != null && "resource_already_exists_exception".equals(ex.error().type());
    }
}
