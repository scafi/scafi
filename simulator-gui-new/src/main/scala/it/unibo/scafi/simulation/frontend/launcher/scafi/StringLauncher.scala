package it.unibo.scafi.simulation.frontend.launcher.scafi

import it.unibo.scafi.simulation.frontend.configuration.parser.ConfigurationMachine
import it.unibo.scafi.simulation.frontend.incarnation.scafi.configuration.ScafiInformation

/**
  * string launcher used to launch simulation via string
  * the file must have commad write like unix like to
  * separate command you must use semicolon
  */
object StringLauncher {
  def apply(string : String): Unit = {
    import ScafiInformation._
    val configurationMachine = new ConfigurationMachine(UnixConfiguration)
    string.split(";") foreach {configurationMachine.process(_)}
    configurationMachine.process("launch")
  }
}
