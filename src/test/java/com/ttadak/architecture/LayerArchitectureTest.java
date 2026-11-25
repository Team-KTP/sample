package com.ttadak.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.springframework.stereotype.Service;

@AnalyzeClasses(packages = "com.ttadak", importOptions = ImportOption.DoNotIncludeTests.class)
class LayerArchitectureTest {

    @ArchTest
    static final ArchRule 의존은_표현에서_응용으로_응용에서_인프라로만_흐른다 = layeredArchitecture()
        .consideringOnlyDependenciesInLayers()
        .layer("presentation")
        .definedBy("..presentation..")
        .layer("application")
        .definedBy("..application..")
        .layer("implement")
        .definedBy("..implement..")
        .layer("infrastructure")
        .definedBy("..infrastructure..")
        .whereLayer("presentation")
        .mayNotBeAccessedByAnyLayer()
        .whereLayer("application")
        .mayOnlyBeAccessedByLayers("presentation")
        .whereLayer("implement")
        .mayOnlyBeAccessedByLayers("application")
        .whereLayer("infrastructure")
        .mayOnlyBeAccessedByLayers("presentation", "application", "implement");

    @ArchTest
    static final ArchRule 도메인은_다른_계층을_참조하지_않는다 = noClasses()
        .that()
        .resideInAPackage("..domain..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "..presentation..",
            "..application..",
            "..implement..",
            "..infrastructure.."
        );

    @ArchTest
    static final ArchRule 도메인_사이에_순환_의존이_없다 = slices()
        .matching("com.ttadak.(*)..")
        .should()
        .beFreeOfCycles();

    @ArchTest
    static final ArchRule 도메인_안의_패키지_사이에_순환_의존이_없다 = slices()
        .matching("com.ttadak.(*).(*)..")
        .should()
        .beFreeOfCycles();

    @ArchTest
    static final ArchRule 응용_계층의_서비스는_다른_서비스를_부르지_않는다 = noClasses()
        .that()
        .resideInAPackage("..application..")
        .and()
        .areAnnotatedWith(Service.class)
        .should(dependOnAnotherService());

    private static ArchCondition<JavaClass> dependOnAnotherService() {
        return new ArchCondition<>("depend on another @Service class") {

            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                javaClass.getDirectDependenciesFromSelf()
                    .stream()
                    .filter(dependency -> !dependency.getTargetClass().equals(javaClass))
                    .filter(
                        dependency -> dependency.getTargetClass().isAnnotatedWith(Service.class)
                    )
                    .forEach(
                        dependency -> events.add(
                            SimpleConditionEvent.satisfied(dependency, dependency.getDescription())
                        )
                    );
            }

        };
    }

}
