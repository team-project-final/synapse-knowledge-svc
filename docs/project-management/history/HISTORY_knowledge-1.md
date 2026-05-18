# Work History: @knowledge-1

> **담당**: knowledge-svc / 노트·위키  
> **관련 문서**: [SCOPE](../scope/SCOPE_knowledge-1.md) | [TASK](../task/TASK_knowledge-1.md) | [WORKFLOW](../workflow/WORKFLOW_knowledge-1_W1.md)

---

## 진행 상태 대시보드

### W1 (2026-05-12 ~ 05-16)

| Step | 내용 | 상태 | 시작일 | 완료일 | 비고 |
|------|------|------|--------|--------|------|
| Step 1 | knowledge-svc 골격 생성 | Not Started | — | — | |
| Step 2 | note Markdown CRUD | Done | 2026-05-18 | 2026-05-18 | 보안/아키텍처 규칙 반영 |
| Step 3 | 위키링크 파싱 | Done | 2026-05-18 | 2026-05-18 | 정규식 파서 및 매핑 구현 |

**W1 진행률**: 2/3 Steps 완료
...
### W2 (2026-05-19 ~ 05-23)

#### 2026-05-18 (월)
- **완료**:
    - feat(note): 노트 기본 CRUD 구현 (생성, 목록 페이징, 상세, 수정, 소프트 삭제)
    - feat(shared): Shared 모듈 인프라 구축 (BaseEntity, GlobalExceptionHandler, Sanitizer)
    - fix(arch): Modulith 경계 준수를 위한 패키지 구조 재편 (shared.internal -> shared)
    - feat(note): 위키링크 파싱 엔진 및 역링크 API 구현 (Step 3)
    - test(note): WikiLinkParser 단위 테스트 및 NoteLink 통합 테스트 통과
- **진행 중**:
    - Step 4: 백링크 그래프 시각화 API 설계
- **이슈**:
    - Spring Modulith 캡슐화 규칙으로 인해 shared.internal 패키지 접근 불가 이슈 발생 -> 루트 패키지로 이동하여 해결
- **다음**:
    - D3.js 호환 그래프 데이터 API (nodes + edges) 구현
...
