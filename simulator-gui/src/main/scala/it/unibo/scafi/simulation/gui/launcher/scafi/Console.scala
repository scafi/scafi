package it.unibo.scafi.simulation.gui.launcher.scafi

import it.unibo.scafi.simulation.gui.configuration.parser.{ConfigurationMachine, RuntimeMachine}
import it.unibo.scafi.simulation.gui.controller.logger.LogManager
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.command._

/**
  * a scafi program launcher via console
  */
private object Console extends App {
  import ScafiParser._
  import it.unibo.scafi.simulation.gui.configuration.launguage.ResourceBundleManager._
  import it.unibo.scafi.simulation.gui.controller.logger.LogManager._
  val configurationMachine = new ConfigurationMachine(UnixConfiguration)
  val runtimeMachine = new RuntimeMachine(UnixRuntime)
  println(international("welcome")(KeyFile.Configuration))
  val log = new ConsoleOutputObserver
  LogManager.attach(log)
  while(!scafiConfiguration.created){
    LogManager.notify(StringLog(Channel.CommandResult,Label.Empty,configurationMachine.process(readLine())))
  }
  LogManager.detach(log)
  while(true) {
    println(runtimeMachine.process((readLine())))
  }
}
