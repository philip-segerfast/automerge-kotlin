plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm()

    sourceSets {
        val kotestVersion = "6.0.0.M1"

        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
            api(libs.rsocket.ktor.client)
            api(libs.ktor.serialization.protobuf)
            implementation(libs.ktor.cio)

            api(projects.automergeKotlinRepo.shared)
            implementation(projects.automergeKotlinCore)
        }

        jvmTest.dependencies {
            implementation("io.kotest:kotest-runner-junit5:$kotestVersion")
        }

        commonTest.dependencies {
            implementation("io.kotest:kotest-assertions-core:$kotestVersion")
            implementation("io.kotest:kotest-property:$kotestVersion")
        }

    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}