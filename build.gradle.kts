import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "2.2.0" apply false
	kotlin("plugin.spring") version "2.2.0" apply false
	id("org.springframework.boot") version "3.4.1" apply false
	id("io.spring.dependency-management") version "1.1.7" apply false
}

allprojects {
	group = "com.yourssu.morupark"
	version = "0.0.1-SNAPSHOT"

	repositories {
		mavenCentral()
		maven { url = uri("https://repo.spring.io/snapshot") }
	}
}

subprojects {
	apply(plugin = "kotlin")
	apply(plugin = "kotlin-spring")
	apply(plugin = "org.springframework.boot")
	apply(plugin = "io.spring.dependency-management")

	configure<JavaPluginExtension> {
		toolchain {
			languageVersion = JavaLanguageVersion.of(21)
		}
	}

	dependencies {
		"implementation"("org.springframework.boot:spring-boot-starter")
		"implementation"("org.springframework.boot:spring-boot-starter-web")
		"implementation"("org.springframework.boot:spring-boot-starter-data-jpa")
		"implementation"("org.springframework.boot:spring-boot-starter-validation")
		"implementation"("org.jetbrains.kotlin:kotlin-reflect")
		"implementation"("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
		"implementation"("com.fasterxml.jackson.module:jackson-module-kotlin")
		
		// Kafka
		"implementation"("org.springframework.kafka:spring-kafka")
		
		// Database
		"runtimeOnly"("com.h2database:h2")
		"runtimeOnly"("mysql:mysql-connector-java:8.0.33")
		
		// Test
		"testImplementation"("org.springframework.boot:spring-boot-starter-test")
		"testImplementation"("org.jetbrains.kotlin:kotlin-test-junit5")
		"testImplementation"("org.springframework.kafka:spring-kafka-test")
		"testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
	}

	tasks.withType<KotlinCompile> {
		compilerOptions {
			freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
		}
	}

	tasks.withType<Test> {
		useJUnitPlatform()
	}
}