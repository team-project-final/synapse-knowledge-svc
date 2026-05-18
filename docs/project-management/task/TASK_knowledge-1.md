# TASK: @knowledge-owner-1

> **담당 서비스**: knowledge-svc (note / graph / chunking)
> **GitHub Repository**: [synapse-knowledge-svc](https://github.com/team-project-final/synapse-knowledge-svc)
> **주차**: W1 (2026-05-12 ~ 2026-05-15, 4 영업일)
> **관련 문서**: [SCOPE](../scope/SCOPE_knowledge-1.md) | [PRD_W1](../prd/PRD_W1.md) | [WORKFLOW](../workflow/WORKFLOW_knowledge-1_W1.md) | [HISTORY](../history/HISTORY_knowledge-1.md)

---

## Step 1: knowledge-svc 골격 생성

- **Step Goal**: knowledge-owner-1이 Spring Boot 4 + Modulith 기반 knowledge-svc를 생성하여 note/graph/chunking 모듈 골격이 동작한다.
- **Done When**:
  - [ ] Spring Boot 4 + Modulith 프로젝트 초기화 완료
  - [ ] note / graph / chunking 3개 모듈 패키지 구조 생성
  - [ ] `./gradlew build` 성공
  - [ ] Modulith 구조 검증 테스트 통과 (`ApplicationModulesTest`)
  - [ ] Docker 이미지 빌드 성공
- **Scope**:
  - In Scope:
    - Spring Boot 4 + Modulith 프로젝트 생성
    - note / graph / chunking 모듈 패키지 구조
    - build.gradle 의존성 설정
    - ApplicationModulesTest 작성
    - Dockerfile 작성
  - Out of Scope:
    - 비즈니스 로직 구현
    - DB 마이그레이션
    - OpenSearch 연동
- **Input**: 03_아키텍처_정의서 §Modulith 구조, platform-svc 골격 참조
- **Instructions**:
  1. Spring Initializr로 프로젝트 생성 (Spring Boot 4, Java 21, Gradle)
  2. Modulith 의존성 추가 (spring-modulith-starter, spring-modulith-test)
  3. note / graph / chunking 패키지 생성 + package-info.java
  4. 각 모듈에 빈 Controller + Service 클래스 생성
  5. ApplicationModulesTest 작성 및 통과 확인
  6. Dockerfile 작성 (multi-stage build)
  7. docker compose에서 knowledge-svc 실행 확인
- **Output Format**: `knowledge-svc/` 프로젝트 디렉토리 + Dockerfile + 테스트 통과 스크린샷
- **Constraints**:
  - Java 21 + Spring Boot 4 + Modulith
  - 모듈 간 순환 의존 금지
  - platform-svc와 동일한 빌드 구조 유지
- **Duration**: 0.5일
- **RULE Reference**: wiki 03_아키텍처_정의서 §Modulith, wiki 18_기술_스택_정의서
- **Assignee**: @knowledge-owner-1
- **Reviewer**: @team-lead

**Status**: [ ] Not Started / [ ] In Progress / [ ] Done

---

## Step 2: note Markdown CRUD

- **Step Goal**: 로그인 사용자가 Markdown 노트를 생성/조회/수정/삭제할 수 있다.
- **Done When**:
  - [x] `POST /notes` → Markdown 노트 생성 (제목 + 본문)
  - [x] `GET /notes` → 노트 목록 조회 (페이징, 소유자 필터)
  - [x] `GET /notes/{id}` → 노트 상세 조회 (Markdown 원문 반환)
  - [x] `PATCH /notes/{id}` → 노트 수정 (소유자만)
  - [x] `DELETE /notes/{id}` → 노트 삭제 (소유자만)
  - [x] notes 테이블 Flyway 마이그레이션 완료
  - [x] 통합 테스트 통과
- **Scope**:
  - In Scope:
    - notes 테이블 설계 + Flyway 마이그레이션
    - Note 엔티티 + Repository
    - NoteService CRUD 로직
    - NoteController REST API
    - 소유자 권한 검증
    - 페이징 조회
    - 통합 테스트
  - Out of Scope:
    - Markdown → HTML 렌더링 (프론트엔드)
    - 노트 검색 (OpenSearch — W3)
    - 노트 공유/협업
    - 버전 히스토리
- **Input**: PRD_W1 노트 기능 요구사항, JWT 인증 토큰 (platform-svc)
- **Instructions**:
  1. notes 테이블 DDL 작성 (id, tenant_id, title, content_md, content_plain, user_id, status, word_count, created_at, updated_at, deleted_at)
     - `tenant_id`: 멀티테넌트 식별 컬럼 (필수)
     - `content_md`: Markdown 원문 저장 (구 `content` → ERD 기준 `content_md`)
     - `content_plain`: 플레인텍스트 버전 (검색/미리보기용)
     - `user_id`: 소유자 식별 (구 `owner_id` → ERD 기준 `user_id`)
     - `status`: 노트 상태 (`active|archived|trashed` — ERD 기준)
     - `word_count`: 단어 수 집계
  2. Flyway V1 마이그레이션 파일 생성
  3. Note 엔티티 + JPA Repository 작성
  4. NoteService 구현 (create, findAll, findById, update, delete)
  5. 소유자 권한 검증 로직 (수정/삭제 시 user_id 확인)
  6. NoteController REST API 구현 (5개 엔드포인트)
  7. 페이징 처리 (Pageable, 기본 20건)
  8. 통합 테스트 작성 (@SpringBootTest + TestContainers)
- **Output Format**: note 모듈 코드 + Flyway 마이그레이션 + API 문서 (Swagger)
- **Constraints**:
  - 제목 최대 200자, 본문 최대 100,000자
  - Markdown 원문 그대로 저장 (서버에서 렌더링 X)
  - Soft delete (deleted_at 컬럼)
- **Duration**: 2일
- **RULE Reference**: wiki 03_아키텍처_정의서 §REST API 규칙, wiki 09_Git_규칙_정의서 §커밋 컨벤션
- **Assignee**: @knowledge-owner-1
- **Reviewer**: @team-lead

**Status**: [ ] Not Started / [ ] In Progress / [x] Done

---

## Step 3: note 위키링크 파싱

- **Step Goal**: 노트 저장 시 [[note-title]] 형식의 위키링크를 자동으로 추출하여 note_links 테이블에 저장한다.
- **Done When**:
  - [x] 노트 생성/수정 시 본문에서 `[[...]]` 패턴 자동 추출
  - [x] 추출된 링크 → note_links 테이블 저장 (source_note_id, target_title)
  - [x] 대상 노트 존재 시 target_note_id 자동 매핑
  - [x] 노트 수정 시 기존 링크 갱신 (삭제 + 재생성)
  - [x] `GET /notes/{id}/backlinks` → 해당 노트를 참조하는 노트 목록 반환 (Wiki는 `/backlinks`만 정의 — `/links` 아님)
  - [x] note_links 테이블 Flyway 마이그레이션 완료
  - [x] 단위/통합 테스트 통과
- **Scope**:
  - In Scope:
    - note_links 테이블 설계 + Flyway 마이그레이션
    - `[[...]]` 정규식 파서 구현
    - 노트 저장 시 링크 추출 + 저장 로직
    - 대상 노트 존재 여부 확인 + ID 매핑
    - 노트 수정 시 링크 갱신 로직
    - 위키링크 목록 조회 API
    - 단위/통합 테스트
  - Out of Scope:
    - 그래프 시각화 (프론트엔드)
    - 역링크(Backlink) 조회 (W2)
    - 링크 기반 추천
- **Input**: notes 테이블, NoteService, 위키링크 문법 정의
- **Instructions**:
  1. note_links 테이블 DDL 작성 (id, source_note_id, target_title, target_note_id nullable, created_at)
  2. Flyway V2 마이그레이션 파일 생성
  3. WikiLinkParser 유틸 구현 (정규식: `\[\[([^\]]+)\]\]`)
  4. NoteService.save/update 후처리에 링크 추출 로직 추가
  5. 추출된 title로 notes 테이블 조회 → target_note_id 매핑
  6. 노트 수정 시 기존 링크 삭제 후 재생성
  7. LinkController `GET /notes/{id}/backlinks` 구현 (Wiki 기준 — `/links` 아님)
  8. WikiLinkParser 단위 테스트 (다양한 케이스)
  9. 통합 테스트 (노트 생성 → 링크 자동 저장 확인)
- **Output Format**: note 모듈 위키링크 코드 + Flyway 마이그레이션 + 테스트 코드
- **Constraints**:
  - 정규식: `\[\[([^\]]+)\]\]` (중첩 불허)
  - 대상 노트 미존재 시에도 title만 저장 (나중에 매핑)
  - 하나의 노트에서 중복 링크는 1건만 저장
  - 링크 갱신은 트랜잭션 내 처리
- **Duration**: 1.5일
- **RULE Reference**: wiki 03_아키텍처_정의서 §지식 그래프, wiki 09_Git_규칙_정의서 §커밋 컨벤션
- **Assignee**: @knowledge-owner-1
- **Reviewer**: @team-lead

**Status**: [ ] Not Started / [ ] In Progress / [x] Done

---

## W2 (2026-05-18 ~ 2026-05-22, 5 영업일)
...
