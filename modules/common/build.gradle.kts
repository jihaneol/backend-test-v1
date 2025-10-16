plugins {
    kotlin("jvm")
}
tasks.jar {
    enabled = true
}

tasks.bootJar {
    enabled = false
}

dependencies {
    implementation(libs.bundles.jackson)
    implementation(libs.spring.boot.starter.logging)
    implementation(libs.spring.boot.starter.web)
    implementation(kotlin("stdlib-jdk8"))
}
repositories {
    mavenCentral()
}