package it.unibo.scafi.simulation.gui.incarnation.scafi.bridge

import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiWorldIncarnation._


object ExportValutation {
  /**
    * allow to valutate export produced by scafi simulation
    * the value produced can be used to put it into a output
    * stream sensor
    */
  type EXPORT_VALUTATION[A] = (EXPORT => A)
  /**
    * standard valutation take an export and
    * turns it into root value
    */
  val standardValutation : EXPORT_VALUTATION[Any] = (e : EXPORT) => e.root().asInstanceOf[Any]
  /**
    * to string valutation take and export and
    * turns it into to string root value representation
    */
  val toStringValutation : EXPORT_VALUTATION[String] = (e : EXPORT) => e.root().toString
}


