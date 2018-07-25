package it.unibo.scafi.simulation.gui.launcher.scafi

object ConsoleProgramBuilder extends App {
  private val launch = false
  println("Welcome!")
  while(!launch) {

    val word = readLine()
    println(word)
  }
}
