# Work History: @knowledge-1

> **담당**: knowledge-svc / 노트·그래프  
> **관련 문서**: [SCOPE](../scope/SCOPE_knowledge-1.md) | [TASK](../task/TASK_knowledge-1.md) | [WORKFLOW](../workflow/WORKFLOW_knowledge-1_W1.md)

---

## 진행 상태 대시보드

### W1 (2026-05-12 ~ 05-15)

| Step   | 내용                    | 상태 | 시작일     | 완료일     | 비고 |
| ------ | ----------------------- | ---- | ---------- | ---------- | ---- |
| Step 1 | knowledge-svc 골격 생성 | Done | 2026-05-12 | 2026-05-19 | 누락 컨트롤러 05-19 추가 완료 |
| Step 2 | note Markdown CRUD      | Done | 2026-05-15 | 2026-05-18 | PR #7 머지 |
| Step 3 | 위키링크 파싱           | Done | 2026-05-18 | 2026-05-18 | PR #7 머지 |

**W1 진행률**: 3/3 Steps 완료

### W2 (2026-05-18 ~ 05-22)

| Step   | 내용                       | 상태        | 시작일     | 완료일     | 비고 |
| ------ | -------------------------- | ----------- | ---------- | ---------- | ---- |
| Step 4 | 백링크/지식 그래프 API     | Done        | 2026-05-22 | 2026-05-22 | GraphQueryPort 패턴 적용 |
| Step 5 | Kafka→ES 자동 동기화       | Not Started | —          | —          | Kafka 다음 주로 연기, ES+PG 우선 |

**W2 진행률**: 1/2 Steps 완료

### W3 (2026-05-26 ~ 05-29)

| Step   | 내용                   | 상태        | 시작일 | 완료일 | 비고 |
| ------ | ---------------------- | ----------- | ------ | ------ | ---- |
| Step 6 | 노트 버전 이력/복원    | Not Started | —      | —      |      |
| Step 7 | 태그 필터링/자동완성   | Not Started | —      | —      |      |

**W3 진행률**: 0/2 Steps 완료

### W5 (2026-06-08 ~ 06-12)

| Step   | 내용                       | 상태        | 시작일 | 완료일 | 비고 |
| ------ | -------------------------- | ----------- | ------ | ------ | ---- |
| Step 8 | 노트/그래프 E2E 테스트     | Not Started | —      | —      |      |
| Step 9 | P0 버그 수정 및 ES 안정화  | Not Started | —      | —      |      |

**W5 진행률**: 0/2 Steps 완료

---

## 작업 로그

### W1 (2026-05-12 ~ 05-15)

#### 2026-05-12 (월)

- **완료**:
  - 프로젝트 초기 부트스트랩 (`chore: bootstrap initial commit`)
  - GitHub Actions CI 워크플로 추가 (Phase 2 bootstrap)
  - `parse-workflow.yml` Dashboard 자동 push 설정
  - 코드 Rule북 17개 파일 배포 (`docs/rules/`)
  - 트랙별 프로젝트 관리 문서 배포
- **진행 중**: Step 1 — knowledge-svc 골격 생성
- **이슈**: CI `parse-workflow.yml` GITOPS_TOKEN 설정 문제
- **다음**: CI 토큰 문제 해결 후 Modulith 프로젝트 구조 생성

#### 2026-05-13 (화)

- **완료**: —
- **진행 중**: Step 1 — Spring Boot 4 + Modulith 프로젝트 구조 설계
- **이슈**: —
- **다음**: note / graph / chunking 패키지 구조 생성

#### 2026-05-14 (수)

- **완료**:
  - CI `parse-workflow.yml` 모든 브랜치 트리거 + push 재시도 설정 안정화
- **진행 중**: Step 1 — 모듈 패키지 구조 생성
- **이슈**: parse-workflow CI 간헐적 실패 → 재시도 로직 추가로 해결
- **다음**: note CRUD 구현 시작

#### 2026-05-15 (목)

- **완료**:
  - AGENTS.md + 프로젝트 compound 스킬 추가 (`docs(agent)`)
  - Modulith 모듈 구조 설정 완료 (knowledge-2 협업, PR #2)
  - W1 Step2 ArchUnit 모듈 경계 테스트 추가 (PR #3)
- **진행 중**: Step 2 — Note Markdown CRUD 구현
- **이슈**: —
- **다음**: Note CRUD API 5개 엔드포인트 구현

#### 2026-05-16 (금)

- **완료**: —
- **진행 중**: Step 2 — NoteService, NoteController 구현
- **이슈**: —
- **주간 요약**: W1 Step 1 골격 생성 완료. ArchUnit 모듈 경계 테스트 추가. Note CRUD 구현 진행 중.

---

### W2 (2026-05-18 ~ 05-22)

#### 2026-05-18 (일)

- **완료**:
  - Step 2 — Note Markdown CRUD 완전 구현 (PR #7)
    - Note 엔티티, NoteRepository, NoteService, NoteController (5개 엔드포인트)
    - Shared 모듈 인프라: BaseEntity, GlobalExceptionHandler, MarkdownSanitizer (XSS 방어)
    - 소유자 권한 검증 (IDOR 403), soft delete, NoteIntegrationTest 통과
  - Step 3 — 위키링크 파싱 완전 구현 (PR #7)
    - WikiLinkParser (정규식 `\[\[([^\]]+)\]\]`, ReDoS 방어)
    - NoteService 연동 — 생성/수정 시 자동 링크 추출·저장
    - `GET /notes/{id}/backlinks` API, NoteLinkIntegrationTest 통과
  - Spring Modulith 2.0.6 기준 요구사항 정렬 (PR #6)
  - CI gradlew 실행 권한 수정 100644→100755 (PR #8)
- **진행 중**: Step 4 — 지식 그래프 API 설계
- **이슈**: gradlew 실행 권한 누락으로 CI 빌드 실패 → 권한 수정으로 해결
- **다음**: GraphController 구현, GraphQueryPort 패턴 설계

#### 2026-05-19 (월)

- **완료**:
  - Step 1 보완 — 누락된 GraphController, ChunkingController 추가 (PR #11)
    - W1 골격 생성 단계에서 누락된 컨트롤러 추가로 아키텍처 완성
  - WORKFLOW W1 문서 상태 동기화 (PR #10)
  - Step3 avro 완료 상태 동기화 (PR #9)
  - TASK 파일 업데이트 (PR #12)
- **진행 중**: Step 4 — 지식 그래프 API 구현
- **이슈**: W1 Step 1 당시 graph/chunking 컨트롤러 누락 — 뒤늦게 발견 및 추가
- **다음**: GraphQueryPort 인터페이스 설계 및 구현

#### 2026-05-20 (화)

- **완료**:
  - Dockerfile EKS 배포용 multi-stage 빌드 개선 (PR #16)
    - 빌드 스테이지 분리, 이미지 크기 최적화
- **진행 중**: Step 4 — GraphQueryPort 설계
- **이슈**: —
- **다음**: GraphService, GraphController 구현

#### 2026-05-21 (수)

- **완료**:
  - BM25 노트 검색 + JWT 인증 골격 추가 (PR #19, knowledge-2 협업)
    - Elasticsearch nori 기반 검색 API, cursor 페이지네이션
    - SecurityConfig, WebMvcConfig JWT 인증 골격
    - SearchElasticsearchIntegrationTest 추가
  - gstack skill routing rules CLAUDE.md 추가
- **진행 중**: Step 4 — GraphQueryPort 구현 시작
  - `shared/GraphQueryPort.java` 인터페이스 정의
  - `shared/GraphNoteData.java` 레코드 정의
- **이슈**: —
- **다음**: GraphLinkData, NoteGraphAdapter, GraphService, GraphController 구현

#### 2026-05-22 (목)

- **완료**:
  - Step 4 — 백링크/지식 그래프 API 완전 구현
    - `shared/GraphLinkData.java` — (sourceNoteId, targetNoteId) 레코드
    - `note/application/NoteGraphAdapter.java` — GraphQueryPort 구현체
      - ArchUnit 모듈 경계 준수 (note → shared 단방향)
      - `findTop1000ByUserIdAndDeletedAtIsNull`, `findBySourceNoteIdIn` 쿼리 추가
    - `graph/application/GraphService.java` — linkCount(in-degree) + PageRank(10회 반복, damping=0.85)
    - `graph/dto/` — GraphDataResponse, GraphNodeResponse, GraphEdgeResponse
    - `graph/presentation/GraphController.java` — `GET /api/graph/data?userId={id}`
    - `GraphIntegrationTest` 5개 케이스 통과
    - ArchUnit 모듈 경계 테스트 통과
  - TASK_knowledge-1.md Step 4 Done 상태 업데이트
- **진행 중**: —
- **이슈**: —
- **다음**: Step 5 Kafka→ES 동기화 (다음 주, Kafka 환경 준비 후)

#### 2026-05-23 (금)

- **완료**: —
- **진행 중**: —
- **이슈**: —
- **주간 요약**: Step 4 지식 그래프 API 완료. GraphQueryPort 포트 패턴으로 ArchUnit 모듈 경계 준수. `GET /api/graph/data?userId={id}` → D3.js 호환 JSON(nodes+edges) 반환. Step 5 Kafka 연동은 다음 주로 연기, ES+PostgreSQL 기반 우선 진행 예정.

---

### W3 (2026-05-26 ~ 05-29)

#### 2026-05-26 (월)

- **완료**:
- **진행 중**:
- **이슈**:
- **다음**:

#### 2026-05-27 (화)

- **완료**:
- **진행 중**:
- **이슈**:
- **다음**:

#### 2026-05-28 (수)

- **완료**:
- **진행 중**:
- **이슈**:
- **다음**:

#### 2026-05-29 (목)

- **완료**:
- **진행 중**:
- **이슈**:
- **다음**:

#### 2026-05-30 (금)

- **완료**:
- **진행 중**:
- **이슈**:
- **주간 요약**:

---

### W4 (2026-06-01 ~ 06-05)

#### 2026-06-02 (화)

- **한 일**:
  - `build` 잡이 인프라 없이 통합 테스트를 실행해 장시간 대기하던 CI 이슈 원인 분석
  - `.github/workflows/ci-java.yml`에 `build` 잡용 compose 기동/정리 스텝 추가
  - `docker-compose.ci.yml`에 Kafka 브로커 추가, `application-test.yml`과 `build.gradle.kts`에 테스트 timeout/빠른 실패 설정 반영
  - `application-test.yml`의 `listener.auto-startup: false` 설정 오기입 발견 및 제거
  - `SearchElasticsearchIntegrationTest` Kafka consumer group 격리 문제 원인 분석 → WORKPLAN에 따라 `@Disabled` 처리
  - `feature/kafka-es-sync` PR CI 최종 통과 확인
  - RULE 04.1·04.4·12.3 컨벤션 위반 수정 → `fix/convention-violations` PR #42 생성
  - W3 Step 6 구현: `note_versions` 테이블(V8 Flyway), `NoteVersion` 엔티티/리포지토리, `NoteVersionService`, `NoteVersionController` (버전 목록/상세/복원 3 엔드포인트), `NoteService.update()` 버전 스냅샷 훅, 단위+통합 테스트
  - W3 Step 7 구현: `TagService`(자동완성 userId 필터링·인기태그 Redis 캐싱), `TagController`, `NoteController` `?tag=` 필터 확장, `RedisConfig`, 단위+통합 테스트
  - 코드 리뷰 버그 수정 8건: autocomplete 타인 태그 노출 보안 패치, restore() 이벤트 위임(NoteService.restoreVersion), LIMIT→setMaxResults, prune try-catch, TagController 인증 정리, keys() NPE, EntityManager 생성자 주입, Note.sanitizeTags 소문자 정규화
  - `feat/step6-step7-note-version-tag-api` 브랜치 생성 (dev 기준)
- **이슈**:
  - 기존 `docker-compose.ci.yml`에는 PostgreSQL/Elasticsearch만 있어 Kafka producer 초기화 경로를 충족하지 못했음
  - 동일 consumer group ID(`knowledge-search-indexer`)를 사용하는 여러 `@SpringBootTest` 컨텍스트가 Kafka 파티션을 경쟁하여 `SearchElasticsearchIntegrationTest`가 메시지를 수신하지 못하는 구조적 문제 확인
- **내일 계획**:
  - PR #42 머지 확인 후 `feat/step6-step7-note-version-tag-api` rebase → W3 PR 생성 (dev 타겟)
  - TASK Step 6·7 Done When 체크박스 업데이트

#### 2026-06-03 (수)

- **한 일**:
  - PR #42 CI 통과 대기 및 리뷰 대응
  - `feat/step6-step7-note-version-tag-api` 브랜치 최종 self-review — diff 전체 검토, 불필요 로그 제거
  - Step 6·7 Done When 체크박스 갱신 준비, 테스트 재실행으로 전체 통과 재확인
- **이슈**:
  - PR #42 CI가 06-02 야간까지 실행 중 → 머지 06-04로 순연
- **내일 계획**:
  - PR #42 머지 완료 후 `feat/step6-step7-note-version-tag-api` PR 생성 (dev 타겟)
  - MSK TLS `security.protocol` 배선 이슈(#44) 처리

#### 2026-06-04 (목)

- **한 일**:
  - PR #42 머지 확인 (`fix: RULE 04.4·04.1·12.3 컨벤션 위반 수정`)
  - `feat/step6-step7-note-version-tag-api` PR #43 생성 및 머지 — W3 Step 6(노트 버전 이력/복원) + Step 7(태그 API) 최종 반영
  - Issue #44 분석: gitops WS3-A 감사에서 발견된 MSK TLS-only 환경 대비 `security.protocol` 누락 확인
  - `feature/KNOW-kafka-security-protocol` 브랜치 생성 → `global/config/KafkaConfig`, `search/config/KafkaConfig` 양쪽에 `spring.kafka.security.protocol` 조건부 주입 추가, `KafkaConfigTest` 신설
  - PR #45 생성 및 머지 (`fix(kafka): MSK TLS security protocol 배선 추가`)
- **이슈**:
  - `applySecurityProtocol()` 미적용 시 MSK SSL 환경에서 producer/consumer 모두 PLAINTEXT로 접속 시도 → `CommonClientConfigs.SECURITY_PROTOCOL_CONFIG` 직접 주입으로 해결
- **내일 계획**:
  - W4 Step 8(E2E) + Step 9(ES 동기화 안정화) 착수
  - KAFKA_ENABLED 게이트 부재 이슈(#46) 확인 후 처리

#### 2026-06-05 (금)

- **한 일**:
  - Issue #46 분석: gitops 오버레이(PR #118·#119)가 `KAFKA_ENABLED=true`를 주입하지만 knowledge-svc에 `@ConditionalOnProperty` 게이트 없어 no-op 확인
  - `fix/kafka-enabled-gate` 브랜치 생성 → engagement-svc 패턴 대조 검증 후 구현
    - `global/config/KafkaConfig`, `search/config/KafkaConfig`, `NoteEventPublisher`, `NoteEventOutboxDispatcher`, `NoteSearchKafkaProducer`, `NoteSearchKafkaConsumer` 6곳에 `@ConditionalOnProperty(synapse.kafka.enabled)` 게이트 추가
    - `application.yml` `synapse.kafka.enabled: ${KAFKA_ENABLED:false}` 바인딩, `application-test.yml` `enabled: true` 설정
    - PR #45에서 추가했던 `security.protocol` 배선이 현 브랜치에서 제거된 것 발견 → `CommonClientConfigs.SECURITY_PROTOCOL_CONFIG` 복원
    - `docker-compose.ci.yml` INTERNAL 리스너 추가 + `confluentinc/cp-schema-registry:7.7.0` 서비스 추가 (포트 8086)
    - `ci-java.yml` `build`·`dev-smoke` 잡 양쪽에 Docker Hub 조건부 로그인 추가 (Issue #39 대응)
    - `.github/workflows/flyway-guard.yml` caller 생성
  - Issue #47(W5 E2E), #48(Flyway 표준) 확인
  - PR #49 (`feat(search): 검색 E2E와 coverage gate 복구`) 검토 — Step 8 완료 처리
- **이슈**:
  - `applySecurityProtocol()` 제거가 이슈 #46 scope를 벗어난 변경임을 교차 검증으로 뒤늦게 발견 → 즉시 복원
  - docker-compose.ci.yml에 schema-registry 없어 CI 환경에서 Avro 직렬화 연결 대상 부재 상태였음
- **내일 계획**:
  - `fix/kafka-enabled-gate` 커밋 + PR 생성 (Issue #46 close)
  - PR #51(Flyway 표준) 머지 확인
  - W5 E2E(Issue #47) 착수

### W5 (2026-06-08 ~ 06-12)

#### 2026-06-09 (월)

- **완료**:
- **진행 중**:
- **이슈**:
- **다음**:

#### 2026-06-10 (화)

- **완료**:
- **진행 중**:
- **이슈**:
- **다음**:

#### 2026-06-11 (수)

- **완료**:
- **진행 중**:
- **이슈**:
- **다음**:

#### 2026-06-12 (목)

- **완료**:
- **진행 중**:
- **이슈**:
- **다음**:

---

## 변경 이력

| 날짜       | 변경 사항                                         |
| ---------- | ------------------------------------------------- |
| 2026-05-22 | W1 전체 + W2 05-22까지 커밋 기반 작업 로그 작성  |
| 2026-05-22 | 대시보드 Step 번호/내용 TASK 파일 기준으로 정렬  |
| 2026-05-11 | W2/W3/W4 대시보드 및 로그 템플릿 추가             |
| 2026-05-11 | 초기 템플릿 생성                                  |
