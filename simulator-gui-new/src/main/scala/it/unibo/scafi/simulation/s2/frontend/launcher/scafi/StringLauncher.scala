package it.unibo.scafi.simulation.s2.frontend.launcher.scafi

import it.unibo.scafi.simulation.s2.frontend.configuration.parser.ConfigurationMachine
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.ScafiInformation

/**
 * string launcher used to launch simulation via string the file must have commad write like unix like to separate
 * command you must use semicolon
 */
object StringLauncher {
  def apply(string: String): Unit = {
    import ScafiInformation._
    val configurationMachine = new ConfigurationMachine(UnixConfiguration)
    string.split(";") foreach { configurationMachine.process(_) }
    configurationMachine.process("launch")
  }
}
