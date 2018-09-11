package it.unibo.scafi.simulation.gui.launcher.scafi

import java.nio.file.{Files, Paths}

import scala.collection.JavaConversions

/**
  * allow to launch simulation by reading file passed
  * the file must have commad write like unix like to
  * separate command you must use semicolon
  */
object FileLauncher {
  def apply(path : String) : Unit = {
    val javaListCommand = Files.readAllLines(Paths.get(path))
    val command = JavaConversions.asScalaBuffer(javaListCommand).mkString(";")
    println(command)
    StringLauncher{command}
  }
}
