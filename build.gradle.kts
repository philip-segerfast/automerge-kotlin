plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
    kotlin("plugin.power-assert") version libs.versions.kotlin apply false
}
