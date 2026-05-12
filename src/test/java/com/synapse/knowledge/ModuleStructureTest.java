package com.synapse.knowledge;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModuleStructureTest {

    @Test
    void verifyModuleStructure() {
        ApplicationModules.of(KnowledgeSvcApplication.class).verify();
    }
}
