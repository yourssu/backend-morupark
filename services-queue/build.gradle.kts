dependencies {
    implementation(project(":services-common"))
    
    // Redis for queue management
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    
    // WebSocket for real-time notifications
    implementation("org.springframework.boot:spring-boot-starter-websocket")

    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation("io.projectreactor:reactor-test")

    testImplementation("io.mockk:mockk:1.13.8")
}
