package it.unibo.scafi.simulation.s2.frontend.util

/**
 * describe some action result
 */
sealed trait Result
object Result {

  /**
   * the action success
   */
  object Success extends Result

  /**
   * the action failed
   * @param reason
   *   the fail reason
   * @tparam E
   *   the type of reason
   */
  case class Fail[E](reason: E) extends Result
}
