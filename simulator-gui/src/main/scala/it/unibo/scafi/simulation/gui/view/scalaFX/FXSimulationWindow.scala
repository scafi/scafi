package it.unibo.scafi.simulation.gui.view.scalaFX

import javafx.event.EventHandler
import javafx.stage.WindowEvent

import com.sun.javafx.perf.PerformanceTracker
import it.unibo.scafi.simulation.gui.view.WindowConfiguration.{FullScreen, Window => WindowBound}
import it.unibo.scafi.simulation.gui.view.scalaFX.common.AbstractFXSimulationPane
import it.unibo.scafi.simulation.gui.view.scalaFX.pane._
import it.unibo.scafi.simulation.gui.view.{SimulationView, Window, WindowConfiguration}

import scalafx.Includes._
import scalafx.animation.FadeTransition
import scalafx.application.Platform
import scalafx.event.ActionEvent
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.Label
import scalafx.scene.image.ImageView
import scalafx.scene.input.KeyEvent
import scalafx.scene.layout._
import scalafx.stage.Stage
import scalafx.util.Duration

private [scalaFX] class FXSimulationWindow(private val simulationPane : AbstractFXSimulationPane,
                                           private val debug: Boolean = false,
                                           override val windowConfiguration : WindowConfiguration)
                                            extends LogoStage(windowConfiguration) with Window[SimulationView] {
  import FXSimulationWindow._
  import PaneExtension._
  private val mainPane = new StackPane
  FXLogger.show()
  windowConfiguration.size match {
    case FullScreen => this.fullScreen = true
    case WindowBound(w,h) => {
      this.width = w
      this.height = h
    }
  }
  this.onCloseRequest = new EventHandler[WindowEvent] {
    override def handle(event: WindowEvent): Unit = System.exit(ExitValue)
  }

  scene = new Scene {
    content = mainPane
  }

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
      val pane = new BorderPane
      pane.style = "-fx-border-color: black"
      val anchorPane = new AnchorPane {
        children = simulationPane
      }
      anchorPane.style = "-fx-border-color: black"
      bindSize(simulationPane)
      bindSize(anchorPane, 0.95,0.85)
      clip(anchorPane)
      zoomPane(anchorPane,simulationPane)
      dragPane(simulationPane)
      pane.setCenter(anchorPane)
      if(debug) {
        trackFps(scene.value)
      }
      val fade = new FadeTransition(Duration.apply(timeToWait),logo)
      fade.toValue = 0
      fade.fromValue = 1
      fade.onFinished = (e:ActionEvent) => {
        this.mainPane.children = pane
      }

      fade.playFromStart()
    }
  }
}

private [scalaFX] object FXSimulationWindow {
  private val SimulationPaneSize = 0.8
  private val Padding = 20
  private val ExitValue = 1
}