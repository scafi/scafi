package it.unibo.scafi.simulation.s2.frontend.view.scalaFX.pane

import scalafx.scene.image.Image

/**
  * allow to create an image with a logo passed
  */
private [scalaFX] object Logo {
  private def toLogo(name : String, w : Double, h : Double) : Image = new Image(getClass.getResourceAsStream(s"/$name"),w,h,true,false)

  /**
    * @param name name of logo image
    * @return a small logo image
    */
  def small(name : String) : Image = toLogo(name,50,50)
  /**
    * @param name name of logo image
    * @return a big logo image
    */
  def big(name : String) : Image = toLogo(name,400,400)
  /**
    * @param name name of logo image
    * @return a middle logo image
    */
  def middle(name : String) : Image = toLogo(name,200,200)
}