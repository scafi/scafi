package it.unibo.scafi.simulation.s2.frontend.launcher.scafi

import java.nio.file.{Files, Paths}

import scala.collection.JavaConversions

object JavaToScalaConversions {
  def toScalaBuffer[A](lst: java.util.List[A]): collection.mutable.Buffer[A] =
    JavaConversions.asScalaBuffer(lst)
}