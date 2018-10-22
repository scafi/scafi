package it.unibo.scafi.simulation.gui.view.scalaFX.common

import scalafx.scene.control.{Label, Tooltip}

/**
  * create a label with help passed
  * @param tooltipHelp the tooltip where the label is attached
  */
private [scalaFX] case class Help(tooltipHelp : Tooltip) extends Label {
  this.text = " ? "
  this.tooltip = tooltipHelp
  this.style = "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);"

}
