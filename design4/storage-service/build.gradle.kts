import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.4.5"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	id("com.google.cloud.tools.jib") version "3.1.1"
	kotlin("jvm")
	kotlin("plugin.spring")
}

group = "com.tempstorage"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
}

dependencies {
	// Web
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-websocket") // Web Socket

	// Documentation
	implementation("org.springdoc:springdoc-openapi-ui:1.5.9")
	implementation("org.springdoc:springdoc-openapi-kotlin:1.5.9")

	// Database
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb") // Mongo Database

	// MinIO
	implementation("io.minio:minio:8.2.1")

	// File Upload
	implementation("commons-fileupload:commons-fileupload:1.4") // MultipartResolver implementation

	// RabbitMQ
	implementation("org.springframework.cloud:spring-cloud-starter-stream-rabbit") // Cloud Stream + RabbitMQ Binder

	// Kotlin
	implementation("org.springframework.boot:spring-boot-configuration-processor")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	// Testing
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:2020.0.3")
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
		image = "tempstorage/centralized-storage-service"
		tags = setOf("latest")
	}
	container {
		jvmFlags = listOf("-Xms512m", "-Dserver.port=8080")
		ports = listOf("8080")
	}
}
