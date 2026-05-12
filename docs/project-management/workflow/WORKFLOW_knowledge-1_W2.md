# WORKFLOW: @knowledge-owner-1 — Week 2

> **Task 문서**: [TASK_knowledge-1.md](../task/TASK_knowledge-1.md)  
> **기간**: 2026-05-19 ~ 2026-05-23  
> **PRD**: [PRD_W2.md](../prd/PRD_W2.md)

---

## Step 4: graph 모듈 — 백링크 + D3.js 데이터 API

### 4.1 TASK 시작
- [ ] Step Goal / Done When / Scope / Input 확인
- [ ] PRD_W2 해당 요구사항 확인 (graph 모듈)
- [ ] Duration 산정 확인

### 4.2 요구사항 분석
- [ ] note_links 기반 양방향 조회 요건 분석 (outgoing/incoming 링크)
- [ ] 백링크 조회 API 요건 정의 (특정 노트를 참조하는 노트 목록)
- [ ] D3.js 시각화용 노드/엣지 JSON 데이터 구조 정의
- [ ] 그래프 탐색 깊이 제한 (depth) 정책 정의
- [ ] Instructions 초안 → TASK 문서 반영

### 4.3 Security 1차 검토
- [ ] 인증 필요 여부: Yes (JWT 인증 필요)
- [ ] 권한 종류: 로그인 사용자 (본인 노트 그래프만)
- [ ] 공개 API 여부: No
- [ ] 결과 → TASK Constraints 반영

### 4.4 ERD 설계
- [ ] note_links 테이블 확인/갱신 (sourceNoteId, targetNoteId, createdAt)
- [ ] 양방향 조회를 위한 인덱스 설계 (sourceNoteId, targetNoteId)
- [ ] 복합 유니크 제약 (sourceNoteId + targetNoteId)
- [ ] Duration(final) 갱신

### 4.5 Security 2차 검토
- [ ] 그래프 데이터 노출 범위 제한 (본인 노트만)
- [ ] 깊이 제한으로 과도한 탐색 방지
- [ ] 행 단위 접근 제어: 필요 (노트 소유자 확인)
- [ ] 결과 → TASK Constraints 반영

### 4.6 DTO / Entity 설계 (API First)
- [ ] BacklinkResponse 정의 (noteId, title, createdAt)
- [ ] GraphNodeDto 정의 (id, title, group)
- [ ] GraphEdgeDto 정의 (source, target)
- [ ] GraphDataResponse 정의 (nodes[], edges[])
- [ ] NoteLink Entity 확인/갱신
- [ ] MapStruct 매퍼 작성
- [ ] Output Format → TASK 반영

### 4.7 Repository 구현
- [ ] NoteLinkRepository 인터페이스 작성/갱신
- [ ] findByTargetNoteId (백링크 조회) 커스텀 쿼리
- [ ] findBySourceNoteId (아웃링크 조회) 커스텀 쿼리
- [ ] 재귀 그래프 탐색 쿼리 (depth 제한 CTE)
- [ ] Flyway 마이그레이션 스크립트 작성 (필요 시)

### 4.8 Service + Test
- [ ] GraphService 구현 (getBacklinks, getOutlinks, getGraphData)
- [ ] 백링크 조회 서비스 (targetNoteId → source 노트 목록)
- [ ] 아웃링크 조회 서비스 (sourceNoteId → target 노트 목록)
- [ ] D3.js용 노드/엣지 JSON 생성 서비스 (depth 제한 BFS/DFS)
- [ ] 소유자 검증 로직 구현
- [ ] 단위 테스트 작성 (Mockito)
- [ ] 테스트 통과 확인

### 4.9 Controller + Test
- [ ] GET /api/v1/notes/{noteId}/backlinks 엔드포인트 구현
- [ ] GET /api/v1/notes/{noteId}/outlinks 엔드포인트 구현
- [ ] GET /api/v1/graph?noteId={id}&depth={n} 엔드포인트 구현 (D3.js 데이터)
- [ ] 슬라이스 테스트 (@WebMvcTest)
- [ ] 401/403 응답 테스트
- [ ] 통합 테스트 (양방향 링크 + 그래프 JSON 검증)
- [ ] 테스트 통과 확인

### 4.10 View + Test (해당 시)
- [ ] Flutter 화면 연동: 해당 없음 (프론트 별도)
- [ ] Swagger API 문서 확인
- [ ] RULE Reference → TASK 반영

**Step 4 Status**: [ ] Not Started / [ ] In Progress / [ ] Done

---

## Step 5: ES 동기화 — 노트 변경 → Kafka → Elasticsearch 인덱싱

### 5.1 TASK 시작
- [ ] Step Goal / Done When / Scope / Input 확인
- [ ] PRD_W2 해당 요구사항 확인 (ES 동기화)
- [ ] Duration 산정 확인

### 5.2 요구사항 분석
- [ ] 노트 변경 이벤트 종류 정의 (created, updated, deleted)
- [ ] Kafka consumer 그룹 및 토픽 (note.updated) 확인
- [ ] Elasticsearch 인덱스 매핑 요건 분석 (title, content, tags, userId)
- [ ] 동기화 지연 허용 범위 (eventual consistency) 정의
- [ ] Instructions 초안 → TASK 문서 반영

### 5.3 Security 1차 검토
- [ ] Kafka 메시지 위변조 방지 확인
- [ ] ES 인덱스 접근 제어 (내부 네트워크만)
- [ ] 사용자 데이터 격리 (userId 필드 기반 검색 필터)
- [ ] 결과 → TASK Constraints 반영

### 5.4 인프라 설계
- [ ] Kafka consumer 설정 (group-id, auto-offset-reset, concurrency)
- [ ] ES 인덱스 매핑 설계 (notes 인덱스 — text/keyword 필드 타입)
- [ ] nori 분석기 설정 (한국어 형태소 분석)
- [ ] Duration(final) 갱신

### 5.5 Security 2차 검토
- [ ] ES 인덱스 데이터 삭제 동기화 확인 (노트 삭제 → ES 문서 삭제)
- [ ] 인덱스 갱신 실패 시 재처리 전략 (DLQ 또는 재시도)
- [ ] 결과 → TASK Constraints 반영

### 5.6 DTO / Entity 설계 (API First)
- [ ] NoteIndexDocument 정의 (noteId, userId, title, content, tags, updatedAt)
- [ ] NoteEvent Avro/DTO 정의 (eventType, noteId, userId, title, content, tags)
- [ ] Output Format → TASK 반영

### 5.7 Repository 구현
- [ ] NoteSearchRepository 인터페이스 작성 (Spring Data Elasticsearch)
- [ ] ES 인덱스 매핑 JSON 작성 (nori analyzer, 필드별 타입)
- [ ] 인덱스 초기화 스크립트 작성

### 5.8 Service + Test
- [ ] NoteIndexService 구현 (index, update, delete)
- [ ] Kafka Consumer 구현 (note.updated 토픽 소비 → ES 인덱싱)
- [ ] 이벤트별 분기 처리 (created→index, updated→update, deleted→delete)
- [ ] 재시도 로직 구현 (RetryTemplate 또는 @Retryable)
- [ ] 단위 테스트 작성 (Mockito)
- [ ] Kafka Consumer 테스트 (@EmbeddedKafka)
- [ ] 테스트 통과 확인

### 5.9 Controller + Test
- [ ] ES 동기화는 Controller 없음 (Kafka Consumer 기반)
- [ ] 인덱스 상태 확인용 Actuator/Admin 엔드포인트 (선택)
- [ ] 통합 테스트 (노트 이벤트 → Kafka → ES 인덱스 확인)
- [ ] 테스트 통과 확인

### 5.10 View + Test (해당 시)
- [ ] Flutter 화면 연동: 해당 없음
- [ ] ES 인덱스 데이터 Kibana/curl 확인
- [ ] RULE Reference → TASK 반영

**Step 5 Status**: [ ] Not Started / [ ] In Progress / [ ] Done
