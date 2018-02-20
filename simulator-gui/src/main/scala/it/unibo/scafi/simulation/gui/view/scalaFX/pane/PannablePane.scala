package it.unibo.scafi.simulation.gui.view.scalaFX.pane

import scalafx.scene.{Node, input}
import scalafx.scene.control.ScrollPane
import scalafx.scene.input.{MouseButton, MouseEvent}
import scalafx.scene.layout.Pane

/**
  * a pane that allow pan over another pane
  * @param pane the pane want to pan
  * @param attachOn if pane is attach on another pane
  */
class PannablePane(pane : Node, attachOn : Option[Pane] = None) extends ScrollPane {
  this.content = pane
  this.pannable = true
  this.hbarPolicy = ScrollPane.ScrollBarPolicy.Never;
  this.vbarPolicy = ScrollPane.ScrollBarPolicy.Never;
  if(attachOn.isDefined) {
    this.prefHeight.bind(attachOn.get.prefHeightProperty())
    this.prefWidth.bind(attachOn.get.prefWidthProperty())
  }

}
