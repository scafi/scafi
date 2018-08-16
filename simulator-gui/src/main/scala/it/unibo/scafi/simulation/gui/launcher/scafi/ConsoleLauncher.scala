package it.unibo.scafi.simulation.gui.launcher.scafi

import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.ScafiLanguage
import it.unibo.scafi.simulation.gui.launcher.MetaLanguage

/**
  * allow to launch scafi simulation
  */
object ConsoleLauncher {
  new MetaLanguage(ScafiLanguage.configurationLanguage,ScafiLanguage.config).run()
}
