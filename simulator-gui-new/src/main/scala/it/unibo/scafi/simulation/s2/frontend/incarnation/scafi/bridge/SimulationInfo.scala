package it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge

import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.ScafiWorldIncarnation.CONTEXT
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.ScafiWorldIncarnation.EXPORT
import ExportEvaluation._

/**
 * describe information fro scafi simulation
 * @param program
 *   program class used to launch scafi simulation
 * @param metaActions
 *   what the bridge can do with export generated
 * @param exportEvaluations
 *   how export value is computed and uses in the gui world
 */
case class SimulationInfo(
    program: Class[_],
    metaActions: List[MetaActionProducer[_]] = List.empty,
    exportEvaluations: List[EXPORT_EVALUATION[_]] = List(standardEvaluation)
)
