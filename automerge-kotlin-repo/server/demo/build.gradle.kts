
plugins {
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.kotlin.jvm)
}

group = "com.example"
version = "0.0.1"

application {
    mainClass = "com.example.ServerApplicationKt"

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

//repositories {
//    mavenCentral()
//    google()
//}

dependencies {
    implementation(projects.automergeKotlinCore)
    implementation(projects.automergeKotlinRepo.shared)
    implementation(projects.automergeKotlinRepo.server)

    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.netty)
    implementation(libs.rsocket.ktor.core)
    implementation(libs.rsocket.ktor.server)
    implementation(libs.logback.classic)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
    implementation(kotlin("stdlib-jdk8"))
}
