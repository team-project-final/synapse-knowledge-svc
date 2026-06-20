# synapse-knowledge-svc

Synapse knowledge service는 노트, 태그, 지식 그래프, 청킹, 검색 동기화를 담당하는 Spring Boot 4 서비스입니다.

## Service Surface

| 영역 | 엔드포인트 | 설명 |
|---|---|---|
| Notes | `POST /api/v1/notes` | 마크다운 노트 생성, 위키링크 파싱, outbox 이벤트 적재 |
| Notes | `GET /api/v1/notes`, `GET /api/v1/notes/{id}` | 소유자 기준 노트 목록/상세 조회 |
| Notes | `PATCH /api/v1/notes/{id}`, `DELETE /api/v1/notes/{id}` | 버전 스냅샷 후 수정, soft delete |
| Graph | `GET /api/v1/graph/data`, `GET /api/v1/graph?noteId=&depth=` | D3 호환 그래프와 이웃 그래프 조회 |
| Links | `GET /api/v1/notes/{id}/backlinks`, `/outlinks` | 위키링크 기반 백링크/아웃링크 |
| Tags | `GET /api/v1/tags/autocomplete`, `/popular` | 태그 자동완성, 인기 태그 |
| Search | `GET /api/v1/notes/search` | Elasticsearch BM25 검색 |
| AI Search | `POST /api/v1/ai/search/semantic`, `/hybrid` | learning-ai 시맨틱 검색과 RRF 하이브리드 검색 |
| Admin | `POST /api/v1/admin/search/accuracy-test` | 검색 정확도 벤치마크 실행 |

OpenAPI UI는 기본 로컬 포트 기준 `http://localhost:8082/swagger-ui.html` 입니다.

## Runtime Dependencies

| 구성요소 | 기본값 | 용도 |
|---|---|---|
| Java | 21 | Spring Boot 4 실행 |
| PostgreSQL | `jdbc:postgresql://localhost:5432/synapse_knowledge` | 노트/링크/버전/outbox 정본 저장소 |
| Redis | `localhost:6379` | 검색 idempotency와 인기 태그 캐시 |
| Kafka | `localhost:9092` | 노트 변경, search-sync 이벤트 |
| Schema Registry | `http://localhost:8086` | Avro schema id 관리 |
| Elasticsearch | `http://localhost:9200` | BM25 인덱스 `notes-v1` |
| learning-ai | `http://localhost:8090` | 시맨틱 검색/임베딩 위임 |
| platform-svc | `http://localhost:8080` | tenant lookup 연동 |

공통 인프라는 `../synapse-shared/docker-compose.yml` 기준으로 올립니다.

```powershell
cd ..\synapse-shared
docker compose up -d
cd ..\synapse-knowledge-svc
.\gradlew.bat bootRun --args='--spring.profiles.active=dev'
```

## Kafka Contract

| 방향 | Topic base | Payload | 비고 |
|---|---|---|---|
| 발행 | `knowledge.note.note-created-v1` | `NoteCreated` Avro | 노트 생성 후 outbox에서 발행 |
| 발행 | `knowledge.note.note-updated-v1` | `NoteUpdated` Avro | 노트 수정 후 발행 |
| 발행 | `knowledge.note.note-deleted-v1` | `NoteDeleted` Avro | soft delete 후 발행 |
| 발행/소비 | `knowledge.note.note-search-sync-v1` | `NoteSearchSyncKafkaEvent` JSON | Elasticsearch 동기화 파이프 |
| DLQ | `{resolved-topic}.dlq` | 원본 payload | search-sync listener recoverer |

`synapse.kafka.topic-prefix` 또는 `KAFKA_TOPIC_PREFIX`가 있으면 모든 base topic 앞에 그대로 붙습니다. 예: `dev.knowledge.note.note-created-v1`.

## Verification

Phase C local verification, 2026-06-21 KST:

```powershell
.\gradlew.bat clean build
```

Result: PASS. 기본 `test` task는 H2 기반 노트/그래프/태그/컨트롤러 회귀와 Kafka topic resolver 단위 검증을 포함합니다. 외부 의존성이 큰 테스트는 별도 task로 분리되어 있습니다.

```powershell
.\gradlew.bat searchE2eTest
.\gradlew.bat chunkingPgTest
.\gradlew.bat topicPrefixLiveTest
```

## Runbook

1. 노트/그래프 API가 실패하면 `KnowledgeGraphFlowE2ETest`와 `NeighborGraphIntegrationTest`를 먼저 실행합니다.
2. 태그 API가 Redis 장애에 끌려가면 안 됩니다. Redis 캐시 조회/저장은 실패 시 DB 조회로 폴백합니다.
3. 검색 반영 지연은 `knowledge.note.note-search-sync-v1` lag, DLQ, `notes-v1` document count 순서로 확인합니다.
4. staging에서 ES sync 안정화를 닫으려면 실제 Kafka + Schema Registry + Elasticsearch 환경에서 `searchE2eTest` 또는 동등한 smoke를 실행하고, 생성/수정/삭제가 `notes-v1`에 반영되는 시간을 기록합니다.
5. 발표 그래프 데이터는 같은 tenant/user 아래에서 노트 제목 충돌 없이 seed하고, 깨진 위키링크는 `targetNoteId = null` outlink로 남는지 확인합니다.

## External Gates

로컬 `clean build`는 통과했습니다. 최종 Phase C 완료 처리에는 staging Elasticsearch, Kafka ACL, Schema Registry compatibility가 켜진 환경에서 검색 반영 지연과 DLQ 0건을 별도 증거로 남겨야 합니다.
