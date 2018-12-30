import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktorVersion = "1.1.1"

plugins {
    kotlin("jvm") version "1.3.10"
    idea
    id("kotlinx-serialization") version "1.3.11"
}

group = "KRisk"
version = "0.1"

repositories {
    mavenCentral()
    jcenter()
    maven("https://kotlin.bintray.com/kotlinx")
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile("io.ktor", "ktor-server-netty",ktorVersion)
    compile("org.slf4j", "slf4j-simple", "1.7.6")
    compile("io.ktor:ktor-jackson:$ktorVersion")
    compile("io.ktor:ktor-websockets:$ktorVersion")
    compile("io.ktor:ktor-client-websocket:$ktorVersion")
    compile("io.ktor:ktor-client-cio:$ktorVersion")
    compile("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.9.1")
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.0.1")
    testCompile("org.jetbrains.kotlin", "kotlin-test", "1.3.10")
    testCompile("org.junit.jupiter", "junit-jupiter-api", "5.2.0")
    testCompile("org.mockito","mockito-core", "2.+")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
