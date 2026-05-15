# knowledge-2 W1 Step 1 Module Structure

## Module Diagram

```text
com.synapse.knowledge
├─ note
│  ├─ NoteModuleApi
│  └─ internal
│     └─ NoteModuleBootstrap
├─ graph
│  ├─ GraphModuleApi
│  └─ internal
│     └─ GraphModuleBootstrap
├─ chunking
│  ├─ ChunkingModuleApi
│  └─ internal
│     └─ ChunkingModuleBootstrap
└─ shared
   └─ internal
      └─ SharedModuleBootstrap
```

## Dependency Rules

- `note -> shared`
- `graph -> shared`
- `chunking -> shared`
- `shared` does not depend on domain modules
- cross-module direct imports are disallowed; use exposed API or events only

## Verification

- `ModuleStructureTest.verifyModuleStructure_모듈경계가유효할때_shouldPassVerification`
- `ModuleStructureTest.writeModuleDocumentation_모듈문서를생성할때_shouldGeneratePlantUmlFiles`
- generated PlantUML output path: `build/spring-modulith-docs`
