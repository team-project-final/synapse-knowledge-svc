# WORKFLOW: @knowledge-owner-2 — Week 3

> **Task 문서**: [TASK_knowledge-2.md](../task/TASK_knowledge-2.md)
> **기간**: 2026-05-26 ~ 2026-05-29, 4 영업일
> **PRD**: [PRD_W3.md](../prd/PRD_W3.md)

---

## Step 6: 검색 RRF — 시맨틱(pgvector) + BM25(ES) 결합 하이브리드 검색

### 6.1 TASK 시작

- [x] Step Goal / Done When / Scope / Input 확인
- [x] PRD_W3 해당 요구사항 확인 (하이브리드 검색)
- [x] Duration 산정 확인

### 6.2 요구사항 분석

- [x] Reciprocal Rank Fusion (RRF) 알고리즘 분석 (k=60 파라미터)
- [x] learning-ai 시맨틱 검색 프록시 연동 요건 분석
- [x] Elasticsearch BM25 검색 요건 분석 (nori 분석기, 필드 가중치)
- [x] 두 검색 결과 병합 전략 정의
- [x] Instructions 초안 → TASK 문서 반영

### 6.3 Security 1차 검토

- [x] 인증 필요 여부: Yes (로그인 사용자)
- [x] 권한 종류: 본인 노트만 검색 가능
- [x] 검색 결과 접근 제어 필터 적용 필수
- [x] 결과 → TASK Constraints 반영

### 6.4 아키텍처 설계

- [x] 검색 파이프라인 설계 (쿼리 → learning-ai semantic proxy + ES 검색 → RRF 병합)
- [x] BM25 후보 검색과 semantic 후보 검색의 공통 결과 모델 정의
- [x] ES 인덱스 매핑 재사용 여부 확인 (nori 분석기, 필드 가중치)
- [x] RRF 병합 로직 설계 (score = Σ 1/(k + rank_i))
- [x] Duration(final) 갱신

### 6.5 Security 2차 검토

- [x] 검색 쿼리 인젝션 방지 (ES query sanitization)
- [x] 시맨틱 검색 프록시 호출 시 사용자 컨텍스트 전달
- [x] 검색 결과 민감정보 마스킹
- [x] 결과 → TASK Constraints 반영

### 6.6 DTO / Entity 설계 (API First)

- [x] HybridSearchRequest DTO 정의 (query, limit, tags)
- [x] HybridSearchResponse DTO 정의 (results[], totalCount, searchTime, semanticFallback)
- [x] SemanticSearchRequest / SemanticSearchResponse / UnifiedSearchResultResponse DTO 정의
- [x] Output Format → TASK 반영

### 6.7 Repository / Client 구현

- [x] LearningAiSearchClient 구현 (시맨틱 검색 프록시)
- [x] Elasticsearch 후보 검색 구현 (BM25 검색 + nori 분석)
- [x] SearchProperties / RestClient 설정 구현

### 6.8 Service + Test

- [x] SemanticSearchService 구현 (learning-ai semantic API 프록시)
- [x] 기존 BM25 검색 경로를 후보 조회와 페이지 조회로 분리
- [x] RRFMergeService 구현 (두 결과 리스트 RRF 병합, k=60)
- [x] HybridSearchService 구현 (시맨틱 + BM25 → RRF → 최종 결과)
- [x] 접근 제어 필터 적용 (사용자 소유 노트만)
- [x] 단위 테스트 작성 (서비스/병합 로직)
- [x] 테스트 통과 확인
- [ ] learning-ai UUID `note_id`와 knowledge `Long noteId`를 연결하는 매핑 전략 확정 및 semantic 결과 실제 RRF 병합 반영

### 6.9 Controller + Test

- [x] GET /api/v1/notes/search?q=... 엔드포인트 유지 (키워드 검색)
- [x] POST /api/v1/ai/search/semantic 엔드포인트 구현 (시맨틱 검색 프록시)
- [x] POST /api/v1/ai/search/hybrid 엔드포인트 구현 (하이브리드 검색 조합)
- [x] 검색 결과 하이라이트 / snippet 유지
- [x] MockMvc 기반 인증/응답 테스트 추가
- [x] 빈 결과, fallback, 필터 테스트
- [x] 테스트 통과 확인

### 6.10 View + Test (해당 시)

- [x] Flutter 화면 연동: 해당 없음 (프론트 별도)
- [x] Swagger/OpenAPI 미구성으로 확인 불가
- [x] RULE Reference → TASK 반영

**Step 6 Status**: [ ] Not Started / [x] In Progress / [ ] Done

---

## Step 7: 검색 정확도 측정 — 테스트 쿼리 세트 → 정확도 리포트

### 7.1 TASK 시작

- [ ] Step Goal / Done When / Scope / Input 확인
- [ ] PRD_W3 해당 요구사항 확인 (검색 정확도 측정)
- [ ] Duration 산정 확인

### 7.2 요구사항 분석

- [ ] 테스트 쿼리 세트 규모 정의 (최소 50개 쿼리)
- [ ] 정확도 지표 정의 (Precision@K, Recall@K, MRR, NDCG)
- [ ] 기대 결과 (ground truth) 라벨링 방법론 정의
- [ ] Instructions 초안 → TASK 문서 반영

### 7.3 Security 1차 검토

- [ ] 테스트 데이터 민감정보 미포함 확인
- [ ] 측정 API 관리자/개발자 전용 접근 제어
- [ ] 테스트 쿼리 세트 저장 위치 (리포지토리 내부)
- [ ] 결과 → TASK Constraints 반영

### 7.4 측정 프레임워크 설계

- [ ] 테스트 쿼리 세트 포맷 정의 (JSON: query, expectedNoteIds[], relevanceScores[])
- [ ] 측정 파이프라인 설계 (쿼리 실행 → 결과 수집 → 지표 계산 → 리포트 생성)
- [ ] 비교 모드 설계 (시맨틱 only vs BM25 only vs 하이브리드 RRF)
- [ ] Duration(final) 갱신

### 7.5 Security 2차 검토

- [ ] 측정 결과 외부 유출 방지 (내부 리포트만)
- [ ] 테스트 데이터 격리 (프로덕션 데이터 미사용)
- [ ] 측정 실행 리소스 제한 (동시 실행 제한)
- [ ] 결과 → TASK Constraints 반영

### 7.6 DTO / Entity 설계 (API First)

- [ ] SearchTestQuery DTO 정의 (query, expectedNoteIds, relevanceScores)
- [ ] SearchAccuracyReport DTO 정의 (precision, recall, mrr, ndcg, details[])
- [ ] SearchComparisonReport DTO 정의 (semantic, bm25, hybrid 각 지표)
- [ ] Output Format → TASK 반영

### 7.7 테스트 쿼리 세트 구축

- [ ] 한국어 + 영어 테스트 쿼리 50건 작성
- [ ] 각 쿼리별 기대 결과 (ground truth) 라벨링
- [ ] relevance score 부여 (0: 무관, 1: 관련, 2: 매우 관련)
- [ ] 테스트 데이터 시드 스크립트 작성

### 7.8 Service + Test

- [ ] SearchAccuracyService 구현 (테스트 쿼리 실행 → 지표 계산)
- [ ] PrecisionRecallCalculator 구현 (Precision@K, Recall@K 계산)
- [ ] MRRCalculator 구현 (Mean Reciprocal Rank 계산)
- [ ] NDCGCalculator 구현 (Normalized Discounted Cumulative Gain 계산)
- [ ] 비교 리포트 생성 (시맨틱 vs BM25 vs RRF)
- [ ] 단위 테스트 작성 (지표 계산 검증)
- [ ] 테스트 통과 확인

### 7.9 Controller + Test

- [ ] POST /admin/search/accuracy-test 엔드포인트 구현 (측정 실행)
- [ ] GET /admin/search/accuracy-report 엔드포인트 구현 (결과 조회)
- [ ] 슬라이스 테스트 (@WebMvcTest)
- [ ] 403 Forbidden 테스트 (비관리자 접근)
- [ ] 테스트 통과 확인

### 7.10 결과 정리

- [ ] 정확도 리포트 생성 (Precision@10, MRR, NDCG 기준)
- [ ] 시맨틱 vs BM25 vs 하이브리드 비교 분석
- [ ] 개선 포인트 도출 (임베딩 모델 교체, 필드 가중치 튜닝 등)
- [ ] RULE Reference → TASK 반영

**Step 7 Status**: [ ] Not Started / [ ] In Progress / [ ] Done
