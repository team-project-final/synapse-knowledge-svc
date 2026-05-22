# synapse-knowledge-svc

## gstack

Available gstack skills (invoke with `/skill-name`):

| Skill | Description |
|---|---|
| `autoplan` | Auto-review pipeline — runs CEO, design, eng, and DX reviews sequentially with auto-decisions |
| `benchmark` | Performance regression detection: page load times, Core Web Vitals, bundle sizes |
| `browse` | Headless browser for QA testing, screenshots, form testing, and UI dogfooding |
| `canary` | Post-deploy canary monitoring — watches live app for errors and regressions |
| `careful` | Safety guardrails for destructive commands (rm -rf, DROP TABLE, force-push, etc.) |
| `checkpoint` | Save and resume working state checkpoints across sessions and workspace handoffs |
| `cso` | Chief Security Officer mode — secrets, supply chain, CI/CD, OWASP Top 10, STRIDE |
| `design-consultation` | Design consultation and ideation session |
| `design-html` | Generate HTML/CSS design artifacts |
| `design-review` | Review UI/UX design for consistency, accessibility, and quality |
| `design-shotgun` | Rapid parallel design exploration across multiple approaches |
| `devex-review` | Developer experience review of APIs, tooling, and workflows |
| `document-release` | Generate release notes and documentation for a release |
| `freeze` | Freeze the current codebase state (blocks further edits) |
| `unfreeze` | Unfreeze the codebase state |
| `gstack-upgrade` | Upgrade gstack to the latest version |
| `guard` | Enable guard mode to protect against accidental destructive actions |
| `health` | Project health check — dependencies, tests, lint, security, and CI status |
| `investigate` | Deep investigation of a bug, incident, or unknown behavior |
| `land-and-deploy` | Land a PR and trigger deployment pipeline |
| `learn` | Learn mode — explains the codebase or a concept in depth |
| `office-hours` | Interactive Q&A / office hours session with guided exploration |
| `open-gstack-browser` | Launch GStack Browser (AI-controlled Chromium with sidebar extension) |
| `pair-agent` | Spawn a pair-programming agent for collaborative problem solving |
| `plan-ceo-review` | CEO-level review of a plan (strategy, ROI, risk) |
| `plan-design-review` | Design review of an implementation plan |
| `plan-devex-review` | Developer experience review of an implementation plan |
| `plan-eng-review` | Engineering review of an implementation plan |
| `qa` | Full QA pass — runs tests, checks coverage, and verifies golden paths |
| `qa-only` | QA-only mode (skips setup steps) |
| `retro` | Sprint/project retrospective — what went well, what to improve |
| `review` | Code review a PR or diff |
| `setup-browser-cookies` | Configure browser cookies for authenticated testing |
| `setup-deploy` | Set up deployment configuration for this project |
| `ship` | Full ship pipeline: review → QA → land → deploy |

## Skill routing

When the user's request matches an available skill, ALWAYS invoke it using the Skill
tool as your FIRST action. Do NOT answer directly, do NOT use other tools first.
The skill has specialized workflows that produce better results than ad-hoc answers.

Key routing rules:
- Product ideas, "is this worth building", brainstorming → invoke office-hours
- Bugs, errors, "why is this broken", 500 errors → invoke investigate
- Ship, deploy, push, create PR → invoke ship
- QA, test the site, find bugs → invoke qa
- Code review, check my diff → invoke review
- Update docs after shipping → invoke document-release
- Weekly retro → invoke retro
- Design system, brand → invoke design-consultation
- Visual audit, design polish → invoke design-review
- Architecture review → invoke plan-eng-review
- Save progress, checkpoint, resume → invoke checkpoint
- Code quality, health check → invoke health
