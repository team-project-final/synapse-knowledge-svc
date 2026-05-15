package com.synapse.knowledge.archunit;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ModuleBoundaryArchTest {

    private JavaClasses classes;

    @BeforeEach
    void setUp() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.synapse.knowledge");
    }

    @Test
    void verifyNoDirectDependenciesBetweenDomainModules_도메인모듈이서로직접참조할때_shouldFail() {
        // Given
        var noteMustNotDependOnOtherDomains = noClasses()
                .that().resideInAPackage("com.synapse.knowledge.note..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "com.synapse.knowledge.graph..",
                        "com.synapse.knowledge.chunking..")
                .because("note 모듈은 graph 또는 chunking을 직접 참조하면 안 된다.");

        var graphMustNotDependOnOtherDomains = noClasses()
                .that().resideInAPackage("com.synapse.knowledge.graph..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "com.synapse.knowledge.note..",
                        "com.synapse.knowledge.chunking..")
                .because("graph 모듈은 note 또는 chunking을 직접 참조하면 안 된다.");

        var chunkingMustNotDependOnOtherDomains = noClasses()
                .that().resideInAPackage("com.synapse.knowledge.chunking..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "com.synapse.knowledge.note..",
                        "com.synapse.knowledge.graph..")
                .because("chunking 모듈은 note 또는 graph를 직접 참조하면 안 된다.");

        // When & Then
        noteMustNotDependOnOtherDomains.check(classes);
        graphMustNotDependOnOtherDomains.check(classes);
        chunkingMustNotDependOnOtherDomains.check(classes);
    }

    @Test
    void verifyNoCyclicDependencies_패키지순환참조가존재할때_shouldFail() {
        // Given
        var noCyclicDependencies = slices()
                .matching("com.synapse.knowledge.(*)..")
                .should().beFreeOfCycles()
                .because("모듈 간 순환 참조는 독립 테스트와 모듈 분리를 어렵게 만든다.");

        // When & Then
        noCyclicDependencies.check(classes);
    }

    @Test
    void verifyInternalPackagesAreNotAccessedExternally_internal패키지를외부모듈이참조할때_shouldFail() {
        // Given
        var noteInternalMustStayPrivate = noClasses()
                .that().resideOutsideOfPackage("com.synapse.knowledge.note..")
                .should().dependOnClassesThat().resideInAnyPackage("com.synapse.knowledge.note.internal..")
                .because("note.internal은 note 모듈 외부에서 접근하면 안 된다.");

        var graphInternalMustStayPrivate = noClasses()
                .that().resideOutsideOfPackage("com.synapse.knowledge.graph..")
                .should().dependOnClassesThat().resideInAnyPackage("com.synapse.knowledge.graph.internal..")
                .because("graph.internal은 graph 모듈 외부에서 접근하면 안 된다.");

        var chunkingInternalMustStayPrivate = noClasses()
                .that().resideOutsideOfPackage("com.synapse.knowledge.chunking..")
                .should().dependOnClassesThat().resideInAnyPackage("com.synapse.knowledge.chunking.internal..")
                .because("chunking.internal은 chunking 모듈 외부에서 접근하면 안 된다.");

        var sharedInternalMustStayPrivate = noClasses()
                .that().resideOutsideOfPackage("com.synapse.knowledge.shared..")
                .should().dependOnClassesThat().resideInAnyPackage("com.synapse.knowledge.shared.internal..")
                .because("shared.internal은 shared 모듈 외부에서 접근하면 안 된다.");

        // When & Then
        noteInternalMustStayPrivate.check(classes);
        graphInternalMustStayPrivate.check(classes);
        chunkingInternalMustStayPrivate.check(classes);
        sharedInternalMustStayPrivate.check(classes);
    }
}
