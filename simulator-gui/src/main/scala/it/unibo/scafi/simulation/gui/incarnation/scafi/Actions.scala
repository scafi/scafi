package it.unibo.scafi.simulation.gui.incarnation.scafi

import it.unibo.scafi.simulation.gui.incarnation.scafi.ScafiWorldIncarnation.EXPORT
import it.unibo.scafi.simulation.gui.launcher.SensorName.{gsensor, gsensor1}
import it.unibo.scafi.simulation.gui.launcher.scalaFX.Launcher.world

/**
  * describe action to actuate to the world, by export produced
  */
object Actions {
  type ACTION = PartialFunction[EXPORT,(ScafiLikeWorld,Int)=>Unit]

  val generalaction= new ACTION {
    override def isDefinedAt(x: EXPORT): Boolean = true
    override def apply(export : EXPORT) : (ScafiLikeWorld, Int) => Unit = {
      (w : ScafiLikeWorld, id : Int) => {
        val devs = w(id).get.devices
        val dev = devs.find {y => y.name == gsensor.name}.get
        world.changeSensorValue(id,gsensor.name,export.root)
      }
    }
  }

  val textaction = new PartialFunction[EXPORT,(ScafiLikeWorld,Int) => Unit] {
    override def isDefinedAt(x: EXPORT): Boolean = !x.root.isInstanceOf[Boolean]

    override def apply(e: EXPORT): (ScafiLikeWorld, Int) => Unit = {
      (w : ScafiLikeWorld, id : Int) => {
        val devs = w(id).get.devices
        val dev = devs.find {y => y.name == gsensor1.name}.get
        if(dev.value != e.root()) {
          world.changeSensorValue(id,gsensor1.name,e.root.toString())
        }
      }
    }
  }
}
