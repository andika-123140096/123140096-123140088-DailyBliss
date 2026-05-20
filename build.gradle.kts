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
    alias(libs.plugins.spotless)
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "com.diffplug.spotless")
    
    spotless {
        kotlin {
            target("**/*.kt")
            targetExclude("**/build/**/*.kt")
            ktlint("1.5.0")
                .setEditorConfigPath(rootProject.file(".editorconfig"))
                .editorConfigOverride(mapOf(
                    "ktlint_standard_function-naming" to "disabled",
                    "ktlint_standard_no-wildcard-imports" to "disabled",
                    "ktlint_standard_value-parameter-comment" to "disabled",
                    "ktlint_standard_max-line-length" to "disabled"
                ))
        }
        kotlinGradle {
            target("*.gradle.kts")
            ktlint()
        }
    }
    
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
