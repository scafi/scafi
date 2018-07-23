package it.unibo.scafi.simulation.gui.view.scalaFX

import javafx.event.EventHandler
import javafx.stage.WindowEvent

import com.sun.javafx.perf.PerformanceTracker
import it.unibo.scafi.simulation.gui.view.scalaFX.common.AbstractFXSimulationPane
import it.unibo.scafi.simulation.gui.view.scalaFX.pane.{LoadingLogo, PannablePane}
import it.unibo.scafi.simulation.gui.view.{GraphicsView, Window}

import scalafx.animation.FadeTransition
import scalafx.application.Platform
import scalafx.event.ActionEvent
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.Label
import scalafx.scene.input.KeyEvent
import scalafx.scene.layout.{AnchorPane, BorderPane, HBox, StackPane}
import scalafx.stage.Stage
import scalafx.util.Duration

class SimulationWindow(private val infoPane : HBox,
                       private val simulationPane : AbstractFXSimulationPane,
                       private val debug: Boolean = false) extends Stage with Window {
  private val Padding = 20
  private val exitValue = 1
  private val logoPanel = new LoadingLogo
  private val mainPane = new StackPane{
    this.minWidth = 800
    this.minHeight = 600
  }
  this.onCloseRequest = new EventHandler[WindowEvent] {
    override def handle(event: WindowEvent): Unit = System.exit(exitValue)
  }
  scene  = new Scene {
    content = mainPane
  }
  import scalafx.Includes._
  scene.value.setOnKeyPressed((e : KeyEvent) => {
    simulationPane.fireEvent(e)
  })
  mainPane.setAlignment(Pos.Center)
  mainPane.children = logoPanel
  this.title = name

  override type OUTPUT = GraphicsView

  override def name: String = "Simulation pane"

  override def close: Unit = this.close()

  def renderSimulation(): Unit = {
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

    pane.setCenter(pannablePane)
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
  override def output: Set[OUTPUT] = ???
}
