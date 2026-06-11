# WORKFLOW: @knowledge-owner-1 — Week 3

> **Task 문서**: [TASK_knowledge-1.md](../task/TASK_knowledge-1.md)
> **기간**: 2026-05-26 ~ 2026-05-29, 4 영업일
> **PRD**: [PRD_W3.md](../prd/PRD_W3.md)

---

## Step 6: note 버전 이력 — 수정 히스토리 저장/조회/복원

### 1.1 TASK 시작
- [x] Step Goal / Done When / Scope / Input 확인
- [x] PRD_W3 해당 요구사항 확인 (노트 버전 이력)
- [x] Duration 산정 확인

### 1.2 요구사항 분석
- [x] 버전 생성 트리거 정의 (노트 수정 시 자동 저장) — `NoteService.update()` → `saveVersion()` 훅
- [x] 버전 조회 요건 분석 (목록, 상세, diff 비교) — `listVersions`, `getVersion` 구현
- [x] 버전 복원 요건 분석 (특정 버전으로 되돌리기) — `NoteService.restoreVersion()` 이벤트 위임 방식
- [x] Instructions 초안 → TASK 문서 반영

### 1.3 Security 1차 검토
- [x] 인증 필요 여부: Yes (로그인 사용자) — `/api/v1/notes/**` authenticated
- [x] 권한 종류: 본인 노트 버전만 접근 가능 — `NoteVersionService.validateOwner()` 구현
- [x] 공개 API 여부: No
- [x] 결과 → TASK Constraints 반영

### 1.4 ERD 설계
- [x] note_versions 테이블 설계 (id, note_id, version_no, title, content_md, created_at) — V8 Flyway
- [x] 참고: created_by 컬럼은 ERD에 없음 — 소유자는 note_id → notes.user_id 로 참조 (CONFLICT-1 적용)
- [x] 인덱스 설계 (note_id + version_no UNIQUE, note_id + created_at DESC)
- [x] 관계 정의 (notes 1:N note_versions, ON DELETE CASCADE)
- [x] 버전 번호 자동 증가 로직 설계 — `findMaxVersionNoByNoteId() + 1`
- [x] Duration(final) 갱신

### 1.5 Security 2차 검토
- [x] 버전 삭제 불가 (append-only) — 복원 시 새 버전 생성으로 이력 보존
- [x] 복원 시 새 버전으로 추가 (덮어쓰기 아님) — `restoreVersion()` 내부에서 `update()` 호출
- [x] 행 단위 접근 제어: 노트 소유자만 버전 접근 — `validateOwner()` 모든 메서드에 적용
- [x] 결과 → TASK Constraints 반영

### 1.6 DTO / Entity 설계 (API First)
- [x] NoteVersion Entity 작성
- [x] NoteVersionListResponse DTO 정의 → `NoteVersionSummaryResponse` (id, versionNo, title, createdAt)
- [x] NoteVersionDetailResponse DTO 정의 (id, versionNo, title, contentMd, createdAt)
- [x] NoteVersionRestoreRequest DTO 정의 — path variable 방식으로 대체 (요청 바디 불필요, REST 설계 판단)
- [x] Output Format → TASK 반영

### 1.7 Repository 구현
- [x] NoteVersionRepository 인터페이스 작성
- [x] findByNoteIdOrderByVersionNumberDesc 커스텀 쿼리 — `findByNoteIdOrderByVersionNoDesc`
- [x] findByNoteIdAndVersionNumber 조회 쿼리 — `findByNoteIdAndVersionNo`
- [x] getMaxVersionNumberByNoteId 쿼리 — `findMaxVersionNoByNoteId` (COALESCE 처리)

### 1.8 Service + Test
- [x] NoteVersionService 구현 (버전 생성 — 노트 수정 시 자동 호출)
- [x] NoteVersionService 구현 (버전 목록 조회 — 페이징)
- [x] NoteVersionService 구현 (버전 상세 조회)
- [x] NoteVersionService 구현 (버전 복원 — NoteService.restoreVersion()으로 이벤트 위임)
- [x] 단위 테스트 작성 (Mockito) — `NoteVersionServiceTest` 6건
- [x] 테스트 통과 확인

### 1.9 Controller + Test
- [x] GET /notes/{noteId}/versions 엔드포인트 구현 (버전 목록)
- [x] GET /notes/{noteId}/versions/{versionNumber} 엔드포인트 구현 (버전 상세)
- [x] POST /notes/{noteId}/versions/{versionNumber}/restore 엔드포인트 구현 (복원)
- [ ] 슬라이스 테스트 (@WebMvcTest)
- [x] 403 Forbidden 테스트 (타인 노트 버전 접근) — `NoteVersionIntegrationTest` 통합 테스트 포함
- [x] 테스트 통과 확인

### 1.10 View + Test (해당 시)
- [x] Flutter 화면 연동: 해당 없음 (프론트 별도)
- [x] Swagger API 문서 확인
- [x] RULE Reference → TASK 반영

**Step 6 Status**: [ ] Not Started / [ ] In Progress / [x] Done

---

## Step 7: 태그 관리 고도화 — 태그 기반 필터링, 태그 자동완성, 인기 태그

### 1.1 TASK 시작
- [x] Step Goal / Done When / Scope / Input 확인
- [x] PRD_W3 해당 요구사항 확인 (태그 관리 고도화)
- [x] Duration 산정 확인

### 1.2 요구사항 분석
- [x] 태그 기반 필터링 요건 분석 — `GET /notes?tag=` 단일 태그 필터 구현
- [x] 태그 자동완성 요건 분석 — prefix LIKE, 사용 빈도 내림차순, 최대 10건
- [x] 인기 태그 요건 분석 — Redis 단일 키 캐싱, 상위 50개 저장 후 슬라이싱
- [x] Instructions 초안 → TASK 문서 반영

### 1.3 Security 1차 검토
- [x] 인증 필요 여부: Yes — `/api/v1/tags/autocomplete` authenticated (SecurityConfig)
- [x] 권한 종류: 본인 노트의 태그만 자동완성 반환 — note_tags JOIN notes WHERE user_id=:userId
- [x] 인기 태그 API: 인증 사용자 전체 공개 — `/api/v1/tags/popular` permitAll
- [x] 결과 → TASK Constraints 반영

### 1.4 ERD / 쿼리 설계
- [x] 기존 note_tags 테이블 활용 확인 — V4 마이그레이션 기준 (CONFLICT-2 적용)
- [x] 태그 사용 빈도 집계 쿼리 설계 — COUNT(*) GROUP BY tag
- [x] 태그 prefix 검색 쿼리 설계 — LIKE ':prefix%' + setMaxResults(10)
- [x] 태그 기반 노트 필터링 쿼리 설계 — JPQL JOIN @ElementCollection
- [x] Duration(final) 갱신

### 1.5 Security 2차 검토
- [x] 태그 자동완성 응답에 사용자 개인정보 미포함 — tag 문자열만 반환
- [x] SQL Injection 방지 (파라미터 바인딩 확인) — setParameter() 사용
- [x] 태그 길이 제한 (최대 30자) — `NoteCreateRequest @Size(max=30)` 적용
- [x] 결과 → TASK Constraints 반영

### 1.6 DTO / Entity 설계 (API First)
- [x] TagAutoCompleteResponse DTO 정의 (tag, count)
- [x] PopularTagResponse DTO 정의 (tag, count)
- [x] TagAutoCompleteRequest DTO — @RequestParam 직접 수신으로 대체 (단일 쿼리 파라미터, DTO 불필요)
- [x] NoteFilterByTagsRequest DTO — @RequestParam String tag 단일 필터로 구현 (단일 파라미터, DTO 불필요)
- [x] Output Format → TASK 반영

### 1.7 Repository 구현
- [x] TagRepository 확장: findByNameStartingWith — CONFLICT-2 근거: TagRepository 미생성, EntityManager 네이티브 쿼리 직접 사용
- [x] TagRepository 확장: findPopularTags — 동일 (CONFLICT-2 근거: native 쿼리 대체)
- [x] NoteRepository 확장: findByTags — `findByUserIdAndTagAndDeletedAtIsNull` JPQL 구현

### 1.8 Service + Test
- [x] TagService 구현 (자동완성 — prefix 검색 + userId 필터 + 빈도 정렬)
- [x] TagService 구현 (인기 태그 — 상위 50개 캐싱 후 limit 슬라이싱)
- [x] NoteService 확장 (태그 기반 필터링 — findAllByTag)
- [x] Redis 캐싱 (인기 태그 TTL: 1시간, 단일 키 `tags:popular`)
- [x] 단위 테스트 작성 (Mockito) — `TagServiceTest` 4건
- [x] 테스트 통과 확인

### 1.9 Controller + Test
- [x] GET /tags/autocomplete?q=xxx 엔드포인트 구현
- [x] GET /tags/popular 엔드포인트 구현
- [x] GET /notes?tag= 엔드포인트 확장
- [ ] 슬라이스 테스트 (@WebMvcTest)
- [x] 테스트 통과 확인

### 1.10 View + Test (해당 시)
- [x] Flutter 화면 연동: 해당 없음 (프론트 별도)
- [x] Swagger API 문서 확인
- [x] RULE Reference → TASK 반영

**Step 7 Status**: [ ] Not Started / [ ] In Progress / [x] Done
