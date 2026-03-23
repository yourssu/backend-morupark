pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}

rootProject.name = "morupark"

include(
    "services-auth",
    "services-queue",
    "services-api-gateway",
    "services-goods"
)