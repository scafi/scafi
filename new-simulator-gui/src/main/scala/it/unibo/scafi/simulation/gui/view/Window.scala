package it.unibo.scafi.simulation.gui.view

/**
  * a window is a container of graphics output
  */
trait Window[OUTPUT <: View] extends Container[OUTPUT] {
  def windowConfiguration : WindowConfiguration
  /**
    * @return the name of windows
    */
  def name : String

  /**
    * close the window
    */
  def close() : Unit
}