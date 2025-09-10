dependencies {
    implementation(project(":services-common"))
    
    // Redis for queue management
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    
    // WebSocket for real-time notifications
    implementation("org.springframework.boot:spring-boot-starter-websocket")
}