package it.unibo.scafi.simulation.gui.launcher.scafi

import javafx.scene.layout.HBox

import it.unibo.scafi.simulation.gui.configuration.ProgramEnvironment.NearRealTimePolicy
import it.unibo.scafi.simulation.gui.configuration.command.FieldParser
import it.unibo.scafi.simulation.gui.demo._
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiSimulationInitializer.RadiusSimulationInitializer
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiSimulationSeed
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.reflection.{Demo, SimulationProfile}
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.{RandomCommandFactory, ScafiProgramBuilder, ScafiSeed}
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiWorldInitializer
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiWorldInitializer.{Grid, Random}
import it.unibo.scafi.simulation.gui.view.scalaFX.drawer.{FastFXOutputPolicy, GradientFXOutputPolicy, StandardFXOutputPolicy}

import scala.collection.immutable.TreeMap
import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafx.scene.control.{Button, ComboBox, Label, TextField}
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.VBox
import scalafx.scene.paint.Color._
import scalafx.stage.Stage

class FXLauncher(private val singleSection : Map[String,FieldParser]) extends Stage{
  val vbox = new VBox()
  singleSection.foreach (
    x => {
      val name = new Label(x._1)
      val added = new VBox
      val field = x._2.fields foreach {
        y => {
          val fieldName = new Label(y.name)
          val input = new TextField()
          added.children.add(new HBox(fieldName,input))
        }
      }
      vbox.children.addAll(name,added)
    }
  )
  this.scene = new Scene {
    fill = White
    content = vbox
  }
}
object FXLauncher extends JFXApp {
  private var simulations = TreeMap.empty[String,Class[_]]
  demos foreach {x => simulations += x.getSimpleName -> x}

  private val drawer = Map("rich" -> StandardFXOutputPolicy, "poor" -> FastFXOutputPolicy, "color" -> GradientFXOutputPolicy)
  val w = 800
  val h = 600
  val comboSim = new ComboBox[String](simulations.keys.toList)
  comboSim.value = simulations.keys.last
  val comboWorld = new ComboBox[String](List("random","grid"))
  comboWorld.value = "grid"
  val comboPolicy = new ComboBox[String](drawer.keys.toList)
  comboPolicy.value = drawer.keys.last
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
  new FXLauncher(Map("random" -> RandomCommandFactory.RandomFieldParser)).show()
  click.onMouseClicked = (e: MouseEvent) => {
    val node = inputNode.text.value.toInt
    val program = comboSim.value.value
    val drawerPassed = comboPolicy.value.value
    val builder = comboWorld.value.value
    val radius = inputRadius.text.value.toInt

    val annotation : Demo = simulations(program).getAnnotation(classOf[Demo])
    val profile : SimulationProfile = annotation.simulationType.profile
    val init : ScafiWorldInitializer= builder match {
      case "random" => Random(node, w, h)
      case "grid" => Grid(w / radius, h / radius, radius)
      case _ => Random(node, w, h)
    }
    ScafiProgramBuilder (
      worldInitializer = init,
      scafiSeed = ScafiSeed(deviceSeed = profile.sensorSeed),
      scafiSimulationSeed = ScafiSimulationSeed(program = simulations(program), action = profile.action),
      simulationInitializer = RadiusSimulationInitializer(radius = inputRadius.text.value.toInt),
      outputPolicy = drawer(drawerPassed),
      neighbourRender = true,
      perfomance = NearRealTimePolicy,
      commandMapping = profile.commandMapping
    ).launch()
    this.stage.close()
  }
}