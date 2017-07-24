package it.unibo.scafi.time

/**
  * @author Roberto Casadei
  *
  */

trait TimeAbstraction {
  type Time
}

trait BasicTimeAbstraction extends TimeAbstraction {
  type Time = java.time.LocalTime
}
