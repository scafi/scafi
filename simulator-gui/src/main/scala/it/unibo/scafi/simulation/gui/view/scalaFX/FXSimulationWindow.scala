package it.unibo.scafi.simulation.gui.view.scalaFX

import javafx.event.EventHandler
import javafx.stage.WindowEvent

import com.sun.javafx.perf.PerformanceTracker
import it.unibo.scafi.simulation.gui.view.scalaFX.common.AbstractFXSimulationPane
import it.unibo.scafi.simulation.gui.view.scalaFX.pane._
import it.unibo.scafi.simulation.gui.view.{SimulationView, Window}

import scalafx.animation.FadeTransition
import scalafx.application.Platform
import scalafx.event.ActionEvent
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.Label
import scalafx.scene.input.{KeyEvent, ScrollEvent}
import scalafx.scene.layout.{BorderPane, HBox, StackPane}
import scalafx.stage.Stage
import scalafx.util.Duration
import scalafx.Includes._
class FXSimulationWindow(private val infoPane : HBox,
                         private val simulationPane : AbstractFXSimulationPane,
                         private val debug: Boolean = false) extends Stage with Window[SimulationView] {
  private val Padding = 20
  private val exitValue = 1
  private val logoPanel = new LoadingLogo

  import javafx.stage.Screen
  val screen = Screen.getPrimary.getVisualBounds
  private val mainPane = new StackPane {
    this.minWidth = screen.getMaxX
    this.minHeight = screen.getMaxY
  }

  this.onCloseRequest = new EventHandler[WindowEvent] {
    override def handle(event: WindowEvent): Unit = System.exit(exitValue)
  }
  scene = new Scene {
    content = mainPane
  }

  ZoomAction(mainPane,simulationPane)
  DragAction(simulationPane)

  scene.value.setOnKeyPressed((e : KeyEvent) => {
    simulationPane.fireEvent(e)
  })

  this.fullScreen = true

  mainPane.setAlignment(Pos.Center)
  mainPane.children = logoPanel
  this.title = name

  override def name: String = "Simulation pane"

  override def close: Unit = this.close()

  override def output : SimulationView = simulationPane

  /**
    * try to render the output
    *
    * @return true if the output is rendered false otherwise
    */
  override def render: Unit = {
    Platform.runLater {
      this.show()
      val timeToWait = 1000
      val pane = new BorderPane {
        padding = Insets(Padding)
      }
      import scalafx.Includes._
      infoPane.padding = Insets(Padding)
      pane.setTop(infoPane)
      val debugInfo = new Label()
      infoPane.children.addAll(debugInfo)
      val pannablePane = new PannablePane(simulationPane,Some(pane))
      pane.setCenter(simulationPane)
      //TODO BETTER
      val tracker = PerformanceTracker.getSceneTracker(scene.value)
      val t = new Thread(new Runnable {
        override def run(): Unit = {
          while(true) {
            Platform.runLater{
              debugInfo.setText("FPS : "+tracker.getInstantFPS + " PULSE: " + tracker.getInstantPulses)
            }
            Thread.sleep(1000)
          }
        }
      })
      if(debug) {
        t.start()
      }
      pane.prefWidthProperty().bind(this.getScene().widthProperty())
      pane.prefHeightProperty().bind(this.getScene().heightProperty())
      val fade = new FadeTransition(Duration.apply(timeToWait),logoPanel)
      fade.toValue = 0
      fade.fromValue = 1
      fade.onFinished = (e:ActionEvent) => {
        this.mainPane.children = pane

      }
      fade.playFromStart()
    }
  }
}
