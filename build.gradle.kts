plugins {
    // Plugins are applied in subprojects
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.detekt)
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    
    detekt {
        config.setFrom(rootProject.file("config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        allRules = false
        parallel = true
        autoCorrect = true
        source.setFrom(files("src/commonMain/kotlin", "src/androidMain/kotlin", "src/iosMain/kotlin"))
    }

    afterEvaluate {
        tasks.findByName("check")?.dependsOn(tasks.named("detekt"))
    }
}
