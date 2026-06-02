# WORKPLAN: @knowledge-owner-1 — W3 현황 진단 및 잔여 작업 계획

> **작성일**: 2026-05-29 (W3 마지막 날)  
> **브랜치**: dev  
> **기준 문서**: TASK_knowledge-1.md | WORKFLOW W1~W4 | rules 01·02·03·07·08·12·14  

---

## 1. 현재 진행 상태 (소스코드 + git log 기반)

> 분석 브랜치: `dev` (latest: `4816c94 feat(graph): Step4 잔여 구현`)

### 1.1 Step별 완료 여부

| Step | 내용 | Due Week | 상태 | 근거 |
|------|------|----------|------|------|
| Step 1 | knowledge-svc 골격 생성 | W1 05-16 | ✅ **DONE** | Modulith 구조, Dockerfile, ModuleStructureTest 전부 존재 |
| Step 2 | note Markdown CRUD | W1 05-16 | ✅ **DONE** | NoteController(5 엔드포인트), NoteService CRUD, Flyway V1, soft delete, 소유자 검증, 통합 테스트 |
| Step 3 | note 위키링크 파싱 | W1 05-16 | ✅ **DONE** | WikiLinkParser, NoteLink 엔티티, Flyway V2, NoteService 후처리, WikiLinkParserTest, NoteLinkIntegrationTest |
| Step 4 | 백링크 조회 + D3.js 그래프 API | W2 05-23 | ✅ **DONE** | GraphService(PageRank 포함), GraphController, GraphQueryPort, 통합 테스트 3건 — WORKFLOW W2 `[x] Done` |
| Step 5 | Kafka→ES 자동 동기화 | W2 05-23 | ⚠️ **PARTIAL** | ES 동기화는 `@TransactionalEventListener` 방식으로 구현(Spring Event, Kafka 아님), BM25 검색 API 구현, `GET /notes/search?q=` 골격 존재, Kafka 연동 미적용 |
| Step 6 | 노트 버전 이력/복원 | W3 05-30 | ❌ **NOT STARTED** | note_versions 테이블 없음, NoteVersionService 없음 |
| Step 7 | 태그 필터링 + 자동완성 | W3 05-30 | ⚠️ **PARTIAL** | V4 태그 테이블 생성 + Note 엔티티에 tags 필드 저장까지만. TagController/TagService/자동완성 API 없음 |
| Step 8 | E2E 테스트 | W4 06-06 | ❌ **NOT STARTED** | 테스트 파일 없음 |
| Step 9 | P0 버그 수정 + ES 안정화 | W4 06-06 | ❌ **NOT STARTED** | - |

### 1.2 전체 진행률

```
Step 1  ████████████ 100%  ✅
Step 2  ████████████ 100%  ✅
Step 3  ████████████ 100%  ✅
Step 4  ████████████ 100%  ✅
Step 5  ██████░░░░░░  50%  ⚠️ (ES sync done, Kafka 연동 미적용)
Step 6  ░░░░░░░░░░░░   0%  ❌
Step 7  ████░░░░░░░░  30%  ⚠️ (태그 저장만, API 없음)
Step 8  ░░░░░░░░░░░░   0%  ❌
Step 9  ░░░░░░░░░░░░   0%  ❌
```

**전체 완료율**: 4.8 / 9 Steps = **약 53%**

---

## 2. Step 5 구현 방식 갭 분석

### 현재 구현
- `NoteService` → `ApplicationEventPublisher.publishEvent(NoteSearchSyncRequested)` 발행
- `NoteSearchIndexingListener` → `@TransactionalEventListener(AFTER_COMMIT)` + `@Async` 수신 → ES upsert/delete

### TASK 요구사항
- Kafka Producer (note.created, note.updated 이벤트 발행)
- Kafka Consumer → ES 인덱싱
- Avro 스키마 + Schema Registry 등록

### 판단
현재 구현은 **기능적으로는 동일**하지만 인프라 방식이 다름.  
TASK Constraints에 "Kafka 기반"이 명시되어 있으므로,  
W4~W5 일정에서 Kafka 연동 여부를 팀 리드와 확인 후 결정 필요.

> **액션 아이템**: `WORKFLOW_knowledge-1_W2.md` Step 5 상태를 현재 구현 내용으로 업데이트 필요

---

## 3. 잔여 작업 및 재조정 일정

### 3.1 남은 소요 시간

| Step | 잔여 작업 | 예상 소요 |
|------|-----------|-----------|
| Step 5 | Kafka 연동 (팀 리드 확인 후 진행 여부 결정) | 0.5~1일 (선택) |
| Step 6 | note_versions 테이블 + 버전 저장/조회/복원 API + 통합 테스트 | 1.5일 |
| Step 7 | 태그 필터링 API + 자동완성 API + 인기 태그 API + 통합 테스트 | 1일 (V4 마이그레이션·저장 완료로 단축) |
| Step 8 | 핵심 E2E 시나리오 (노트→위키링크→그래프→검색) | 1.5일 |
| Step 9 | P0 버그 수정 + ES 동기화 성공률 확인 | 1일 |

**총 잔여 소요**: 5~5.5일 (Kafka 선택 포함 시 6~6.5일)

### 3.2 가용 시간

| 기간 | 날짜 | 가용일 |
|------|------|--------|
| W3 잔여 | 2026-05-29 (오늘) ~ 05-30 (금) | **1.5일** |
| W4 | 2026-06-02 (월) ~ 06-06 (금) | **5일** |
| W5 | 2026-06-08 (월) ~ 06-12 (금) | **5일** |
| **합계** | | **11.5일** |

### 3.3 재조정 일정표

#### W3 잔여 (05-29 ~ 05-30) — 1.5일

| 날짜 | 작업 | 완료 조건 |
|------|------|-----------|
| **05-29 (오늘)** | WORKFLOW W2 Step 5 상태 업데이트 + **Step 6 착수**: note_versions DDL + Flyway 마이그레이션 작성 | V5__init_note_versions_table.sql 작성 완료 |
| **05-30 (금)** | **Step 6 진행**: NoteVersion 엔티티 + NoteVersionRepository + NoteVersionService (버전 저장 + 목록 조회) | NoteVersion.java, NoteVersionRepository.java, 버전 생성 로직 |

**W3 종료 Done When**:
- [ ] `V5__init_note_versions_table.sql` Flyway 마이그레이션 작성
- [ ] NoteVersion 엔티티 + NoteVersionRepository 작성
- [ ] NoteService.update에 이전 버전 자동 저장 로직 추가
- [ ] `GET /notes/{id}/versions` 엔드포인트 구현

---

#### W4 (06-02 ~ 06-06) — 5일

| 날짜 | 작업 | 완료 조건 |
|------|------|-----------|
| **06-02 (월)** | **Step 6 완료**: 버전 상세 조회 + 복원 API + 통합 테스트 | `GET /notes/{id}/versions/{versionNumber}`, `POST .../restore` 통합 테스트 통과 |
| **06-03 (화)** | **Step 7 진행**: TagService (필터링) + `GET /notes?tag=` 파라미터 확장 | 태그 기반 노트 필터링 API 구현 |
| **06-04 (수)** | **Step 7 완료**: 태그 자동완성 + 인기 태그 API + 통합 테스트 | `GET /tags/autocomplete?q=`, `GET /tags/popular` 통합 테스트 통과 |
| **06-05 (목)** | **Step 5 Kafka 검토**: 팀 리드 확인 후 Kafka 연동 또는 현 구현 유지 결정. Step 8 E2E 시나리오 설계 | Kafka 결정 문서 or PR, E2E 시나리오 초안 |
| **06-06 (금)** | **Step 8 착수**: E2E 테스트 환경 구성 + 노트 생성→위키링크 E2E 작성 | E2E 테스트 셋업 + 첫 시나리오 통과 |

**W4 종료 Done When**:
- [ ] `GET /notes/{id}/versions` 수정 이력 목록 반환
- [ ] `GET /notes/{id}/versions/{versionNumber}` 특정 버전 상세 조회
- [ ] `POST /notes/{id}/versions/{versionNumber}/restore` 복원 동작
- [ ] `GET /notes?tag={tagName}` 태그 필터링
- [ ] `GET /tags/autocomplete?q={prefix}` 최대 10건 응답 < 100ms
- [ ] `GET /tags/popular` 인기 태그 상위 N개
- [ ] Step 5 Kafka 연동 결정 완료

---

#### W5 (06-08 ~ 06-12) — 5일

| 날짜 | 작업 | 완료 조건 |
|------|------|-----------|
| **06-08 (월)** | **Step 8 진행**: 위키링크→그래프→검색 E2E 시나리오 | E2E 시나리오 2~3개 통과 |
| **06-09 (화)** | **Step 8 진행**: 태그 추가→필터링 E2E + 버전 이력→복원 E2E | E2E Happy Path 전체 통과 |
| **06-10 (수)** | **Step 8 완료**: 실패 케이스 식별 + 이슈 등록 + Step 9 시작 | GitHub Issues P0 목록 작성 |
| **06-11 (목)** | **Step 9 진행**: P0 버그 수정 + ES 동기화 성공률 확인 | P0 버그 수정 PR |
| **06-12 (금)** | **Step 9 완료**: 회귀 테스트 전체 통과 + HISTORY 최종 업데이트 | 회귀 테스트 전체 통과, ES 성공률 > 99% |

**W5 종료 Done When**:
- [ ] E2E Happy Path 100% 통과 (노트→위키링크→그래프→검색→태그→버전 복원)
- [ ] P0 버그 0건
- [ ] ES 동기화 성공률 > 99%
- [ ] HISTORY_knowledge-1.md 전 주차 완료 기록

---

## 4. 오늘(05-29) 즉시 실행 계획

### Step 1: WORKFLOW W2 Step 5 상태 업데이트

```
파일: docs/project-management/workflow/WORKFLOW_knowledge-1_W2.md
변경: Step 5 체크박스 현재 구현 상태로 업데이트
커밋: docs(workflow): step5 현재 구현(Spring Event 기반 ES sync) 상태 반영
```

### Step 2: Step 6 착수 — note_versions 테이블 설계

```
브랜치: feat/step6-note-version
작업 순서 (RULE 14 10단계 워크플로):
  ① 요구사항 확인: TASK Step 6 Done When 전체 확인
  ② 설계: note_versions ERD + API 스펙
  ③ 브랜치 생성: feat/step6-note-version
  ④ Flyway V5__init_note_versions_table.sql 작성
  ⑤ NoteVersion 엔티티 작성
  ⑥ NoteVersionRepository 작성
```

**note_versions 테이블 설계 (TASK Step 6 기준)**:
```sql
CREATE TABLE note_versions (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    note_id     BIGINT      NOT NULL,
    version_no  INT         NOT NULL,
    title       VARCHAR(200) NOT NULL,
    content_md  TEXT,
    created_by  BIGINT      NOT NULL,
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_note_versions_note FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE CASCADE,
    CONSTRAINT uq_note_version UNIQUE (note_id, version_no)
);
CREATE INDEX idx_note_versions_note_id_created_at ON note_versions(note_id, created_at DESC);
```

**핵심 제약 (TASK Step 6 Constraints)**:
- 버전 번호: 자동 증가 (1, 2, 3, ...)
- 복원 시 새 버전으로 생성 (이력 보존, 덮어쓰기 X)
- 최대 50개 버전 보존 (초과 시 가장 오래된 버전 삭제)
- 소유자만 버전 접근 가능 (JWT 인증, RULE 01-security)

---

## 5. 브랜치 및 PR 계획

| Step | 브랜치명 | PR 조건 (RULE 12) |
|------|---------|------------------|
| Step 6 | `feat/step6-note-version` | 3개 엔드포인트 + 통합 테스트 통과, 400줄 이하 |
| Step 7 | `feat/step7-tag-api` | 태그 필터링+자동완성+인기태그 + 통합 테스트 통과 |
| Step 8 | `test/step8-e2e` | E2E Happy Path 100% 통과 |
| Step 9 | `fix/step9-p0-bugs` | P0 버그 PR 목록 + 회귀 테스트 전체 통과 |

**PR 공통 필수 항목** (RULE 12.2):
```markdown
## 변경 사항
- [feat] ...

## 테스트 결과
- [x] 통합 테스트 N건 통과
- [x] 로컬 수동 검증
```

---

## 6. 제약 조건 체크리스트 (RULE 준수)

### Step 6 구현 시 체크 (RULE 01 Security 1차)
- [ ] JWT 인증 필수 (`@CurrentUserAuth`)
- [ ] 본인 노트의 버전만 접근 (소유자 검증)
- [ ] 버전 데이터에 민감 정보 없음 확인

### Step 7 구현 시 체크 (RULE 01 Security 1차)
- [ ] 태그명 최대 30자 `@Size(max=30)` (TASK 제약)
- [ ] SQL Injection 방지 — LIKE 파라미터 바인딩
- [ ] 태그 자동완성 응답에 타인 노트 정보 미포함
- [ ] 한 노트 최대 10개 태그 검증

### 전체 공통 (RULE 03 Technical)
- [ ] 모든 DB 변경: Flyway 마이그레이션 파일로 관리
- [ ] 통합 테스트: `@SpringBootTest` + Testcontainers
- [ ] Soft delete 정책 준수 (`deleted_at` 기반)

---

## 7. HISTORY.md 오늘 업데이트 (RULE 12.3 MUST)

파일: [HISTORY_knowledge-1.md](history/HISTORY_knowledge-1.md)  
오늘 퇴근 전 아래 형식으로 갱신:

```markdown
## 2026-05-29 (목) — @knowledge-owner-1

**한 일**
- docs(workflow): WORKFLOW W2 Step5 현재 구현 상태 반영
- feat(note-version): V5__init_note_versions_table.sql Flyway 마이그레이션 작성
- feat(note-version): NoteVersion 엔티티 + NoteVersionRepository 작성

**이슈**
- Step 5 Kafka 연동 미적용 → 팀 리드 확인 필요

**내일 계획**
- NoteVersionService 구현 (버전 저장 + 목록/상세 조회)
- GET /notes/{id}/versions 엔드포인트 구현
```
