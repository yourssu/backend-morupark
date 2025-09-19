dependencies {
    implementation(project(":services-common"))
    
    // Redis for queue management
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    
    // WebSocket for real-time notifications
    implementation("org.springframework.boot:spring-boot-starter-websocket")

    implementation("org.springframework.kafka:spring-kafka")

    // Webflux for webClient
    implementation("org.springframework.boot:spring-boot-starter-webflux")
}
