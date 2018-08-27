package it.unibo.scafi.simulation.gui.view.scalaFX.pane

import scalafx.scene.image.Image

private [scalaFX] object Logo {
  private def toLogo(name : String, w : Double, h : Double) : Image = new Image(getClass.getResourceAsStream(s"/$name"),w,h,true,false)
  def small(name : String) : Image = toLogo(name,50,50)

  def big(name : String) : Image = toLogo(name,400,400)

  def middle(name : String) : Image = toLogo(name,200,200)
}