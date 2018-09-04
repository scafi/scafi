package it.unibo.scafi.simulation.gui.incarnation.scafi.bridge

import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.Actuator.ACTION
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiWorldIncarnation.{CONTEXT, EXPORT}

/**
  * describe information fro scafi simulation
  * @param program program class used to launch scafi simulation
  * @param action output action, what the bridge do when scafi generate ad export
  */
case class ScafiSimulationInformation(program : Class[_], action : Actuator[_] = Actuator.generalActuator) {
  require(program.newInstance().isInstanceOf[CONTEXT=>EXPORT])
}