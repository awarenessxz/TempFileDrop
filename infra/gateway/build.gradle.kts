import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.4.5"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
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
	// Spring Framework
	implementation("org.springframework.cloud:spring-cloud-starter-gateway")
	implementation("org.springframework.boot:spring-boot-starter-actuator")

	// Security
	implementation("org.springframework.boot:spring-boot-starter-oauth2-client") // oauth2 (authenticate with keycloak)
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server") // oauth2 (store user’s data and http services which can return user data to authenticated clients.)
//	implementation("io.jsonwebtoken:jjwt:0.9.1") // parsing jwt token
//	implementation("javax.xml.bind:jaxb-api:2.3.1") // parsing jwt token
//	implementation("org.keycloak:keycloak-core") // using Keycloak interfaces

	// Kotlin
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	// Testing
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:2020.0.3")
//		mavenBom("org.keycloak.bom:keycloak-adapter-bom:11.0.2")
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
