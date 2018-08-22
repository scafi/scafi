package it.unibo.scafi.simulation.gui.launcher.scafi

import it.unibo.scafi.simulation.gui.configuration.parser.{ConfigurationMachine, RuntimeMachine}
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.command._

/**
  * a scafi program launcher via console
  */
object Console extends App {
  import ScafiParser._

  val configurationMachine = new ConfigurationMachine(UnixConfiguration)
  val runtimeMachine = new RuntimeMachine(UnixRuntime)
  println("welcome")
  while(!scafiConfiguration.created){
    println(configurationMachine.process(readLine()))
  }
  while(true) {
    println(runtimeMachine.process((readLine())))
  }
}
