package it.unibo.scafi.simulation.gui.launcher.scalaFX

import javafx.embed.swing.JFXPanel

import it.unibo.scafi.simulation.gui.controller.BasicRender
import it.unibo.scafi.simulation.gui.controller.logger.LogManager
import it.unibo.scafi.simulation.gui.controller.synchronization.Scheduler.scheduler
import it.unibo.scafi.simulation.gui.incarnation.scafi._
import it.unibo.scafi.simulation.gui.model.graphics2D.BasicShape2D.Circle
import it.unibo.scafi.simulation.gui.view.scalaFX.{FXLogger, FXSimulationPane, SimulationWindow}

import scala.util.Random
import scalafx.application.Platform
import scalafx.scene.layout.HBox
//TODO SIMPLE EXAMPLE , REMBEMER TO CREATE A LAUNCHER USED TO LAUNCH EXTERNAL SIMULATION
object Launcher extends App {
  val r = new Random()
  new JFXPanel()
  import it.unibo.scafi.simulation.gui.launcher.scalaFX.WorldConfig._
  //WORLD DEFINITION
  val world = SimpleScafiWorld
  val shape = Circle(1)
  val ticked = 100
  val littleRadius = 50
  val bigN = 1000
  val maxPoint = 1000
  val neighbourRender = true
  devs = Set(
    dev(source,false),
    dev(destination,false),
    dev(obstacle,false),
    dev(gsensor,false)//,
    //dev(id,"")
  )
  nodeProto = NodePrototype(shape)
  randomize2D(bigN,maxPoint)
  val sourceTest = 0
  val destinationTest = 1
  world.changeSensorValue(sourceTest, source.name , true)
  world.changeSensorValue(destinationTest, destination.name,true)
  //world.nodes foreach {x => world.changeSensorValue(x.id,id.name,x.id.toString)}
  //SHOW THE WINDOW IMMEDIATLY
  val inputLogic = new ScafiInputController(world)
  val pane = new FXSimulationPane(inputLogic)
  var window : Option[SimulationWindow] = None
  Platform.runLater {
    window = Some(new SimulationWindow(new HBox{}, pane, true))
    window.get.show
  }
  implicit val scafi = new ScafiSimulationObserver(world)
  scafi.setProgramm(classOf[Simple])
  scafi.simulationPrototype = Some(ScafiBridge.createRadiusPrototype(littleRadius))
  scafi.init()
  val render = new BasicRender(world,pane,neighbourRender)
  Platform.runLater{
    pane.outNode(world.nodes)
    if(neighbourRender) {
      pane.outNeighbour(world.network.neighbours() map{x => world(x._1).get -> world(x._2)})
    }
    pane.outDevice(world.nodes)
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