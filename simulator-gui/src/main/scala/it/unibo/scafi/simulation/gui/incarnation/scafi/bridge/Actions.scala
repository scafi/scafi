package it.unibo.scafi.simulation.gui.incarnation.scafi.bridge

import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiWorldIncarnation.EXPORT
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiLikeWorld
import it.unibo.scafi.simulation.gui.launcher.SensorName.gsensor
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
        if(dev.value != export.root) world.changeSensorValue(id,gsensor.name,export.root)
      }
    }
  }
}
