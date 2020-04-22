package it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.world

import it.unibo.scafi.simulation.s2.frontend.model.common.world.WorldDefinition.World3D
import it.unibo.scafi.simulation.s2.frontend.model.simulation.PlatformDefinition.SensorPlatform
import it.unibo.scafi.simulation.s2.frontend.model.simulation.implementation.StandardNetwork
import it.unibo.scafi.simulation.s2.frontend.model.simulation.implementation.mutable.{SensorDefinition, StandardNodeDefinition}

/**
  * scafi world definition
  */
trait ScafiLikeWorld extends SensorPlatform
  with World3D
  with SensorDefinition
  with StandardNodeDefinition
  with StandardNetwork {

  override type ID = Int
  override type NAME = String
}