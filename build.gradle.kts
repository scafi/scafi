plugins {
    idea
    scala
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.adtran:scala-multiversion-plugin:1.+")
    }
}

val scalaSuffix: String by project

val bcelVersion: String by project
val akkaVersion: String by project
val scalaTestVersion: String by project
val scoptVersion: String by project
val shapelessVersion: String by project
val playJsonVersion: String by project
val scalafxVersion: String by project
val slf4jlog4Version: String by project
val log4Version: String by project

val akkaActor  = "com.typesafe.akka:akka-actor_%%:$akkaVersion"
val akkaRemote = "com.typesafe.akka:akka-remote_%%:$akkaVersion"
val bcel       = "org.apache.bcel:bcel:$bcelVersion"
val scalatest  = "org.scalatest:scalatest_%%:$scalaTestVersion"
val scopt      = "com.github.scopt:scopt_%%:$scoptVersion"
val shapeless  = "com.chuusai:shapeless_%%:$shapelessVersion"
val playJson   = "com.typesafe.play:play-json_%%:$playJsonVersion"
val scalafx = "org.scalafx:scalafx_%%:$scalafxVersion"
val slf4jlog4  = "org.slf4j:slf4j-log4j12:$slf4jlog4Version"
val log4 = "log4j:log4j:$log4Version"

val javaFXModules = listOf("base", "controls", "graphics", "media", "swing", "web")

val javaVersion = System.getProperty("java.version").removePrefix("openjdk")
val jdkVersion = javaVersion.split('.').stream().findFirst().orElse(if(javaVersion.isEmpty()) "11" else javaVersion)
val os = System.getProperty("os.name")
val osName = when {
    os.startsWith("Linux") -> "linux"
    os.startsWith("Mac") -> "mac"
    os.startsWith("Windows") -> "win"
    else -> throw Exception("Unknown platform!")
}

allprojects {
    apply(plugin = "scala")
    apply(plugin = "com.adtran.scala-multiversion-plugin")
    group = "it.unibo.apice.scafiteam"
    version = ""

    repositories {
        jcenter()
    }

    dependencies {
        "implementation"("org.scala-lang:scala-library:%scala-version%")
        "testImplementation"("org.scalatest:scalatest_%%:${scalaTestVersion}")
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

project(":scafi-commons") {
}

project(":scafi-core") {
    dependencies {
        "implementation"(project(":scafi-commons"))
    }

    project.the<SourceSetContainer>()["main"].withConvention(ScalaSourceSet::class) {
        scala {
            srcDirs(listOf("src/main/scala", "src/main/scala" + scalaSuffix.replace("_", "-")))
        }
    }
}

project(":scafi-stdlib-ext") {
    dependencies {
        "implementation"(project(":scafi-commons"))
        "implementation"( project(":scafi-core"))
        "implementation"(shapeless)
    }
}

project(":scafi-simulator") {
    dependencies {
        "implementation"(project(":scafi-commons"))
        "implementation"( project(":scafi-core"))
    }
}

project(":scafi-simulator-gui") {
    dependencies {
        "implementation"(project(":scafi-commons"))
        "implementation"(project(":scafi-core"))
        "implementation"(project(":scafi-simulator"))
        "implementation"(scopt)
    }
}

project(":scafi-tests") {
    dependencies {
        "implementation"(project(":scafi-commons"))
        "implementation"(project(":scafi-core"))
        "implementation"(project(":scafi-simulator"))
    }
}

project(":scafi-simulator-gui-new") {
    dependencies {
        "implementation"(project(":scafi-commons"))
        "implementation"(project(":scafi-core"))
        "implementation"(project(":scafi-simulator"))
        "implementation"(project(":spala"))
        "implementation"(project(":scafi-distributed"))
        "implementation"(akkaActor)
        "implementation"(scalafx)
        "implementation"(playJson)

        /*
        "implementation"(scalafx)
        if(Integer.parseInt(jdkVersion).getOrElse(0) >= 11) {
            javaFXModules.forEach { m ->
                "implementation"("org.openjfx:javafx-$m:$jdkVersion$classifier$osName")
            }
        }
         */
    }
}

project(":spala") {
    dependencies {
        "implementation"(project(":scafi-commons"))
        "implementation"(project(":scafi-core"))
        "implementation"(akkaActor)
        "implementation"(akkaRemote)
        "implementation"(bcel)
        "implementation"(scopt)
        "implementation"(playJson)
        "implementation"(slf4jlog4)
        "implementation"(log4)
    }
}

project(":scafi-distributed") {
    dependencies {
        "implementation"(project(":scafi-commons"))
        "implementation"(project(":scafi-core"))
        "implementation"(project(":spala"))
        "implementation"(akkaActor)
        "implementation"(playJson)
        "implementation"(scopt)
    }
}

project(":scafi-demos") {
    dependencies {
        "implementation"(project(":scafi-commons"))
        "implementation"(project(":scafi-core"))
        "implementation"(project(":scafi-stdlib-ext"))
        "implementation"(project(":spala"))
        "implementation"(project(":scafi-distributed"))
        "implementation"(project(":scafi-simulator"))
        "implementation"(project(":scafi-simulator-gui"))
        "implementation"(akkaActor)
        "implementation"(scopt)
        "implementation"(playJson)
    }
}

project(":scafi-demos-new") {
    dependencies {
        "implementation"(project(":scafi-commons"))
        "implementation"(project(":scafi-core"))
        "implementation"(project(":scafi-stdlib-ext"))
        "implementation"(project(":spala"))
        "implementation"(project(":scafi-distributed"))
        "implementation"(project(":scafi-simulator"))
        "implementation"(project(":scafi-simulator-gui-new"))
        "implementation"(akkaActor)
        "implementation"(scopt)
        "implementation"(playJson)
        "implementation"(scalafx)
    }
}
