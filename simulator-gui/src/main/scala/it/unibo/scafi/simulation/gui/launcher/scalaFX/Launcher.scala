package it.unibo.scafi.simulation.gui.launcher.scalaFX

import javafx.embed.swing.JFXPanel

import it.unibo.scafi.simulation.gui.controller.synchronization.Scheduler.scheduler
import it.unibo.scafi.simulation.gui.controller.input.inputCommandController
import it.unibo.scafi.simulation.gui.controller.logical.SimulationCommandSpace
import it.unibo.scafi.simulation.gui.controller.presenter.SimulationPresenter
import it.unibo.scafi.simulation.gui.demos.Simple
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.Actions.{ACTION, _}
import it.unibo.scafi.simulation.gui.incarnation.scafi._
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiBridge
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.{ScafiCommandSpace, ScafiWorld}
import it.unibo.scafi.simulation.gui.launcher.SensorName._
import it.unibo.scafi.simulation.gui.launcher.WorldConfig._
import it.unibo.scafi.simulation.gui.model.graphics2D.BasicShape2D.Rectangle
import it.unibo.scafi.simulation.gui.model.sensor.SensorConcept.{sensorInput, sensorOutput}
import it.unibo.scafi.simulation.gui.view.scalaFX.common.{FXSelectionArea, KeyboardManager}
import it.unibo.scafi.simulation.gui.view.scalaFX.drawer.{FXDrawer, StandardFXDrawer}
import it.unibo.scafi.simulation.gui.view.scalaFX.pane.FXSimulationPane
import it.unibo.scafi.simulation.gui.view.scalaFX.{RichPlatform, SimulationWindow}

import scala.util.Random
import scalafx.application.Platform
import scalafx.scene.layout.HBox

object Launcher {
  val r = new Random()

  //WORLD DEFINITION
  val world = ScafiWorld
  var drawer : FXDrawer = StandardFXDrawer
  val shape = Rectangle(3,3)
  var boundary : Option[Rectangle] = None
  val commandWorldSpace = ScafiCommandSpace
  val commnadSimulationSpace = SimulationCommandSpace
  val ticked = 16
  var radius = 70.0
  var nodes = 1000
  var action : ACTION = generalaction;
  var maxPoint = 1000
  var neighbourRender = false
  var outSensor = Set(dev(gsensor,true,sensorOutput))
  var program : Class[_] = classOf[Simple]
  devs = Set(
    dev(sens1,false,sensorInput),
    dev(sens2,false,sensorInput),
    dev(sens3,false,sensorInput)
  )
  //SHOW THE WINDOW IMMEDIATLY
  val inputLogic = inputCommandController

  implicit val scafi = ScafiBridge(world)

  def launch(): Unit = {
    devs ++= outSensor
    if(boundary.isDefined) {
      putBoundary(boundary.get)
    }
    import it.unibo.scafi.simulation.gui.view.AbstractKeyboardManager._
    val pane = new FXSimulationPane(drawer) with KeyboardManager with FXSelectionArea
    commnadSimulationSpace.simulation = Some(scafi)
    pane.addCommand(Code1, (ids : Set[Any]) => commandWorldSpace.ToggleDeviceCommand(ids,sens1.name))
    pane.addCommand(Code2, (ids : Set[Any]) => commandWorldSpace.ToggleDeviceCommand(ids,sens2.name))
    pane.addCommand(Code3, (ids : Set[Any]) => commandWorldSpace.ToggleDeviceCommand(ids,sens3.name))
    pane.addCommand(Code4, (ids : Set[Any]) => commnadSimulationSpace.StopSimulation)
    pane.addCommand(Code5, (ids : Set[Any]) => commnadSimulationSpace.ContinueSimulation)
    pane.addMovementAction((ids : Map[Any,world.P]) => commandWorldSpace.MoveCommand(ids))
    var window : Option[SimulationWindow] = None
    SimulationWindow
    Platform.runLater {
      window = Some(new SimulationWindow(new HBox{}, pane, true))
    }



    val render = new SimulationPresenter(world,neighbourRender)
    randomize2D(nodes,boundary)

    //gridLike2D(100,100,radius)
    scafi.setAction(action)
    scafi.setProgramm(program)
    scafi.simulationPrototype = Some(ScafiBridge.createRadiusPrototype(radius))
    val x = System.currentTimeMillis()
    scafi.init()
    println("creation time = " + (System.currentTimeMillis() - x))

    render.output(pane)
    Platform.runLater {
      window.get.render
      scafi.start()
      val movement = new MovementSyncController(0.01f,world,500)
      movement.start
      scheduler <-- inputLogic <-- movement <-- scafi <-- render
      scheduler.delta_=(ticked)
      scheduler.start()
    }
  }
}