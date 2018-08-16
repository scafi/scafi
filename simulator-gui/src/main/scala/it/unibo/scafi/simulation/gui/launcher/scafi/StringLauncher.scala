package it.unibo.scafi.simulation.gui.launcher.scafi

import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.ScafiLanguage

object StringLauncher {
  val lang = ScafiLanguage.configurationLanguage
  def apply(commands : String): Unit = {
    lang.parse(commands)
    println(lang.parse(ScafiLanguage.launch))
  }
}
