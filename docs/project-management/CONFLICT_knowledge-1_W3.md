# CONFLICT: @knowledge-owner-1 — W3 문서 불일치 및 충돌 대응 계획

> **작성일**: 2026-06-02  
> **브랜치**: feat/step6-note-version, feat/step7-tag-api  
> **관련 문서**: [TASK_knowledge-1.md](task/TASK_knowledge-1.md) | [WORKFLOW_knowledge-1_W3.md](workflow/WORKFLOW_knowledge-1_W3.md)

---

## CONFLICT-1: `note_versions.created_by` 컬럼 포함 여부

### 불일치 내용

| 문서 | 명세 |
|------|------|
| TASK Step 6 (1.7) | `note_versions` DDL에 `created_by BIGINT NOT NULL` 포함 |
| WORKFLOW Step 6 (1.4) | "created_by 컬럼은 ERD에 없음 — 소유자는 `note_id → notes.user_id`로 참조" |

### 현재 채택한 방향

**WORKFLOW 기준으로 `created_by` 제외** — 버전 작성자는 `note_id → notes.user_id`로 추적.

> **근거**: WORKFLOW가 TASK보다 나중에 작성된 실행 문서이며, ERD 정합성을 명시적으로 언급했음.

### 충돌이 생기는 시나리오

1. **팀 리드 리뷰 시** — "TASK 명세대로 `created_by`가 왜 없냐"는 코멘트
2. **프론트엔드 연동 시** — 버전별 수정자 표시 UI 요구사항이 생기는 경우
3. **다른 서비스 연동 시** — 버전 작성자 정보를 외부 API로 제공해야 하는 경우

### 충돌 대응 계획

#### 시나리오 1 — 팀 리드 리뷰 코멘트

```
대응:
1. PR 코멘트에 WORKFLOW 1.4 근거 명시
   "WORKFLOW 1.4에서 ERD 기준으로 created_by 제외 결정됨.
    TASK 명세와 불일치가 있어 WORKFLOW를 우선 적용했습니다.
    TASK 명세 수정이 필요하면 알려주세요."
2. 팀 리드 승인 시 → TASK Step 6 DDL 항목에서 created_by 삭제로 문서 정합성 맞춤
3. 팀 리드가 created_by 추가를 요구 시 → Flyway 마이그레이션 추가 (V9)
```

#### 시나리오 2 — 프론트엔드에서 버전별 수정자 표시 요구

```
대응:
1. 단기: note_id → notes.user_id JOIN으로 충족 가능 여부 확인
   → 가능하면 컬럼 추가 없이 해결
2. 불가능하면: V9 마이그레이션으로 created_by 추가
   ALTER TABLE note_versions ADD COLUMN created_by BIGINT;
   (기존 데이터는 notes.user_id로 백필)
3. 이 경우 TASK 문서 복원 (created_by 항목 다시 추가)
```

#### 시나리오 3 — 외부 API 제공 요구

```
대응: 시나리오 2와 동일 절차. 외부 API 명세서(Wiki)에 반영 후 팀 리드 확인.
```

### 즉시 해야 할 액션

- [ ] PR 리뷰 시 팀 리드에게 이 불일치를 코멘트로 명시적으로 알릴 것
- [ ] TASK Step 6 DDL 항목 수정 여부를 팀 리드 피드백 후 결정

---

## CONFLICT-2: 태그 자동완성 — TagRepository vs. note_tags 직접 쿼리

### 불일치 내용

| 문서 | 명세 |
|------|------|
| WORKFLOW Step 7 (1.7) | `TagRepository` 인터페이스 작성 (`findByNameStartingWith`, `findPopularTags`) |
| 실제 DB 스키마 (V4) | `note_tags(note_id, tag)` — 별도 `tags` 테이블 없음, Tag 엔티티 없음 |

### 현재 채택한 방향

**별도 `TagRepository` 없이 `note_tags` 테이블에 네이티브 쿼리 직접 사용**

```sql
-- 자동완성
SELECT tag, COUNT(*) AS cnt
FROM note_tags
WHERE tag LIKE :prefix%
GROUP BY tag
ORDER BY cnt DESC
LIMIT 10;

-- 인기 태그
SELECT tag, COUNT(*) AS cnt
FROM note_tags
GROUP BY tag
ORDER BY cnt DESC
LIMIT :limit;
```

> **근거**: 별도 `tags` 테이블을 만들면 V4 이후 새 마이그레이션이 필요하고,  
> `note_tags`의 `tag` 컬럼이 이미 인덱싱되어 있어 집계 쿼리 성능 충분.

### 충돌이 생기는 시나리오

1. **팀 리드 리뷰 시** — "WORKFLOW대로 TagRepository 왜 안 만들었냐"는 코멘트
2. **태그 색상/메타데이터 요구 시** — 프론트엔드에서 태그별 색상 표시 필요
3. **성능 이슈 시** — 데이터 증가로 집계 쿼리 느려지는 경우

### 충돌 대응 계획

#### 시나리오 1 — 팀 리드 리뷰 코멘트

```
대응:
1. PR 코멘트에 V4 스키마 근거 명시
   "V4 마이그레이션 기준 별도 tags 테이블이 없어 note_tags 직접 집계 방식으로 구현.
    TagRepository 분리가 필요하면 tags 테이블 추가 마이그레이션과 함께 진행하겠습니다."
2. 팀 리드가 현 방식 승인 시 → WORKFLOW 1.7 항목 주석 처리 또는 수정
3. tags 테이블 추가를 요구 시 → 아래 시나리오 2 절차 진행
```

#### 시나리오 2 — 태그 색상 등 메타데이터 요구

```
대응:
1. Flyway V9으로 tags 테이블 추가
   CREATE TABLE tags (
       id     BIGINT AUTO_INCREMENT PRIMARY KEY,
       name   VARCHAR(30) NOT NULL UNIQUE,
       color  VARCHAR(7),          -- #RRGGBB
       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
   );
2. note_tags 테이블에 tag_id FK 컬럼 추가 (V10)
   또는 tag 문자열 컬럼 유지하고 tags 테이블과 JOIN
3. TagRepository(JPA) 도입, 기존 네이티브 쿼리 서비스 대체
4. ERD 기준 SCOPE 문서 동기화 필요 (Wiki API 명세서 담당에게 알릴 것)
```

#### 시나리오 3 — 집계 쿼리 성능 이슈

```
대응 (순서대로 시도):
1. note_tags(tag) 인덱스 확인 → 이미 존재 (V4에서 생성됨)
2. 인기 태그 Redis 캐시 TTL 단축 검토 (현재 1시간 → 유지 또는 조정)
3. 캐시로 해결 안 되면 → 시나리오 2와 동일하게 tags 테이블 분리
```

### 즉시 해야 할 액션

- [ ] PR 리뷰 시 팀 리드에게 TagRepository 미생성 이유를 코멘트로 명시
- [ ] WORKFLOW Step 7 (1.7) 항목을 현재 구현 방식으로 업데이트

---

## 요약

| 충돌 번호 | 현재 채택 방향 | 뒤집힐 조건 | 뒤집힐 경우 비용 |
|-----------|---------------|-------------|-----------------|
| CONFLICT-1 | `created_by` 제외 | 팀 리드 요구 또는 수정자 표시 UI 필요 | Flyway V9 추가, 백필 쿼리 |
| CONFLICT-2 | `note_tags` 직접 집계 | 태그 메타데이터 요구 또는 팀 리드 요구 | Flyway V9~V10, TagRepository 신규 작성 |

> **공통 원칙**: 충돌 발생 시 임의로 결정하지 않고, 반드시 PR 코멘트 또는 슬랙으로 팀 리드에게 먼저 확인 후 진행.
