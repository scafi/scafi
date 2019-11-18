plugins {
    id("org.openjfx.javafxplugin") version "0.0.8"
}

repositories {
    jcenter()
    mavenCentral()
    maven { url = uri("http://mvnrepository.com") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/releases") }
}

val javaFXModules = listOf("base", "controls", "graphics", "media", "swing", "web")

val javafxVersion: String by project

val javaVersion = System.getProperty("java.version").removePrefix("openjdk")
val jdkVersion = javaVersion.split('.').stream().findFirst().orElse(if(javaVersion.isEmpty()) "11" else javaVersion)
val os = System.getProperty("os.name")
val osName = when {
    os.startsWith("Linux") -> "linux"
    os.startsWith("Mac") -> "mac"
    os.startsWith("Windows") -> "win"
    else -> throw Exception("Unknown platform!")
}

javafx {
    version = jdkVersion
    modules = listOf("javafx.base", "javafx.controls", "javafx.graphics", "javafx.media", "javafx.swing", "javafx.web")
}