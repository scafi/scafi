package it.unibo.scafi.simulation.gui.view.scalaFX

import javafx.event.EventHandler
import javafx.scene.control.TabPane
import javafx.scene.input.{KeyCodeCombination, KeyCombination}
import javafx.stage.WindowEvent

import it.unibo.scafi.simulation.gui.view.WindowConfiguration.{FullScreen, Window => WindowBound}
import it.unibo.scafi.simulation.gui.view._
import it.unibo.scafi.simulation.gui.view.scalaFX.logger.FXLogger
import it.unibo.scafi.simulation.gui.view.scalaFX.pane._

import scalafx.Includes._
import scalafx.animation.FadeTransition
import scalafx.application.Platform
import scalafx.event.ActionEvent
import scalafx.geometry.Pos
import scalafx.scene.Scene
import scalafx.scene.control.{Label, TextField}
import scalafx.scene.input.{KeyCode, KeyEvent}
import scalafx.scene.layout._
import scalafx.stage.Screen
import scalafx.util.Duration
import FXSimulationWindow._
import PaneExtension._
import PaneDecoration._
import it.unibo.scafi.simulation.gui.configuration.launguage.ResourceBundleManager._

private [scalaFX] class FXSimulationWindow(private val simulationPane : FXSimulationPane,
                                           private val debug: Boolean = false,
                                           override val windowConfiguration : WindowConfiguration)
                                            extends LogoStage(windowConfiguration) with Window[SimulationView] {

  val text = new TabPane
  private val mainPane = new VBox()
  this.onCloseRequest = new EventHandler[WindowEvent] {
    override def handle(event: WindowEvent): Unit = System.exit(ExitValue)
  }

  scene = new Scene {
    content = mainPane
  }

  val menuBar = createMenu(simulationPane,simulationPane.commandDescription + "\n" + international("command-description")(KeyFile.View))
  scene.value.setOnKeyPressed((e : KeyEvent) => {
    simulationPane.fireEvent(e)
  })
  mainPane.setAlignment(Pos.Center)
  mainPane.children = logo
  this.title = name

  override def name: String = windowConfiguration.name

  override def close: Unit = this.close()

  override def output : SimulationView = simulationPane

  override def render: Unit = {
    Platform.runLater {
      implicit val sceneValue : Scene = scene.value
      bindSize(mainPane)
      val timeToWait = 1000
      this.show()
      val pane = new Pane()
      val anchorPane = new AnchorPane {
        children = simulationPane
      }
      val console = createConsole(anchorPane)
      bindSize(simulationPane)
      bindSize(anchorPane)
      clip(anchorPane)
      zoomPane(anchorPane,simulationPane)
      dragPane(simulationPane)
      pane.children = anchorPane :: FXLogger :: Nil
      bindSize(FXLogger, LogWidth,LogHeight)
      if(debug) {
        trackFps(scene.value)
      }
      val fade = new FadeTransition(Duration.apply(timeToWait),logo)
      fade.toValue = 0
      fade.fromValue = 1
      fade.onFinished = (e:ActionEvent) => {
        this.mainPane.children = List(menuBar,pane,console)
        showHidePanel(pane, console, 0, -outOfBoundScreen(ConsoleHeight), new KeyCodeCombination(KeyCode.K, KeyCombination.CONTROL_DOWN))
        showHidePanel(pane, FXLogger,outOfBoundScreen(LogWidth), 0, new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN))
      }
      fade.playFromStart()
    }
  }
}

private [scalaFX] object FXSimulationWindow {
  private val SimulationPaneSize = 0.8
  private val Padding = 20
  private val ExitValue = 1
  private val LogWidth = 0.3
  private val LogHeight = 0.7
  private val ConsoleHeight = 0.1
  private def outOfBoundScreen(widthPercentage : Double) : Double = 2 * widthPercentage * (Screen.primary.bounds.width)
}