package it.unibo.scafi.simulation.gui.view.scalaFX.launcher

import javafx.scene.text.{Font, FontWeight}
import scalafx.Includes._
import scalafx.scene.control.{Label, Tooltip}

case class Help(val tooltipHelp : Tooltip) extends Label {
  this.text = " ? "
  this.tooltip = tooltipHelp
  this.font = Font.font("Verdana", FontWeight.BOLD, 15)
  this.style = "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);"

}
