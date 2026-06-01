# WORKPLAN: Step 5 — Kafka 기반 ES 자동 동기화

> **작성일**: 2026-05-29  
> **브랜치**: `feat/step5-kafka-es`  
> **기준 문서**: TASK_knowledge-1.md Step 5 | WORKFLOW_knowledge-1_W2.md Step 5  
> **적용 Rules**: 03-technical · 07-platform-spring · 08-kafka-event · 12-working-log

---

## 1. 현재 상태 vs 목표

### 현재 구현 (Spring Event 방식)

```
NoteService
  └─ ApplicationEventPublisher.publishEvent(NoteSearchSyncRequested)
        └─ NoteSearchIndexingListener  (@TransactionalEventListener + @Async)
              └─ NoteSearchRepository.upsert / deleteByNoteId()  →  Elasticsearch
```

### 목표 구현 (Kafka 방식)

```
NoteService
  └─ ApplicationEventPublisher.publishEvent(NoteSearchSyncRequested)  ← 기존 유지
        └─ NoteSearchKafkaProducer  (@TransactionalEventListener AFTER_COMMIT)
              └─ KafkaTemplate.send("knowledge.note.note-search-sync-v1", event)
                    └─ NoteSearchKafkaConsumer  (@KafkaListener)
                          └─ NoteSearchRepository.upsert / deleteByNoteId()  →  Elasticsearch
```

> Spring Application Event는 내부 모듈 통신용으로 유지 (RULE 03.5 Modulith 경계).  
> Kafka는 외부 전송 계층 — 내구성 + eventual consistency 보장.

---

## 2. 제약 조건 정리

### RULE 08 — Kafka Event [MUST]

| # | 항목 | 제약 내용 | 적용 위치 |
|---|------|-----------|-----------|
| 08.1 | 토픽 네이밍 | `{서비스}.{도메인}.{이벤트}-v{N}` 패턴 | 토픽: `knowledge.note.note-search-sync-v1` |
| 08.1 | DLQ 토픽 | 원본 토픽명 + `.dlq` | `knowledge.note.note-search-sync-v1.dlq` |
| 08.2 | CloudEvents 1.0 | `specversion`, `id`(UUID), `source`, `type`, `time`(timestamp-millis), `tenantid`, `datacontenttype` 전부 필수 | `NoteSearchSyncKafkaEvent` record |
| 08.3 | Avro 스키마 | namespace `com.synapse.event.knowledge`, 모든 필드 default 값, camelCase, tenantId 포함 | 이번 PR: JSON 직렬화. Avro + Schema Registry 등록은 synapse-shared 레포 별도 PR |
| 08.4 | 호환성 모드 | knowledge 도메인: `BACKWARD_TRANSITIVE` | Schema Registry 등록 시 적용 |
| 08.5 | 금지 | 필드 이름 변경 금지 / default 없는 필드 추가 금지 / enum 값 제거 금지 | `NoteSearchSyncKafkaEvent` 설계 시 모든 필드에 default 상당 값 설계 |
| 08.6 | 멱등 Consumer | ES upsert는 `noteId` 기준 자연 멱등. 중복 이벤트 수신 시 로그만 남기고 skip | `NoteSearchKafkaConsumer` |
| 08.7 | DLQ + 재시도 | 3회 재시도 (1s → 2s → 4s, max 7s), 실패 시 DLQ 전송 | `KafkaConfig.java` |

### RULE 03 — Technical [MUST]

| # | 항목 | 제약 내용 | 적용 위치 |
|---|------|-----------|-----------|
| 03.3 | 트랜잭션 | Kafka 발행은 DB 트랜잭션 커밋 완료 후. 트랜잭션 내에서 외부 시스템 호출 금지 | `@TransactionalEventListener(phase = AFTER_COMMIT)` |
| 03.5 | Modulith 경계 | `search` 모듈의 `allowedDependencies = {"shared", "global"}` 준수. 모듈 간 순환 의존 금지 | 신규 클래스 전부 `search` 모듈 내 배치 |
| 03.1 | 메서드 크기 | 30~40줄 이내. 초과 시 private 메서드로 분리 | Consumer/Producer 각 메서드 |

### RULE 07-platform-spring [MUST/SHOULD]

| # | 항목 | 제약 내용 | 적용 위치 |
|---|------|-----------|-----------|
| 07.1.1 | Modulith verify() | 모듈 경계 위반 시 CI 빌드 실패. `ApplicationModules.of(...).verify()` 통과 필수 | 신규 패키지 위치 검증 |
| 07.1.2 | DTO as Record | Kafka 메시지 DTO는 Java Record 사용 | `NoteSearchSyncKafkaEvent` |
| 07.1.3 | Entity @Setter 금지 | Kafka 이벤트 DTO에 setter 사용 금지 | Record이므로 자동 준수 |

### RULE 12 — Working Log [MUST]

| # | 항목 | 제약 내용 |
|---|------|-----------|
| 12.1 | 커밋 메시지 | Conventional Commits: `feat(search):`, `chore(kafka):` 형식 |
| 12.2 | PR 본문 | `## 변경 사항` + `## 테스트 결과` 필수, 400줄 이하 권고 |
| 12.3 | HISTORY | 작업 완료 후 HISTORY_knowledge-1.md W2 섹션 갱신 |

---

## 3. 구현 파일 목록

### 3.1 신규 생성 (소스)

| 파일 경로 | 역할 |
|-----------|------|
| `src/main/java/com/synapse/knowledge/search/event/NoteSearchSyncKafkaEvent.java` | CloudEvents 1.0 envelope + 페이로드 (Java Record) |
| `src/main/java/com/synapse/knowledge/search/service/producer/NoteSearchKafkaProducer.java` | Spring Event 수신 → Kafka 발행 |
| `src/main/java/com/synapse/knowledge/search/service/consumer/NoteSearchKafkaConsumer.java` | Kafka 소비 → ES 인덱싱 (멱등 처리) |
| `src/main/java/com/synapse/knowledge/search/config/KafkaConfig.java` | Consumer factory + DLQ 설정 |

### 3.2 수정 (소스)

| 파일 경로 | 변경 내용 |
|-----------|-----------|
| `build.gradle.kts` | `spring-kafka`, `spring-kafka-test`, `testcontainers:kafka` 의존성 추가 |
| `src/main/resources/application.yml` | `spring.kafka.*` 기본 설정 추가 |
| `src/main/resources/application-dev.yml` | `spring.kafka.bootstrap-servers` 개발 환경 값 추가 |
| `src/test/resources/application-test.yml` | `@EmbeddedKafka` 연동 설정 추가 |
| `search/service/listener/NoteSearchIndexingListener.java` | **삭제** — Kafka Producer로 역할 대체 |

### 3.3 신규 생성 (테스트)

| 파일 경로 | 테스트 내용 |
|-----------|-------------|
| `src/test/java/com/synapse/knowledge/search/service/consumer/NoteSearchKafkaConsumerTest.java` | `@EmbeddedKafka` — 메시지 발행 → Consumer 수신 → Repository 호출 검증 (Mockito) |

---

## 4. 설계 상세

### 4.1 NoteSearchSyncKafkaEvent (RULE 08.2 · 08.3)

```java
// CloudEvents 1.0 필수 필드 + 페이로드
public record NoteSearchSyncKafkaEvent(
    String       specversion,     // "1.0" 고정
    String       id,              // UUID — 멱등 키 (RULE 08.6)
    String       source,          // "knowledge-svc"
    String       type,            // "com.synapse.event.knowledge.NoteSearchSyncRequested"
    long         time,            // epoch millis (RULE 08.3: timestamp-millis)
    String       tenantid,        // 멀티테넌트 식별자 (RULE 08.2 필수)
    String       datacontenttype, // "application/json" 고정
    Long         noteId,
    Long         userId,
    String       title,
    String       contentPlain,
    List<String> tags,
    boolean      deleted
) {
    static final String TOPIC = "knowledge.note.note-search-sync-v1";  // RULE 08.1

    static NoteSearchSyncKafkaEvent from(NoteSearchSyncRequested event) { ... }
}
```

### 4.2 NoteSearchKafkaProducer (RULE 03.3)

```java
@Component
class NoteSearchKafkaProducer {
    private final KafkaTemplate<String, NoteSearchSyncKafkaEvent> kafkaTemplate;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)  // DB 커밋 후 발행
    void onNoteSearchSyncRequested(NoteSearchSyncRequested event) {
        NoteSearchSyncKafkaEvent kafkaEvent = NoteSearchSyncKafkaEvent.from(event);
        kafkaTemplate.send(TOPIC, kafkaEvent.noteId().toString(), kafkaEvent);
        // key = noteId → 같은 노트의 이벤트가 동일 파티션에서 순서 보장
    }
}
```

### 4.3 NoteSearchKafkaConsumer (RULE 08.6 · 08.7)

```java
@Component
class NoteSearchKafkaConsumer {
    private final NoteSearchRepository noteSearchRepository;

    @KafkaListener(
        topics = NoteSearchSyncKafkaEvent.TOPIC,
        groupId = "knowledge-search-indexer",
        containerFactory = "searchSyncKafkaListenerContainerFactory"  // DLQ 연결
    )
    void handle(NoteSearchSyncKafkaEvent event) {
        // ES upsert/delete = noteId 기준 멱등 (RULE 08.6)
        if (event.deleted()) {
            noteSearchRepository.deleteByNoteId(event.noteId());
            return;
        }
        noteSearchRepository.upsert(new NoteSearchDocument(
            event.noteId(), event.tenantid(), event.userId(),
            event.title(), event.contentPlain(),
            event.tags() == null ? List.of() : event.tags(),
            Instant.now()
        ));
    }
}
```

### 4.4 KafkaConfig — DLQ (RULE 08.7)

```java
@Configuration
class KafkaConfig {

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, NoteSearchSyncKafkaEvent>
        searchSyncKafkaListenerContainerFactory(...) {

        // 3회 재시도: 1s → 2s → 4s (총 max 7s) — RULE 08.7
        var backOff = new ExponentialBackOff(1_000L, 2.0);
        backOff.setMaxElapsedTime(7_000L);

        // 실패 시 .dlq 토픽으로 전송 — RULE 08.7
        var recoverer = new DeadLetterPublishingRecoverer(dlqTemplate,
            (record, ex) -> new TopicPartition(record.topic() + ".dlq", -1));

        factory.setCommonErrorHandler(new DefaultErrorHandler(recoverer, backOff));
        return factory;
    }
}
```

### 4.5 application.yml 추가 내용

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      group-id: knowledge-search-indexer
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "com.synapse.knowledge.search.event"
        spring.json.value.default.type: "com.synapse.knowledge.search.event.NoteSearchSyncKafkaEvent"
```

---

## 5. Modulith 경계 확인 (RULE 03.5 · 07.1.1)

```
search 모듈 (allowedDependencies = {"shared", "global"})
  ├── search/event/NoteSearchSyncKafkaEvent.java        ← 신규 (search 모듈 내부) ✅
  ├── search/service/producer/NoteSearchKafkaProducer.java
  │     └─ 의존: shared.NoteSearchSyncRequested         ← shared 허용 ✅
  ├── search/service/consumer/NoteSearchKafkaConsumer.java
  │     └─ 의존: search/event, search/repository        ← 동일 모듈 ✅
  └── search/config/KafkaConfig.java                    ← 신규 (search 모듈 내부) ✅

삭제: search/service/listener/NoteSearchIndexingListener.java
      (직접 ES 호출 → Kafka Producer로 역할 전환)
```

> `NoteSearchSyncKafkaEvent`는 Kafka 전송 전용 DTO.  
> `shared`에 두지 않음 — search 모듈 외부에서 참조 불필요.  
> `ModuleStructureTest.verifyModuleStructure()` 통과 필수.

---

## 6. 작업 순서 (RULE 14 10단계 워크플로)

| 순서 | 작업 | Done When |
|------|------|-----------|
| ① | 브랜치 생성: `feat/step5-kafka-es` | 브랜치 체크아웃 완료 |
| ② | `build.gradle.kts` 의존성 추가 | `./gradlew build` 성공 |
| ③ | yml 설정 추가 (application.yml / dev / test) | 앱 기동 시 Kafka 설정 로드 확인 |
| ④ | `NoteSearchSyncKafkaEvent` 작성 | CloudEvents 필드 전부 포함, 컴파일 통과 |
| ⑤ | `NoteSearchKafkaProducer` 작성 | `@TransactionalEventListener(AFTER_COMMIT)` |
| ⑥ | `KafkaConfig` 작성 | DLQ + ContainerFactory 빈 등록 |
| ⑦ | `NoteSearchKafkaConsumer` 작성 | `@KafkaListener` + 멱등 처리 |
| ⑧ | `NoteSearchIndexingListener` 삭제 | 컴파일 통과, 기존 테스트 회귀 없음 |
| ⑨ | `NoteSearchKafkaConsumerTest` 작성 + 통과 | `@EmbeddedKafka` 3개 시나리오 통과 |
| ⑩ | `ModuleStructureTest` 통과 확인 | `./gradlew test` 그린 |

---

## 7. 커밋 계획 (RULE 12.1)

```
chore(kafka): add spring-kafka, spring-kafka-test, testcontainers:kafka dependency
chore(kafka): add Kafka bootstrap-servers config (application.yml / dev / test)
feat(search): add NoteSearchSyncKafkaEvent CloudEvents 1.0 envelope
feat(search): add NoteSearchKafkaProducer — Spring Event AFTER_COMMIT → Kafka
feat(search): add NoteSearchKafkaConsumer — Kafka → ES indexing with DLQ retry
feat(search): add KafkaConfig — searchSyncKafkaListenerContainerFactory with DLQ
refactor(search): remove NoteSearchIndexingListener (replaced by Kafka producer)
test(search): add NoteSearchKafkaConsumerTest with @EmbeddedKafka
```

---

## 8. PR 체크리스트 (RULE 12.2)

```markdown
## 변경 사항
- [chore] spring-kafka 의존성 추가 (build.gradle.kts)
- [feat] NoteSearchSyncKafkaEvent — CloudEvents 1.0 envelope (specversion, id, tenantid 포함)
- [feat] NoteSearchKafkaProducer — @TransactionalEventListener(AFTER_COMMIT) → Kafka 발행
- [feat] NoteSearchKafkaConsumer — @KafkaListener, ES upsert/delete (멱등)
- [feat] KafkaConfig — DLQ + exponential backoff 3회 재시도
- [refactor] NoteSearchIndexingListener 제거 (Kafka Producer로 대체)

## 테스트 결과
- [ ] NoteSearchKafkaConsumerTest @EmbeddedKafka 3개 시나리오 통과
- [ ] ModuleStructureTest (Modulith verify) 통과
- [ ] ./gradlew build 성공
- [ ] 기존 NoteSearchControllerTest 회귀 없음

## 비고
- Avro 스키마 / Schema Registry 등록: synapse-shared 레포 별도 PR 예정 (RULE 08.3)
- SearchElasticsearchIntegrationTest @Disabled 유지 (ES/OpenSearch 호환성 별도 과제)
```

---

## 9. TASK Step 5 Done When 매핑

| Done When 항목 | 이번 PR 처리 여부 |
|----------------|-------------------|
| 노트 생성 시 note.created Kafka 이벤트 발행 | ✅ NoteSearchKafkaProducer |
| 노트 수정 시 note.updated Kafka 이벤트 발행 | ✅ NoteSearchKafkaProducer |
| Kafka Consumer → ES 인덱싱 | ✅ NoteSearchKafkaConsumer |
| `GET /notes/search?q=` 검색 결과 반환 | ✅ 기존 NoteSearchController 유지 |
| 한글 형태소 분석(nori) 동작 | ⚠️ 기존 ES 설정 유지 (SearchElasticsearchIntegrationTest @Disabled) |
| 통합 테스트 통과 (Embedded Kafka + Testcontainers ES) | ✅ NoteSearchKafkaConsumerTest @EmbeddedKafka |
| Avro 스키마 + Schema Registry 등록 | ⏳ 별도 PR (synapse-shared 레포) |
