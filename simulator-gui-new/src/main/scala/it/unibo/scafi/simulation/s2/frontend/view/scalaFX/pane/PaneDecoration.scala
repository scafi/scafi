package it.unibo.scafi.simulation.s2.frontend.view.scalaFX.pane

import it.unibo.scafi.simulation.s2.frontend.configuration.command.CommandFactory
import it.unibo.scafi.simulation.s2.frontend.configuration.command.factory.ExitCommandFactory
import it.unibo.scafi.simulation.s2.frontend.configuration.launguage.ResourceBundleManager.KeyFile
import it.unibo.scafi.simulation.s2.frontend.configuration.launguage.ResourceBundleManager._
import it.unibo.scafi.simulation.s2.frontend.configuration.parser.RuntimeMachine
import it.unibo.scafi.simulation.s2.frontend.controller.input.InputCommandController
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.ScafiInformation

import scalafx.Includes._
import scalafx.event.ActionEvent
import scalafx.scene.Node
import scalafx.scene.control._
import scalafx.scene.input.KeyCode
import scalafx.scene.input.KeyEvent
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.Pane
/**
 * a set of method that allow to decorate a scalafx pane
 */
object PaneDecoration {
  /**
   * create a menu bar that allow to close application and show help associated to command insert to application
   * @param outerPane
   *   the main pane
   * @param help
   *   string
   * @return
   *   a menu bar with help string
   */
  def createMenu(outerPane: Pane, help: String): MenuBar = {
    implicit val keyFile: String = KeyFile.View
    val helpTooltip = new Tooltip(help)
    helpTooltip.wrapText = true
    val menuBar = new MenuBar()
    menuBar.stylesheets.add("style/menu-bar.css")
    val file = new Menu("File")
    val helpLabel = new Label(i"help")
    val exit = new MenuItem(i"exit")
    val helpItem = new CustomMenuItem(helpLabel)
    helpItem.onAction = (_: ActionEvent) => {
      val bounds = helpLabel.localToScreen(helpLabel.boundsInLocal.value)
      helpTooltip.show(helpLabel, bounds.getMaxX, bounds.getMaxY)
    }
    outerPane.onMouseClicked = (_: MouseEvent) => helpTooltip.hide()
    file.getItems.addAll(helpItem, new SeparatorMenuItem(), exit)

    exit.onAction = (_: ActionEvent) =>
      InputCommandController.virtualMachine.process(ExitCommandFactory, CommandFactory.emptyArg)

    menuBar.menus += file

    menuBar
  }

  /**
   * create a console uses to process text command
   * @param outerPane
   *   the pane where console showed
   * @return
   *   console created
   */
  def createConsole(outerPane: Pane): Node = {
    val runtime = new RuntimeMachine[String](ScafiInformation.UnixRuntime)
    val inputText = new TextField()

    outerPane.handleEvent(MouseEvent.MouseClicked) { _: MouseEvent =>
      outerPane.requestFocus()
    }
    inputText.handleEvent(KeyEvent.KeyPressed) { key: KeyEvent =>
      {
        if (key.code == KeyCode.Enter) {
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
