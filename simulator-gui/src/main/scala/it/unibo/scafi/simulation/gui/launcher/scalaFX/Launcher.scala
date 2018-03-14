package it.unibo.scafi.simulation.gui.launcher.scalaFX

import javafx.embed.swing.JFXPanel

import it.unibo.scafi.simulation.gui.controller.logger.LogManager
import it.unibo.scafi.simulation.gui.controller.synchronization.Scheduler.scheduler
import it.unibo.scafi.simulation.gui.controller.{BasicRender, SimpleInputController}
import it.unibo.scafi.simulation.gui.demos.Simple
import it.unibo.scafi.simulation.gui.incarnation.scafi._
import it.unibo.scafi.simulation.gui.model.graphics2D.BasicShape2D.Rectangle
import it.unibo.scafi.simulation.gui.view.AbstractSelectionArea
import it.unibo.scafi.simulation.gui.view.scalaFX.pane.{FXSelectionArea, KeyboardManager}
import it.unibo.scafi.simulation.gui.view.scalaFX.{AbstractFXSimulationPane, FXLogger, FXSimulationPane, SimulationWindow}

import scala.util.Random
import scalafx.application.Platform
import scalafx.scene.input.KeyCode
import scalafx.scene.layout.HBox
//TODO SIMPLE EXAMPLE , REMBEMER TO CREATE A LAUNCHER USED TO LAUNCH EXTERNAL SIMULATION
object Launcher {
  val r = new Random()
  new JFXPanel()
  import it.unibo.scafi.simulation.gui.launcher.scalaFX.WorldConfig._
  //WORLD DEFINITION
  val world = SimpleScafiWorld

  type PANE = AbstractFXSimulationPane with KeyboardManager[world.ID] with AbstractSelectionArea[world.ID,world.P]
  val shape = Rectangle(1,1)
  val ticked = 100
  var radius = 70
  var nodes = 1000
  var maxPoint = 1000
  val neighbourRender = true
  var program : Class[_] = classOf[Simple]
  devs = Set(
    dev(source,false),
    dev(destination,false),
    dev(obstacle,false),
    dev(gsensor,false),
    dev(gsensor1,"")
  )
  nodeProto = NodePrototype(shape)
  //world.nodes foreach {x => world.changeSensorValue(x.id,id.name,x.id.toString)}
  //SHOW THE WINDOW IMMEDIATLY
  val inputLogic = new SimpleInputController[ScafiLikeWorld](world)
  SimpleInputController.instance = Some(inputLogic)
  val pane : PANE = new FXSimulationPane(SimpleInputController) with KeyboardManager[world.ID] with FXSelectionArea[world.ID]
  import it.unibo.scafi.simulation.gui.view.AbstractKeyboardManager._
  pane.addCommand(Code1, (ids : Set[Int]) => inputLogic.DeviceOnCommand(ids,source.name))
  pane.addCommand(Code2, (ids : Set[Int]) => inputLogic.DeviceOnCommand(ids,destination.name))
  pane.addCommand(Code3, (ids : Set[Int]) => inputLogic.DeviceOnCommand(ids,obstacle.name))
  pane.addMovementAction((ids : Map[Int,world.P]) => inputLogic.MoveCommand(ids))
  var window : Option[SimulationWindow] = None
  Platform.runLater {
    window = Some(new SimulationWindow(new HBox{}, pane, true))
    window.get.show
  }
  implicit val scafi = ScafiBridge(world)
  import it.unibo.scafi.simulation.gui.incarnation.scafi.ScafiWorldIncarnation._
  val sensaction : EXPORT => Option[(ScafiLikeWorld, world.ID) => Unit] =  (e : EXPORT) => {
    if(!e.root().isInstanceOf[Boolean]) {
      None
    } else {
      Some((w : ScafiLikeWorld, id : Int) => {
        val devs = w(id).get.devices
        val dev = devs.find {y => y.name == gsensor.name}.get
        if(dev.value != e.root()) {
          world.changeSensorValue(id,gsensor.name,e.root)
        }
      })
    }
  }

  val textaction :  EXPORT => Option[(ScafiLikeWorld, world.ID) => Unit] =  (e : EXPORT) => {
    if(e.root().isInstanceOf[Boolean]) {
      None
    } else {
      Some((w : ScafiLikeWorld, id : Int) => {
        val devs = w(id).get.devices
        val dev = devs.find {y => y.name == gsensor1.name}.get
        if(dev.value != e.root()) {
          world.changeSensorValue(id,gsensor1.name,e.root.toString())
        }
      })
    }
  }

  def launch(): Unit = {
    randomize2D(nodes,maxPoint)
    scafi.addAction(sensaction)
    scafi.addAction(textaction)
    scafi.setProgramm(program)
    scafi.simulationPrototype = Some(ScafiBridge.createRadiusPrototype(radius))
    scafi.init()
    val render = new BasicRender(world,pane,neighbourRender)
    Platform.runLater{
      pane.outNode(world.nodes)
      if(neighbourRender) {
        pane.outNeighbour(world.network.neighbours() map{x => world(x._1).get -> world(x._2)})
      }
      window.get.renderSimulation()
      scafi.start()
      scheduler <-- inputLogic <-- scafi <-- render
      scheduler.delta_=(ticked)
      scheduler.start()

      val logger : FXLogger = new FXLogger
      LogManager <-- logger
      logger.show()
    }
  }
}