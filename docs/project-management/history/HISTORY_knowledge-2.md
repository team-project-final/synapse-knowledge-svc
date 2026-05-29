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

| Step   | 내용               | 상태        | 시작일     | 완료일     | 비고                                                                 |
| ------ | ------------------ | ----------- | ---------- | ---------- | -------------------------------------------------------------------- |
| Step 4 | chunking 전략 구현 | Done        | 2026-05-19 | 2026-05-19 | Spring event + @Async 기반 비동기 청크 분할, 수정/삭제 정리까지 구현 |
| Step 5 | BM25 검색 엔진     | Done        | 2026-05-20 | 2026-05-20 | JWT 검증 골격 + BM25 검색 API/비동기 인덱싱 + live ES nori 통합 검증 완료 |

**W2 진행률**: 2/2 Steps 완료

### W3 (2026-05-26 ~ 05-29)

| Step   | 내용                   | 상태        | 시작일 | 완료일 | 비고 |
| ------ | ---------------------- | ----------- | ------ | ------ | ---- |
| Step 6 | 하이브리드 검색        | Done | 2026-05-26 | 2026-05-29 | semantic contract 정렬 + `note_identity_map` UUID 매핑으로 hybrid RRF 병합 복구 |
| Step 7 | 정확도 측정 파이프라인 | Not Started | —      | —      |      |

**W3 진행률**: 1/2 Steps 완료

### W4 (2026-06-01 ~ 06-05)

| Step    | 내용            | 상태        | 시작일 | 완료일 | 비고 |
| ------- | --------------- | ----------- | ------ | ------ | ---- |
| Step 10 | 검색 E2E 테스트 | Not Started | —      | —      |      |
| Step 11 | 검색 튜닝       | Not Started | —      | —      |      |
| Step 12 | 안정화          | Not Started | —      | —      |      |

**W4 진행률**: 0/3 Steps 완료

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
  - test(search): `HybridSearchServiceTest`, `RrfMergeServiceTest`, `SearchServiceTest`, `NoteIntegrationTest`, `ChunkingIntegrationTest`, `KnowledgeSvcApplicationTests` 재검증 통과
- **이슈**:
  - 전체 `./gradlew.bat test`는 기존 `NeighborGraphIntegrationTest`의 Docker 환경 탐지 실패로 1건 실패
- **다음**:
  - W3 Step 7 검색 정확도 측정 및 리포트 착수

### W4 (2026-06-01 ~ 06-05)

#### 2026-06-01 (월)

- **완료**:
- **진행 중**:
- **이슈**:
- **다음**:

#### 2026-06-02 (화)

- **완료**:
- **진행 중**:
- **이슈**:
- **다음**:

#### 2026-06-03 (수)

- **완료**:
- **진행 중**:
- **이슈**:
- **다음**:

#### 2026-06-04 (목)

- **완료**:
- **진행 중**:
- **이슈**:
- **다음**:

#### 2026-06-05 (금)

- **완료**:
- **진행 중**:
- **이슈**:
- **주간 요약**:

---

## 변경 이력

| 날짜       | 변경 사항                             |
| ---------- | ------------------------------------- |
| 2026-05-11 | W2/W3/W4 대시보드 및 로그 템플릿 추가 |
| 2026-05-11 | 초기 템플릿 생성                      |
