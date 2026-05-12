# 작업 스코프: @knowledge-owner-1

## 담당자 정보

| 항목 | 내용 |
|------|------|
| Handle | @knowledge-owner-1 |
| 역할 | 트랙 C-1 (2명 중 1명) |
| 담당 서비스 | synapse-knowledge-svc |
| 담당 모듈 | note, graph |
| GitHub Repository | [synapse-knowledge-svc](https://github.com/team-project-final/synapse-knowledge-svc) |

## 4주 전체 책임 범위

### 도메인 경계

- **In Scope**:
  - Markdown 노트 CRUD (제목/본문/태그)
  - 위키링크 파싱 (`[[note-title]]` → note_links 저장)
  - 백링크 조회 (어떤 노트가 이 노트를 참조하는지)
  - D3.js 지식 그래프 데이터 API (노드/엣지)
  - Elasticsearch 동기화 (Kafka로 노트 변경 → ES 인덱싱)
  - 노트 버전 이력 (수정 히스토리)
  - 태그 관리 고도화
  - Graph PageRank (시간 허용 시)
- **Out of Scope**:
  - chunking/검색 (knowledge-owner-2 담당)
  - Spring Modulith 설정/ArchUnit (knowledge-owner-2 담당)
  - AI 카드 생성 (learning-ai-owner 담당)

### 주차별 스코프 매트릭스

| 주차 | 기간 | 핵심 목표 | 산출물 | 의존성 |
|------|------|-----------|--------|--------|
| W1 | 05-12~16 | knowledge-svc 골격 + note CRUD + 위키링크 | 서비스 골격, 노트 API, 위키링크 파싱 | 인프라 (team-lead) |
| W2 | 05-19~23 | graph 백링크 + ES 동기화 (Kafka) | graph API, ES 인덱싱 | Kafka 토픽 (team-lead W2) |
| W3 | 05-26~30 | 노트 버전이력 + 태그 고도화 + PageRank | 버전 API, 태그 관리, PageRank | graph 완성 (W2) |
| W4 | 06-02~06 | 버그 수정 + 통합 테스트 | 안정화 | 전체 통합 (W3) |

## 협업 인터페이스

| 상대 | 주고받는 것 | 방향 |
|------|------------|------|
| @knowledge-owner-2 | note/graph 모듈 경계 합의 | 양방향 |
| @learning-ai-owner | note.created Kafka 이벤트 → AI 카드 생성 트리거 | 발행 → |
| @engagement-owner | 노트 공유 시 노트 정보 제공 (내부 API) | 제공 → |
| Frontend | 노트 에디터 + 그래프 뷰 데이터 제공 | 제공 → |

## 성공 기준

- [ ] Markdown 노트 CRUD 완전 동작
- [ ] 위키링크 파싱 → 양방향 링크 조회
- [ ] 그래프 시각화 데이터 API 동작
- [ ] ES 검색 동기화 (노트 생성/수정 → ES 반영)
- [ ] 노트 버전 이력 조회
