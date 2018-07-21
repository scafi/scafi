package it.unibo.scafi.simulation.gui.controller

import it.unibo.scafi.simulation.gui.incarnation.scafi.ScafiLikeWorld
import it.unibo.scafi.simulation.gui.model.aggregate.implementation.mutable.AggregateWorld
import it.unibo.scafi.simulation.gui.model.sensor.implementation.mutable.SensorDefinition.Led
//TODO REFACTOR THIS
class SimpleInputController[W <: AggregateWorld](val w: ScafiLikeWorld) extends InputCommandController[W] {
  case class MoveCommand(ids : Map[w.ID, w.P]) extends Command {
    //TODO UNMAKE
    override def make(): Unit = ids.foreach(x => w.moveNode(x._1,x._2))

    override def unmake(): Unit = ???
  }

  case class DeviceOnCommand(ids : Set[w.ID], name : w.NAME) extends Command {
    //TODO UNMAKE
    override def make(): Unit = ids.foreach{x => {
      val dev : w.DEVICE = w(x).get.getDevice(name).get
      dev match {
        case Led(value) => w.changeSensorValue(x,name,!value)
        case _ =>
      }

    }}

    override def unmake(): Unit = ???
  }
}