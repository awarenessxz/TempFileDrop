import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.4.5"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	id("com.google.cloud.tools.jib") version "3.1.1"
	kotlin("jvm") version "1.4.32"
	kotlin("plugin.spring") version "1.4.32"
}

group = "com.tempfiledrop"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
}

dependencies {
	// Web
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-actuator")

	// Security
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.keycloak:keycloak-spring-boot-starter") // Keycloak

	// Database
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb") // Mongo Database

	// RabbitMQ
	implementation("org.springframework.cloud:spring-cloud-starter-stream-rabbit") // Cloud Stream + RabbitMQ Binder

	// Kotlin
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	// Testing
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:2020.0.3")
		mavenBom("org.keycloak.bom:keycloak-adapter-bom:11.0.2")
	}
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

jib {
	from {
		image = "gcr.io/distroless/java:11"
	}
	to {
		image = "tempfiledrop/webserver"
		tags = setOf("latest")
	}
	container {
		jvmFlags = listOf("-Xms512m", "-Dserver.port=8080")
		ports = listOf("8080")
	}
}
