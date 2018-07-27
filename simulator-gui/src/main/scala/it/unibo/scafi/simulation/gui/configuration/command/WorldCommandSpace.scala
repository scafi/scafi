package it.unibo.scafi.simulation.gui.configuration.command

import it.unibo.scafi.simulation.gui.controller.input.Command
import it.unibo.scafi.simulation.gui.model.sensor.SensorConcept.SensorDevice
import it.unibo.scafi.simulation.gui.model.simulation.PlatformDefinition.SensorPlatform
import it.unibo.scafi.simulation.gui.model.space.Point3D

trait WorldCommandSpace[W <: SensorPlatform] extends CommandSpace{
  val world : W

  /**
    * a command used to move a set of id to another position in a scafi world
    * @param ids the id to move
    */
  case class MoveCommand(ids : Map[_ <: Any, Point3D]) extends Command {
    var oldPos : Map[world.ID,world.P] = Map()
    override def make(): Unit = ids.foreach(x => {
      x._1 match {
        case id : world.ID => {
          oldPos += id -> world(id).get.position
          world.moveNode(id,x._2.asInstanceOf[world.P])
        }

        case _ =>
      }
    })

    override def unmake(): Unit = oldPos foreach { x => world.moveNode(x._1,x._2)}

    override def buildFromString(string: String): Option[Command] = None

  }

  /**
    * switch on or off a sensor in a scafi world
    * @param ids the set of node id
    * @param name the name of device
    */
  case class ToggleDeviceCommand(ids : Set[_ <: Any], name : Any) extends Command {
    private def toggleDevice(): Unit =  name match {
      case name : world.NAME => {
        ids foreach {
          x => x match {
            case id : world.ID => {
              val node = world(id)
              if(node.isDefined) {
                val dev = node.get.getDevice(name)
                dev.get match {
                  case SensorDevice(sens) => sens.value match {
                    case led: Boolean => world.changeSensorValue(id, name, !led)
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
