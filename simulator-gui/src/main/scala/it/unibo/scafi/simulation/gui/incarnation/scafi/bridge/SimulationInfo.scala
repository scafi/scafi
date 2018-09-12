package it.unibo.scafi.simulation.gui.incarnation.scafi.bridge

import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiWorldIncarnation.{CONTEXT, EXPORT}
import ExportValutation._

/**
  * describe information fro scafi simulation
  * @param program program class used to launch scafi simulation
  * @param actuators what the bridge do when scafi generate ad export
  * @param exportValutations how export value is computed and uses in the gui world
  */
case class SimulationInfo(program : Class[_],
                          actuators : List[Actuator[_]] = List.empty,
                          exportValutations : List[EXPORT_VALUTATION[_]] = List(standardValutation)) {
  require(program.newInstance().isInstanceOf[CONTEXT=>EXPORT])
}