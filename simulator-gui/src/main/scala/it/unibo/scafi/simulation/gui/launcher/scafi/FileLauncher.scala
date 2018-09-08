package it.unibo.scafi.simulation.gui.launcher.scafi

import java.nio.file.{Files, Paths}

import scala.collection.JavaConversions

/**
  * allow to launch simulation by reading file passed
  */
object FileLauncher {
  def apply(path : String) = {
    val javaListCommand = Files.readAllLines(Paths.get(path))
    val command = JavaConversions.asScalaBuffer(javaListCommand).mkString(";")
    println(command)
    StringLauncher{command}
  }
}
