package it.unibo.scafi.simulation.gui.launcher.scalaFX

import javafx.embed.swing.JFXPanel

import it.unibo.scafi.simulation.gui.controller.synchronization.Scheduler.scheduler
import it.unibo.scafi.simulation.gui.controller.{BasicPresenter, SimpleInputController}
import it.unibo.scafi.simulation.gui.demos.Simple
import it.unibo.scafi.simulation.gui.incarnation.scafi.Actions.{ACTION, _}
import it.unibo.scafi.simulation.gui.incarnation.scafi._
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
  new JFXPanel()
  //WORLD DEFINITION
  val world = ScafiWorld
  var drawer : FXDrawer = StandardFXDrawer
  val shape = Rectangle(3,3)
  var boundary : Option[Rectangle] = None
  val ticked = 1000
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
  val inputLogic = new SimpleInputController[ScafiLikeWorld](world)

  implicit val scafi = ScafiBridge(world)

  def launch(): Unit = {
    devs ++= outSensor
    if(boundary.isDefined) {
      putBoundary(boundary.get)
    }
    import it.unibo.scafi.simulation.gui.view.AbstractKeyboardManager._
    val pane = new FXSimulationPane(inputLogic,drawer) with KeyboardManager with FXSelectionArea

    pane.addCommand(Code1, (ids : Set[Any]) => inputLogic.DeviceOnCommand(ids,sens1.name))
    pane.addCommand(Code2, (ids : Set[Any]) => inputLogic.DeviceOnCommand(ids,sens2.name))
    pane.addCommand(Code3, (ids : Set[Any]) => inputLogic.DeviceOnCommand(ids,sens3.name))
    pane.addMovementAction((ids : Map[Any,world.P]) => inputLogic.MoveCommand(ids))
    var window : Option[SimulationWindow] = None
    Platform.runLater {
      window = Some(new SimulationWindow(new HBox{}, pane, true))
      window.get.show
    }

    randomize2D(nodes,boundary)
    //gridLike2D(100,100,radius)


    scafi.setAction(action)
    scafi.setProgramm(program)
    scafi.simulationPrototype = Some(ScafiBridge.createRadiusPrototype(radius))
    val x = System.currentTimeMillis()
    scafi.init()
    println("creation time = " + (System.currentTimeMillis() - x))
    val render = new BasicPresenter(world,neighbourRender)
    render.out = Some(pane)
    RichPlatform.thenRunLater{
      world.nodes foreach {pane.outNode(_)}
      if(neighbourRender) {
        world.network.neighbours()  foreach {pane.outNeighbour(_)}
      }
      window.get.renderSimulation()
    } {
      scafi.start()
      val movement = new MovementSyncController(0.01f,world,50)
      movement.start
      scheduler <-- inputLogic <-- movement <-- scafi <-- render
      scheduler.delta_=(ticked)
      scheduler.start()
      //TODO ERRORE ! SCHEDLUER SLEEP FOR A LOT OF TIME
    }
  }
}