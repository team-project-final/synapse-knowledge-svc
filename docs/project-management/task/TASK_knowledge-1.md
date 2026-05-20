# TASK: @knowledge-owner-1

> **담당 서비스**: knowledge-svc (note / graph / chunking)
> **GitHub Repository**: [synapse-knowledge-svc](https://github.com/team-project-final/synapse-knowledge-svc)
> **주차**: W1 (2026-05-12 ~ 2026-05-15, 4 영업일)
> **관련 문서**: [SCOPE](../scope/SCOPE_knowledge-1.md) | [PRD_W1](../prd/PRD_W1.md) | [WORKFLOW](../workflow/WORKFLOW_knowledge-1_W1.md) | [HISTORY](../history/HISTORY_knowledge-1.md)

---

## Step 1: knowledge-svc 골격 생성

- **Step Goal**: knowledge-owner-1이 Spring Boot 4 + Modulith 기반 knowledge-svc를 생성하여 note/graph/chunking 모듈 골격이 동작한다.
- **Done When**:
  - [x] Spring Boot 4 + Modulith 프로젝트 초기화 완료
  - [x] note / graph / chunking 3개 모듈 패키지 구조 생성
  - [x] `./gradlew build` 성공
  - [x] Modulith 구조 검증 테스트 통과 (`ApplicationModulesTest`)
  - [x] Docker 이미지 빌드 성공
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
- **Input**: 03*아키텍처*정의서 §Modulith 구조, platform-svc 골격 참조
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
- **RULE Reference**: wiki 03*아키텍처*정의서 §Modulith, wiki 18*기술*스택\_정의서
- **Assignee**: @knowledge-owner-1
- **Reviewer**: @team-lead

**Status**: [ ] Not Started / [ ] In Progress / [x] Done

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
- **RULE Reference**: wiki 03*아키텍처*정의서 §REST API 규칙, wiki 09*Git*규칙\_정의서 §커밋 컨벤션
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
- **RULE Reference**: wiki 03*아키텍처*정의서 §지식 그래프, wiki 09*Git*규칙\_정의서 §커밋 컨벤션
- **Assignee**: @knowledge-owner-1
- **Reviewer**: @team-lead

**Status**: [ ] Not Started / [ ] In Progress / [x] Done

---

## W2 (2026-05-18 ~ 2026-05-22, 5 영업일)

---

## Step 4: 백링크 조회 및 D3.js 지식 그래프 API

- **Step Name**: 백링크/지식 그래프 API
- **Step Goal**: 사용자가 노트 간 백링크를 조회하고, D3.js 지식 그래프 데이터를 API로 받을 수 있다.
- **Done When**:
  - [ ] `GET /notes/{id}/backlinks` → 해당 노트를 참조하는 노트 목록 반환
  - [ ] `GET /graph/data` → 전체 노트 그래프 데이터 반환 (nodes + edges 단일 엔드포인트, Wiki 기준 — `/graph/nodes`·`/graph/edges` 분리 없음)
    - 쿼리 파라미터로 `userId={id}` 지원 (구 `GET /graph?userId={id}` 대체)
  - [ ] D3.js force-directed graph 호환 JSON 포맷
  - [ ] 통합 테스트 통과
- **Scope**:
  - In Scope:
    - 백링크 조회 API (note_links 역방향 조회)
    - 그래프 데이터 단일 API `GET /graph/data` (nodes + edges 함께 반환)
      - 노드: id, title, `linkCount` + `pageRank` (구 `val` → `linkCount`+`pageRank`)
      - 엣지: source, target, `type` 필드 포함 (배열명 `edges` — 구 `links` 아님)
    - D3.js 호환 JSON 포맷 (nodes + edges)
    - 사용자별 그래프 필터링
    - 통합 테스트
  - Out of Scope:
    - D3.js 프론트엔드 시각화 구현
    - 그래프 클러스터링/분석 알고리즘
    - 실시간 그래프 업데이트 (WebSocket)
- **Input**: note_links 테이블, notes 테이블, JWT 인증 토큰
- **Instructions**:
  1. 백링크 조회 로직 구현 (note_links.target_note_id → source_note_id 역추적)
  2. BacklinkController `GET /notes/{id}/backlinks` 구현
  3. GraphService 구현 (노트 → 노드, 링크 → 엣지 변환)
  4. GraphController `GET /graph/data` 구현 (nodes + edges 단일 엔드포인트)
     - 노드 응답 필드: `id`, `title`, `linkCount`, `pageRank` (구 `val` → 분리)
     - 엣지 배열명: `edges` (구 `links` 아님), 각 엣지에 `type` 필드 포함
  5. (구 `/graph/nodes`, `/graph/edges` 별도 엔드포인트 제거 — Wiki 미정의)
  6. D3.js 호환 JSON 포맷 정의 (`{nodes: [{id, title, linkCount, pageRank}], edges: [{source, target, type}]}`)
  7. 사용자별 그래프 필터링 로직 추가 (`GET /graph/data?userId={id}`)
  8. 통합 테스트 작성
- **Output Format**: graph 모듈 코드 + API 문서 (Swagger) + 테스트 코드
- **Constraints**:
  - 그래프 노드 최대 1,000개 (페이징 또는 제한)
  - API 응답 시간 < 500ms (1,000 노드 기준)
  - D3.js force-directed graph JSON 포맷 준수
- **Duration**: 2일
- **RULE Reference**: wiki 03*아키텍처*정의서 §지식 그래프, wiki 18*기술*스택\_정의서
- **Assignee**: @knowledge-owner-1
- **Reviewer**: @team-lead

**Status**: [ ] Not Started / [ ] In Progress / [ ] Done

---

## Step 5: Kafka 기반 Elasticsearch 자동 동기화

- **Step Name**: Kafka→ES 자동 동기화
- **Step Goal**: 노트 생성/수정 시 Kafka를 통해 Elasticsearch에 자동 동기화되어 검색에 반영된다.
- **Done When**:
  - [ ] 노트 생성 시 note.created Kafka 이벤트 발행
  - [ ] 노트 수정 시 note.updated Kafka 이벤트 발행
  - [ ] Kafka Consumer가 이벤트를 소비하여 ES에 인덱싱
  - [ ] `GET /notes/search?q={keyword}` → ES 기반 검색 결과 반환
  - [ ] 한글 형태소 분석 (nori) 동작
  - [ ] 통합 테스트 통과
- **Scope**:
  - In Scope:
    - Kafka Producer 구현 (note.created, note.updated 이벤트)
    - Kafka Consumer 구현 (ES 인덱싱)
    - Elasticsearch 인덱스 설정 (nori 한글 형태소)
    - 검색 API 구현 (ES 쿼리)
    - Avro 스키마 + Schema Registry 등록
    - 통합 테스트
  - Out of Scope:
    - 실시간 검색 자동완성 (typeahead)
    - 검색 결과 랭킹 최적화
    - 노트 삭제 시 ES 동기화 (soft delete 기반)
- **Input**: notes 테이블, Kafka 토픽, Elasticsearch 접속 정보, Schema Registry
- **Instructions**:
  1. note.created / note.updated Avro 스키마 정의
  2. Schema Registry에 스키마 등록
  3. NoteService에 Kafka Producer 추가 (생성/수정 시 이벤트 발행)
  4. Elasticsearch 인덱스 생성 (notes 인덱스, nori analyzer)
  5. Kafka Consumer 구현 (이벤트 → ES 문서 인덱싱)
  6. 검색 API 구현 (`GET /notes/search?q={keyword}`)
  7. 통합 테스트 작성 (Embedded Kafka + Testcontainers ES)
- **Output Format**: note 모듈 Kafka/ES 코드 + Avro 스키마 + 테스트 코드
- **Constraints**:
  - ES 인덱싱 지연: 이벤트 발행 후 5초 이내
  - 검색 API 응답 시간 < 200ms
  - nori 한글 형태소 분석 필수
- **Duration**: 1일
- **RULE Reference**: wiki 03*아키텍처*정의서 §이벤트 설계, wiki 18*기술*스택\_정의서 §Elasticsearch
- **Assignee**: @knowledge-owner-1
- **Reviewer**: @team-lead

**Status**: [ ] Not Started / [ ] In Progress / [ ] Done

---

## W3 (2026-05-26 ~ 2026-05-29, 5/25 부처님오신날 제외)

---

## Step 6: 노트 수정 이력 및 버전 복원

- **Step Name**: 노트 버전 이력/복원
- **Step Goal**: 사용자가 노트 수정 이력을 조회하고 이전 버전으로 복원할 수 있다.
- **Done When**:
  - [ ] 노트 수정 시 note_versions 테이블에 이전 버전 자동 저장
  - [ ] `GET /notes/{id}/versions` → 수정 이력 목록 조회
  - [ ] `GET /notes/{id}/versions/{versionId}` → 특정 버전 상세 조회
  - [ ] `POST /notes/{id}/versions/{versionId}/restore` → 이전 버전으로 복원 (Wiki에 추가 예정)
  - [ ] note_versions 테이블 Flyway 마이그레이션 완료
  - [ ] 통합 테스트 통과
- **Scope**:
  - In Scope:
    - note_versions 테이블 설계 + Flyway 마이그레이션
    - 노트 수정 시 이전 버전 자동 저장 로직
    - 수정 이력 목록 조회 API
    - 특정 버전 상세 조회 API
    - 이전 버전 복원 API
    - 통합 테스트
  - Out of Scope:
    - 버전 간 diff 표시
    - 자동 병합 (conflict resolution)
    - 버전 보존 기간 정책
- **Input**: notes 테이블, NoteService, JWT 인증 토큰
- **Instructions**:
  1. note_versions 테이블 DDL 작성 (id, note_id, version_number, title, content_md, change_summary, created_by, created_at)
     - `content_md`: Markdown 원문 (구 `content` → ERD 기준 `content_md`)
     - `change_summary`: 버전 변경 요약 메모
  2. Flyway 마이그레이션 파일 생성
  3. NoteVersion 엔티티 + Repository 작성
  4. NoteService.update에 이전 버전 저장 로직 추가
  5. 수정 이력 목록 조회 API 구현 (`GET /notes/{id}/versions`)
  6. 특정 버전 상세 조회 API 구현
  7. 복원 API 구현 (선택 버전 → 현재 노트에 덮어쓰기 + 새 버전 생성)
  8. 통합 테스트 작성 (수정 → 이력 확인 → 복원 시나리오)
- **Output Format**: note 모듈 버전 관리 코드 + Flyway 마이그레이션 + 테스트 코드
- **Constraints**:
  - 버전 번호: 자동 증가 (1, 2, 3, ...)
  - 복원 시 새 버전으로 생성 (이력 보존)
  - 최대 50개 버전 보존 (초과 시 가장 오래된 버전 삭제)
- **Duration**: 1.5일
- **RULE Reference**: wiki 03*아키텍처*정의서 §REST API 규칙, wiki 09*Git*규칙\_정의서 §커밋 컨벤션
- **Assignee**: @knowledge-owner-1
- **Reviewer**: @team-lead

**Status**: [ ] Not Started / [ ] In Progress / [ ] Done

---

## Step 7: 태그 필터링 및 자동완성

- **Step Name**: 태그 필터링/자동완성
- **Step Goal**: 사용자가 태그로 노트를 필터링하고, 태그 자동완성을 사용할 수 있다.
- **Done When**:
  - [ ] `POST /notes/{id}/tags` → 노트에 태그 추가 (Wiki에 추가 예정)
  - [ ] `DELETE /notes/{id}/tags/{tagId}` → 노트에서 태그 제거 (Wiki에 추가 예정)
  - [ ] `GET /notes?tag={tagName}` → 태그 기반 노트 필터링
  - [ ] `GET /tags/autocomplete?q={prefix}` → 태그 자동완성 (prefix 매칭, Wiki에 추가 예정)
  - [ ] tags / note_tags 테이블 Flyway 마이그레이션 완료
  - [ ] 통합 테스트 통과
- **Scope**:
  - In Scope:
    - tags 테이블 + note_tags 조인 테이블 설계 + Flyway 마이그레이션
    - 태그 추가/제거 API
    - 태그 기반 노트 필터링 API
    - 태그 자동완성 API (prefix 매칭)
    - 인기 태그 목록 API
    - 통합 테스트
  - Out of Scope:
    - 태그 기반 추천
    - 태그 계층 구조 (parent-child)
    - 태그 동의어 관리
- **Input**: notes 테이블, JWT 인증 토큰, PRD 태그 기능 요구사항
- **Instructions**:
  1. tags 테이블 DDL 작성 (id, name, color, created_at)
     - `color`: 태그 색상 (ERD에 포함 — `usage_count`는 ERD에 없으므로 제외 또는 별도 집계)
     - 주의: `usage_count`는 ERD에 정의되지 않음 — 필요 시 note_tags 집계 쿼리로 대체
  2. note_tags 테이블 DDL 작성 (note_id, tag_id)
  3. Flyway 마이그레이션 파일 생성
  4. Tag 엔티티 + Repository 작성
  5. TagService 구현 (addTag, removeTag, autocomplete)
  6. 자동완성 정렬: 사용 빈도 기준 (note_tags 집계 쿼리 활용)
  7. 노트 필터링 API 수정 (tag 파라미터 추가)
  8. 자동완성 API 구현 (LIKE '{prefix}%' + 사용 빈도 내림차순)
  9. 통합 테스트 작성
- **Output Format**: note 모듈 태그 코드 + Flyway 마이그레이션 + 테스트 코드
- **Constraints**:
  - 태그명: 최대 30자, 소문자 정규화
  - 한 노트 최대 10개 태그
  - 자동완성: 최대 10개 결과, 응답 < 100ms
- **Duration**: 1.5일
- **RULE Reference**: wiki 03*아키텍처*정의서 §REST API 규칙, wiki 09*Git*규칙\_정의서 §커밋 컨벤션
- **Assignee**: @knowledge-owner-1
- **Reviewer**: @team-lead

**Status**: [ ] Not Started / [ ] In Progress / [ ] Done

---

## W5 (2026-06-08 ~ 2026-06-12 — E2E + 발표 준비)

---

## Step 8: 노트/그래프 전체 E2E 테스트

- **Step Name**: 노트/그래프 E2E 테스트
- **Step Goal**: 노트/그래프 전체 E2E(노트생성→위키링크→그래프→검색) 시나리오가 통과한다.
- **Done When**:
  - [ ] 노트 생성 → 위키링크 파싱 → 링크 저장 시나리오 통과
  - [ ] 백링크 조회 → 그래프 노드/엣지 데이터 반환 시나리오 통과
  - [ ] 노트 생성 → Kafka → ES 인덱싱 → 검색 시나리오 통과
  - [ ] 태그 추가 → 태그 필터링 시나리오 통과
  - [ ] 노트 수정 → 버전 이력 → 복원 시나리오 통과
  - [ ] 실패 케이스 식별 및 이슈 등록
- **Scope**:
  - In Scope:
    - 노트 CRUD → 위키링크 → 그래프 E2E
    - Kafka → ES 인덱싱 → 검색 E2E
    - 태그 추가/필터링 E2E
    - 버전 이력/복원 E2E
    - 서비스 간 이벤트 흐름 검증
    - 실패 케이스 이슈 등록
  - Out of Scope:
    - 부하/성능 테스트
    - 프론트엔드 연동 테스트
    - 다른 서비스 연동 E2E
- **Input**: staging 환경, Kafka 토픽, Elasticsearch, 테스트 사용자 계정
- **Instructions**:
  1. E2E 테스트 환경 설정 (staging, 테스트 데이터 초기화)
  2. 노트 생성 → 위키링크 파싱 → note_links 확인
  3. 백링크 조회 → 그래프 API 데이터 확인
  4. 노트 생성 → Kafka 이벤트 → ES 인덱싱 → 검색 결과 확인
  5. 태그 추가 → 태그 필터링 → 자동완성 확인
  6. 노트 수정 → 버전 이력 → 복원 확인
  7. 실패 케이스 식별 및 이슈 등록
- **Output Format**: E2E 테스트 코드 + 테스트 결과 리포트
- **Constraints**:
  - Happy Path 100% 통과 필수
  - ES 인덱싱 지연 포함 E2E: 10초 이내 완료
  - 테스트 데이터 자동 정리 (teardown)
- **Duration**: 1.5일
- **RULE Reference**: wiki 03*아키텍처*정의서 §테스트 전략, wiki 09*Git*규칙\_정의서 §이슈 관리
- **Assignee**: @knowledge-owner-1
- **Reviewer**: @team-lead

**Status**: [ ] Not Started / [ ] In Progress / [ ] Done

---

## Step 9: P0 버그 수정 및 ES 동기화 안정화

- **Step Name**: P0 버그 수정/ES 안정화
- **Step Goal**: knowledge-svc의 P0 버그가 모두 수정되고 ES 동기화가 안정화된다.
- **Done When**:
  - [ ] P0 버그 목록 확인 및 전수 수정 완료
  - [ ] 수정된 버그 재현 테스트 통과
  - [ ] ES 동기화 성공률 > 99%
  - [ ] ES 인덱싱 지연 < 5초 안정화
  - [ ] Kafka Consumer lag 0 유지
  - [ ] 회귀 테스트 전체 통과
- **Scope**:
  - In Scope:
    - P0 버그 전수 수정
    - 버그 수정 후 재현 테스트
    - ES 동기화 안정화 (재시도 로직 보강)
    - Kafka Consumer 오류 처리 강화
    - ES 인덱싱 모니터링 메트릭 추가
    - 회귀 테스트 실행
  - Out of Scope:
    - P1/P2 버그 수정
    - 새 기능 추가
    - ES 성능 최적화 (샤드/레플리카 튜닝)
- **Input**: P0 버그 목록 (GitHub Issues), ES 동기화 로그, Kafka Consumer 메트릭
- **Instructions**:
  1. P0 버그 목록 확인 (GitHub Issues 필터)
  2. 각 버그 재현 → 원인 분석 → 수정
  3. 수정 후 재현 테스트 작성 및 통과 확인
  4. ES 동기화 실패 원인 분석 (네트워크, 스키마, 타임아웃)
  5. Consumer 재시도 로직 보강 (exponential backoff)
  6. ES 인덱싱 모니터링 메트릭 추가 (성공/실패/지연)
  7. 전체 회귀 테스트 실행 및 통과 확인
- **Output Format**: 버그 수정 PR 목록 + 회귀 테스트 결과 + 안정화 리포트
- **Constraints**:
  - P0 버그 0건 달성 필수
  - 수정 시 회귀 방지 (테스트 추가 필수)
  - ES 동기화 성공률 > 99%
- **Duration**: 1.5일
- **RULE Reference**: wiki 09*Git*규칙*정의서 §이슈 관리, wiki 03*아키텍처\_정의서 §이벤트 설계
- **Assignee**: @knowledge-owner-1
- **Reviewer**: @team-lead

**Status**: [ ] Not Started / [ ] In Progress / [ ] Done
