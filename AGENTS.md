# AGENTS.md

`synapse-knowledge-svc`에서 작업하는 에이전트를 위한 시작 문서다.

## Purpose

이 문서는 이 레포에서 작업을 시작할 때 먼저 읽는 진입 가이드다.

- 이 문서는 규칙 원문을 대체하지 않는다.
- 실제 기준 문서는 항상 `docs/rules/*.md`다.
- `docs/rules`에서 `[MUST]`로 표시된 규칙은 우회하지 않는다.
- 작업이 모호하면 이 문서만 믿지 말고 관련 규칙 원문을 다시 연다.

## Startup Checklist

1. 현재 작업의 Task/Workflow 문서부터 읽는다.
   - `docs/project-management/task/*`
   - `docs/project-management/workflow/*`
2. 코드를 건드리기 전에 변경 범위를 먼저 분류한다.
   - API
   - 도메인 로직
   - 인증/인가
   - Spring/Modulith
   - Kafka/이벤트 스키마
   - 관측성
   - 컨테이너/배포
   - 개인정보/마스킹
3. `Always Read` 규칙을 먼저 읽는다.
4. 변경 범위에 맞는 `Read When Relevant` 규칙을 추가로 읽는다.
5. 계획을 제안할 때 어떤 규칙 파일을 근거로 삼았는지 밝힌다.
6. 구현을 마치면 같은 규칙 기준으로 검증한 뒤 작업을 닫는다.

## Always Read

중간 이상 크기의 작업에서는 아래 문서를 항상 읽는다.

- `docs/rules/01-security.md`
  - Secrets, 접근 제어, CORS, 입력 검증, 기본 보안 원칙.
- `docs/rules/03-technical.md`
  - 메서드 크기, 트랜잭션 경계, N+1 방지, AOP, Modulith 경계.
- `docs/rules/04-quality.md`
  - 테스트 구조, 정적 분석, 리뷰 기대치, 필요한 테스트 종류.
- `docs/rules/12-working-log.md`
  - 커밋 메시지, PR 본문, 일일 HISTORY 작성 규칙.
- `docs/rules/14-task-structure.md`
  - Task 필드, `Step Goal`, `Done When`, `Scope`, 작업 절차 구조.

## Read When Relevant

아래 문서는 `Always Read`에 더해서 변경 주제에 맞게 읽는다.

| 변경 유형 | 추가로 읽을 문서 |
| --- | --- |
| API 또는 비즈니스 기능 변경 | `docs/rules/02-function.md` |
| 인증, 권한, JWT, OAuth, 세션, 블랙리스트 | `docs/rules/06-auth-token.md` |
| Spring Boot, Modulith, Gradle, DTO, Entity, Validation | `docs/rules/07-platform.md`, `docs/rules/07-platform-spring.md` |
| Kafka, CloudEvents, Avro, Schema Registry, DLQ | `docs/rules/08-kafka-event.md` |
| 로깅, 메트릭, 트레이싱 | `docs/rules/09-observability.md` |
| Docker, 이미지 태그, Kustomize, ArgoCD, K8s 리소스 | `docs/rules/10-container-k8s.md`, `docs/rules/05-operation.md` |
| 개인정보, 마스킹, 보존, 데이터 주권 | `docs/rules/11-data-sovereignty.md` |
| 보안 체크리스트나 부록 참고가 필요한 경우 | `docs/rules/appendix-a-asvs.md`, `docs/rules/appendix-b-owasp.md`, `docs/rules/appendix-c-checklist.md` |

## Execution Rules

- 계획 수립:
  - 계획을 제안할 때 참고한 규칙 파일명을 함께 적는다.
  - Task 문서의 수용 기준이 비어 있거나 약하면, 워크플로가 요구하는 범위 안에서 먼저 Task 구조를 바로잡는다.
- 구현:
  - `[MUST]` 항목은 강한 제약으로 취급한다.
  - 속도를 이유로 보안, 아키텍처 경계, 테스트 요구사항을 희생하지 않는다.
  - 선언된 Scope 안에서 구현한다. 작업이 커지면 Task 문서를 갱신하거나 일을 나눈다.
- 검증:
  - 테스트, 정적 분석, 검증 절차를 관련 규칙 문서 기준으로 확인한다.
  - 아키텍처나 연동 작업은 정상 경로만 보지 말고 경계 위반과 실패 동작도 확인한다.
- 모호성 처리:
  - 이 문서와 규칙 원문이 다르게 느껴지면 규칙 원문을 따른다.
  - 여러 규칙이 동시에 적용되면 더 엄격한 해석을 따른다.

## Output And Log Rules

- 커밋 메시지:
  - Conventional Commits 형식인 `<type>(<scope>): <subject>`를 따른다.
  - 필요하면 본문에 왜 이 변경이 필요한지 적는다.
- PR 본문:
  - `## 변경 사항` 섹션을 항상 포함한다.
  - `## 테스트 결과` 섹션을 항상 포함한다.
  - PR은 리뷰 가능한 크기로 유지한다. `12-working-log.md`는 가능하면 400줄 이내를 권장한다.
- 일일 로그:
  - 업무 종료 전 `HISTORY.md`를 갱신한다.
  - `12-working-log.md`의 `한 일 / 이슈 / 내일 계획` 3단 형식을 따른다.
- Task 문서:
  - `14-task-structure.md`의 필수 10필드를 따른다.
  - `Done When`은 `Step Goal` 바로 다음에 둔다.
  - `Scope`는 `In Scope`와 `Out of Scope`로 나눈다.
- 워크플로 준수:
  - Task/Workflow 문서는 참고사항이 아니라 작업 계약으로 취급한다.
  - 문서 업데이트가 필요한 작업이면 완료 경로에 함께 포함한다.

## Source Of Truth

- 실제 정책 원문은 `docs/rules/*.md`에 있다.
- 이 문서는 시작 속도와 문서 라우팅을 돕기 위한 얇은 안내문이다.
- 규칙이 바뀌면 이 문서에는 링크와 적용 조건만 수정한다.
- 긴 정책 본문을 이 문서에 복사하지 않는다.
- `docs/rules` 아래 새 규칙이 생기면 `Always Read` 또는 `Read When Relevant` 중 어디에 넣을지 결정해 반영한다.
