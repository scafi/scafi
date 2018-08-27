package it.unibo.scafi.simulation.gui.incarnation.scafi.world

import it.unibo.scafi.simulation.gui.configuration.command.factory.WorldTypeAnalyzer
import it.unibo.scafi.simulation.gui.model.common.world.WorldDefinition.World3D
import it.unibo.scafi.simulation.gui.model.simulation.PlatformDefinition.SensorPlatform
import it.unibo.scafi.simulation.gui.model.simulation.implementation.mutable.{SensorDefinition, StandardNodeDefinition}
import it.unibo.scafi.simulation.gui.model.space.Point3D

/**
  * a world describe a scafi platform
  */
trait ScafiLikeWorld extends SensorPlatform with World3D with SensorDefinition with StandardNodeDefinition {
  override type ID = Int
  override type NAME = String
  override type P = Point3D
}

object ScafiLikeWorld {
  /**
    * scafi runtime analyzer
    */
  implicit val analyzer: WorldTypeAnalyzer = new WorldTypeAnalyzer {
    override def acceptName(name: Any): Boolean = name match {
      case name : scafiWorld.NAME => true
      case _ => false
    }

    override def acceptId(id: Any): Boolean = id match {
      case id : scafiWorld.ID => true
      case _ => false
    }
  }
}