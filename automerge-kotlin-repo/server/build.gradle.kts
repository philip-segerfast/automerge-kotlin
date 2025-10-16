plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
            api(libs.rsocket.ktor.server)
            api(libs.ktor.serialization.protobuf)

            implementation(projects.automergeKotlinRepo.shared)
            implementation(projects.automergeKotlinCore)
        }
    }
}