package lib

import it.unibo.scafi.incarnations.BasicSimulationIncarnation._

/**
  * @author Roberto Casadei
  *
  */

trait FieldUtils { self: AggregateProgram =>
  def reifyField[T](expr: => T): Map[ID,T] = {
    foldhood[Seq[(ID,T)]](Seq[(ID,T)]())(_ ++ _){ Seq(mid() -> expr) }.toMap
  }
}
