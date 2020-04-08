import org.openjfx.gradle.JavaFXOptions
import de.marcphilipp.gradle.nexus.NexusPublishExtension

plugins {
    idea
    scala
    id("com.github.alisiikh.scalastyle") version "3.1.0"
    id("de.marcphilipp.nexus-publish") version "0.3.0"
    signing
    id("com.palantir.git-version") version "0.12.2"
}

val gitVersion : groovy.lang.Closure<Any> by extra
val versionDetails : groovy.lang.Closure<Any> by extra
val verDetails = versionDetails()

// Set project version
version = gitVersion()

buildscript {
    repositories {
        jcenter()
        mavenCentral()
    }
    dependencies {
        classpath("com.adtran:scala-multiversion-plugin:1.+")
        classpath("org.openjfx:javafx-plugin:0.0.8")
    }
}

/*
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
 */

tasks.withType<ScalaCompile>().configureEach {
    options.apply {
        targetCompatibility = JavaVersion.VERSION_1_8.toString()
        sourceCompatibility = JavaVersion.VERSION_1_8.toString()
    }
}

val scalaSuffix: String by project
val scalaVersion: String by project

val bcelVersion: String by project
val akkaVersion: String by project
val scalaTestVersion: String by project
val scoptVersion: String by project
val shapelessVersion: String by project
val playJsonVersion211: String by project
val playJsonVersion212: String by project
val scalafxVersion212: String by project
val scalafxVersion213: String by project
val slf4jlog4Version: String by project
val log4Version: String by project
val scalaLoggingVersion: String by project

val akkaActor  = "com.typesafe.akka:akka-actor_%%:$akkaVersion"
val akkaRemote = "com.typesafe.akka:akka-remote_%%:$akkaVersion"
val bcel       = "org.apache.bcel:bcel:$bcelVersion"
val scalatest  = "org.scalatest:scalatest_%%:$scalaTestVersion"
val scopt      = "com.github.scopt:scopt_%%:$scoptVersion"
val shapeless  = "com.chuusai:shapeless_%%:$shapelessVersion"
val playJson211 = "com.typesafe.play:play-json_%%:$playJsonVersion211"
val playJson212 = "com.typesafe.play:play-json_%%:$playJsonVersion212"
val scalafx212 = "org.scalafx:scalafx_%%:$scalafxVersion212"
val scalafx213 = "org.scalafx:scalafx_%%:$scalafxVersion213"
val slf4jlog4  = "org.slf4j:slf4j-log4j12:$slf4jlog4Version"
val log4 = "log4j:log4j:$log4Version"
val scalaLogging  = "com.typesafe.scala-logging:scala-logging_%%:$scalaLoggingVersion"

val javaFXModules = kotlin.collections.listOf("base", "controls", "graphics", "media", "swing", "web")

val javafxVersion: kotlin.String by project

fun javaVersion(): Int {
    var version = System.getProperty("java.version")
    val orig = version
    if (version.startsWith("1.")) {
        version = version.substring(2, 3)
    } else {
        val dot = version.indexOf(".")
        if (dot != -1) {
            version = version.substring(0, dot)
        }
    }
    println("Java version: extracted $version from $orig")
    return Integer.parseInt(version)
}

val jdkVersion = javaVersion()
val os = System.getProperty("os.name")
val osName = when {
    os.startsWith("Linux") -> "linux"
    os.startsWith("Mac") -> "mac"
    os.startsWith("Windows") -> "win"
    else -> throw Exception("Unknown platform!")
}

allprojects {
    apply(plugin = "scala")
    apply(plugin = "java-library")
    apply(plugin = "com.adtran.scala-multiversion-plugin")

    group = "it.unibo.apice.scafiteam"

    repositories {
        jcenter()
    }

    dependencies {
        "implementation"("org.scala-lang:scala-library:%scala-version%")
        "testImplementation"("org.scalatest:scalatest_%%:${scalaTestVersion}")
    }

    if(!listOf("scafi-demos","scafi-demos-new","scafi-demos-distributed").contains(project.name)) {
        apply(plugin = "com.github.alisiikh.scalastyle")

        scalastyle {
            setConfig(File("${project.rootProject.rootDir}/scalastyle-config.xml"))
            //includeTestSourceDirectory = true
            //source = "src/main/scala"
            //testSource = "src/test/scala"
            //failOnViolation = true // default
            //failOnWarning = false // default
            //quiet = true
        }
    }

    tasks.register<JavaExec>("scalaTest"){
        main = "org.scalatest.tools.Runner"
        args("-R", "build/classes/scala/test", "-o")
        classpath(sourceSets["test"].runtimeClasspath)
    }

    tasks {
        "test" { dependsOn("scalaTest") }
    }
}

/*
nexusStaging {
    packageGroup = "no.nav"
    username = System.getenv("SONATYPE_USER")
    password = System.getenv("SONATYPE_PASSWORD")
}
*/

subprojects {
    apply(plugin = "de.marcphilipp.nexus-publish")
    apply(plugin = "signing")

    /*
    configure<NexusPublishExtension> {
        username.set(System.getenv("SONATYPE_USER"))
        password.set(System.getenv("SONATYPE_PASSWORD"))
    }
     */

    if(listOf("spala", "scafi-distributed", "scafi-demos", "scafi-demos-new").contains(project.name) && scalaVersion.startsWith("2.13")){
        // TODO: disable this project
    }

    if(!listOf("scafi-demos","scafi-demos-new","scafi-tests").contains(project.name)){
        extra["signing.keyId"] = "D5FA9509"
        extra["signing.secretKeyRingFile"] = File("${project.rootProject.rootDir}/.travis/local.secring.asc")
        extra["signing.password"] = System.getenv("PGP_PASS")

        publishing {
            publications {
                create<MavenPublication>("scafi") {
                    groupId = "${project.group}"
                    artifactId = "${project.name}"
                    version = "${project.version}"
                    from(components["java"])

                    pom {
                        name.set("ScaFi")
                        description.set("An aggregate programming toolkit on the JVM")
                        url.set("https://scafi.github.io")
                        licenses {
                            license {
                                name.set("Apache-2.0")
                                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                            }
                        }
                        developers {
                            developer {
                                id.set("metaphori")
                                name.set("Roberto Casadei")
                                email.set("roby.casadei@unibo.it")
                                url.set("https://metaphori.github.io")
                            }
                            developer {
                                id.set("mviroli")
                                name.set("Mirko Viroli")
                                email.set("mirko.viroli@unibo.it")
                                url.set("http://mirkoviroli.apice.unibo.it")
                            }
                        }
                        scm {
                            connection.set("scm:git:git@github.org:scafi/scafi.git")
                            developerConnection.set("scm:git:ssh@github.org:scafi/scafi.git")
                            url.set("https://github.com/scafi/scafi")
                        }
                    }
                }
            }
        }
        nexusPublishing {
            repositories {
                sonatype {
                    username.set(System.getenv("SONATYPE_USER"))
                    password.set(System.getenv("SONATYPE_PASSWORD"))
                }
            }
        }
        signing {
            sign(publishing.publications["scafi"])
        }
    }
}

project(":scafi-commons") {
}

project(":scafi-core") {
    sourceSets {
        main {
            withConvention(ScalaSourceSet::class) {
                scala {
                    srcDirs(listOf(
                            "${project.projectDir}/src/main/scala",
                            "${project.projectDir}/src/main/scala" + scalaSuffix.replace("_", "-")))
                }
            }
        }
    }

    dependencies {
        "api"(project(":scafi-commons"))
    }
}

project(":scafi-stdlib-ext") {
    dependencies {
        "api"( project(":scafi-core"))
        "api"(shapeless)
    }
}

project(":scafi-simulator") {
    dependencies {
        //"implementation"(project(":scafi-commons"))
        "api"( project(":scafi-core"))
    }
}

project(":scafi-simulator-gui") {
    dependencies {
        "api"(project(":scafi-simulator"))
        "api"(project(":scafi-renderer-3d"))
        "api"(scopt)
    }
}

project(":scafi-tests") {
    dependencies {
        "api"(project(":scafi-simulator"))
    }
}

project(":scafi-simulator-gui-new") {
    dependencies {
        "api"(project(":scafi-simulator"))
        "api"(project(":scafi-distributed"))
        if(scalaVersion.startsWith("2.13")) {
            "api"(scalafx213)
        } else {
            "api"(scalafx212)
        }
    }
}

project(":scafi-renderer-3d") {
    dependencies {
        if(scalaVersion.startsWith("2.13")) {
            "api"(scalafx213)
        } else {
            "api"(scalafx212)
        }
        "api"(scalaLogging)
    }
}

project(":spala") {
    dependencies {
        "api"(project(":scafi-core"))
        "api"(akkaActor)
        "api"(akkaRemote)
        "api"(bcel)
        "api"(scopt)
        if(scalaVersion.startsWith("2.11")) {
            "api"(playJson211)
        } else {
            "api"(playJson212)
        }
        "api"(slf4jlog4)
        "api"(log4)
    }
}

project(":scafi-distributed") {
    dependencies {
        "api"(project(":spala"))
    }
}

project(":scafi-demos") {
    dependencies {
        "implementation"(project(":scafi-stdlib-ext"))
        "implementation"(project(":scafi-simulator-gui"))
        "implementation"(project(":scafi-distributed"))
    }
}

project(":scafi-demos-new") {
    dependencies {
        "implementation"(project(":scafi-stdlib-ext"))
        "implementation"(project(":scafi-simulator-gui-new"))
        "implementation"(project(":scafi-distributed"))
    }
}

project(":scafi-demos-distributed") {
    dependencies {
        "implementation"(project(":scafi-stdlib-ext"))
        "implementation"(project(":scafi-distributed"))
    }
}

configure(subprojects.filter { listOf("scafi-simulator-gui", "scafi-simulator-gui-new", "scafi-demos-new", "scafi-renderer-3d").contains(it.name) }) {
    if(jdkVersion>8) {
        apply(plugin = "org.openjfx.javafxplugin")

        repositories {
            jcenter()
            mavenCentral()
            maven { url = uri("http://mvnrepository.com") }
            maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
            maven { url = uri("https://oss.sonatype.org/content/repositories/releases") }
        }

        /*javafx*/
        configure<JavaFXOptions> {
            println("Configure JavaFX with JDK version $jdkVersion")
            version = "$jdkVersion"
            modules = listOf("javafx.base", "javafx.controls", "javafx.graphics", "javafx.media", "javafx.swing", "javafx.web")
        }
    }
}

tasks.register<Jar>("fatJar") {
    dependsOn(subprojects.map { it.tasks.withType<Jar>() })
    manifest {
        attributes(mapOf(
                "Implementation-Title" to "${project.name}",
                "Implementation-Version" to project.version
                // "Main-Class" to "it.unibo.alchemist.Alchemist",
                // "Automatic-Module-Name" to "it.unibo.scafi"
        ))
    }
    archiveBaseName.set("${rootProject.name}-redist")
    isZip64 = true
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }) {
        // remove all signature files
        exclude("META-INF/")
        exclude("build")
        exclude(".gradle")
        exclude("build.gradle")
        exclude("gradle")
        exclude("gradlew")
        exclude("gradlew.bat")
    }
    with(tasks.jar.get() as CopySpec)
}

fun makeMain(name: String, projectName: String, klass: String) {
    project(projectName) {
        task<JavaExec>("$name") {
            classpath = sourceSets["main"].runtimeClasspath
            main = "$klass"
        }
    }
}

makeMain("demos", ":scafi-demos", "sims.DemoLauncher")
makeMain("newdemos", ":scafi-demos-new", "frontend.sims.standard.DISIDemo")