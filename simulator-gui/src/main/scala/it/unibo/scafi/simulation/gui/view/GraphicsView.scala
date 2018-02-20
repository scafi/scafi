package it.unibo.scafi.simulation.gui.view

/**
  * an graphical output
  */
trait GraphicsView extends View

/**
  * a window is a container of graphics output
  */
trait Window extends Container {

  override type OUTPUT <: GraphicsView

  /**
    * @return the name of windows
    */
  def name : String

  /**
    * close the window
    */
  def close
}