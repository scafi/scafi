package it.unibo.scafi.simulation.gui.view.scalaFX.pane

import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory
import it.unibo.scafi.simulation.gui.configuration.command.factory.ExitCommandFactory
import it.unibo.scafi.simulation.gui.configuration.launguage.ResourceBundleManager.{KeyFile, _}
import it.unibo.scafi.simulation.gui.configuration.parser.RuntimeMachine
import it.unibo.scafi.simulation.gui.controller.input.InputCommandController
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.ScafiParser

import scalafx.Includes._
import scalafx.event.ActionEvent
import scalafx.scene.Node
import scalafx.scene.control._
import scalafx.scene.input.{KeyCode, KeyEvent, MouseEvent}
import scalafx.scene.layout.Pane
/**
  * a set of method that allow to decorate a pane
  */
object PaneDecoration {
  /**
    * create a menu bar
    * @param outerPane the main pane
    * @param help string
    * @return a menu bar with help string
    */
  def createMenu(outerPane : Pane, help : String) : MenuBar = {
    implicit val keyFile = KeyFile.View
    val helpTooltip = new Tooltip(help)
    helpTooltip.prefWidth = 200
    helpTooltip.wrapText = true
    val menuBar = new MenuBar()
    menuBar.stylesheets.add("style/menu-bar.css")
    val file = new Menu("File")
    val helpLabel = new Label(i"help")
    val exit = new MenuItem(i"exit")
    val helpItem = new CustomMenuItem(helpLabel)
    helpItem.onAction = (e : ActionEvent) => {
      val bounds = helpLabel.localToScreen(helpLabel.boundsInLocal.value)
      helpTooltip.show(helpLabel,bounds.getMaxX,bounds.getMaxY)
    }
    outerPane.onMouseClicked = (e: MouseEvent) => helpTooltip.hide()
    file.getItems.addAll(helpItem,new SeparatorMenuItem(),exit)

    exit.onAction = (e : ActionEvent) => InputCommandController.virtualMachine.process(ExitCommandFactory,CommandFactory.emptyArg)

    menuBar.menus += file

    menuBar
  }

  /**
    * create a console uses to process text command
    * @param outerPane the pane where console showed
    * @return console created
    */
  def createConsole(outerPane : Pane) : Node = {
    val runtime = new RuntimeMachine[String](ScafiParser.UnixRuntime)
    val inputText = new TextField()

    outerPane.handleEvent(MouseEvent.MouseClicked) {
      me : MouseEvent => outerPane.requestFocus()
    }
    inputText.handleEvent(KeyEvent.KeyPressed) {
      key : KeyEvent => {
        if(key.code == KeyCode.Enter) {
          outerPane.requestFocus()
          runtime.process(inputText.text.value)
          inputText.text = ""
        }
      }
    }
    inputText.stylesheets.add("style/console-like.css")
    inputText
  }
}
