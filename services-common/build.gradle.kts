dependencies {
    // Common 모듈에서만 필요한 의존성
    api("org.springframework.boot:spring-boot-starter")
    api("org.springframework.boot:spring-boot-starter-validation")
    api("org.jetbrains.kotlin:kotlin-reflect")
    api("org.jetbrains.kotlin:kotlin-stdlib")
    api("com.fasterxml.jackson.module:jackson-module-kotlin")
}

// Common 모듈은 실행 가능한 jar를 생성하지 않음
tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
    archiveClassifier = ""
}