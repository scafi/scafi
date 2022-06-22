package it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge

import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.ScafiWorldIncarnation._

object ExportEvaluation {
  /**
   * allow to evaluate export produced by scafi simulation the value produced can be used to put it into a output stream
   * sensor
   */
  type EXPORT_EVALUATION[A] = EXPORT => A
  /**
   * standard evaluation take an export and turns it into root value
   */
  val standardEvaluation: EXPORT_EVALUATION[Any] = (e: EXPORT) => e.root().asInstanceOf[Any]
  /**
   * to string evaluation take and export and turns it into to string root value representation
   */
  val toStringEvaluation: EXPORT_EVALUATION[String] = (e: EXPORT) => e.root().toString
}
