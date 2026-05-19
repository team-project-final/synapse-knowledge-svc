# TASK: @knowledge-owner-2

> **담당 서비스**: knowledge-svc
> **GitHub Repository**: [synapse-knowledge-svc](https://github.com/team-project-final/synapse-knowledge-svc)
> **주차**: W1 (2026-05-12 ~ 2026-05-15, 4 영업일)
> **관련 문서**: [SCOPE](../scope/SCOPE_knowledge-2.md) | [PRD_W1](../prd/PRD_W1.md) | [WORKFLOW](../workflow/WORKFLOW_knowledge-2_W1.md) | [HISTORY](../history/HISTORY_knowledge-2.md)

---

## Step 1: Modulith 모듈 구조 설정

| 필드 | 내용 |
|------|------|
| **Step Name** | Modulith 모듈 구조 설정 |
| **Step Goal** | knowledge-owner-2가 knowledge-svc의 note/graph/chunking 모듈에 @ApplicationModule을 설정하고 모듈 간 의존성 규칙을 정의한다. |
| **Done When** | ApplicationModules.verify() 통과 + 모듈 간 직접 import 시 빌드 실패 |
| **Scope** | **In**: Modulith 설정, 모듈 패키지 구조 정의 (note, graph, chunking) / **Out**: 비즈니스 로직 구현 |
| **Input** | Spring Modulith 공식 문서, knowledge-svc 기존 패키지 구조, PRD_W1 모듈 분리 요구사항 |
| **Instructions** | 1. `build.gradle.kts`에 Spring Modulith 의존성 추가<br>2. `note`, `graph`, `chunking` 패키지 생성 및 `package-info.java` 작성<br>3. 각 모듈에 `@ApplicationModule(allowedDependencies=...)` 어노테이션 설정<br>4. 모듈 간 public API용 인터페이스 정의 (internal 패키지 분리)<br>5. `ApplicationModules.verify()` 통합 테스트 작성<br>6. 의존 위반 시 빌드 실패하는지 수동 검증<br>7. 모듈 구조 다이어그램 문서화 |
| **Output Format** | 모듈별 패키지 구조 + verify() 테스트 코드 + 빌드 로그 캡처 |
| **Constraints** | - Spring Modulith 2.0.x 사용<br>- 모듈 간 순환 의존 금지<br>- internal 패키지 외부 접근 시 컴파일 에러 보장<br>- 각 모듈은 독립 테스트 가능해야 함 |
| **Duration** | 1일 |
| **RULE Reference** | [03-아키텍처](../../wiki/03-아키텍처.md) · [18-기술-스택](../../wiki/18-기술-스택.md) |
| **Assignee** | @knowledge-owner-2 |
| **Reviewer** | @team-lead |

---

## Step 2: ArchUnit 모듈 경계 테스트

| 필드 | 내용 |
|------|------|
| **Step Name** | ArchUnit 모듈 경계 테스트 |
| **Step Goal** | knowledge-owner-2가 ArchUnit 테스트로 모듈 경계 위반을 자동 감지하고 CI에서 위반 시 빌드가 실패한다. |
| **Done When** | ArchUnit 테스트 3건 + CI 실행 + 위반 코드 FAIL 확인 |
| **Scope** | **In**: ArchUnit 테스트 규칙 정의, CI 파이프라인 연동 / **Out**: 런타임 모듈 격리, 성능 테스트 |
| **Input** | Step 1 완료된 모듈 구조, ArchUnit 라이브러리 문서, GitHub Actions CI 설정 |
| **Instructions** | 1. `archunit-junit5` 의존성 추가<br>2. 모듈 간 직접 의존 금지 규칙 테스트 작성<br>3. 순환 참조 감지 테스트 작성<br>4. internal 패키지 외부 접근 금지 테스트 작성<br>5. CI workflow에 ArchUnit 테스트 단계 추가<br>6. 의도적 위반 코드 push → FAIL 확인<br>7. 테스트 통과 후 위반 코드 revert |
| **Output Format** | ArchUnit 테스트 파일 3건 + CI 파이프라인 설정 + FAIL/PASS 스크린샷 |
| **Constraints** | - ArchUnit 1.3+ 사용<br>- 테스트 실행 시간 10초 이내<br>- CI에서 ArchUnit 실패 시 전체 빌드 FAIL<br>- 테스트는 `src/test/java` 내 별도 패키지로 관리 |
| **Duration** | 1.5일 |
| **RULE Reference** | [03-아키텍처](../../wiki/03-아키텍처.md) · [09-버전-관리-정책](../../wiki/09-버전-관리-정책.md) |
| **Assignee** | @knowledge-owner-2 |
| **Reviewer** | @team-lead |

---

## Step 3: Avro 스키마 등록 및 호환성 검증

| 필드 | 내용 |
|------|------|
| **Step Name** | Avro 스키마 등록 및 호환성 검증 |
| **Step Goal** | knowledge-owner-2가 `synapse-shared`의 knowledge note Avro 스키마를 Schema Registry에 등록하고 `knowledge.note.note-created-v1-value` subject에 대해 `BACKWARD_TRANSITIVE` 호환성 검증 경로를 제공한다. |
| **Done When** | `synapse-shared/src/main/avro/knowledge/note-created-v1.avsc` 추가 + compatible 샘플 통과 경로 확보 + default 없는 required 필드 추가 샘플 거부 확인 |
| **Scope** | **In**: `synapse-shared` Avro 스키마 정의, Schema Registry 연동 스크립트, 호환성 모드 설정, `knowledge-svc` Step3 문서 동기화 / **Out**: Kafka Producer/Consumer 구현, 이벤트 핸들러, CloudEvents envelope 전면 리팩토링 |
| **Input** | note-created 이벤트 명세 (PRD_W1), Confluent Schema Registry 문서, Avro 스키마 규격, `docs/rules/08-kafka-event.md` |
| **Instructions** | 1. `synapse-shared/src/main/avro/knowledge/note-created-v1.avsc` 스키마 파일 작성 (`noteId`, `tenantId`, `title`, `content`, `createdAt` 필드)<br>2. `docker-compose.schema-registry.yml`로 로컬 Schema Registry 구동 경로 구성<br>3. Gradle Avro 플러그인 기반 코드 생성 및 `testSchemasTask` 검증 task 추가<br>4. Schema Registry subject `knowledge.note.note-created-v1-value` 등록 스크립트 작성<br>5. subject 호환성 모드 `BACKWARD_TRANSITIVE` 설정<br>6. 비호환 변경(default 없는 required 필드 추가) 시 등록 거부 테스트<br>7. 호환 변경(optional 필드 추가) 시 등록 성공 테스트<br>8. 구현 결과에 맞춰 `TASK/WORKFLOW/HISTORY` 문서 동기화 |
| **Output Format** | Avro 스키마 파일 + sample 스키마 2종 + docker-compose 설정 + 등록/호환성 검사 스크립트 + 문서 동기화 결과 |
| **Constraints** | - Schema Registry 호환성 모드: `BACKWARD_TRANSITIVE`<br>- 스키마 subject 네이밍: `knowledge.note.note-created-v1-value`<br>- 모든 신규 필드는 default 포함 또는 optional union 사용<br>- 비호환 검증은 default 없는 required 필드 추가로 재현<br>- 스키마 ID는 Registry 자동 증분 관리 |
| **Duration** | 1.5일 |
| **RULE Reference** | [03-아키텍처](../../wiki/03-아키텍처.md) · [18-기술-스택](../../wiki/18-기술-스택.md) · [14-배포-가이드](../../wiki/14-배포-가이드.md) |
| **Assignee** | @knowledge-owner-2 |
| **Reviewer** | @team-lead |

---

# W2 (2026-05-18 ~ 2026-05-22, 5 영업일)

## Step 4: 임베딩용 청크 분할

| 필드 | 내용 |
|------|------|
| **Step Name** | 임베딩용 청크 분할 |
| **Step Goal** | knowledge-owner-2가 노트를 비동기 이벤트 기반으로 임베딩용 청크로 분할하여 `note_chunks` 테이블에 저장하고, 수정/삭제 시 관련 청크 상태를 정합하게 유지한다. |
| **Done When** | 노트 저장/수정 시 비동기 청크 분할 완료 + 노트 삭제 시 관련 청크 정리 확인 + `note_chunks` 테이블 저장 확인 + 단위/통합 테스트 통과 |
| **Scope** | **In**: `note_chunks` 테이블, Spring Application Event + `@Async` 기반 비동기 분할 로직, 수정 시 재생성, 삭제 시 청크 정리 / **Out**: 벡터 생성(ai-owner 담당), Kafka transport 구현 |
| **Input** | Step 3 완료된 Avro 스키마, 노트 엔티티, 청크 분할 전략 문서, PRD_W2 요구사항 |
| **Instructions** | 1. `note_chunks` 테이블 스키마 설계 (`id`, `note_id`, `chunk_index`, `chunk_text`, `token_count`, `embedding`, `created_at`)<br>   - `embedding vector(1536)`: pgvector 임베딩 컬럼 (learning-ai-owner가 채움)<br>2. Flyway 마이그레이션 스크립트 작성 (`CREATE EXTENSION IF NOT EXISTS vector` 포함)<br>3. `NoteService` 저장/수정/삭제 흐름에서 chunking 요청 이벤트 발행<br>4. Spring Application Event + `@TransactionalEventListener(AFTER_COMMIT)` + `@Async` 기반 비동기 청크 분할 서비스 구현<br>5. 청크 크기 전략 정의 (최대 512 토큰, 오버랩 50 토큰, 공백 기준 token counter 1차 적용)<br>6. 노트 저장/수정 시 기존 청크 삭제 후 재생성, 노트 삭제 시 관련 청크 정리<br>7. 단위 테스트: 빈 노트, 매우 긴 노트, 한국어 포함, overlap 검증<br>8. 통합 테스트: 노트 저장/수정/삭제 → `note_chunks` 테이블 상태 검증 |
| **Output Format** | `note_chunks` 테이블 DDL + chunking 이벤트/서비스 코드 + 단위/통합 테스트 결과 |
| **Constraints** | - 청크 최대 크기: 512 토큰<br>- 오버랩: 50 토큰<br>- 비동기 처리 (노트 저장 응답 지연 금지)<br>- 빈 노트는 청크 생성하지 않음<br>- 현재 단계의 transport는 Spring Application Event + `@Async`, Kafka는 추후 전환<br>- PostgreSQL 사용 (pgvector 확장 필요), 테스트 환경(H2)은 문자열 컬럼으로 대체 매핑 |
| **Duration** | 1.5일 |
| **RULE Reference** | [03-아키텍처](../../wiki/03-아키텍처.md) · [18-기술-스택](../../wiki/18-기술-스택.md) |
| **Assignee** | @knowledge-owner-2 |
| **Reviewer** | @team-lead |
| **Status** | DONE |

---

## Step 5: BM25 기반 Elasticsearch 검색

| 필드 | 내용 |
|------|------|
| **Step Name** | BM25 기반 Elasticsearch 검색 |
| **Step Goal** | 사용자가 키워드로 노트를 BM25 기반 Elasticsearch 검색할 수 있다. |
| **Done When** | 키워드 검색 API + BM25 스코어링 + 페이지네이션 + 테스트 통과 |
| **Scope** | **In**: Elasticsearch 인덱스 설정, BM25 검색 API, 노트 인덱싱 / **Out**: 시맨틱 검색, 하이브리드 검색, 검색 튜닝 |
| **Input** | Step 4 완료된 청크 데이터, Elasticsearch 공식 문서, PRD_W2 검색 요구사항 |
| **Instructions** | 1. Elasticsearch Docker 컨테이너 구성 (docker-compose)<br>2. 노트 인덱스 매핑 정의 (title, content, tags 필드)<br>3. 노트 생성/수정 시 Elasticsearch 인덱싱 연동<br>4. BM25 기반 키워드 검색 API 구현 (`GET /notes/search?q=...` — `/api/v1/` 접두사 제거)<br>5. 검색 결과 하이라이팅 적용<br>6. 페이지네이션 및 정렬 옵션 구현<br>7. 통합 테스트: 키워드 검색 정확도 검증 |
| **Output Format** | Elasticsearch 인덱스 매핑 + 검색 API 응답 예시 + 테스트 결과 |
| **Constraints** | - Elasticsearch 8.x 사용<br>- BM25 기본 파라미터 (k1=1.2, b=0.75)<br>- 검색 결과 최대 100건 페이지네이션<br>- 한국어 형태소 분석기 (nori) 적용<br>- 인덱싱은 비동기 처리 |
| **Duration** | 1.5일 |
| **RULE Reference** | [03-아키텍처](../../wiki/03-아키텍처.md) · [18-기술-스택](../../wiki/18-기술-스택.md) · [14-배포-가이드](../../wiki/14-배포-가이드.md) |
| **Assignee** | @knowledge-owner-2 |
| **Reviewer** | @team-lead |
| **Status** | TODO |

---

# W3 (2026-05-26 ~ 2026-05-29, 5/25 부처님오신날 제외)

## Step 6: 하이브리드 검색 (BM25 + 시맨틱 RRF)

| 필드 | 내용 |
|------|------|
| **Step Name** | 하이브리드 검색 (BM25 + 시맨틱 RRF) |
| **Step Goal** | 사용자가 하이브리드 검색(BM25+시맨틱 RRF)으로 노트를 검색할 수 있다. |
| **Done When** | 하이브리드 검색 API + RRF 점수 병합 + BM25/시맨틱 단독 대비 정확도 향상 확인 |
| **Scope** | **In**: RRF 점수 병합 로직, 시맨틱 검색 연동, 하이브리드 검색 API / **Out**: 검색 UI, 자동완성, 필터링 고도화 |
| **Input** | Step 5 완료된 BM25 검색, learning-ai 시맨틱 검색 API, RRF 알고리즘 문서 |
| **Instructions** | 1. learning-ai 서비스의 시맨틱 검색 API 연동 (HTTP 클라이언트)<br>2. RRF(Reciprocal Rank Fusion) 점수 병합 로직 구현<br>3. 하이브리드 검색 API 구현 → `POST /ai/search/hybrid` (Wiki 기준: GET 아님, `/api/v1/` 접두사 제거, 단일 mode 파라미터 방식 폐기)<br>4. 검색 모드별 별도 엔드포인트: keyword → `GET /notes/search?q=...`, semantic → `POST /ai/search/semantic`, hybrid → `POST /ai/search/hybrid`<br>5. RRF 파라미터(k=60) 설정 및 조정 가능하도록 구현<br>6. 검색 결과 정렬: RRF 점수 내림차순<br>7. 통합 테스트: 하이브리드 검색 결과 품질 검증 |
| **Output Format** | 하이브리드 검색 API 응답 + RRF 병합 로직 코드 + 검색 모드별 비교 결과 |
| **Constraints** | - RRF 기본 k=60<br>- BM25와 시맨틱 검색 병렬 실행 (응답 시간 최소화)<br>- 타임아웃: 시맨틱 검색 3초 초과 시 BM25 결과만 반환<br>- 검색 모드별 별도 엔드포인트 사용 (단일 엔드포인트에 mode 파라미터 방식 사용 금지): keyword=`GET /notes/search?q=...`, semantic=`POST /ai/search/semantic`, hybrid=`POST /ai/search/hybrid` |
| **Duration** | 1.5일 |
| **RULE Reference** | [03-아키텍처](../../wiki/03-아키텍처.md) · [18-기술-스택](../../wiki/18-기술-스택.md) |
| **Assignee** | @knowledge-owner-2 |
| **Reviewer** | @team-lead |
| **Status** | TODO |

---

## Step 7: 검색 정확도 측정 및 리포트

| 필드 | 내용 |
|------|------|
| **Step Name** | 검색 정확도 측정 및 리포트 |
| **Step Goal** | knowledge-owner-2가 테스트 쿼리 세트로 검색 정확도를 측정하고 리포트를 산출한다. |
| **Done When** | 테스트 쿼리 세트 20건 + 검색 모드별 MRR/Precision@10 측정 + 리포트 문서 산출 |
| **Scope** | **In**: 테스트 쿼리 세트 작성, 정확도 메트릭 계산, 리포트 생성 / **Out**: 검색 알고리즘 자체 변경, 프로덕션 배포 |
| **Input** | Step 6 완료된 하이브리드 검색, 검색 품질 평가 방법론, 테스트 데이터셋 |
| **Instructions** | 1. 테스트 쿼리 세트 작성 (20건 이상, 기대 결과 포함)<br>2. 자동화된 검색 정확도 측정 스크립트 작성<br>3. MRR (Mean Reciprocal Rank) 계산<br>4. Precision@10 계산<br>5. BM25 단독 / 시맨틱 단독 / 하이브리드 모드별 비교<br>6. 검색 정확도 리포트 문서 작성<br>7. 정확도 기준 미달 시 개선 포인트 도출 |
| **Output Format** | 테스트 쿼리 세트 + 정확도 메트릭 결과표 + 검색 정확도 리포트 문서 |
| **Constraints** | - 테스트 쿼리 최소 20건<br>- MRR 목표: 0.7 이상<br>- Precision@10 목표: 0.6 이상<br>- 3개 검색 모드 모두 측정<br>- 측정 결과 재현 가능해야 함 |
| **Duration** | 1.5일 |
| **RULE Reference** | [03-아키텍처](../../wiki/03-아키텍처.md) · [09-버전-관리-정책](../../wiki/09-버전-관리-정책.md) |
| **Assignee** | @knowledge-owner-2 |
| **Reviewer** | @team-lead |
| **Status** | TODO |

---

# W4 (2026-06-01 ~ 2026-06-05, 6/3 지방선거 제외 — 검색 튜닝 + E2E)

## Step 8: 하이브리드 검색 E2E 테스트

| 필드 | 내용 |
|------|------|
| **Step Name** | 하이브리드 검색 E2E 테스트 |
| **Step Goal** | 하이브리드 검색 E2E(키워드+시맨틱 RRF)가 통과하고 정확도 리포트가 산출된다. |
| **Done When** | E2E 테스트 시나리오 전체 통과 + 정확도 리포트 최종 산출 + CI 연동 |
| **Scope** | **In**: E2E 테스트 시나리오 작성, CI 연동, 최종 정확도 리포트 / **Out**: 프로덕션 배포, 모니터링 설정 |
| **Input** | Step 7 완료된 정확도 리포트, 하이브리드 검색 API, CI 파이프라인 설정 |
| **Instructions** | 1. E2E 테스트 시나리오 정의 (노트 생성 → 청크 분할 → 인덱싱 → 검색)<br>2. 키워드 검색 E2E 테스트 작성<br>3. 시맨틱 검색 E2E 테스트 작성<br>4. 하이브리드(RRF) 검색 E2E 테스트 작성<br>5. CI 파이프라인에 E2E 테스트 단계 추가<br>6. 최종 정확도 리포트 산출 및 기준 충족 확인<br>7. 실패 시나리오 테스트 (Elasticsearch 다운, 시맨틱 검색 타임아웃) |
| **Output Format** | E2E 테스트 코드 + CI 파이프라인 설정 + 최종 정확도 리포트 |
| **Constraints** | - E2E 테스트 실행 시간 5분 이내<br>- CI에서 E2E 실패 시 빌드 FAIL<br>- 정확도 기준: MRR ≥ 0.7, Precision@10 ≥ 0.6<br>- Testcontainers로 Elasticsearch 구동 |
| **Duration** | 1.5일 |
| **RULE Reference** | [03-아키텍처](../../wiki/03-아키텍처.md) · [09-버전-관리-정책](../../wiki/09-버전-관리-정책.md) · [14-배포-가이드](../../wiki/14-배포-가이드.md) |
| **Assignee** | @knowledge-owner-2 |
| **Reviewer** | @team-lead |
| **Status** | TODO |

---

## Step 9: 검색 튜닝 및 P0 버그 수정

| 필드 | 내용 |
|------|------|
| **Step Name** | 검색 튜닝 및 P0 버그 수정 |
| **Step Goal** | 검색 튜닝이 완료되고 P0 버그가 모두 수정된다. |
| **Done When** | 검색 파라미터 튜닝 완료 + P0 버그 0건 + 회귀 테스트 통과 |
| **Scope** | **In**: 검색 파라미터 튜닝, P0 버그 수정, 회귀 테스트 / **Out**: 프로덕션 배포, 신규 기능 추가 |
| **Input** | Step 8 E2E 테스트 결과, P0 버그 목록, 검색 정확도 리포트 |
| **Instructions** | 1. P0 버그 목록 정리 및 우선순위 배정<br>2. 각 P0 버그 원인 분석 및 수정<br>3. 검색 파라미터 튜닝 (BM25 k1/b, RRF k값 조정)<br>4. 튜닝 전/후 정확도 비교 측정<br>5. 회귀 테스트 전체 실행 및 통과 확인<br>6. 수정 사항 코드 리뷰 및 반영<br>7. 최종 검색 품질 보고서 작성 |
| **Output Format** | P0 버그 수정 내역 + 튜닝 파라미터 변경 이력 + 최종 품질 보고서 |
| **Constraints** | - P0 버그 0건 달성 필수<br>- 튜닝으로 인한 기존 테스트 회귀 금지<br>- 파라미터 변경 시 변경 사유 문서화<br>- 코드 프리즈 전 완료 |
| **Duration** | 1.5일 |
| **RULE Reference** | [03-아키텍처](../../wiki/03-아키텍처.md) · [09-버전-관리-정책](../../wiki/09-버전-관리-정책.md) |
| **Assignee** | @knowledge-owner-2 |
| **Reviewer** | @team-lead |
| **Status** | TODO |
