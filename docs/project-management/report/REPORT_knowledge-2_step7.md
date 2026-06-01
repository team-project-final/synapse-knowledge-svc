# REPORT: knowledge-2 Step 7 검색 정확도 측정

## 구현 범위

- benchmark 노트 시드: `src/main/resources/search/accuracy/search-benchmark-notes.json`
- 테스트 쿼리 세트: `src/main/resources/search/accuracy/search-test-queries.json`
- 관리자 API:
  - `POST /api/v1/admin/search/accuracy-test`
  - `GET /api/v1/admin/search/accuracy-report`
- 메트릭:
  - Precision@10
  - Recall@10
  - MRR
  - NDCG@10

## 측정 방식

1. 전용 benchmark tenant/user로 검색용 노트를 upsert한다.
2. Elasticsearch 인덱싱 완료를 대기한다.
3. 50개 이상의 한/영 테스트 쿼리를 BM25 / semantic / hybrid 세 모드로 실행한다.
4. 쿼리별 detail과 모드별 aggregate metric을 계산한다.
5. 개선 포인트를 함께 리포트한다.

## 보안/운영 제약

- benchmark 데이터는 리포지토리 내부 JSON만 사용한다.
- 관리자 권한(`ROLE_ADMIN`)이 없는 사용자는 정확도 API에 접근할 수 없다.
- 측정 실행은 서비스 내부에서 직렬화되어 동시 실행을 제한한다.
- semantic 측정 실패 시 전체 실행을 깨지 않고 개선 포인트에 원인을 남긴다.

## 검증 메모

- 자동 테스트에는 deterministic 검증을 위해 semantic client mock을 사용한다.
- live semantic 품질 평가는 `learning-ai` 연결 상태와 인덱싱 상태에 따라 달라질 수 있다.
- 최신 수치는 `GET /api/v1/admin/search/accuracy-report` 응답을 기준으로 확인한다.
