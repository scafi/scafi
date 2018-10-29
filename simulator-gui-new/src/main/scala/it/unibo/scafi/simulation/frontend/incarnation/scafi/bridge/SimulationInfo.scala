package it.unibo.scafi.simulation.frontend.incarnation.scafi.bridge

import it.unibo.scafi.simulation.frontend.incarnation.scafi.bridge.ScafiWorldIncarnation.{CONTEXT, EXPORT}
import ExportValutation._

/**
  * describe information fro scafi simulation
  * @param program program class used to launch scafi simulation
  * @param metaActions what the bridge can do with export generated
  * @param exportValutations how export value is computed and uses in the gui world
  */
case class SimulationInfo(program : Class[_],
                          metaActions : List[MetaActionProducer[_]] = List.empty,
                          exportValutations : List[EXPORT_VALUTATION[_]] = List(standardValutation)) {
  require(program.newInstance().isInstanceOf[CONTEXT=>EXPORT])
}