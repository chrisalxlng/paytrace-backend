package dev.christopherlang.paytrace;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "dev.christopherlang.paytrace")
public class ArchitectureTests {

    @ArchTest
    static final ArchRule apiPackageShouldNotAccessData = noClasses()
        .that()
        .resideInAPackage("..api..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("..data..");

    @ArchTest
    static final ArchRule domainPackageShouldNotAccessApi = noClasses()
        .that()
        .resideInAPackage("..domain..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("..api..")
        .orShould()
        .dependOnClassesThat()
        .resideInAPackage("..data..");

    @ArchTest
    static final ArchRule dataPackageShouldNotAccessApiOrDomain = noClasses()
        .that()
        .resideInAPackage("..data..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("..api..");

}
