package it.unibo.scafi.simulation.gui.view.scalaFX.pane

import scalafx.scene.Node
import scalafx.scene.control.ScrollPane
import scalafx.scene.layout.Pane

/**
  * a pane that allow pan over another pane
  * @param pane the pane want to pan
  * @param attachOn if pane is attach on another pane
  */
class PannablePane(pane : Node, attachOn : Option[Pane] = None) extends ScrollPane {
  this.content = pane
  this.pannable = false
  this.hbarPolicy = ScrollPane.ScrollBarPolicy.Always;
  this.vbarPolicy = ScrollPane.ScrollBarPolicy.Always;
  if(attachOn.isDefined) {
    this.prefHeight.bind(attachOn.get.prefHeightProperty())
    this.prefWidth.bind(attachOn.get.prefWidthProperty())
  }
}
