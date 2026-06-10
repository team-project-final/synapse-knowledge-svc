# REPORT: knowledge-2 Step 9 검색 튜닝 + 버그 수정

## 구현 범위

- semantic duplicate hit가 같은 note에 중복 가산되던 P0 검색 품질 버그 수정
- BM25 / hybrid / semantic 검색 파라미터 튜닝
- 회귀 테스트 및 coverage gate 재검증

## 튜닝/수정 항목

- `HybridSearchService`
  - semantic hit를 `externalNoteId` 기준으로 note 단위 dedupe
  - 동일 note의 여러 chunk hit 중 최고 `semanticScore`만 유지
- `RrfMergeService`
  - 같은 source에서 같은 note가 반복되어도 RRF 점수를 한 번만 가산
- `ElasticsearchNoteSearchRepository`
  - BM25 쿼리 field boost를 설정 기반으로 전환
  - `minimum_should_match=70%` 적용
  - tuned BM25 similarity(`k1=1.4`, `b=0.65`) 적용
- 설정값 조정
  - `search.ai.threshold=0.55`
  - `search.hybrid.rrf-k=40`
  - `search.hybrid.candidate-multiplier=5`
  - `search.bm25.title-boost=4.0`
  - `search.bm25.content-boost=1.0`
  - `search.bm25.tag-boost=2.5`

## 검증 시나리오

- duplicate semantic hit가 있어도 최종 결과가 note 단위로 중복되지 않는다.
- duplicate semantic hit가 same note에 몰려도 RRF 순위가 과대 상승하지 않는다.
- semantic timeout 시 BM25 fallback이 유지된다.
- benchmark accuracy 경로와 admin accuracy API 관련 테스트가 회귀 없이 통과한다.
- search 모듈 line coverage 80% gate가 유지된다.

## 테스트 데이터

- 기존 benchmark 노트/쿼리 세트 재사용
  - `src/main/resources/search/accuracy/search-benchmark-notes.json`
  - `src/main/resources/search/accuracy/search-test-queries.json`
- `SearchElasticsearchIntegrationTest`에서 duplicate semantic hit 시나리오 추가

## 재검증 결과

- `./gradlew.bat test` 통과
- `./gradlew.bat jacocoTestCoverageVerification` 통과
- 신규/수정 테스트
  - `HybridSearchServiceTest`
  - `RrfMergeServiceTest`
  - `SearchAccuracyServiceTest`
  - `SearchElasticsearchIntegrationTest`

## 실패 항목 기록

- 없음

## API 문서 상태

- 공개 API contract 변경 없음
- endpoint/request/response 스키마 변경이 없어 기존 API 문서는 최신 상태로 유지
