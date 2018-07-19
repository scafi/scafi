package it.unibo.scafi.simulation.gui.controller

import it.unibo.scafi.simulation.gui.incarnation.scafi.ScafiLikeWorld
import it.unibo.scafi.simulation.gui.model.aggregate.implementation.immutable.AggregateWorld
//TODO REFACTOR THIS
class SimpleInputController[W <: AggregateWorld](val w: ScafiLikeWorld) extends InputCommandController[W] {
  case class MoveCommand(ids : Map[w.ID, w.P]) extends Command {
    //TODO UNMAKE
    override def make(): Unit = w.moveNodes(ids)

    override def unmake(): Unit = ???
  }

  case class DeviceOnCommand(ids : Set[w.ID], name : w.NAME) extends Command {
    //TODO UNMAKE
    override def make(): Unit = ids.foreach{x => {
      val dev : w.DEVICE = w(x).get.getDevice(name).get
      if(dev.value.isInstanceOf[Boolean]){
        w.changeSensorValue(x,name,!dev.value.asInstanceOf[Boolean])
      }
    }}

    override def unmake(): Unit = ???
  }
}