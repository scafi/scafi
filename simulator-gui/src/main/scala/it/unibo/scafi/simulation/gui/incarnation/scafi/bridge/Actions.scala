package it.unibo.scafi.simulation.gui.incarnation.scafi.bridge

import it.unibo.scafi.simulation.gui.configuration.SensorName.output1
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiWorldIncarnation.EXPORT
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.{ScafiLikeWorld, scafiWorld}
import it.unibo.scafi.simulation.gui.model.space.{Point, Point3D}

import scala.util.Try

/**
  * describe action to actuate to the world, by export produced
  */
object Actions {
  private val w = scafiWorld
  type ACTION = PartialFunction[EXPORT,Int => Unit]

  /**
    * a standard action used to change the sensor value associated to the node
    */
  val generalAction : ACTION = new ACTION {
    override def isDefinedAt(x: EXPORT): Boolean = true
    override def apply(export : EXPORT) : w.ID => Unit = (id : w.ID) => w(id) match {
      case Some(node) => node.getDevice(output1) match {
        case Some(dev) => if (dev.value != export.root()) w.changeSensorValue(id, output1, export.root())
        case _ =>
      }
      case _ =>
    }
  }

  /**
    * an action that allow to move world node with the value passed
    */
  val movementAction : ACTION = new ACTION {
    override def isDefinedAt(x: EXPORT): Boolean = true

    override def apply(v1: EXPORT): w.ID => Unit = (id : w.ID) => {
      Try{
        val (x,y) = v1.root().asInstanceOf[(Double,Double)]
        val oldPos = w(id).get.position
        val point = Point3D(oldPos.x + (x * 1000),oldPos.y + (y * 1000),oldPos.z)
        w.moveNode(id,point)
      }
    }
  }

}