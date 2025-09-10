pluginManagement {
	repositories {
		maven { url = uri("https://repo.spring.io/snapshot") }
		gradlePluginPortal()
	}
}
rootProject.name = "morupark"

include("services-common")
include("services-auth") 
include("services-queue")
