package it.unibo.scafi.time

/**
  * @author Roberto Casadei
  *
  */

trait TimeAbstraction {
  type T
}

trait BasicTimeAbstraction extends TimeAbstraction {
  type T = java.time.LocalTime
}
