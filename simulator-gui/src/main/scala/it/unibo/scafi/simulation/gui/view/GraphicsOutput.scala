package it.unibo.scafi.simulation.gui.view


trait GraphicsOutput extends Output
trait Window extends Container {

  override type OUTPUT <: GraphicsOutput

  def name : String

  def close

}