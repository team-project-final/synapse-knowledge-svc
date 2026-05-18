# WORKFLOW: @knowledge-owner-2 — Week 1

> **Task 문서**: [TASK_knowledge-2.md](../task/TASK_knowledge-2.md)
> **기간**: 2026-05-12 ~ 2026-05-15, 4 영업일
> **기능개발 Workflow**: [README §7](../README.md)

---

## Step 1: Modulith 모듈 구조 설정

### 1.1 TASK 시작
- [ ] Step Goal / Done When / Scope / Input 확인
- [ ] PRD_W1 해당 요구사항 확인 (모듈 분리 요구사항)
- [ ] Duration 산정 확인 (1일)

### 1.2 요구사항 분석
- [ ] Spring Modulith 공식 문서 분석
- [ ] note/graph/chunking 모듈 간 의존성 규칙 도출
- [ ] 모듈별 public API 인터페이스 정의 기준
- [ ] Instructions 초안 → TASK 문서 반영

### 1.3 Security 1차 검토
- [ ] 인증 필요 여부: No (설정 작업)
- [ ] 권한 종류: 없음
- [ ] 공개 API 여부: No
- [ ] 모듈 간 internal 패키지 외부 접근 차단 확인
- [ ] 결과 → TASK Constraints 반영

### 1.4 모듈 정의 및 구조 설계
- [ ] note, graph, chunking 패키지 생성
- [ ] 각 모듈 `package-info.java` 작성
- [ ] `@ApplicationModule(allowedDependencies=...)` 설정
- [ ] 모듈별 internal 패키지 분리
- [ ] 모듈 간 public API 인터페이스 정의
- [ ] 모듈 구조 다이어그램 문서화

### 1.5 모듈 경계 검증
- [ ] `ApplicationModules.verify()` 통합 테스트 작성
- [ ] 의존 위반 시 빌드 실패 수동 검증
- [ ] 순환 의존 감지 확인
- [ ] internal 패키지 외부 접근 시 컴파일 에러 확인

### 1.6 ~ 1.10 N/A (설정 작업 — DTO/Entity/Repository/Service/Controller/View 해당 없음)

**Step 1 Status**: [ ] Not Started / [ ] In Progress / [ ] Done

---

## Step 2: ArchUnit 모듈 경계 테스트

### 1.1 TASK 시작
- [ ] Step Goal / Done When / Scope / Input 확인
- [ ] PRD_W1 해당 요구사항 확인 (모듈 경계 자동 감지)
- [ ] Duration 산정 확인 (1.5일)

### 1.2 요구사항 분석
- [ ] ArchUnit 라이브러리 문서 분석
- [ ] 테스트 규칙 3건 도출 (직접 의존 금지, 순환 참조, internal 접근)
- [ ] CI 파이프라인 연동 요건 분석
- [ ] Instructions 초안 → TASK 문서 반영

### 1.3 Security 1차 검토
- [ ] 인증 필요 여부: No (테스트 작성)
- [ ] 권한 종류: 없음
- [ ] 공개 API 여부: No
- [ ] 결과 → TASK Constraints 반영

### 1.4 테스트 규칙 설계
- [ ] 모듈 간 직접 의존 금지 규칙 정의
- [ ] 순환 참조 감지 규칙 정의
- [ ] internal 패키지 외부 접근 금지 규칙 정의
- [ ] CI workflow 연동 구조 설계

### 1.5 테스트 작성 및 검증
- [ ] `archunit-junit5` 의존성 추가
- [ ] 모듈 간 직접 의존 금지 테스트 작성
- [ ] 순환 참조 감지 테스트 작성
- [ ] internal 패키지 외부 접근 금지 테스트 작성
- [ ] 의도적 위반 코드 push → FAIL 확인
- [ ] 테스트 통과 후 위반 코드 revert

### 1.6 CI 파이프라인 연동
- [ ] CI workflow에 ArchUnit 테스트 단계 추가
- [ ] ArchUnit 실패 시 전체 빌드 FAIL 확인
- [ ] 테스트 실행 시간 10초 이내 확인

### 1.7 ~ 1.10 N/A (테스트 작성 — Repository/Service/Controller/View 해당 없음)

**Step 2 Status**: [ ] Not Started / [ ] In Progress / [ ] Done

---

## Step 3: Avro 스키마 등록 및 호환성 검증

### 1.1 TASK 시작
- [ ] Step Goal / Done When / Scope / Input 확인
- [ ] PRD_W1 해당 요구사항 확인 (이벤트 스키마 관리)
- [ ] Duration 산정 확인 (1.5일)

### 1.2 요구사항 분석
- [ ] note-created 이벤트 명세 분석 (noteId, title, content, createdAt)
- [ ] Schema Registry 호환성 모드 (BACKWARD) 분석
- [ ] Avro 스키마 네이밍 규칙 (`{topic}-value`) 확인
- [ ] Instructions 초안 → TASK 문서 반영

### 1.3 Security 1차 검토
- [ ] 인증 필요 여부: No (Schema Registry 내부 접근)
- [ ] 권한 종류: 없음 (인프라 간 통신)
- [ ] 공개 API 여부: No (내부 서비스 전용)
- [ ] 결과 → TASK Constraints 반영

### 1.4 스키마 설계
- [ ] `note-created-v1.avsc` 스키마 파일 작성
- [ ] 필수 필드 정의 (noteId, title, content, createdAt)
- [ ] subject 네이밍 확정 (`note-created-value`)
- [ ] Gradle Avro 플러그인 설정

### 1.5 스키마 등록 및 검증
- [ ] Schema Registry Docker 컨테이너 구성
- [ ] 스키마 등록 스크립트 작성
- [ ] BACKWARD 호환성 모드 설정
- [ ] 비호환 변경(필드 삭제) 시 등록 거부 테스트
- [ ] 호환 변경(optional 필드 추가) 시 등록 성공 테스트
- [ ] Avro 코드 생성 확인

### 1.6 ~ 1.10 N/A (스키마 등록 — DTO/Entity/Repository/Service/Controller/View 해당 없음)

**Step 3 Status**: [ ] Not Started / [ ] In Progress / [ ] Done
