package it.unibo.scafi.simulation.gui.view.scalaFX.pane

import scalafx.scene.Group
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.VBox

/**
  * the pane of loading..
  */
//TODO RICORDATI DI GESTIRE LE STRINGE CON RESOURCE BOUNDLE
class LoadingLogo extends Group {
  val image =  new Image(getClass.getResourceAsStream("/loadingLogo.png"))
  this.children = new VBox{
    this.children = new ImageView(image)
  }
}
