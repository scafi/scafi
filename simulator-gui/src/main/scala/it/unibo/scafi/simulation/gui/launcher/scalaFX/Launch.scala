package it.unibo.scafi.simulation.gui.launcher.scalaFX

import javafx.embed.swing.JFXPanel
import it.unibo.scafi.simulation.gui.controller.synchronization.Scheduler
import it.unibo.scafi.simulation.gui.incarnation.scafi._
import it.unibo.scafi.simulation.gui.model.graphics2D.BasicShape2D.{Circle, Rectangle}
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
  val shape = Circle(1)
  val ticked = 33
  val littleRadius = 50
  val bigN = 200
  val maxPoint = 500
  val minDelta = 1
  val maxDelta = 10
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
  val contract = new ScafiSimulationContract[ScafiLikeWorld,ScafiPrototype]
  //SHOW THE WINDOW IMMEDIATLY
  val inputLogic = new ScafiInputController(world)
  val pane = new FXSimulationPane(inputLogic)
  var window : Option[SimulationWindow] = None
  Platform.runLater {
    window = Some(new SimulationWindow(new HBox{}, pane, true))
    window.get.show
  }
  val scafiPrototype = new ScafiPrototype {override def randomSeed: Long = r.nextLong()

    override def randomDeviceSeed: Long = r.nextLong()

    override def radius: Double = littleRadius
  }
  val scafi = new ScafiSimulationObserver(world,contract,0,0,scafiPrototype,"program")
  scafi.init()
  val render = new ScafiFXRender(world,pane,contract,neighbourRender)

  Platform.runLater{
    pane.outNode(world.nodes)
    if(neighbourRender) {
      pane.outNeighbour(contract.getSimulation.get.getAllNeighbours() map {x => world(x._1).get -> world.apply(x._2.toSet)})
    }
    pane.outDevice(world.nodes)
    window.get.renderSimulation()
    scafi.start()
    import Scheduler._
    scheduler <-- inputLogic <-- scafi <-- render
    scheduler.delta_=(ticked)
    scheduler.start()
  }
}