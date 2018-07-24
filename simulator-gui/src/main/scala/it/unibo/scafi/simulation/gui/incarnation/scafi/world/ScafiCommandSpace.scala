package it.unibo.scafi.simulation.gui.incarnation.scafi.world

import it.unibo.scafi.simulation.gui.configuration.CommandSpace
import it.unibo.scafi.simulation.gui.controller.input.Command
import it.unibo.scafi.simulation.gui.model.sensor.SensorConcept.SensorDevice
import it.unibo.scafi.simulation.gui.model.space.Point3D

/**
  * a place used to defined command from scafi framework
  */
object ScafiCommandSpace extends CommandSpace {

  /**
    * a command used to move a set of id to another position in a scafi world
    * @param ids the id to move
    */
  case class MoveCommand(ids : Map[Any, Point3D]) extends Command {
    var oldPos : Map[ScafiWorld.ID,Point3D] = Map()
    override def make(): Unit = ids.foreach(x => {
      x._1 match {
        case id : ScafiWorld.ID => {
          oldPos += id -> ScafiWorld(id).get.position
          ScafiWorld.moveNode(id,x._2)
        }

        case _ =>
      }
    })

    override def unmake(): Unit = oldPos foreach { x => ScafiWorld.moveNode(x._1,x._2)}

    override def buildFromString(string: String): Option[Command] = None
  }

  /**
    * switch on or off a sensor in a scafi world
    * @param ids the set of node id
    * @param name the name of device
    */
  case class ToggleDeviceCommand(ids : Set[Any], name : Any) extends Command {
    private def toggleDevice(): Unit =  name match {
      case name : ScafiWorld.NAME => {
        ids foreach {
          x => x match {
            case id : ScafiWorld.ID => {
              val node = ScafiWorld(id)
              if(node.isDefined) {
                val dev = node.get.getDevice(name)
                dev.get match {
                  case SensorDevice(sens) => sens.value match {
                    case led: Boolean => ScafiWorld.changeSensorValue(id, name, !led)
                    case _ =>
                  }
                }

              }
            }
            case _ =>
          }
        }
      }
      case _ =>
    }


    override def make(): Unit = toggleDevice()

    override def unmake(): Unit = toggleDevice()

    override def buildFromString(string: String): Option[Command] = None
  }

  override def fromString(string: String): Option[Command] = None
}
