package it.unibo.scafi.simulation.s2.frontend.launcher.scafi

import java.nio.file.Files
import java.nio.file.Paths
import scala.collection.JavaConverters._

/**
 * allow to launch simulation by reading file passed the file must have commad write like unix like to separate command
 * you must use semicolon
 */
object FileLauncher {
  def apply(path: String): Unit = {
    val javaListCommand = Files.readAllLines(Paths.get(path))
    val command = JavaToScalaConversions.toScalaBuffer(javaListCommand).mkString(";")
    StringLauncher(command)
  }
}

object JavaToScalaConversions {
  def toScalaBuffer[A](list: java.util.List[A]): collection.mutable.Buffer[A] =
    list.asScala
}
