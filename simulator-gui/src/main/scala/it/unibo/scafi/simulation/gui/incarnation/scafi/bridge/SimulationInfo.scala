package it.unibo.scafi.simulation.gui.incarnation.scafi.bridge

import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiWorldIncarnation.{CONTEXT, EXPORT}

/**
  * describe information fro scafi simulation
  * @param program program class used to launch scafi simulation
  * @param actuator what the bridge do when scafi generate ad export
  */
case class SimulationInfo(program : Class[_], actuator : Actuator[_] = Actuator.generalActuator) {
  require(program.newInstance().isInstanceOf[CONTEXT=>EXPORT])
}