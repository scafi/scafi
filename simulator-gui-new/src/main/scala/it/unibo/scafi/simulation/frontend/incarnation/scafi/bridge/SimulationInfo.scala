package it.unibo.scafi.simulation.frontend.incarnation.scafi.bridge

import it.unibo.scafi.simulation.frontend.incarnation.scafi.bridge.ScafiWorldIncarnation.{CONTEXT, EXPORT}
import ExportEvaluation._

/**
  * describe information fro scafi simulation
  * @param program program class used to launch scafi simulation
  * @param metaActions what the bridge can do with export generated
  * @param exportEvaluations how export value is computed and uses in the gui world
  */
case class SimulationInfo(program : Class[_],
                          metaActions : List[MetaActionProducer[_]] = List.empty,
                          exportEvaluations : List[EXPORT_EVALUATION[_]] = List(standardEvaluation)) {
  require(program.newInstance().isInstanceOf[CONTEXT=>EXPORT])
}