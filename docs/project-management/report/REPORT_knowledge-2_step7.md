# REPORT: knowledge-2 Step 7 검색 정확도 측정

## 구현 범위

- benchmark 노트 시드: `src/main/resources/search/accuracy/search-benchmark-notes.json`
- 테스트 쿼리 세트: `src/main/resources/search/accuracy/search-test-queries.json`
- 데이터셋 버전: `test-v1`
- benchmark 노트 수: 20건
- 테스트 쿼리 수: 59건
- 평가 기준 top-k: 10
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

## Task 완료 기준 매핑

| Task 요구사항 | 현재 근거 |
| --- | --- |
| 테스트 쿼리 세트 50건 이상 | `search-test-queries.json` 59건 |
| benchmark 노트 시드 경로 작성 | `search-benchmark-notes.json` 20건 |
| Precision@10 / Recall@10 / MRR / NDCG 계산 | `SearchAccuracyService`와 계산기 구현 |
| 3개 검색 모드 모두 측정 | BM25 / semantic / hybrid 리포트 구조 제공 |
| 관리자 전용 실행 API | `POST /api/v1/admin/search/accuracy-test` |
| 관리자 전용 조회 API | `GET /api/v1/admin/search/accuracy-report` |
| 리포트 문서 산출 | 본 문서 + Step 9 후속 튜닝 보고서 |

## 산출물 요약

- 데이터셋
  - benchmark 노트: 20건
  - 테스트 쿼리: 59건
  - relevance score: `0=무관`, `1=관련`, `2=매우 관련`
- 코드
  - `SearchAccuracyService`
  - `PrecisionRecallCalculator`
  - `MrrCalculator`
  - `NdcgCalculator`
- 운영 경로
  - 관리자 권한으로 측정 실행
  - 최신 결과는 조회 API 응답으로 재확인

## 메트릭 결과표 기록 형식

최신 실측값은 `POST /api/v1/admin/search/accuracy-test` 실행 후 `GET /api/v1/admin/search/accuracy-report` 응답으로 확인한다.

| Mode | Query Count | Precision@10 | Recall@10 | MRR | NDCG@10 | 목표 충족 기준 |
| --- | ---: | ---: | ---: | ---: | ---: | --- |
| BM25 | API 응답 기준 | API 응답 기준 | API 응답 기준 | API 응답 기준 | API 응답 기준 | Precision@10 참고 |
| Semantic | API 응답 기준 | API 응답 기준 | API 응답 기준 | API 응답 기준 | API 응답 기준 | 참고 지표 |
| Hybrid | API 응답 기준 | API 응답 기준 | API 응답 기준 | API 응답 기준 | API 응답 기준 | Precision@10 >= 0.6, MRR >= 0.7 |

## 관리자 API 응답 예시

아래 수치는 컨트롤러 응답 예시를 고정하기 위한 테스트 fixture 기준이다. 실측 최신값은 운영/CI 실행 시점의 `/accuracy-report` 응답을 따른다.

```json
{
  "success": true,
  "data": {
    "datasetVersion": "test-v1",
    "semanticAvailable": true,
    "bm25": {
      "queryCount": 50,
      "precisionAt10": 0.82,
      "recallAt10": 0.74,
      "mrr": 0.86,
      "ndcgAt10": 0.88
    },
    "semantic": {
      "queryCount": 50,
      "precisionAt10": 0.80,
      "recallAt10": 0.70,
      "mrr": 0.83,
      "ndcgAt10": 0.85
    },
    "hybrid": {
      "queryCount": 50,
      "precisionAt10": 0.88,
      "recallAt10": 0.78,
      "mrr": 0.90,
      "ndcgAt10": 0.92
    },
    "improvements": [
      "Hybrid MRR이 높아 현재 조합을 기본값으로 유지합니다."
    ]
  }
}
```

## 목표 판정 기준

| 지표 | 목표 | 적용 모드 | 비고 |
| --- | --- | --- | --- |
| Precision@10 | 0.6 이상 | 최소 Hybrid 우선 확인 | Task 제약 기준 |
| MRR | 0.7 이상 | 최소 Hybrid 우선 확인 | Task 제약 기준 |
| Recall@10 | 참고 | BM25 / semantic / hybrid 비교 | 보조 지표 |
| NDCG@10 | 참고 | BM25 / semantic / hybrid 비교 | 랭킹 품질 비교용 |

## 보안/운영 제약

- benchmark 데이터는 리포지토리 내부 JSON만 사용한다.
- 관리자 권한(`ROLE_ADMIN`)이 없는 사용자는 정확도 API에 접근할 수 없다.
- 측정 실행은 서비스 내부에서 직렬화되어 동시 실행을 제한한다.
- semantic 측정 실패 시 전체 실행을 깨지 않고 개선 포인트에 원인을 남긴다.

## 검증 메모

- 자동 테스트에는 deterministic 검증을 위해 semantic client mock을 사용한다.
- live semantic 품질 평가는 `learning-ai` 연결 상태와 인덱싱 상태에 따라 달라질 수 있다.
- 최신 수치는 `GET /api/v1/admin/search/accuracy-report` 응답을 기준으로 확인한다.
- 컨트롤러 응답 예시는 `SearchAccuracyAdminControllerTest` fixture를 기준으로 문서화했다.
