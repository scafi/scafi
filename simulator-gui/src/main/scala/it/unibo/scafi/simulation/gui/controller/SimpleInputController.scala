package it.unibo.scafi.simulation.gui.controller

import it.unibo.scafi.simulation.gui.controller.synchronization.Scheduler.SchedulerObserver
import it.unibo.scafi.simulation.gui.incarnation.scafi.ScafiLikeWorld
import it.unibo.scafi.simulation.gui.model.aggregate.AggregateWorld
import it.unibo.scafi.simulation.gui.model.simulation.implementation.mutable.SensorDefinition.Led
import it.unibo.scafi.simulation.gui.model.space.Point3D
//TODO REFACTOR THIS
class SimpleInputController[W <: AggregateWorld](val w: ScafiLikeWorld) extends InputCommandController[W]  {
  case class MoveCommand(ids : Map[Any, Point3D]) extends Command {
    //TODO UNMAKE
    override def make(): Unit = ids.foreach(x => {
      x._1 match {
        case id : w.ID => w.moveNode(id,x._2)
        case _ =>
      }
    })

    override def unmake(): Unit = ???
  }

  case class DeviceOnCommand(ids : Set[Any], name : Any) extends Command {
    //TODO UNMAKE
    override def make(): Unit = name match {
      case name : w.NAME => {
        ids foreach {
          x => x match {
            case id : w.ID => {
              val node = w(id)
              if(node.isDefined) {
                val dev = node.get.getDevice(name)
                dev.get match {
                  case Led(v) => w.changeSensorValue(id,name,!v)
                  case _ =>
                }

              }
            }
            case _ =>
          }
        }
      }
      case _ =>
    }

    override def unmake(): Unit = ???
  }
}