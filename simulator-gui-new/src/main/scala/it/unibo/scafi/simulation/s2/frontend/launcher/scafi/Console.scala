package it.unibo.scafi.simulation.s2.frontend.launcher.scafi

import it.unibo.scafi.simulation.s2.frontend.configuration.parser.ConfigurationMachine
import it.unibo.scafi.simulation.s2.frontend.configuration.parser.RuntimeMachine
import it.unibo.scafi.simulation.s2.frontend.controller.logger.LogManager

import scala.io.StdIn

/**
 * a scafi program launcher via console with unix like language to run a simulation in console you can type: <pre>
 * {@code random-world 1000 500 500 radius-simulation Simple 10 launch } </pre>
 */
object Console extends App {
  import it.unibo.scafi.simulation.s2.frontend.configuration.launguage.ResourceBundleManager._
  import it.unibo.scafi.simulation.s2.frontend.controller.logger.LogManager._
  import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.ScafiInformation._
  val configurationMachine = new ConfigurationMachine(UnixConfiguration)
  val runtimeMachine = new RuntimeMachine(UnixRuntime)
  println(international("welcome")(KeyFile.Configuration))
  val log = new ConsoleOutputObserver
  LogManager.attach(log)
  while (!configurationBuilder.created)
    LogManager.notify(StringLog(Channel.CommandResult, Label.Empty, configurationMachine.process(StdIn.readLine())))
  LogManager.detach(log)
  while (true)
    println(runtimeMachine.process(StdIn.readLine()))
}
