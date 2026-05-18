# WORKFLOW: @knowledge-owner-1 — Week 3

> **Task 문서**: [TASK_knowledge-1.md](../task/TASK_knowledge-1.md)
> **기간**: 2026-05-26 ~ 2026-05-29, 4 영업일
> **PRD**: [PRD_W3.md](../prd/PRD_W3.md)

---

## Step 6: note 버전 이력 — 수정 히스토리 저장/조회/복원

### 1.1 TASK 시작
- [ ] Step Goal / Done When / Scope / Input 확인
- [ ] PRD_W3 해당 요구사항 확인 (노트 버전 이력)
- [ ] Duration 산정 확인

### 1.2 요구사항 분석
- [ ] 버전 생성 트리거 정의 (노트 수정 시 자동 저장)
- [ ] 버전 조회 요건 분석 (목록, 상세, diff 비교)
- [ ] 버전 복원 요건 분석 (특정 버전으로 되돌리기)
- [ ] Instructions 초안 → TASK 문서 반영

### 1.3 Security 1차 검토
- [ ] 인증 필요 여부: Yes (로그인 사용자)
- [ ] 권한 종류: 본인 노트 버전만 접근 가능
- [ ] 공개 API 여부: No
- [ ] 결과 → TASK Constraints 반영

### 1.4 ERD 설계
- [ ] note_versions 테이블 설계 (id, note_id, version_number, title, content_md, change_summary, created_at)
- [ ] 참고: created_by 컬럼은 ERD에 없음 — 소유자는 note_id → notes.user_id 로 참조
- [ ] 인덱스 설계 (note_id + version_number UNIQUE, note_id + created_at DESC)
- [ ] 관계 정의 (notes 1:N note_versions)
- [ ] 버전 번호 자동 증가 로직 설계
- [ ] Duration(final) 갱신

### 1.5 Security 2차 검토
- [ ] 버전 삭제 불가 (append-only)
- [ ] 복원 시 새 버전으로 추가 (덮어쓰기 아님)
- [ ] 행 단위 접근 제어: 노트 소유자만 버전 접근
- [ ] 결과 → TASK Constraints 반영

### 1.6 DTO / Entity 설계 (API First)
- [ ] NoteVersion Entity 작성
- [ ] NoteVersionListResponse DTO 정의 (id, versionNumber, createdAt, summary)
- [ ] NoteVersionDetailResponse DTO 정의 (id, versionNumber, title, content, createdAt)
- [ ] NoteVersionRestoreRequest DTO 정의 (versionId)
- [ ] Output Format → TASK 반영

### 1.7 Repository 구현
- [ ] NoteVersionRepository 인터페이스 작성
- [ ] findByNoteIdOrderByVersionNumberDesc 커스텀 쿼리
- [ ] findByNoteIdAndVersionNumber 조회 쿼리
- [ ] getMaxVersionNumberByNoteId 쿼리 (다음 버전 번호 산출)

### 1.8 Service + Test
- [ ] NoteVersionService 구현 (버전 생성 — 노트 수정 시 자동 호출)
- [ ] NoteVersionService 구현 (버전 목록 조회 — 페이징)
- [ ] NoteVersionService 구현 (버전 상세 조회)
- [ ] NoteVersionService 구현 (버전 복원 — 선택 버전 내용을 새 버전으로 생성)
- [ ] 단위 테스트 작성 (Mockito)
- [ ] 테스트 통과 확인

### 1.9 Controller + Test
- [ ] GET /notes/{noteId}/versions 엔드포인트 구현 (버전 목록)
- [ ] GET /notes/{noteId}/versions/{versionNumber} 엔드포인트 구현 (버전 상세)
- [ ] POST /notes/{noteId}/versions/{versionNumber}/restore 엔드포인트 구현 (복원)
- [ ] 슬라이스 테스트 (@WebMvcTest)
- [ ] 403 Forbidden 테스트 (타인 노트 버전 접근)
- [ ] 테스트 통과 확인

### 1.10 View + Test (해당 시)
- [ ] Flutter 화면 연동: 해당 없음 (프론트 별도)
- [ ] Swagger API 문서 확인
- [ ] RULE Reference → TASK 반영

**Step 6 Status**: [ ] Not Started / [ ] In Progress / [ ] Done

---

## Step 7: 태그 관리 고도화 — 태그 기반 필터링, 태그 자동완성, 인기 태그

### 1.1 TASK 시작
- [ ] Step Goal / Done When / Scope / Input 확인
- [ ] PRD_W3 해당 요구사항 확인 (태그 관리 고도화)
- [ ] Duration 산정 확인

### 1.2 요구사항 분석
- [ ] 태그 기반 필터링 요건 분석 (단일/다중 태그 AND/OR 필터)
- [ ] 태그 자동완성 요건 분석 (prefix 기반 검색, 최대 10건)
- [ ] 인기 태그 요건 분석 (사용 빈도 기준 상위 N개)
- [ ] Instructions 초안 → TASK 문서 반영

### 1.3 Security 1차 검토
- [ ] 인증 필요 여부: Yes (로그인 사용자)
- [ ] 권한 종류: 본인 노트의 태그만 수정 가능
- [ ] 인기 태그 API: 인증 사용자 전체 공개
- [ ] 결과 → TASK Constraints 반영

### 1.4 ERD / 쿼리 설계
- [ ] 기존 tags / note_tags 테이블 활용 확인
- [ ] 태그 사용 빈도 집계 쿼리 설계 (COUNT GROUP BY)
- [ ] 태그 prefix 검색 쿼리 설계 (LIKE 'prefix%' + 빈도 정렬)
- [ ] 태그 기반 노트 필터링 쿼리 설계 (JOIN + IN/EXISTS)
- [ ] Duration(final) 갱신

### 1.5 Security 2차 검토
- [ ] 태그 자동완성 응답에 사용자 개인정보 미포함
- [ ] SQL Injection 방지 (파라미터 바인딩 확인)
- [ ] 태그 길이 제한 (최대 50자)
- [ ] 결과 → TASK Constraints 반영

### 1.6 DTO / Entity 설계 (API First)
- [ ] TagAutoCompleteRequest DTO 정의 (prefix, limit)
- [ ] TagAutoCompleteResponse DTO 정의 (name, count)
- [ ] PopularTagResponse DTO 정의 (name, count)
- [ ] NoteFilterByTagsRequest DTO 정의 (tags[], operator: AND/OR)
- [ ] Output Format → TASK 반영

### 1.7 Repository 구현
- [ ] TagRepository 확장: findByNameStartingWith (자동완성)
- [ ] TagRepository 확장: findPopularTags (사용 빈도 상위 N)
- [ ] NoteRepository 확장: findByTags (태그 기반 필터링)

### 1.8 Service + Test
- [ ] TagService 구현 (자동완성 — prefix 검색 + 빈도 정렬)
- [ ] TagService 구현 (인기 태그 — 상위 N개 캐싱)
- [ ] NoteService 확장 (태그 기반 필터링 — AND/OR 지원)
- [ ] Redis 캐싱 (인기 태그 TTL: 1시간)
- [ ] 단위 테스트 작성 (Mockito)
- [ ] 테스트 통과 확인

### 1.9 Controller + Test
- [ ] GET /tags/autocomplete?prefix=xxx 엔드포인트 구현
- [ ] GET /tags/popular 엔드포인트 구현
- [ ] GET /notes?tags=a,b&tagOp=AND 엔드포인트 확장
- [ ] 슬라이스 테스트 (@WebMvcTest)
- [ ] 테스트 통과 확인

### 1.10 View + Test (해당 시)
- [ ] Flutter 화면 연동: 해당 없음 (프론트 별도)
- [ ] Swagger API 문서 확인
- [ ] RULE Reference → TASK 반영

**Step 7 Status**: [ ] Not Started / [ ] In Progress / [ ] Done
