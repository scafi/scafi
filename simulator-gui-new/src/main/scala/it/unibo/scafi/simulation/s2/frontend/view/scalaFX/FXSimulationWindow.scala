package it.unibo.scafi.simulation.s2.frontend.view.scalaFX

import javafx.event.EventHandler
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.stage.WindowEvent

import it.unibo.scafi.simulation.s2.frontend.configuration.launguage.ResourceBundleManager._
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.ScafiInformation
import it.unibo.scafi.simulation.s2.frontend.view._
import it.unibo.scafi.simulation.s2.frontend.view.scalaFX.FXSimulationWindow._
import it.unibo.scafi.simulation.s2.frontend.view.scalaFX.common.AbstractFXSimulationPane
import it.unibo.scafi.simulation.s2.frontend.view.scalaFX.logger.FXLogger
import it.unibo.scafi.simulation.s2.frontend.view.scalaFX.pane.PaneDecoration._
import it.unibo.scafi.simulation.s2.frontend.view.scalaFX.pane.PaneExtension._
import it.unibo.scafi.simulation.s2.frontend.view.scalaFX.pane._
import it.unibo.scafi.simulation.s2.frontend.view.ViewSetting

import scalafx.Includes._
import scalafx.animation.FadeTransition
import scalafx.application.Platform
import scalafx.event.ActionEvent
import scalafx.geometry.Pos
import scalafx.scene.Scene
import scalafx.scene.input.KeyCode
import scalafx.scene.input.KeyEvent
import scalafx.scene.layout._
import scalafx.stage.Screen
import scalafx.util.Duration

/**
 * scalafx implementation of Window this window has a VBox pane that show:
 * -1 menuBar: with menu bar you can exit to application and see all command supported
 * -2 logPane: with CTRL + L you enable the log pane: show the log of program
 * -3 simulationPane: show the output of simulation
 * -4 console: with CTRL + L you enable console: allow to run command to change world / simulation state
 * @param simulationPane
 *   the simulation pane
 * @param debug
 *   if you want to show fps or not
 * @param windowConfiguration
 *   the window configuration
 */
private[scalaFX] class FXSimulationWindow(
    private val simulationPane: AbstractFXSimulationPane with AbstractSelectionArea with AbstractKeyboardManager,
    private val debug: Boolean = false,
    override val windowConfiguration: WindowConfiguration
) extends LogoStage(windowConfiguration)
    with Window[SimulationView] {
  /*
   * main pane is a vbox, at the top there is menu bar, at bottom there is console
   */
  private val mainPane = new VBox()
  // close window close entire application
  this.onCloseRequest = new EventHandler[WindowEvent] {
    override def handle(event: WindowEvent): Unit = System.exit(ExitValue)
  }

  scene = new Scene {
    content = mainPane
  }

  // menu bar used to show help and allow to exit to application
  private val menuBar = createMenu(
    simulationPane,
    simulationPane.commandDescription + "\n" + international("command-description")(KeyFile.View)
  )
  // allow to catch key pressed on simulation pane
  scene.value.setOnKeyPressed((e: KeyEvent) => simulationPane.fireEvent(e))
  mainPane.setAlignment(Pos.Center)
  mainPane.children = logo

  this.title = windowConfiguration.name

  override def name: String = windowConfiguration.name

  override def close(): Unit = this.close()

  override def output: SimulationView = simulationPane
  // render entire window
  override def render(): Unit = {
    Platform.runLater {
      // used for pane extension
      implicit val sceneValue: Scene = scene.value
      bindSize(mainPane)
      // external pane used to put simulation pane and console
      val pane = new Pane()
      // a wrapper of simulation pane used to allow zoom and drag
      val anchorPane = new AnchorPane {
        children = simulationPane
      }
      // create console and attach it to anchorPane
      val console = createConsole(anchorPane)
      bindSize(simulationPane)
      bindSize(anchorPane)
      clip(anchorPane)
      zoomPane(anchorPane, simulationPane)
      dragPane(simulationPane)
      // configure main pane
      pane.children = anchorPane :: FXLogger :: Nil
      bindSize(FXLogger, LogWidth, LogHeight)
      // if debug is enable show fps
      if (debug) {
        trackFps(scene.value)
      }
      // fade transition used to show logo at launch time
      val fade = new FadeTransition(Duration.apply(fadeTime), logo)
      fade.toValue = 0
      fade.fromValue = 1
      // at the end of transition simulation pane in render
      fade.onFinished = (_: ActionEvent) => {
        this.mainPane.children = List(menuBar, pane, console)
        // start code to fit simulation pane
        if (ViewSetting.fitting) {
          val paneSize = ScafiInformation.configuration.worldInitializer.size
          import it.unibo.scafi.space.graphics2D.BasicShape2D.{Rectangle => SRect}
          fitIn(simulationPane, paneSize, SRect(anchorPane.minWidth().toFloat, anchorPane.minHeight().toFloat))
        }
        // end code to fit simulation pane
        showHidePanel(
          pane,
          console,
          0,
          -outOfBoundScreen(ConsoleHeight),
          new KeyCodeCombination(KeyCode.K, KeyCombination.CONTROL_DOWN)
        )
        showHidePanel(
          pane,
          FXLogger,
          outOfBoundScreen(LogWidth),
          0,
          new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN)
        )
      }
      fade.playFromStart()
      this.show()
    }
  }
}

private[scalaFX] object FXSimulationWindow {
  private val ExitValue = 1
  private val LogWidth = 0.3
  private val LogHeight = 0.7
  private val fadeTime = 1000
  private val ConsoleHeight = 0.1
  private def outOfBoundScreen(widthPercentage: Double): Double = 2 * widthPercentage * Screen.primary.bounds.width
}
