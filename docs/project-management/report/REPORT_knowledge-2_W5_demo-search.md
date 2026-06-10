# REPORT: knowledge-2 W5 발표용 검색 데모 쿼리 검증

## 목적

- 최종 발표에서 사용할 검색 데모 쿼리를 고정한다.
- 기존 Step 7 정확도 리포트, Step 8 검색 E2E, Step 9 튜닝 결과를 기준으로 데모 쿼리의 재현성과 시연 포인트를 정리한다.

## 검증 근거

- benchmark 노트 시드: `src/main/resources/search/accuracy/search-benchmark-notes.json`
- 테스트 쿼리 세트: `src/main/resources/search/accuracy/search-test-queries.json`
- 정확도 리포트 기준: `POST /api/v1/admin/search/accuracy-test`, `GET /api/v1/admin/search/accuracy-report`
- Step 8 근거: `SearchElasticsearchIntegrationTest`의 BM25/nori, hybrid RRF, semantic timeout fallback, Elasticsearch down 시나리오
- Step 9 근거: `REPORT_knowledge-2_step9.md`의 duplicate semantic dedupe, BM25/RRF 튜닝, 회귀 테스트 및 coverage gate 재검증

## 대상 환경

- 기준 환경: CI와 동일한 Docker Compose 검색 검증 환경
- 재검증 이력:
  - 2026-06-05: `clean build -> searchE2eTest` 순서 재현 통과
  - 2026-06-08: `searchE2eTest`, `test`, `jacocoTestCoverageVerification` 포함 CI 경로 재검증 통과
- 전제 조건:
  - benchmark seed가 적재되어 있어야 한다.
  - Elasticsearch/Kafka 검색 경로가 정상 기동해야 한다.
  - semantic/hybrid 데모는 learning-ai semantic proxy contract가 유지된다는 전제에서 시연한다.

## 데모 데이터 전제

- benchmark tenant/user 기준 검색용 노트가 적재되어 있어야 한다.
- 발표에서는 benchmark 쿼리 세트에서 이미 정확도 검증에 사용한 쿼리만 사용한다.
- 기대 결과는 `expectedBenchmarkIds`와 benchmark note title 매핑 기준으로 고정한다.

## 발표용 검색 데모 쿼리 목록

| ID | Mode | Endpoint | Query | Expected Top Results | Demo Point | Preconditions |
|---|---|---|---|---|---|---|
| DQ-01 | BM25 | `GET /api/v1/notes/search?q=...` | `엘라스틱서치 nori 분석기` | `엘라스틱서치 nori 형태소 분석` | 한국어 nori 분석과 키워드 매칭 | benchmark seed loaded |
| DQ-02 | Semantic | `POST /api/v1/ai/search/semantic` | `semantic search on postgresql` | `PostgreSQL pgvector semantic search`, `Hybrid search with semantic ranking and RRF` | semantic 유사도 기반 검색 | semantic proxy available |
| DQ-03 | Hybrid | `POST /api/v1/ai/search/hybrid` | `hybrid search rrf` | `Hybrid search with semantic ranking and RRF` | BM25 + semantic RRF 병합 | ES + semantic both healthy |
| DQ-04 | Hybrid | `POST /api/v1/ai/search/hybrid` | `timeout fallback candidate merge` | `Hybrid search with semantic ranking and RRF` | fallback/merge 설명용 보조 시나리오 | semantic timeout fallback path available |

## 안정성 검증 정리

- DQ-01은 benchmark 쿼리 세트의 BM25 대표 시나리오이며 Step 8 검색 E2E 복구 후 CI 동일 환경에서 반복 검증됐다.
- DQ-02는 benchmark 쿼리 세트에 포함된 semantic 대표 시나리오이며 Step 7 정확도 리포트 대상 쿼리로 유지됐다.
- DQ-03은 benchmark 쿼리 세트와 `SearchElasticsearchIntegrationTest`의 hybrid RRF 경로가 함께 검증하는 대표 시나리오다.
- DQ-04는 발표 메인 시나리오라기보다 fallback 설명용 보조 쿼리이며, semantic timeout 시 BM25 fallback 유지 검증 근거를 제공한다.
- 2026-06-08 튜닝 이후 duplicate semantic hit dedupe와 RRF 중복 가산 방어가 반영되어, 데모 중 same note 과대 노출 리스크를 줄였다.

## 시연 시 주의사항

- semantic/hybrid 데모는 learning-ai semantic proxy 응답 가능 상태를 먼저 확인한다.
- 발표에서는 DQ-01 ~ DQ-03을 기본 시나리오로 사용하고, DQ-04는 fallback 설명이 필요할 때만 사용한다.
- 시연 전 benchmark seed와 검색 인덱스가 최신 상태인지 확인한다.

## 결론

- 발표용 검색 데모 쿼리는 DQ-01 ~ DQ-04로 확정한다.
- 위 쿼리는 benchmark accuracy 세트, Step 8 검색 E2E, Step 9 튜닝/회귀 검증 근거를 공유하므로 발표 시연용 기준 시나리오로 재사용 가능하다.
- 안정 동작 판단은 benchmark seed 적재와 semantic proxy contract 유지라는 전제하에 성립한다.
