package it.unibo.scafi.simulation.gui.launcher.scafi

import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration._
import it.unibo.scafi.simulation.gui.launcher.MetaLanguage

/**
  * allow to run scafi simulation and run some runtime command
  */
object Console extends App {
  new MetaLanguage(ScafiLanguage.configurationLanguage,ScafiLanguage.config,Some(ScafiLanguage.runtimeLanguage)).run()
}
