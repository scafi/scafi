package it.unibo.scafi.simulation.gui.incarnation.scafi.world

import it.unibo.scafi.simulation.gui.configuration.command.factory.WorldTypeAnalyzer
import it.unibo.scafi.simulation.gui.model.common.world.WorldDefinition.World3D
import it.unibo.scafi.simulation.gui.model.simulation.PlatformDefinition.SensorPlatform
import it.unibo.scafi.simulation.gui.model.simulation.implementation.StandardNetwork
import it.unibo.scafi.simulation.gui.model.simulation.implementation.mutable.{SensorDefinition, StandardNodeDefinition}
import it.unibo.scafi.simulation.gui.model.space.Point3D

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

object ScafiLikeWorld {
  /**
    * scafi runtime analyzer, it is used to verify the correctness of scafi parameter type at run time
    * it used for example by {@see it.unibo.scafi.simulation.gui.configuration.command.factory.MultiMoveCommandFactory}
    */
  implicit val analyzer: WorldTypeAnalyzer = new WorldTypeAnalyzer {
    override def acceptName(name: Any): Boolean = name match {
      case _ : scafiWorld.NAME => true
      case _ => false
    }

    override def acceptId(id: Any): Boolean = id match {
      case _ : scafiWorld.ID => true
      case _ => false
    }
  }
}