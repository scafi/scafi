package it.unibo.scafi.simulation.gui.launcher.scalaFX

import it.unibo.scafi.simulation.gui.configuration.ProgramEnvironment.NearRealTimePolicy
import it.unibo.scafi.simulation.gui.demos.{Demo, ShortestPathProgram, Simple}
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiSimulation.RadiusSimulation
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiWorldInitializer
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiWorldInitializer.{Grid, Random}
import it.unibo.scafi.simulation.gui.launcher.scafi.ScafiProgramBuilder
import it.unibo.scafi.simulation.gui.view.scalaFX.drawer.{FastFXOutputPolicy, StandardFXOutputPolicy}

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafx.scene.control.{Button, ComboBox, Label, TextField}
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.VBox
import scalafx.scene.paint.Color._
object Launcher extends JFXApp  {
  val classes : List[Class[_]] = ClassFinder.getClasses("it.unibo.scafi.simulation.gui.demos")
  val finded = classes.filter(x => x.isAnnotationPresent(classOf[Demo]) && !x.isInterface)

  private var simulations = Map.empty[String,Class[_]]
  finded foreach {x => simulations += x.getName() -> x}
  private val drawer = Map("rich" -> StandardFXOutputPolicy, "poor" -> FastFXOutputPolicy)
  val w = 800
  val h = 600
  val comboSim = new ComboBox[String](simulations.keys.toList)
  val comboWorld = new ComboBox[String](List("random","grid"))
  val comboPolicy = new ComboBox[String](drawer.keys.toList)
  val inputNode = new TextField()
  val inputRadius = new TextField()
  val click = new Button("start")
  stage = new JFXApp.PrimaryStage {
    title.value = "Hello Stage"
    width = w
    height = h
    scene = new Scene {
      fill = White
      content = new VBox() {
        children = List(
          new Label("welcome"),
          new Label("node"),
          inputNode,
          new Label("radius"),
          inputRadius,
          new Label("builder"),
          comboWorld,
          new Label("simulation"),
          comboSim,
          new Label("output policy"),
          comboPolicy,
          click
          )
      }
    }
  }

  click.onMouseClicked = (e: MouseEvent) => {
    val node = inputNode.text.value.toInt
    val program = comboSim.value.value
    val drawerPassed = comboPolicy.value.value
    val builder = comboWorld.value.value
    val radius = inputRadius.text.value.toInt
    val init : ScafiWorldInitializer= builder match {
      case "random" => Random(node, w, h)
      case "grid" => Grid(w / radius, h / radius, radius)
      case _ => Random(node, w, h)
    }
    ScafiProgramBuilder (
      worldInitializer = init,
      simulation = RadiusSimulation(program = simulations(program), radius = inputRadius.text.value.toInt ),
      outputPolicy = drawer(drawerPassed),
      neighbourRender = true,
      perfomance = NearRealTimePolicy

    ).launch()
    this.stage.close()
  }
}