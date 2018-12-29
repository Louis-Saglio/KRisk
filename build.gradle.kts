import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.10"
    idea
}

group = "KRisk"
version = "0.1"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile("io.ktor", "ktor-server-netty","1.1.1")
    compile("org.slf4j", "slf4j-simple", "1.7.6")
    compile("io.ktor:ktor-jackson:1.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.0.1")
    testCompile("org.jetbrains.kotlin", "kotlin-test", "1.3.10")
    testCompile("org.junit.jupiter", "junit-jupiter-api", "5.2.0")
    testCompile("org.mockito","mockito-core", "2.+")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
