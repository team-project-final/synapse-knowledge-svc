# knowledge-2 W1 Step 2 ArchUnit Boundaries

## Test Rules

- direct dependency ban
  - `note` must not depend on `graph`, `chunking`
  - `graph` must not depend on `note`, `chunking`
  - `chunking` must not depend on `note`, `graph`
- cycle detection
  - `com.synapse.knowledge.(*)..` slices must be free of cycles
- internal isolation
  - `note.internal`, `graph.internal`, `chunking.internal`, `shared.internal` must not be referenced from outside each owning module

## CI Integration

- workflow: `.github/workflows/ci-java.yml`
- added step: `ArchUnit boundaries`
- command: `./gradlew test --tests '*ModuleBoundaryArchTest' --no-daemon`

## Verification

- pass: `./gradlew.bat test --tests *ModuleBoundaryArchTest`
- pass: `./gradlew.bat test`
- fail reproduction:
  - temporary violating class added under `chunking.internal`
  - `chunking` -> `graph` direct dependency caused `ModuleBoundaryArchTest` failure
  - violating class removed and tests re-run successfully

## Notes

- Task 문서에는 `push` 기반 FAIL 확인이 적혀 있었지만, 실제 원격 브랜치를 깨뜨리지 않기 위해 로컬 위반 재현으로 검증했다.
- 이 단계는 Step 1에서 만든 Modulith 구조를 테스트와 CI로 강제하는 목적에 집중한다.
