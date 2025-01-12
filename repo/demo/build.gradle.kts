import org.jetbrains.compose.desktop.application.dsl.TargetFormat

private val projectPackageName = "dev.psegerfast.automergekotlin.repo.demo"

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    // KMP - Compose
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    // KMP - Kotlin Serialization
    alias(libs.plugins.kotlinx.serialization)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    jvm("desktop")

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(projects.automergeKotlinCore)
            implementation(projects.automergeKotlinRepo)
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "$projectPackageName.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = projectPackageName
            packageVersion = "1.0.0"
        }
    }
}