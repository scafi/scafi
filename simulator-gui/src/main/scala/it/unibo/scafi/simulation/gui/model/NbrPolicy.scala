package it.unibo.scafi.simulation.gui.model

/**
  * @author Roberto Casadei
  *
  */

sealed trait NbrPolicy

case class EuclideanDistanceNbr(radius: Double) extends NbrPolicy
