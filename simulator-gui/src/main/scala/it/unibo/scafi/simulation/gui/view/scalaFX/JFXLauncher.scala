package it.unibo.scafi.simulation.gui.view.scalaFX


import javafx.geometry.Insets

import it.unibo.scafi.simulation.gui.configuration.command.FieldParser
import it.unibo.scafi.simulation.gui.configuration.command.FieldParser.{Field, IntField, MultipleField, StringField}
import it.unibo.scafi.simulation.gui.launcher.scafi.FieldLauncher

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.event.ActionEvent
import scalafx.scene.{Node, Scene}
import scalafx.scene.control.{Button, ComboBox, Label, TextField}
import scalafx.scene.input.{ContextMenuEvent, InputEvent, InputMethodEvent, MouseEvent}
import scalafx.scene.layout._
import scalafx.scene.text.Font

object JFXLauncher extends JFXApp {
  private val windowWidth = 800
  private val windowHeight = 600
  private val mainContent = new VBox()
  private val windowName = "Scafi"
  mainContent.children += title
  stage = new JFXApp.PrimaryStage {
    title.value = windowName
    width = windowWidth
    height = windowHeight
    scene = new Scene {
      content = mainContent
    }
  }
  stage.show()
  mainContent.prefWidth.bind(stage.width)
  mainContent.vgrow = Priority.Always
  FieldLauncher.section.map{x => FieldInfo(x._2.name.toString,x._1)} foreach {mainContent.children += _}
  FieldLauncher.subsection.map{x => MultiFieldInfo(x._1,x._2.map{x => x._2.name.toString -> x._1})} foreach {mainContent.children += _}
  mainContent.children.add(new Button("launch"))
  private object title extends StackPane {
    val title = new Label(windowName)
    title.font = Font("Verdana",40)
    this.children.add(title)
  }

  case class FieldInfo(sectionName : String, fieldParser : FieldParser) extends VBox {
    private val label = new Label(sectionName)
    label.font = Font("Verdana",25)
    private val titlePane = new StackPane{children.add(label)}
    this.children += titlePane
    private val mainControls : Set[HBox] = fieldParser.fields.map {x => (FieldControl(x))}
    private val mainBox = new VBox
    mainControls foreach {mainBox.children += _}
    this.children += mainBox
  }

  case class FieldControl(field: Field) extends HBox {
    val name = new StackPane()
    name.minWidth = 200
    val value = new StackPane()
    val nameLabel = new Label(field.name)
    nameLabel.font = Font("Verdana",15)
    name.children += nameLabel
    val control : Node = field.fieldType match {
      case IntField => new TextField()
      case StringField => new TextField()
      case MultipleField(value) => new ComboBox[Any](value.toSeq)
    }
    val valueControl = new TextField()
    this.children += name
    this.children += control
  }
  case class MultiFieldInfo(name : String, sections : Map[String, FieldParser]) extends VBox {
    private val title = new StackPane()
    private val comboPane = new StackPane()
    private val titleLabel = new Label(name)
    private var lastPane : Option[Node] = None
    private val controls : Map[String,Set[FieldControl]] = sections map {x => x._1 -> x._2.fields.map{FieldControl(_)}}
    titleLabel.font = Font("Verdana",25)
    val combo = new ComboBox[String](sections.keySet.toSeq)
    title.children.addAll(titleLabel)
    comboPane.children.add(combo)
    this.children.addAll(title,comboPane)
    combo.setOnAction((e : ActionEvent) => {
      if(lastPane.isDefined) this.children.remove(lastPane.get)
      val mainBox = new VBox()
      controls.get(combo.value.get).foreach {x => x.foreach {mainBox.children.add(_)}}
      lastPane = Some(mainBox)
      this.children.add(mainBox)
    })
  }
}
