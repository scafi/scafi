package it.unibo.scafi.simulation.gui.view.scalaFX.launcher

import javafx.scene.Node

import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory
import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory.{CommandArgDescription, IntType, LimitedValueType}
import it.unibo.scafi.simulation.gui.view.scalaFX.launcher.ScalaFXLauncher.{FieldBoxSpacing}
import javafx.scene.layout.{StackPane => JFXStackPane}

import it.unibo.scafi.simulation.gui.view.WindowConfiguration
import it.unibo.scafi.simulation.gui.view.scalaFX.launcher.FieldBox.ArgBox

import scalafx.beans.property.ObjectProperty
import scalafx.scene.control.{ComboBox, Label, TextField, Tooltip}
import scalafx.scene.layout.{HBox, VBox}

case class FieldBox(factory: CommandFactory)(implicit val window : WindowConfiguration) extends VBox {
  private val argBoxes = factory.commandArgsDescription.map{ArgBox(_)}
  this.spacing = FieldBoxSpacing
  argBoxes foreach {x => this.children.add(x)}

  def toUnix : String = {
    factory.name + " " + argBoxes.map{_.value}.mkString(" ")
  }
}

object FieldBox {
  val PercentageLeft = 0.2
  val PercentageRight = 0.7
  case class ArgBox(arg : CommandArgDescription)(implicit val window : WindowConfiguration) extends HBox {
    private val objectProperty : ObjectProperty[Any] = new ObjectProperty[Any]()
    private val argPane = new JFXStackPane()
    private val argName = new Label(arg.name)
    argName.tooltip = new Tooltip((arg.description))
    argPane.getChildren.add(argName)
    this.children.add(argPane)
    argPane.setPrefWidth(window.width * PercentageLeft)
    private val node : Node = arg.valueType match {
      case LimitedValueType(values @_*) => {
        val combo = new ComboBox[Any](values)
        if(arg.defaultValue.isDefined){
          (0 until values.size).filter(i => values.apply(i) == arg.defaultValue.get)
            .foreach(x => combo.getSelectionModel.select(x))
        }
        objectProperty.bind(combo.value)
        combo
      }
      case IntType => {
        val field = new IntField {}
        objectProperty.bind(field.text)
        field
      }
      case _ => {
        val field = new TextField{
          text = arg.defaultValue.map(_.toString).getOrElse("")
        }
        objectProperty.bind(field.text)
        field
      }
    }

    private val nodePane = new JFXStackPane(node)
    nodePane.setPrefWidth(window.width * PercentageRight)
    this.children.add(nodePane)
    def value : String = objectProperty.value.toString
  }
}
