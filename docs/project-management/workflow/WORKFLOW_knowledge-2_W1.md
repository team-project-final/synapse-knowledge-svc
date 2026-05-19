# WORKFLOW: @knowledge-owner-2 — Week 1

> **Task 문서**: [TASK_knowledge-2.md](../task/TASK_knowledge-2.md)
> **기간**: 2026-05-12 ~ 2026-05-15, 4 영업일
> **기능개발 Workflow**: [README §7](../README.md)

---

## Step 1: Modulith 모듈 구조 설정

### 1.1 TASK 시작

- [x] Step Goal / Done When / Scope / Input 확인
- [x] PRD_W1 해당 요구사항 확인 (모듈 분리 요구사항)
- [x] Duration 산정 확인 (1일)

### 1.2 요구사항 분석

- [x] Spring Modulith 공식 문서 분석
- [x] note/graph/chunking 모듈 간 의존성 규칙 도출
- [x] 모듈별 public API 인터페이스 정의 기준
- [x] Instructions 초안 → TASK 문서 반영

### 1.3 Security 1차 검토

- [x] 인증 필요 여부: No (설정 작업)
- [x] 권한 종류: 없음
- [x] 공개 API 여부: No
- [x] 모듈 간 internal 패키지 외부 접근 차단 확인
- [x] 결과 → TASK Constraints 반영

### 1.4 모듈 정의 및 구조 설계

- [x] note, graph, chunking 패키지 생성
- [x] 각 모듈 `package-info.java` 작성
- [x] `@ApplicationModule(allowedDependencies=...)` 설정
- [x] 모듈별 internal 패키지 분리
- [x] 모듈 간 public API 인터페이스 정의
- [x] 모듈 구조 다이어그램 문서화

### 1.5 모듈 경계 검증

- [x] `ApplicationModules.verify()` 통합 테스트 작성
- [x] 의존 위반 시 빌드 실패 수동 검증
- [x] 순환 의존 감지 확인
- [x] internal 패키지 외부 접근 시 컴파일 에러 확인

### 1.6 ~ 1.10 N/A (설정 작업 — DTO/Entity/Repository/Service/Controller/View 해당 없음)

**Step 1 Status**: [ ] Not Started / [ ] In Progress / [x] Done

---

## Step 2: ArchUnit 모듈 경계 테스트

### 2.1 TASK 시작

- [x] Step Goal / Done When / Scope / Input 확인
- [x] PRD_W1 해당 요구사항 확인 (모듈 경계 자동 감지)
- [x] Duration 산정 확인 (1.5일)

### 2.2 요구사항 분석

- [x] ArchUnit 라이브러리 문서 분석
- [x] 테스트 규칙 3건 도출 (직접 의존 금지, 순환 참조, internal 접근)
- [x] CI 파이프라인 연동 요건 분석
- [x] Instructions 초안 → TASK 문서 반영

### 2.3 Security 1차 검토

- [x] 인증 필요 여부: No (테스트 작성)
- [x] 권한 종류: 없음
- [x] 공개 API 여부: No
- [x] 결과 → TASK Constraints 반영

### 2.4 테스트 규칙 설계

- [x] 모듈 간 직접 의존 금지 규칙 정의
- [x] 순환 참조 감지 규칙 정의
- [x] internal 패키지 외부 접근 금지 규칙 정의
- [x] CI workflow 연동 구조 설계

### 2.5 테스트 작성 및 검증

- [x] `archunit-junit5` 의존성 추가
- [x] 모듈 간 직접 의존 금지 테스트 작성
- [x] 순환 참조 감지 테스트 작성
- [x] internal 패키지 외부 접근 금지 테스트 작성
- [x] 의도적 위반 코드 push → FAIL 확인
- [x] 테스트 통과 후 위반 코드 revert

### 2.6 CI 파이프라인 연동

- [x] CI workflow에 ArchUnit 테스트 단계 추가
- [x] ArchUnit 실패 시 전체 빌드 FAIL 확인
- [x] 테스트 실행 시간 10초 이내 확인

### 2.7 ~ 2.10 N/A (테스트 작성 — Repository/Service/Controller/View 해당 없음)

**Step 2 Status**: [ ] Not Started / [ ] In Progress / [x] Done

---

## Step 3: Avro 스키마 등록 및 호환성 검증

> **구현 레포**: `synapse-shared` (스키마/스크립트/compose)  
> **문서 동기화 레포**: `synapse-knowledge-svc`

### 3.1 TASK 시작

- [x] Step Goal / Done When / Scope / Input 확인
- [x] PRD_W1 해당 요구사항 확인 (이벤트 스키마 관리)
- [x] Duration 산정 확인 (1.5일)

### 3.2 요구사항 분석

- [x] note-created 이벤트 명세 분석 (noteId, title, content, createdAt)
- [x] Schema Registry 호환성 모드 (`BACKWARD_TRANSITIVE`) 분석
- [x] Avro 스키마 네이밍 규칙 (`knowledge.note.note-created-v1-value`) 확인
- [x] Instructions 초안 → TASK 문서 반영

### 3.3 Security 1차 검토

- [x] 인증 필요 여부: No (Schema Registry 내부 접근)
- [x] 권한 종류: 없음 (인프라 간 통신)
- [x] 공개 API 여부: No (내부 서비스 전용)
- [x] 결과 → TASK Constraints 반영

### 3.4 스키마 설계

- [x] `note-created-v1.avsc` 스키마 파일 작성
- [x] 필수 필드 정의 (`noteId`, `tenantId`, `title`, `content`, `createdAt`)
- [x] subject 네이밍 확정 (`knowledge.note.note-created-v1-value`)
- [x] Gradle Avro 플러그인 기반 `testSchemasTask` 설정

### 3.5 스키마 등록 및 검증

- [x] Schema Registry Docker Compose 파일 구성 및 `docker compose ... config` 검증
- [x] 스키마 등록 스크립트 작성
- [x] `BACKWARD_TRANSITIVE` 호환성 검사 스크립트/Gradle task 추가
- [x] 로컬 Docker daemon 기동 후 Registry 컨테이너 실행 확인
- [x] 비호환 변경(default 없는 required 필드 추가) 시 등록 거부 런타임 검증
- [x] 호환 변경(optional 필드 추가) 시 등록 성공 런타임 검증
- [x] Avro 코드 생성 및 `testSchemasTask` live Registry 실행 확인

### 3.6 ~ 3.10 N/A (스키마 등록 — DTO/Entity/Repository/Service/Controller/View 해당 없음)

**Step 3 Status**: [ ] Not Started / [ ] In Progress / [x] Done
