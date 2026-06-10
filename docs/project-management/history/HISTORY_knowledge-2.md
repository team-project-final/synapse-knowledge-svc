# Work History: @knowledge-2

> **담당**: Spring Modulith / 아키텍처 검증  
> **관련 문서**: [SCOPE](../scope/SCOPE_knowledge-2.md) | [TASK](../task/TASK_knowledge-2.md) | [WORKFLOW](../workflow/WORKFLOW_knowledge-2_W1.md)

---

## 진행 상태 대시보드

### W1 (2026-05-12 ~ 05-15)

| Step   | 내용                      | 상태 | 시작일     | 완료일     | 비고                                                                     |
| ------ | ------------------------- | ---- | ---------- | ---------- | ------------------------------------------------------------------------ |
| Step 1 | Spring Modulith 모듈 정의 | Done | 2026-05-15 | 2026-05-15 | Modulith verify + internal 경계 반영                                     |
| Step 2 | ArchUnit 경계 검증        | Done | 2026-05-15 | 2026-05-15 | 경계 테스트 3건 + CI 단계 + FAIL 재현                                    |
| Step 3 | Schema Registry 연동 검증 | Done | 2026-05-19 | 2026-05-19 | Runtime 등록/호환성 검증과 `testSchemasTask` live Registry 실행까지 완료 |

**W1 진행률**: 3/3 Steps 완료

### W2 (2026-05-18 ~ 05-22)

| Step   | 내용               | 상태 | 시작일     | 완료일     | 비고                                                                      |
| ------ | ------------------ | ---- | ---------- | ---------- | ------------------------------------------------------------------------- |
| Step 4 | chunking 전략 구현 | Done | 2026-05-19 | 2026-05-19 | Spring event + @Async 기반 비동기 청크 분할, 수정/삭제 정리까지 구현      |
| Step 5 | BM25 검색 엔진     | Done | 2026-05-20 | 2026-05-20 | JWT 검증 골격 + BM25 검색 API/비동기 인덱싱 + live ES nori 통합 검증 완료 |

**W2 진행률**: 2/2 Steps 완료

### W3 (2026-05-26 ~ 05-29)

| Step   | 내용                   | 상태 | 시작일     | 완료일     | 비고                                                                                |
| ------ | ---------------------- | ---- | ---------- | ---------- | ----------------------------------------------------------------------------------- |
| Step 6 | 하이브리드 검색        | Done | 2026-05-26 | 2026-05-29 | semantic contract 정렬 + `note_identity_map` UUID 매핑으로 hybrid RRF 병합 복구     |
| Step 7 | 정확도 측정 파이프라인 | Done | 2026-06-01 | 2026-06-01 | benchmark 노트/쿼리 세트 + 관리자 API + Precision@10/Recall@10/MRR/NDCG 리포트 구현 |

**W3 진행률**: 1/2 Steps 완료

### W4 (2026-06-01 ~ 06-05)

| Step   | 내용            | 상태 | 시작일     | 완료일     | 비고                                                                                                   |
| ------ | --------------- | ---- | ---------- | ---------- | ------------------------------------------------------------------------------------------------------ |
| Step 8 | 검색 E2E 테스트 | Done | 2026-06-05 | 2026-06-05 | Kafka consumer group 테스트 격리, semantic timeout/Elasticsearch down 실패 경로, CI 검색 E2E 단계 반영 |
| Step 9 | 검색 튜닝       | Done | 2026-06-08 | 2026-06-08 | semantic duplicate hit dedupe, tuned BM25/RRF 파라미터, 전체 테스트 및 coverage gate 재검증 완료       |

**W4 진행률**: 2/2 Steps 완료

---

## 작업 로그

### W1 (2026-05-12 ~ 05-15)

#### 2026-05-12 (화)

- **완료**:
- **진행 중**:
- **이슈**:
- **다음**:

#### 2026-05-13 (수)

- **완료**:
- **진행 중**:
- **이슈**:
- **다음**:

#### 2026-05-14 (목)

- **완료**:
- **진행 중**:
- **이슈**:
- **다음**:

#### 2026-05-15 (금)

- **완료**:
  - feat(modulith): note/graph/chunking 공개 API와 internal bootstrap 구조 분리
  - test(modulith): `ApplicationModules.verify()` 및 Modulith 문서 생성 테스트 보강
  - docs(workflow): Step 1 모듈 구조 문서와 Workflow/Task 상태 동기화
  - test(archunit): 모듈 직접 의존 금지, 순환 참조 금지, internal 외부 접근 금지 규칙 3건 추가
  - chore(ci): GitHub Actions CI에 ArchUnit 테스트 단계 추가
  - verify(archunit): 의도적 위반 코드로 FAIL 재현 후 제거, 전체 테스트 재통과 확인
- **진행 중**:
  - 없음
- **이슈**:
  - 없음
- **다음**:
  - W1 Step 3 Avro 스키마 등록 및 호환성 검증 착수

### W2 (2026-05-18 ~ 05-22)

#### 2026-05-18 (월)

- **완료**:
  - chore(build): Spring Modulith BOM을 `2.0.6`으로 상향해 Boot 4 기준과 정렬
  - docs(task): knowledge-2 Task 관련 문서 링크와 Step 1 Modulith 버전 제약을 실제 기준에 맞게 수정
  - verify(modulith): `ModuleStructureTest`, `ModuleBoundaryArchTest`, `./gradlew.bat build --no-daemon` 통과 확인
- **진행 중**:
  - 없음
- **이슈**:
  - 없음
- **다음**:
  - W1 Step 3 Avro 스키마 등록 및 호환성 검증 착수 여부 정리

#### 2026-05-19 (화)

- **완료**:
  - feat(schema): `synapse-shared`에 `note-created-v1.avsc`, compatible/incompatible 샘플 스키마, Schema Registry 스크립트, 로컬 compose 파일 추가
  - chore(build): `synapse-shared`에 `testSchemasTask`를 추가해 Avro 코드 생성과 Registry 호환성 검사 경로를 연결
  - docs(step3): Step3 Task/Workflow를 `BACKWARD_TRANSITIVE`, 실제 subject 명, 런타임 검증 대기 상태에 맞게 동기화
  - verify(schema): Docker daemon 기동 후 실제 Registry 컨테이너 실행, 스키마 등록, 호환/비호환 샘플 검증, `testSchemasTask` live Registry 실행까지 완료
  - fix(schema): PowerShell 스크립트 payload 직렬화와 `testSchemasTask` 응답 스트림 재사용 버그 수정
  - feat(chunking): `note_chunks` 엔티티/리포지토리/Flyway 마이그레이션과 Spring event + `@Async` 기반 비동기 청크 분할 경로 구현
  - feat(chunking): 노트 저장/수정 시 청크 재생성, 노트 soft delete 시 관련 청크 비동기 정리, 공백 기준 token counter + overlap 정책 적용
  - test(chunking): 분할 로직 단위 테스트 5건, 저장/수정/삭제 비동기 청크 통합 테스트 3건 추가 후 `./gradlew.bat test` 통과
  - docs(step4): Step4 Task/Workflow/HISTORY를 Spring event 우선 구현 결정과 실제 완료 상태에 맞게 동기화
- **진행 중**:
  - 없음
- **이슈**:
  - Kafka transport는 아직 미구현이라 현재 Step 4는 Spring Application Event + `@Async`를 transport로 사용
  - Step 4 Workflow의 `노트 삭제 시 관련 청크 cascade 삭제 확인`, `ChunkCreateEvent 정의` 체크 문구가 실제 구현(`soft delete + 비동기 deleteByNoteId`, `NoteChunkingRequested`)과 아직 완전히 일치하지 않음
  - `application-test.yml`에서 Flyway가 비활성화되어 있어 Step 4 테스트 통과는 H2 + JPA 스키마 생성 경로 기준이며, `V3__init_note_chunks_table.sql`과 pgvector 적용은 별도 Postgres 검증이 추가로 필요함
- **다음**:
  - W2 Step 5 BM25 기반 Elasticsearch 검색 착수

#### 2026-05-20 (수)

- **완료**:
  - feat(search): `search` Modulith 모듈, BM25 multi_match 검색, `search_after` cursor, highlight 파싱, 비동기 ES 인덱싱 리스너 구현
  - feat(auth): `/api/v1/notes/**` JWT Resource Server 검증 골격과 `CurrentUser` userId 해석 경로 추가, 기존 note API의 `mockUserId` 제거
  - feat(note): 노트 태그 저장을 위한 `note_tags` 테이블과 DTO/엔티티 반영, 검색 인덱스에 tags 필드 포함
  - test(search): cursor/service/controller 테스트와 401/403 MockMvc 검증, Testcontainers 기반 live Elasticsearch+nori 통합 테스트 추가 후 `./gradlew.bat test` 통과
  - docs(step5): Step 5 Workflow/Task/HISTORY를 완료 상태와 live ES 검증 결과까지 포함해 동기화
- **진행 중**:
  - 없음
- **이슈**:
  - Boot 4 테스트 환경에서 컨트롤러 검증은 기존 `@WebMvcTest` 대신 `spring-boot-starter-webmvc-test` 기반 MockMvc 조합으로 맞춤
  - Swagger/OpenAPI는 현재 프로젝트에 미구성 상태라 확인 항목만 반영했고, 문서 자동화가 필요하면 별도 범위로 다루는 편이 맞음
- **다음**:
  - W3 Step 6 하이브리드 검색(RRF) 착수

#### 2026-05-21 (목)

- **완료**:
- **진행 중**:
- **이슈**:
- **다음**:

#### 2026-05-22 (금)

- **완료**:
  - refactor(structure): template `skeleton/knowledge/w1` README 기준으로 knowledge 도메인 패키지 골격 정렬
    - `note`, `graph`, `chunking`, `search`의 혼합 패키지 구조를 `controller/service/repository/entity/dto` 기준으로 재배치
    - `chunking`, `search`의 보조 컴포넌트는 `service/listener`, `service/support`로 분리해 역할에 맞게 정리
    - `chunking`의 빈 `ChunkingController`를 제거하고, 관련 테스트 패키지와 import를 함께 동기화
  - refactor(global): template `skeleton/knowledge/w2` README 기준으로 `global/` 횡단 골격 추가
    - `config`와 `shared`에 섞여 있던 보안, MVC resolver, 예외 처리, sanitizer를 `global/config`, `global/security`, `global/exception`, `global/util`로 재배치
    - `shared`는 `BaseEntity`, 이벤트 계약, graph query port 같은 모듈 간 공통 계약 위주로 축소
    - `application.properties`를 `application.yml` + `application-{local,dev,prod}.yml` 골격으로 전환
  - refactor(api): `note`, `graph`, `search` HTTP 응답을 `global/response/ApiResponse<T>` 구조로 정렬
  - verify(modulith): `global` 모듈을 OPEN으로 선언하고 도메인 `allowedDependencies`를 조정해 `ApplicationModules.verify()` 경계 검증 통과
  - verify(test): `./gradlew.bat test` 통과
- **진행 중**:
  - 없음
- **이슈**:
  - `checkstyleMain`, `spotbugsMain` task가 현재 Gradle 설정에 없어 정적 분석 검증은 실행 불가
- **주간 요약**:
  - 기존 기능은 유지한 채 template W1 README가 설명하는 도메인별 골격으로 패키지 구조를 정렬했다. 특히 `chunking`, `search`의 보조 컴포넌트는 `service` 하위 `listener/support`로 분리해 템플릿 뼈대와 역할 의미를 함께 맞췄다.
  - 이어서 template W2 README 기준의 `global/` 횡단 골격과 환경별 설정 파일 뼈대를 추가해, 템플릿과 완전히 동일한 구현은 아니더라도 공통 파일을 `global`로 관리하는 구조는 맞췄다.

### W3 (2026-05-26 ~ 05-29)

#### 2026-05-26 (화)

- **완료**:
  - docs(workflow): `WORKFLOW_knowledge-2_W3.md`의 Step 6/7 세부 체크 번호를 `1.x`에서 `6.x`/`7.x`로 정렬
  - feat(search): `GET /api/v1/notes/search` BM25 경로를 유지한 채 `POST /api/v1/ai/search/semantic`, `POST /api/v1/ai/search/hybrid`와 learning-ai semantic 프록시를 추가
  - feat(search): BM25 후보 조회 + semantic 후보 조회를 병렬 실행하고 RRF(k=60)로 병합하는 `HybridSearchService`와 fallback 경로를 구현
  - test(search): semantic/hybrid MockMvc, RRF 병합 단위 테스트, fallback 테스트, ES 기반 hybrid 통합 테스트를 추가하고 `./gradlew.bat test` 통과
  - docs(step6): Step 6 Task/Workflow/HISTORY를 `/api/v1` 규칙, Swagger 미구성 상태, 실제 하이브리드 검색 완료 상태에 맞게 동기화
- **진행 중**:
  - 없음
- **이슈**:
  - Step 6 Task 초안은 `/api/v1` 접두사 제거를 전제로 했지만, `docs/rules/02-function.md` 기준에 맞춰 `/api/v1` 엔드포인트로 정리함
- **다음**:
  - W3 Step 7 검색 정확도 측정 및 리포트 착수

#### 2026-05-27 (수)

- **완료**:
- **진행 중**:
- **이슈**:
- **다음**:

#### 2026-05-28 (목)

- **완료**:
- **진행 중**:
- **이슈**:
- **다음**:

#### 2026-05-29 (금)

- **완료**:
  - fix(search): `search.ai.*`, `search.hybrid.*` 설정을 `application.yml`, `application-dev.yml`, `application-prod.yml`, `application-test.yml`에 추가하고 `SearchProperties` validation을 보강
  - fix(search): learning-ai semantic 프록시를 실제 contract에 맞게 정렬
    - path: `/ai/search/semantic`
    - request: `query`, `top_k`, `threshold`
    - response: `chunk_id`, `note_id`, `content`, `score`
  - refactor(search): semantic/hybrid 호출에 `SearchIdentity`를 도입해 BM25용 `userId`와 semantic용 actor 식별자를 분리
  - feat(search): `note_identity_map` 테이블과 조회 포트를 추가해 learning-ai UUID `note_id`를 knowledge `Long noteId`로 연결하고 hybrid semantic 결과를 실제 RRF 후보로 병합
  - feat(search): 검색 인덱스 이벤트/ES 문서/BM25 후보 모델에 `externalNoteId`를 반영해 BM25와 semantic 결과를 공통 UUID 키로 병합
  - fix(search): Elasticsearch 9.x(Lucene 9/10) Nori 분석기 POS 태그 호환성 수정
    - 삭제된 `E`, `J` 태그를 `EP, EF, EC, ETN, ETM` 및 `JKS, JKC, JKG, JKO, JKB, JKV, JKQ, JX, JC`로 교체
  - test(search): `SearchElasticsearchIntegrationTest` 포함 전체 테스트 재검증 통과
- **이슈**:
  - 전체 `./gradlew.bat test`는 기존 `NeighborGraphIntegrationTest`의 Docker 환경 탐지 실패로 1건 실패
- **다음**:
  - W3 Step 7 검색 정확도 측정 및 리포트 착수

### W4 (2026-06-01 ~ 06-05)

#### 2026-06-01 (월)

- **완료**:
- feat(search): benchmark 노트/테스트 쿼리 JSON, 정확도 계산기, `SearchAccuracyService`, 관리자 전용 정확도 실행/조회 API를 추가
- feat(note): search 모듈의 benchmark 시드를 위해 `shared.NoteCommandPort`와 note 어댑터를 추가해 모듈 경계를 유지한 채 노트 upsert 경로를 제공
- feat(security): `/api/v1/admin/search/**` 관리자 권한 체크와 JWT `roles` -> `ROLE_*` 변환을 추가
- test(search): 정확도 계산기 단위 테스트, admin controller 보안 테스트, ES 기반 benchmark accuracy 통합 테스트를 추가
- docs(step7): Task/Workflow/HISTORY를 Step 7 완료 상태와 `/api/v1/admin/search/*` 경로 기준으로 동기화
- feat(kafka): `synapse-shared` Avro 원본 기준 `NoteCreated` / `NoteUpdated` producer 스키마와 `spring-kafka` 발행 설정을 knowledge-svc에 추가
- feat(note): `note/kafka/producer` 패키지와 `@TransactionalEventListener(AFTER_COMMIT)` 기반 Kafka publisher를 도입하고 노트 생성/수정 성공 후 발행 요청 이벤트를 연결
- test(kafka): note producer payload 구성/발행 요청 경로를 검증하는 단위 테스트를 추가
- feat(kafka): note 생성/수정 Kafka 발행 경로를 outbox 기반으로 전환하고 `eventId` 중복 적재 방지 제약, scheduler dispatcher, 발행 실패 재시도 메타데이터를 추가
- feat(note): controller가 JWT `subject`를 이벤트 계약용 `userId`로 넘기도록 조정하고, note service는 소유권 검사용 `Long userId`와 이벤트용 식별자를 분리
- test(kafka): outbox enqueue/dispatch 단위 테스트와 producer UUID `userId` 검증을 추가
- **진행 중**:
- 없음
- **이슈**:
- live semantic 품질 지표는 `learning-ai` 응답 품질에 좌우되므로, 자동 테스트는 empty semantic mock 기준으로 재현성을 확보
- 전체 `./gradlew.bat test`는 기존 `NeighborGraphIntegrationTest`의 Docker 환경 미감지로 1건 실패
- `deckId`는 shared 스키마에서 nullable이지만 현재 knowledge-svc 노트 API 입력에는 값이 없어 producer는 `null`로 발행하며, learning-ai 쪽 nullable 처리 정렬이 후속으로 필요할 수 있음
- JWT `subject`가 UUID 계약이라는 가정으로 event `userId`를 매핑했으므로, auth 서비스 claim 규약이 다르면 consumer/issuer 기준 재정렬이 필요함
- **다음**:
- 로컬 Kafka/Schema Registry에서 `note-created-v1`, `note-updated-v1` actual publish 검증 및 shared `kafka-e2e-test.sh`/learning-ai 교차 확인

#### 2026-06-02 (화)

- **완료**:
- fix(kafka): note event outbox dispatcher를 `PENDING -> IN_PROGRESS -> PUBLISHED` claim lease 흐름으로 보강해 멀티 인스턴스 중복 발행 위험을 줄임
- feat(kafka): outbox `claimed_by`, `claim_expires_at` 컬럼과 claim service를 추가하고 만료된 `IN_PROGRESS` 이벤트 재claim 경로를 반영
- fix(security): `CurrentUserArgumentResolver`가 숫자형 `userId` claim만 소유권 검증에 사용하고, UUID `subject`는 이벤트용 식별자로만 유지하도록 JWT 해석 경로를 정리
- test(kafka): outbox claim/dispatch 상태 전이 테스트와 `CurrentUserArgumentResolverTest`를 추가하고 변경분 관련 Gradle 테스트 통과
- verify(kafka): 로컬 PostgreSQL + Kafka + Schema Registry + `knowledge-svc` 실제 기동 후 `POST /api/v1/notes`, `PATCH /api/v1/notes/{id}` 호출로 `knowledge.note.note-created-v1`, `knowledge.note.note-updated-v1` Avro 메시지 수신과 `note_event_outbox`의 created/updated `PUBLISHED` 상태를 직접 확인
- verify(schema): Ubuntu WSL2에 `jq`를 설치하고 `synapse-shared`의 `kafka-e2e-test.sh --avro`를 런타임 CRLF 정규화로 실행해 `PASS 8 / FAIL 0 / RESULT: PASSED` 확인
- docs(history): Kafka 실발행 검증 결과와 shared Avro 스크립트 통과 결과를 Notion 테스트 기록 페이지 및 HISTORY에 반영
- **진행 중**:
- 없음
- **이슈**:
- Windows PowerShell 환경에서는 `kafka-e2e-test.sh`가 CRLF 줄바꿈과 `jq` 부재 때문에 바로 실행되지 않았고, WSL2 + `jq` 설치 + 런타임 줄바꿈 정규화가 필요했음
- 전체 `./gradlew.bat test`는 이번에도 수행하지 않았고, 기존 `NeighborGraphIntegrationTest`의 Docker 환경 문제는 여전히 별도 이슈로 남아 있음
- **다음**:
- learning-ai 실제 소비 교차 확인 범위와 `deckId`/조회 API/인증 계약 정렬 필요 여부를 후속으로 협의

#### 2026-06-03 (수)

- **완료**:
- **진행 중**:
- **이슈**:
- **다음**:

#### 2026-06-04 (목)

- **완료**:
- fix(kafka): `spring.kafka.security.protocol` 설정을 `global` producer와 `search` producer/consumer factory props에 조건부 반영해 MSK TLS-only(9094) 연결 경로를 추가
- test(kafka): `KafkaConfig` 단위 테스트 5건을 추가해 SSL 주입 시 `security.protocol=SSL`, 기본값 PLAINTEXT 시 미주입 동작을 검증
- config(kafka): `application.yml`에 `spring.kafka.security.protocol` 환경변수 바인딩을 명시
- **진행 중**:
- 없음
- **이슈**:
- 없음
- **다음**:
- 최신 main 기준 브랜치에서 PR 생성 및 후속 배포 검증 경로 정리

#### 2026-06-05 (금)

- **완료**:
- test(search): `SearchElasticsearchIntegrationTest`의 `@Disabled`를 제거하고 BM25/nori, 태그 필터, hybrid RRF, semantic timeout fallback, Elasticsearch down 실패 경로를 포함한 Step 8 E2E 시나리오를 복구
- fix(search): `NoteSearchKafkaConsumer`의 고정 `groupId`를 제거해 테스트/환경별 consumer group 설정을 `spring.kafka.consumer.group-id`로 주입 가능하게 정리
- chore(ci): GitHub Actions `ci-java.yml`에 `SearchElasticsearchIntegrationTest` 전용 검색 E2E 단계를 추가해 CI에서 실패 시 빌드가 깨지도록 반영
- fix(ci): `build.gradle.kts`에 `searchE2eTest` 전용 Gradle task를 분리하고 기본 `test`에서는 `SearchElasticsearchIntegrationTest`를 제외해 `clean build`와 검색 E2E 단계의 중복 실행으로 인한 CI 플래키 실패를 제거
- chore(coverage): `build.gradle.kts`에 JaCoCo report/verification을 추가하고 `search` 실행 로직 기준 line coverage 80% gate 및 CI coverage 단계 연동
- fix(search): `searchSyncConsumerFactory`의 `auto.offset.reset` 하드코딩을 설정값 주입으로 바꾸고, `SearchElasticsearchIntegrationTest`는 `latest` 오프셋으로 시작하게 조정해 `clean build`가 남긴 Kafka backlog를 재소비하지 않도록 수정
- test(search): `KafkaConfigTest`에 `auto.offset.reset` 설정 배선 회귀 테스트를 추가하고, CI와 동일하게 `docker compose -f docker-compose.ci.yml up -d --wait` 후 `./gradlew.bat clean build --no-daemon`, `./gradlew.bat searchE2eTest --no-daemon` 재현 순서까지 통과 확인
- fix(search): `NoteSearchKafkaConsumer`에 listener id를 부여하고 `SearchElasticsearchIntegrationTest`가 Kafka listener running + partition assignment 완료를 기다린 뒤 note를 생성하게 바꿔, 느린 CI에서 `latest` consumer가 첫 search sync event를 놓치는 startup race를 제거
- verify(search): 같은 compose 환경에서 `clean build -> searchE2eTest` 순서를 다시 실행해 통과 확인
- docs(step8): W4 Workflow/Task/HISTORY를 Step 8 완료 상태와 실패 항목 기록 기준으로 동기화
- **진행 중**:
- 없음
- **이슈**:
- coverage gate는 DTO/entity/internal bootstrap 및 live external infra 의존성이 큰 search client/repository/seeder 계층을 제외한 search 실행 로직 범위 기준으로 설정했음
- `SearchElasticsearchIntegrationTest`는 Kafka topic을 비우지 않고 새 consumer group으로만 격리하면 backlog를 재생할 수 있으므로, E2E에서는 `spring.kafka.consumer.auto-offset-reset=latest`가 필요함
- `latest`만으로는 충분하지 않았고, listener assignment 전에 첫 이벤트가 발행되면 CI에서 인덱싱이 0건이 될 수 있어 listener readiness wait가 함께 필요했음
- **주간 요약**:

### W5 (2026-06-08 ~ 06-12)

#### 2026-06-08 (월)

- **완료**:
  - fix(kafka): `synapse.kafka.enabled` 게이트를 `application.yml`, `global/search KafkaConfig`, note/search producer·consumer·dispatcher에 반영해 `KAFKA_ENABLED`가 실제로 Kafka 빈 활성 여부를 제어하도록 수정
  - fix(kafka): Kafka 비활성화 시 `NoteEventOutboxService`가 outbox row를 적재하지 않고 no-op 처리하도록 보강
  - test(kafka): Kafka gate 비활성 시 bean 미등록 검증과 outbox no-op 테스트를 추가하고 관련 테스트 4종을 통과 확인
  - fix(search): `SearchElasticsearchIntegrationTest`가 `synapse.kafka.enabled=true`를 자체 주입하도록 바꾸고, `KafkaConfigTest`에 enabled 경로 bean 등록 회귀 테스트를 추가해 CI 환경 변수 상태와 무관하게 검색 E2E가 동일하게 동작하도록 고정
  - verify(ci): `docker compose -f docker-compose.ci.yml up -d --wait` 후 `./gradlew.bat clean build --no-daemon`, `./gradlew.bat searchE2eTest --no-daemon`, `./gradlew.bat jacocoTestCoverageVerification jacocoTestReport --no-daemon`, `./gradlew.bat test --tests '*ModuleStructureTest' --no-daemon`를 순서대로 재현해 전체 CI 빌드 경로 통과 확인
  - fix(search): `HybridSearchService`에서 같은 note의 duplicate semantic hit를 note 단위로 dedupe하고 최고 `semanticScore`만 반영하도록 수정
  - fix(search): `RrfMergeService`에서 같은 source의 동일 note가 RRF 점수에 중복 가산되지 않도록 방어 로직 추가
  - tune(search): `search.ai.threshold`, `search.hybrid.rrf-k`, `candidate-multiplier`, BM25 field boost와 tuned BM25 similarity(`k1=1.4`, `b=0.65`)를 설정 기반으로 조정
  - test(search): duplicate semantic hit unit/integration 시나리오를 추가하고 `./gradlew.bat test`, `./gradlew.bat jacocoTestCoverageVerification` 통과 확인
  - docs(step9): `REPORT_knowledge-2_step9.md`, W4 workflow, HISTORY를 Step 9 완료 상태로 동기화
- **진행 중**:
  - 없음
- **이슈**:
  - 없음
- **다음**:
  - 필요 시 tuned search 파라미터를 staging benchmark 결과와 비교해 추가 보정 여부 점검

#### 2026-06-09 (화)

- **완료**:
  - docs(workflow): `WORKFLOW_knowledge-2_W5.md`를 실제 완료 상태 기준으로 동기화하고 발표용 검색 데모 쿼리 항목을 반영
  - docs(report): `REPORT_knowledge-2_W5_demo-search.md`를 추가해 발표용 검색 데모 쿼리, 기대 결과, 시연 전제를 고정
  - test(chunking): `ChunkingPostgresFlywayIntegrationTest`와 `chunkingPgTest` task를 추가해 W2 Step 4의 Postgres/Flyway/pgvector 실제 검증 경로를 보강하고 `./gradlew.bat chunkingPgTest --no-daemon` 통과를 확인
  - docs(report): `REPORT_knowledge-2_step7.md`에 dataset 규모, Task 완료 기준 매핑, 메트릭 결과표 형식, 관리자 API 응답 예시를 보강해 Step 7 산출물 기준을 명확히 정리
  - docs(task): Step 9 상태를 실제 완료 이력과 Step 9 보고서 기준으로 동기화
  - fix(test): `SearchElasticsearchIntegrationTest`를 Testcontainers Elasticsearch 부팅 대신 `docker-compose.ci.yml` 외부 Elasticsearch를 사용하도록 전환하고, Elasticsearch 플러그인 설정을 compose 마운트로 옮겨 Windows 로컬과 CI에서 같은 경로로 `searchE2eTest`를 실행 가능하게 정리
  - verify(search): `docker compose -f docker-compose.ci.yml up -d --wait` 후 `./gradlew.bat searchE2eTest --no-daemon --rerun-tasks`를 재실행해 7 tests / 0 skipped / 0 failures를 확인
- **진행 중**:
  - 없음
- **이슈**:
  - semantic/hybrid 발표 시연 안정성은 benchmark seed 적재와 learning-ai semantic proxy contract 유지가 전제다
- **다음**:
  - 필요 시 발표용 검색 데모 쿼리를 staging 환경에서 한 번 더 리허설

#### 2026-06-10 (수)

- **완료**:
  - fix(test): `ChunkingPostgresFlywayIntegrationTest`를 Testcontainers 대신 `docker-compose.ci.yml` Postgres 경로로 전환해 Windows/WSL에서 더 이상 skip 없이 실행되도록 정리
  - fix(chunking): `NoteChunk`의 `chunkText`를 실제 PostgreSQL `TEXT` 컬럼에 맞게 매핑하고 `embedding vector(1536)` 컬럼은 insert/update 대상에서 제외해 pgvector 저장 경로 충돌을 제거
  - verify(chunking): `docker compose -f docker-compose.ci.yml up -d --wait postgres`, `./gradlew.bat chunkingPgTest --no-daemon --rerun-tasks`에서 `2 tests / 0 skipped / 0 failures` 확인
  - verify(build): `docker compose -f docker-compose.ci.yml up -d --wait redis`, `./gradlew.bat clean build --no-daemon` 재실행까지 통과 확인
  - fix(openapi): `springdoc-openapi-starter-webmvc-ui`를 `3.0.3`으로 정렬하고 `CurrentUser` ignore + `Pageable @ParameterObject` 설정을 추가해 `/v3/api-docs` 문서 생성 경로를 보강
  - fix(openapi): `io.confluent:kafka-avro-serializer:7.5.0` 전이 의존성에서 비-jakarta `io.swagger.core.v3:swagger-annotations:2.1.10`을 제외해 `Schema.types()` `NoSuchMethodError` 충돌을 제거
  - test(openapi): `OpenApiDocumentationIntegrationTest`를 추가해 `/v3/api-docs` 200과 Swagger UI 200 회귀 검증 경로를 고정
  - verify(openapi): `./gradlew.bat test --tests "*OpenApiDocumentationIntegrationTest"` 통과와 `dependencyInsight --dependency swagger-annotations --configuration runtimeClasspath`에서 `swagger-annotations-jakarta:2.2.47`만 남는 것 확인
  - fix(kafka): search sync listener의 `auto-startup`을 전역 `spring.kafka.listener.auto-startup`에서 분리하고 `synapse.kafka.search-sync-listener.auto-startup` 전용 키로 제어하도록 수정
  - fix(kafka): `NoteSearchKafkaConsumer`에 명시적 `groupId`와 `idIsGroup=false`를 지정해 런타임 consumer group이 리스너 ID가 아닌 `spring.kafka.consumer.group-id`(`knowledge-search-indexer`)를 따르도록 수정
  - test(kafka): search listener 전용 `auto-startup` 회귀 테스트를 추가해 전역 listener 설정과 독립적으로 동작함을 검증
  - test(search): `SearchElasticsearchIntegrationTest`가 listener readiness와 함께 runtime `groupId`까지 검증하도록 보강
  - fix(observability): `GlobalExceptionHandler.handleException`가 C500 응답 전에 `log.error`로 path와 stack trace를 남기도록 보강
  - test(exception): `GlobalExceptionHandlerTest`를 추가해 500 응답과 error 로그 회귀를 고정
- **이슈**:
- 없음
- **다음**:
- PR 본문 정리 후 `fix/KNOW-openapi-docs-500` 브랜치 리뷰 요청

---

## 변경 이력

| 날짜       | 변경 사항                             |
| ---------- | ------------------------------------- |
| 2026-05-11 | W2/W3/W4 대시보드 및 로그 템플릿 추가 |
| 2026-05-11 | 초기 템플릿 생성                      |
