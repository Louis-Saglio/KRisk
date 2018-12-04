import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlin_version = "1.3.10"

plugins {
    kotlin("jvm") version "1.3.10"
}

group = "KRisk"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.0.1")
    compile("org.jetbrains.kotlin", "kotlin-test", "1.3.10")
    testCompile("org.junit.jupiter", "junit-jupiter-api", "5.2.0")
    testCompile("org.mockito","mockito-core", "2.+")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
