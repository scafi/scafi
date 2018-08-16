package it.unibo.scafi.simulation.gui.incarnation.scafi.configuration

import it.unibo.scafi.simulation.gui.configuration.Configuration
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiSimulationInitializer
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiWorldInitializer

/**
  * scafi configuration used to create a scafi program
  * @param worldInitializer world initializer used to initialize the world
  * @param worldSeed a seed used to set some parameter
  * @param launched describe if program is launched or not
  * @param demo the class of scafi demo
  * @param scafiSimulationInitializer simulation initializer used to create a scafi bridge simulation
  */
class ScafiConfiguration(var worldInitializer: Option[ScafiWorldInitializer] = None,
                          var worldSeed: Option[ScafiSeed] = Some(ScafiSeed()),
                          var launched: Boolean = false,
                          var demo : Option[Class[_]] = None,
                          var scafiSimulationInitializer : Option[ScafiSimulationInitializer] = None
                        ) extends Configuration[ScafiSeed,ScafiWorldInitializer]{

}
