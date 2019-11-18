plugins {
    id("org.openjfx.javafxplugin") version "0.0.8"
}

repositories {
    jcenter()
    mavenCentral()
    maven { url = uri("http://mvnrepository.com") }
}

val javaFXModules = listOf("base", "controls", "graphics", "media", "swing", "web")

val javafxVersion: String by project

javafx {
    version = javafxVersion
    modules = listOf("javafx.base", "javafx.controls", "javafx.graphics", "javafx.media", "javafx.swing", "javafx.web")
}