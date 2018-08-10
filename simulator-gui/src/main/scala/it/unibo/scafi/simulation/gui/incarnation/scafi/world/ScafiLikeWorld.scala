package it.unibo.scafi.simulation.gui.incarnation.scafi.world

import it.unibo.scafi.simulation.gui.configuration.command.WorldCommandSpace
import it.unibo.scafi.simulation.gui.configuration.command.WorldCommandSpace.WorldTypeAcceptor
import it.unibo.scafi.simulation.gui.controller.input.Command
import it.unibo.scafi.simulation.gui.controller.input.Command.CommandDescription
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
    * a description of a scafi world space
    */
  object scafiWorldCommandSpace extends WorldCommandSpace[ScafiLikeWorld] {
    //scafi world
    val world: ScafiLikeWorld = scafiWorld
    /**
      * the description of move command in scafi world
      */
    val moveDescription = new CommandDescription("use move id->x,y,z to move the node with the id specified to the position",
    "Move command allow to move selected id to another position") {
      override def parseFromString(command: String): Option[Command] = {
        val regex = raw"move (\d)->(\d+)\,(\d+),(\d+)".r
        command match {
          case regex(id,x,y,z) => Some(MoveCommand(Map(id.toInt -> Point3D(x.toInt,y.toInt,z.toInt))))
          case _=> None
        }
      }
    }
    /**
      * the description of toggle device in the scafi world
      */
    val toggleDescritpion = new CommandDescription("use toggle id,name to toggle the state of a on off sensor",
      "Toggle command allow to toggle the state of a on off sensor"){

      override def parseFromString(command: String): Option[Command] = {
        val regex = raw"toggle (\d+)\,(.*)".r
        command match {
          case regex(id,name) => Some(ToggleDeviceCommand(Set(id.toInt),name.toString))
          case _=> None
        }
      }
    }
    override val descriptors: List[Command.CommandDescription] = List(moveDescription,toggleDescritpion)
    /**
      * the acceptor policy in scafi world
      */
    override val acceptor: WorldTypeAcceptor = new WorldTypeAcceptor {
      override def acceptName(name: Any): Boolean = name match {
        case name : world.NAME => true
        case _ => false
      }

      override def acceptId(id: Any): Boolean = id match {
        case id : world.ID => true
        case _ => false
      }
    }
  }
}