package it.unibo.scafi.simulation.gui.launcher.scafi

import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.DemoCommandFactory.DemoFieldParser
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.GridCommandFactory.GridFieldParser
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.RandomCommandFactory.RandomFieldParser
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.{DemoCommandFactory, GridCommandFactory, RandomCommandFactory, ScafiLanguage}
import it.unibo.scafi.simulation.gui.launcher.MetaFieldLauncher

object FieldLauncher extends MetaFieldLauncher (
    section = Map(
      DemoFieldParser -> new DemoCommandFactory(ScafiLanguage.config)
    ),
  subsection = Map("world initializer" -> Map(RandomFieldParser -> new RandomCommandFactory(ScafiLanguage.config),
                                              GridFieldParser -> new GridCommandFactory(ScafiLanguage.config))),
  launchStrategy = () => ScafiLanguage.configurationLanguage.parse(ScafiLanguage.launch)
  ){

}
