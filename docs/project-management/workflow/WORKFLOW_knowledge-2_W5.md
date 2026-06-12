# WORKFLOW: @knowledge-owner-2 — Week 5

> **Task 문서**: [TASK_knowledge-2.md](../task/TASK_knowledge-2.md)
> **기간**: 2026-06-08 ~ 2026-06-12, 5 영업일
> **목표**: 검색 E2E, 하이브리드 검색 정확도 리포트 최종화, P0 버그 수정
> **메모**: W4에서 시작한 Step 8/9 구현이 2026-06-08까지 연장 완료되어, 본 문서는 실제 산출물과 발표 준비 상태를 최종 동기화한다.

## Step 8: 검색 E2E

### 8.1 시나리오 정의
- [x] BM25 검색 시나리오 작성
- [x] 시맨틱 검색 프록시 연동 시나리오 작성
- [x] 하이브리드 RRF 검색 시나리오 작성

### 8.2 실행 및 수정
- [x] 검색 E2E 테스트 실행
- [x] 검색 권한/tenant 격리 확인
- [x] P0 버그 수정 및 회귀 테스트

**Step 8 Status**: [ ] Not Started / [ ] In Progress / [x] Done

## Step 9: 정확도 리포트

### 9.1 데이터셋 준비
- [x] 테스트 쿼리와 기대 결과 세트 확정
- [x] MRR@10 또는 Top-5 관련도 기준 확정
- [x] 실패 쿼리 분류

### 9.2 튜닝 결과 정리
- [x] BM25/시맨틱/RRF 비교 결과 작성
- [x] 정확도 미달 항목 개선안 작성
- [x] 발표용 검색 데모 쿼리 확정
  - 근거: [REPORT_knowledge-2_W5_demo-search.md](../report/REPORT_knowledge-2_W5_demo-search.md)

**Step 9 Status**: [ ] Not Started / [ ] In Progress / [x] Done

## Done When

- [x] 검색 E2E가 통과한다.
- [x] 정확도 리포트가 작성된다.
- [x] knowledge-2 P0 버그가 0건이다.
- [x] live AI 카드 생성 차단 원인이던 `note-created.deckId` 누락이 해소됐다.
  - 근거: `NoteCreateRequest` nullable `deckId` 입력 + `notes.deck_id` 저장 + `knowledge.note.note-created-v1` payload 전파
- [x] knowledge-svc JaCoCo service line coverage가 80% 이상이다.
  - 근거: `build/reports/jacoco/test/jacocoTestReport.xml` line coverage `84.11%` (`covered=1212`, `missed=229`) + `jacocoTestCoverageVerification` 통과
- [x] 발표용 검색 쿼리가 안정적으로 동작한다.
  - 근거: benchmark seed + 정확도 리포트 + CI 동일 compose 재검증 + [REPORT_knowledge-2_W5_demo-search.md](../report/REPORT_knowledge-2_W5_demo-search.md)
