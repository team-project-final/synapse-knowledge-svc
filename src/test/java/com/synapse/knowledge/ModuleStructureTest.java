package com.synapse.knowledge;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModuleStructureTest {

    private final ApplicationModules modules = ApplicationModules.of(KnowledgeSvcApplication.class);

    @Test
    void verifyModuleStructure_모듈경계가유효할때_shouldPassVerification() {
        modules.verify();
    }

    @Test
    void writeModuleDocumentation_모듈문서를생성할때_shouldGeneratePlantUmlFiles() {
        new Documenter(modules)
                .writeModulesAsPlantUml()
                .writeIndividualModulesAsPlantUml();
    }
}
