# WORKFLOW: @knowledge-owner-2 — Week 2

> **Task 문서**: [TASK_knowledge-2.md](../task/TASK_knowledge-2.md)
> **기간**: 2026-05-18 ~ 2026-05-22, 5 영업일
> **PRD**: [PRD_W2.md](../prd/PRD_W2.md)

---

## Step 4: chunking 모듈 — 비동기 청크 분할

### 4.1 TASK 시작
- [ ] Step Goal / Done When / Scope / Input 확인
- [ ] PRD_W2 해당 요구사항 확인 (chunking 모듈)
- [ ] Duration 산정 확인

### 4.2 요구사항 분석
- [ ] 노트 → N개 청크 분할 전략 정의 (문단/토큰 단위)
- [ ] 청크 크기/오버랩 정책 (max_tokens, overlap_tokens)
- [ ] 비동기 분할 트리거 시점 정의 (노트 저장/갱신 시)
- [ ] 청크 재생성 정책 (노트 갱신 시 기존 청크 삭제 후 재생성)
- [ ] Instructions 초안 → TASK 문서 반영

### 4.3 Security 1차 검토
- [ ] 인증 필요 여부: 내부 서비스 호출 (Kafka 기반, 직접 API 노출 없음)
- [ ] 권한 종류: 시스템 내부 처리
- [ ] 공개 API 여부: No
- [ ] 결과 → TASK Constraints 반영

### 4.4 ERD 설계
- [ ] note_chunks 테이블 설계 (id, note_id, chunk_index, chunk_text, token_count, embedding vector(1536), created_at)
- [ ] 참고: 임베딩은 별도 테이블(chunk_embeddings)이 아닌 note_chunks.embedding 컬럼에 저장
- [ ] 인덱스 설계 (note_chunks.note_id, note_chunks.note_id+chunk_index UNIQUE)
- [ ] 관계 정의 (note_chunks.note_id → notes.id FK)
- [ ] Duration(final) 갱신

### 4.5 Security 2차 검토
- [ ] 청크 데이터 접근 제어 (내부 서비스만)
- [ ] 노트 삭제 시 관련 청크 cascade 삭제 확인
- [ ] 행 단위 접근 제어: 노트 소유자 기반 간접 제어
- [ ] 결과 → TASK Constraints 반영

### 4.6 DTO / Entity 설계 (API First)
- [ ] ChunkResponse 정의 (id, note_id, chunk_index, chunk_text, token_count)
- [ ] ChunkCreateEvent 정의 (note_id, chunk_text)
- [ ] NoteChunk Entity 작성
- [ ] MapStruct 매퍼 작성
- [ ] Output Format → TASK 반영

### 4.7 Repository 구현
- [ ] NoteChunkRepository 인터페이스 작성
- [ ] findByNoteIdOrderByChunkIndex 커스텀 쿼리
- [ ] deleteByNoteId 벌크 삭제 쿼리
- [ ] Flyway 마이그레이션 스크립트 작성

### 4.8 Service + Test
- [ ] ChunkingService 구현 (chunkNote, getNoteChunksByNoteId, deleteNoteChunksByNoteId)
- [ ] 노트 → N개 청크 분할 로직 구현 (문단/토큰 기반)
- [ ] 비동기 처리 구현 (@Async 또는 Kafka Consumer)
- [ ] 청크 재생성 로직 (기존 삭제 → 새 청크 생성)
- [ ] 토큰 카운팅 유틸리티 구현
- [ ] 단위 테스트 작성 (Mockito — 다양한 노트 길이)
- [ ] 경계값 테스트 (빈 노트, 매우 긴 노트, 한국어 포함)
- [ ] 테스트 통과 확인

### 4.9 Controller + Test
- [ ] 내부 서비스 — 외부 Controller 해당 없음
- [ ] 관리용 청크 조회 엔드포인트 (선택): GET /notes/{noteId}/chunks
- [ ] 통합 테스트 (노트 입력 → 청크 분할 결과 검증)
- [ ] 테스트 통과 확인

### 4.10 View + Test (해당 시)
- [ ] Flutter 화면 연동: 해당 없음
- [ ] 청크 분할 결과 DB 확인
- [ ] RULE Reference → TASK 반영

**Step 4 Status**: [ ] Not Started / [ ] In Progress / [ ] Done

---

## Step 5: 검색 BM25 통합 — ES nori 기반 한국어 검색

### 5.1 TASK 시작
- [ ] Step Goal / Done When / Scope / Input 확인
- [ ] PRD_W2 해당 요구사항 확인 (BM25 검색)
- [ ] Duration 산정 확인

### 5.2 요구사항 분석
- [ ] 검색 API 요건 정의 (키워드 검색, 필터, 정렬, 페이지네이션)
- [ ] ES nori 분석기 한국어 형태소 분석 요건
- [ ] 하이라이팅(검색어 강조) 요건 분석
- [ ] 검색 범위 (title, content, tags)
- [ ] Instructions 초안 → TASK 문서 반영

### 5.3 Security 1차 검토
- [ ] 인증 필요 여부: Yes (JWT 인증 필요)
- [ ] 권한 종류: 로그인 사용자 (본인 노트만 검색)
- [ ] 공개 API 여부: No
- [ ] 결과 → TASK Constraints 반영

### 5.4 인프라 설계
- [ ] ES 인덱스 매핑 설계 (nori analyzer + BM25 scoring)
- [ ] nori_tokenizer + nori_part_of_speech 필터 설정
- [ ] multi_match 쿼리 전략 (best_fields / cross_fields)
- [ ] 하이라이팅 태그 설정 (<em> 또는 <mark>)
- [ ] Duration(final) 갱신

### 5.5 Security 2차 검토
- [ ] 검색 결과 userId 필터 강제 (본인 노트만)
- [ ] 검색 쿼리 인젝션 방지 (쿼리 이스케이프)
- [ ] 검색 결과 민감정보 노출 방지
- [ ] 결과 → TASK Constraints 반영

### 5.6 DTO / Entity 설계 (API First)
- [ ] SearchRequest 정의 (query, filters, cursor, limit)
- [ ] SearchResultResponse 정의 (noteId, title, highlights[], score)
- [ ] SearchPageResponse 정의 (results[], totalCount, cursor — cursor-based pagination)
- [ ] Output Format → TASK 반영

### 5.7 Repository 구현
- [ ] NoteSearchRepository 갱신 (BM25 검색 메서드 추가)
- [ ] ES 인덱스 매핑 JSON 갱신 (nori analyzer 설정)
- [ ] multi_match + highlight 쿼리 빌더 구현

### 5.8 Service + Test
- [ ] SearchService 구현 (search, searchWithHighlight)
- [ ] BM25 키워드 검색 구현 (multi_match query)
- [ ] 하이라이팅 결과 파싱 구현
- [ ] userId 필터 강제 적용
- [ ] 페이지네이션 처리
- [ ] 단위 테스트 작성 (Mockito)
- [ ] 한국어 형태소 분석 검증 테스트
- [ ] 테스트 통과 확인

### 5.9 Controller + Test
- [ ] GET /notes/search?q={query}&cursor=...&limit=20 엔드포인트 구현
- [ ] 슬라이스 테스트 (@WebMvcTest)
- [ ] 401/403 응답 테스트
- [ ] 통합 테스트 (인덱싱 → 검색 → 하이라이팅 검증)
- [ ] 한국어 검색 통합 테스트 (nori 형태소 분석)
- [ ] 테스트 통과 확인

### 5.10 View + Test (해당 시)
- [ ] Flutter 화면 연동: 해당 없음 (프론트 별도)
- [ ] Swagger API 문서 확인
- [ ] RULE Reference → TASK 반영

**Step 5 Status**: [ ] Not Started / [ ] In Progress / [ ] Done
