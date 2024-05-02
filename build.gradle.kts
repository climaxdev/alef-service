
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.0.6"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.7.22"
    kotlin("plugin.spring") version "1.7.22"
    id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
    id("com.palantir.docker") version "0.35.0"
}

group = "docker-registry.alefed.com"
version = "CCL-15984-2"
java.sourceCompatibility = JavaVersion.VERSION_17

val env: MutableMap<String, String> = System.getenv()
val nexusRepoUsername = env["NEXUS_REPO_USERNAME"] ?: project.findProperty("nexusRepoUsername") as String?
val nexusRepoPassword = env["NEXUS_REPO_PASSWORD"] ?: project.findProperty("nexusRepoPassword") as String?

val rcTag = if (project.hasProperty("rc_tag")) project.property("rc_tag") as String else "2"
val gitCommit = if (project.hasProperty("git_commit")) project.property("git_commit") as String else "unspecified"

project.extra["rc_tag"] = rcTag
project.extra["git_commit"] = gitCommit

repositories {
    mavenCentral()
    maven {
        url = uri("https://nexus-repo.alefed.com/repository/Maven2-Group/")
        credentials {
            username = nexusRepoUsername
            password = nexusRepoPassword
        }
    }
    maven {
        url = uri("https://jitpack.io")
    }
}

ext {
    set("springCloudVersion", "2022.0.1")
    set("testcontainersVersion", "1.17.6")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.cloud:spring-cloud-starter-stream-kafka")
    implementation("org.springframework.cloud:spring-cloud-function-context")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("com.senacor.elasticsearch.evolution:spring-boot-starter-elasticsearch-evolution:0.4.2")

    implementation("jakarta.json:jakarta.json-api:2.0.1")
    implementation("io.arrow-kt:arrow-core:1.2.0-RC")
    implementation("io.arrow-kt:arrow-fx-coroutines:1.2.0-RC")
    implementation("io.kotest.extensions:kotest-extensions-spring:1.1.2")

    testImplementation("io.kotest:kotest-runner-junit5:5.4.2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.testcontainers:elasticsearch")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:kafka")
    testImplementation("com.ninja-squad:springmockk:3.1.1")
    testImplementation("org.springframework.cloud:spring-cloud-stream-test-support")
    testImplementation("org.springframework.cloud:spring-cloud-stream-test-binder")
}

dependencyManagement {
    imports {
        mavenBom("org.testcontainers:testcontainers-bom:${property("testcontainersVersion")}")
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xcontext-receivers")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks {

    docker {
        val build = project.getTasksByName("assemble", false).firstOrNull()
        dependsOn(build)
        name = "${project.group}/${project.name}"
        setDockerfile(File(projectDir, "src/main/docker/"))
        files(bootJar.get().archivePath)
        buildArgs(
            mapOf(
                "JAR_FILE" to "${bootJar.get().archiveFileName.get()}",
                "VERSION" to "$version",
                "GIT_COMMIT" to "$gitCommit"
            )
        )
        tag("dockerTag", "${project.group}/${bootJar.name}:$rcTag")
    }
}
