package it.unibo.scafi.simulation.gui.util

/**
  * utility class used to create a simple synchronization
  */
trait Sync {
  /**
    * @return true if the function is blocked false otherwise
    */
  def blocked : Boolean

  /**
    * the function execute in safe context
    * @param f the function
    */
  def apply(f : => Unit)
}
object Sync {
  /**
    * @return a Synchronization object
    */
  def apply : Sync = new Sync {
    private var block = false
    override def blocked: Boolean = block

    override def apply(f: => Unit): Unit = {
      block = true
      f
      block = false
    }
  }
}
