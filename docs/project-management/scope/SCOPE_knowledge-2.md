# 작업 스코프: @knowledge-owner-2

## 담당자 정보

| 항목 | 내용 |
|------|------|
| Handle | @knowledge-owner-2 |
| 역할 | 트랙 C-2 (2명 중 1명) |
| 담당 서비스 | synapse-knowledge-svc |
| 담당 모듈 | chunking, 검색, Spring Modulith 검증 |
| GitHub Repository | [synapse-knowledge-svc](https://github.com/team-project-final/synapse-knowledge-svc) |

## 5주 전체 책임 범위

### 도메인 경계

- **In Scope**:
  - Spring Modulith 모듈 정의 (@ApplicationModule 설정)
  - ArchUnit 경계 검증 테스트 + CI 연동
  - Schema Registry 연동 검증 (Avro 등록 + 호환성)
  - 비동기 청크 분할 (노트 → 임베딩용 청크)
  - 검색 BM25 (Elasticsearch nori)
  - 검색 RRF (시맨틱 + BM25 하이브리드 결합)
  - 검색 정확도 측정
- **Out of Scope**:
  - 노트/그래프 CRUD (knowledge-owner-1 담당)
  - 시맨틱 검색 벡터 생성 (learning-ai-owner 담당)
  - 서비스 비즈니스 로직 전반

### 주차별 스코프 매트릭스

| 주차 | 기간 | 핵심 목표 | 산출물 | 의존성 |
|------|------|-----------|--------|--------|
| W1 | 05-12~15 | Modulith 모듈 정의 + ArchUnit + Schema Registry 검증 | @ApplicationModule 설정, ArchUnit 테스트, Avro 스키마 등록 | knowledge-svc 골격 (owner-1) |
| W2 | 05-18~22 | chunking 모듈 + BM25 검색 통합 | 청크 분할 API, 키워드 검색 API | ES (team-lead), note CRUD (owner-1) |
| W3 | 05-26~29 | RRF 하이브리드 검색 + 정확도 측정 | 통합 검색 API, 정확도 리포트 | 시맨틱 벡터 (learning-ai W2) |
| W4 | 06-01~05 | 버그 수정 + 검색 튜닝 | 안정화 | 전체 통합 (W3) |
| W5 | 06-08~12 | 검색 E2E + 정확도 리포트 최종화 + P0 버그 수정 | 검색 정확도 리포트, E2E 결과, 튜닝 내역 | knowledge-1 데이터, learning-ai 시맨틱 검색 |

## 협업 인터페이스

| 상대 | 주고받는 것 | 방향 |
|------|------------|------|
| @knowledge-owner-1 | 모듈 경계 합의 + note 변경 이벤트 | ← 수신 |
| @learning-ai-owner | 시맨틱 벡터 (pgvector) 조회 | ← 요청 |
| @team-lead | Schema Registry 정책 협의 | 양방향 |

## 성공 기준

- [ ] ApplicationModules.verify() 통과
- [ ] ArchUnit 모듈 경계 위반 시 CI 빌드 실패
- [ ] Schema Registry에 Avro 스키마 등록 + 호환성 검증
- [ ] 청크 분할 동작 (노트 → N개 청크)
- [ ] BM25 + RRF 검색 동작
