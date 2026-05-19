# WORKFLOW: @knowledge-owner-1 — Week 1

> **Task 문서**: [TASK_knowledge-1.md](../task/TASK_knowledge-1.md)
> **기간**: 2026-05-12 ~ 2026-05-15, 4 영업일
> **기능개발 Workflow**: [README §7](../README.md)

---

## Step 1: knowledge-svc 골격 생성

### 1.1 TASK 시작

- [x] Step Goal / Done When / Scope / Input 확인
- [x] PRD_W1 해당 요구사항 확인 (프로젝트 골격)
- [x] Duration 산정 확인 (0.5일)

### 1.2 요구사항 분석

- [x] Spring Boot 4 + Modulith 프로젝트 구조 분석
- [x] note/graph/chunking 3개 모듈 역할 정의
- [x] platform-svc와 동일 빌드 구조 확인
- [x] Instructions 초안 → TASK 문서 반영

### 1.3 Security 1차 검토

- [x] 인증 필요 여부: No (골격만 생성)
- [x] 권한 종류: 없음
- [x] 공개 API 여부: No (Health endpoint만)
- [x] 결과 → TASK Constraints 반영

### 1.4 ERD 설계

- [x] 골격 단계 — ERD 해당 없음
- [x] 모듈별 패키지 구조도 작성
- [x] Duration(final) 갱신

### 1.5 Security 2차 검토

- [x] 민감 정보 암호화: 비해당 (골격 단계)
- [x] Soft Delete 정책: 비해당
- [x] 행 단위 접근 제어: 불필요
- [x] 결과 → TASK Constraints 반영

### 1.6 DTO / Entity 설계 (API First)

- [x] 골격 단계 — 빈 Controller/Service 클래스만 생성
- [x] 각 모듈 package-info.java 작성
- [x] Output Format → TASK 반영

### 1.7 Repository 구현

- [x] 골격 단계 — Repository 해당 없음
- [x] ApplicationModulesTest 구조 검증 테스트 작성

### 1.8 Service + Test

- [x] 빈 Service 클래스 생성 (3개 모듈)
- [x] ApplicationModulesTest 통과 확인
- [x] `./gradlew build` 성공 확인

### 1.9 Controller + Test

- [x] 빈 Controller 클래스 생성 (3개 모듈)
- [x] Dockerfile 작성 (multi-stage build)
- [x] Docker 이미지 빌드 성공 확인

### 1.10 View + Test (해당 시)

- [x] Flutter 화면 연동: 해당 없음
- [x] docker compose에서 knowledge-svc 실행 확인
- [x] RULE Reference → TASK 반영

**Step 1 Status**: [ ] Not Started / [ ] In Progress / [x] Done

---

## Step 2: note Markdown CRUD

### 1.1 TASK 시작

- [x] Step Goal / Done When / Scope / Input 확인
- [x] PRD_W1 해당 요구사항 확인 (FR-KN-xxx 노트 기능)
- [x] Duration 산정 확인 (2일)

### 1.2 요구사항 분석

- [x] 노트 CRUD API 엔드포인트 5개 정의
- [x] Markdown 원문 저장 정책 확인 (서버 렌더링 X)
- [x] 소유자 권한 검증 로직 설계
- [x] Instructions 초안 → TASK 문서 반영

### 1.3 Security 1차 검토

- [x] 인증 필요 여부: Yes (JWT 인증 필요)
- [x] 권한 종류: 로그인 사용자 (수정/삭제는 소유자만)
- [x] 공개 API 여부: No
- [x] 결과 → TASK Constraints 반영

### 1.4 ERD 설계

- [x] notes 테이블 설계 (id, tenant_id, user_id, title, content_md, content_plain, status: active|archived|trashed, word_count, metadata jsonb, created_at, updated_at, deleted_at)
- [x] 인덱스 설계 (user_id, created_at DESC)
- [x] 관계 정의 (notes.user_id → users.id FK)
- [x] Duration(final) 갱신

### 1.5 Security 2차 검토

- [x] 민감 정보 암호화: 비해당 (노트 내용은 평문 저장)
- [x] Soft Delete 정책: 논리삭제 (deleted_at)
- [x] 행 단위 접근 제어: 필요 (수정/삭제 시 user_id 확인)
- [x] 결과 → TASK Constraints 반영

### 1.6 DTO / Entity 설계 (API First)

- [x] NoteCreateRequest 정의 (title, content_md)
- [x] NoteUpdateRequest 정의 (title, content_md)
- [x] NoteResponse 정의 (id, title, content_md, content_plain, userId, status, word_count, createdAt, updatedAt)
- [x] NoteListResponse 정의 (id, title, userId, status, createdAt — content 제외)
- [x] Note Entity 작성
- [x] MapStruct 매퍼 작성
- [x] Output Format → TASK 반영

### 1.7 Repository 구현

- [x] NoteRepository 인터페이스 작성
- [x] findByUserIdAndDeletedAtIsNull 커스텀 쿼리
- [x] 페이징 처리 (Pageable)

### 1.8 Service + Test

- [x] NoteService 구현 (create, findAll, findById, update, delete)
- [x] 소유자 권한 검증 로직 구현
- [x] 제목 200자/본문 100,000자 검증 구현
- [x] 단위 테스트 작성 (Mockito)
- [x] 테스트 통과 확인

### 1.9 Controller + Test

- [x] NoteController REST API 구현 (5개 엔드포인트)
- [x] 슬라이스 테스트 (@WebMvcTest)
- [x] 401/403 응답 테스트 (미인증, 비소유자)
- [x] 통합 테스트 (@SpringBootTest + TestContainers)
- [x] 테스트 통과 확인

### 1.10 View + Test (해당 시)

- [x] Flutter 화면 연동: 해당 없음 (프론트 별도)
- [x] Swagger API 문서 확인
- [x] RULE Reference → TASK 반영

**Step 2 Status**: [ ] Not Started / [ ] In Progress / [ Done

---

## Step 3: note 위키링크 파싱

### 1.1 TASK 시작

- [x] Step Goal / Done When / Scope / Input 확인
- [x] PRD_W1 해당 요구사항 확인 (FR-KN-xxx 위키링크)
- [x] Duration 산정 확인 (1.5일)

### 1.2 요구사항 분석

- [x] `[[...]]` 위키링크 문법 정규식 정의
- [x] 노트 생성/수정 시 링크 추출 → 저장 플로우 분석
- [x] 대상 노트 미존재 시 title만 저장 정책
- [x] Instructions 초안 → TASK 문서 반영

### 1.3 Security 1차 검토

- [x] 인증 필요 여부: Yes (JWT 인증 필요)
- [x] 권한 종류: 로그인 사용자 (본인 노트의 링크만 접근)
- [x] 공개 API 여부: No
- [x] 결과 → TASK Constraints 반영

### 1.4 ERD 설계

- [x] note_links 테이블 설계 (id, source_note_id, target_note_id nullable, link_type: wikilink|reference|embed, context_snippet, created_at)
- [x] 참고: target_title 컬럼은 ERD에 없음 — 애플리케이션 레벨에서 target_note_id로 조회하여 처리
- [x] 인덱스 설계 (source_note_id, target_note_id, source+target_note_id UNIQUE)
- [x] 관계 정의 (note_links → notes FK x2)
- [x] Duration(final) 갱신

### 1.5 Security 2차 검토

- [x] 민감 정보 암호화: 비해당
- [x] Soft Delete 정책: 물리삭제 (링크 갱신 시 삭제+재생성)
- [x] 행 단위 접근 제어: 필요 (소유 노트의 링크만 조회)
- [x] 결과 → TASK Constraints 반영

### 1.6 DTO / Entity 설계 (API First)

- [x] NoteLinkResponse 정의 (id, sourceNoteId, targetNoteId, linkType, contextSnippet)
- [x] NoteLink Entity 작성
- [x] MapStruct 매퍼 작성
- [x] Output Format → TASK 반영

### 1.7 Repository 구현

- [x] NoteLinkRepository 인터페이스 작성
- [x] findBySourceNoteId 커스텀 쿼리
- [x] deleteBySourceNoteId 커스텀 쿼리

### 1.8 Service + Test

- [x] WikiLinkParser 유틸 구현 (정규식: `\[\[([^\]]+)\]\]`)
- [x] NoteService.save/update 후처리에 링크 추출 로직 추가
- [x] 추출된 title로 notes 조회 → target_note_id 매핑
- [x] 노트 수정 시 기존 링크 삭제 후 재생성 (트랜잭션)
- [x] WikiLinkParser 단위 테스트 (다양한 케이스)
- [x] 테스트 통과 확인

### 1.9 Controller + Test

- [x] GET /notes/{id}/backlinks 엔드포인트 구현
- [x] 슬라이스 테스트 (@WebMvcTest)
- [x] 401/403 응답 테스트 (미인증, 비소유자)
- [x] 통합 테스트 (노트 생성 → 링크 자동 저장 확인)
- [x] 테스트 통과 확인

### 1.10 View + Test (해당 시)

- [x] Flutter 화면 연동: 해당 없음 (프론트 별도)
- [x] Swagger API 문서 확인
- [x] RULE Reference → TASK 반영

**Step 3 Status**: [ ] Not Started / [ ] In Progress / [x] Done
