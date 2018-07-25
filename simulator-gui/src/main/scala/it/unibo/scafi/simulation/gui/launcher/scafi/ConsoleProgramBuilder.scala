package it.unibo.scafi.simulation.gui.launcher.scafi

import it.unibo.scafi.simulation.gui.util.ClassFinder

object ConsoleProgramBuilder extends App {
  private val launch = false
  println(ClassFinder.getClasses("it.unibo.scafi.simulation.gui"))
}
