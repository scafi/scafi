package it.unibo.scafi.simulation.gui

import it.unibo.scafi.simulation.gui.BasicSpatialIncarnation.NBR_RANGE_NAME
import it.unibo.scafi.simulation.gui.model.implementation.SensorEnum
import BasicSpatialIncarnation._

/**
  * @author Roberto Casadei
  *
  */

class GradientAggregateProgram extends AggregateProgramSpecification with ExecutionTemplate with Constructs with Builtins {
  override type MainResult = Double

  def isSource = sense[Boolean](SensorEnum.SOURCE.getName)
  def isObstacole = sense[Boolean](SensorEnum.OBSTACLE.getName)

  //gradiente
  override def main(): Double =
    if (isObstacole) {
      Double.MaxValue
    } else {
      rep(Double.MaxValue) {
        distance => mux(isSource) { 0.0 } { minHoodPlus { nbr { distance } + nbrvar[Double](NBR_RANGE_NAME) } }
      }
    }
}
