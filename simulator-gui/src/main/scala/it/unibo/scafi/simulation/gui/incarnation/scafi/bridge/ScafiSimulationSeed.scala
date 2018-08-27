package it.unibo.scafi.simulation.gui.incarnation.scafi.bridge

import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.Actions.ACTION
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiWorldIncarnation.{CONTEXT, EXPORT}

/**
  * describe a scafi simulation skeleton
  * @param program program class used to launch scafi simulation
  * @param action output action, what the bridge do when scafi generate ad export
  */
case class ScafiSimulationSeed(program : Class[_], action : ACTION = Actions.generalAction) {
  require(program.newInstance().isInstanceOf[CONTEXT=>EXPORT])
}