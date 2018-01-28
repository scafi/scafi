package it.unibo.scafi.simulation.gui.launcher.scalaFX

import javafx.embed.swing.JFXPanel
import javafx.event.EventHandler
import javafx.stage.WindowEvent

import it.unibo.scafi.simulation.gui.controller.synchronization.Scheduler
import it.unibo.scafi.simulation.gui.incarnation.scafi._
import it.unibo.scafi.simulation.gui.model.graphics2D.BasicShape2D.Circle
import it.unibo.scafi.simulation.gui.view.scalaFX.{FXSimulationPane, SimulationWindow}

import scala.util.Random
import scalafx.application.Platform
import scalafx.scene.layout.HBox
//TODO SIMPLE EXAMPLE , REMBEMER TO CREATE A LAUNCHER USED TO LAUNCH EXTERNAL SIMULATION
object Launch extends App {
  val r = new Random()
  new JFXPanel()
  import it.unibo.scafi.simulation.gui.launcher.scalaFX.WorldConfig._
  //WORLD DEFINITION
  val world = SimpleScafiWorld
  val shape = Circle(1f)
  val ticked = 100
  val littleRadius = 150
  val bigN = 1000
  val maxPoint = 2000
  val minDelta = 1
  val maxDelta = 10
  val neighbourRender = true
  devs = Set(
    dev(source,true),
    dev(destination,true),
    dev(obstacle,true),
    dev(gsensor,false)//,
    //dev(id,"")
  )
  nodeProto = NodePrototype(shape)
  randomize2D(bigN,maxPoint)
  //world.nodes foreach {x => world.changeSensorValue(x.id,id.name,x.id.toString)}
  val contract = new ScafiSimulationContract[ScafiLikeWorld,ScafiPrototype]
  contract.initialize(world,new ScafiPrototype {override def randomSeed: Long = r.nextLong()

    override def randomDeviceSeed: Long = r.nextLong()

    override def radius: Double = littleRadius
  })
  val simpleLogic = new MovementSyncController(0.02f,world)(world.nodes.take(100))
  val inputLogic = new ScafiInputController(world)
  val pane = new FXSimulationPane(inputLogic)
  Platform.runLater {
    pane.outNode(world.nodes)
    world.nodes foreach {pane.outDevice(_)}
    val dialogStage = new SimulationWindow(new HBox(){
    }, pane)
    dialogStage.show
    dialogStage.onCloseRequest = new EventHandler[WindowEvent] {
      override def handle(event: WindowEvent): Unit = System.exit(1)
    }
    if(neighbourRender) {
      contract.getSimulation.get.getAllNeighbours().foreach { x =>
        pane.outNeighbour(world(x._1).get,world.apply(x._2.toSet))
      }
    }
    simpleLogic.start()
  }
  val render = new ScafiFXRender(world,pane,contract,neighbourRender)
  Scheduler.scheduler <-- inputLogic <-- simpleLogic <-- render
  Scheduler.scheduler.delta_=(ticked)
  Scheduler.scheduler.start()
}