package it.unibo.scafi.simulation.gui.launcher.scafi

import it.unibo.scafi.simulation.gui.configuration.parser.ConfigurationMachine
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.ScafiParser

/**
  * string launcher used to launch simulation via string
  */
object StringLauncher {
  def apply(string : String): Unit = {
    import ScafiParser._
    val configurationMachine = new ConfigurationMachine(UnixConfiguration)
    string.split(";") foreach {configurationMachine.process(_)}
    configurationMachine.process("launch")
  }
}
