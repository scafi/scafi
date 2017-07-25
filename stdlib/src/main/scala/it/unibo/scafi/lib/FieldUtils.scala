package it.unibo.scafi.lib

/**
  * @author Roberto Casadei
  *
  */

trait FieldUtils {
  self: StandardLibrary.Subcomponent =>

  trait FieldUtils {
    self: AggregateProgram =>

    def reifyField[T](expr: => T): Map[ID, T] = {
      foldhood[Seq[(ID, T)]](Seq[(ID, T)]())(_ ++ _) {
        Seq(nbr{mid()} -> expr)
      }.toMap
    }
  }

}
