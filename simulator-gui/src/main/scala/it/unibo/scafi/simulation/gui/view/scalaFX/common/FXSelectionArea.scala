package it.unibo.scafi.simulation.gui.view.scalaFX.common

import it.unibo.scafi.simulation.gui.controller.input.InputCommandController
import it.unibo.scafi.simulation.gui.view
import it.unibo.scafi.simulation.gui.view.AbstractSelectionArea
import it.unibo.scafi.simulation.gui.view.scalaFX._

import scalafx.application.Platform
import scalafx.beans.property.DoubleProperty
import scalafx.geometry.Point2D
import scalafx.scene.Node
import scalafx.scene.input.{MouseButton, MouseEvent}
import scalafx.scene.paint.Color
import scalafx.scene.shape.Circle
import scalafx.Includes._
import scalafx.scene.layout.Pane

/**
  * the logic of selection a set of node
  */
private [scalaFX] trait FXSelectionArea extends AbstractSelectionArea {
  self : AbstractFXSimulationPane =>
  private var moved : Map[Any,Node] = Map.empty
  private var _selected : Set[Any] = Set.empty
  private var startPoint : Point2D = new Point2D(0,0)
  private val radiusProperty : DoubleProperty = DoubleProperty(0)
  private var circle : Option[Circle] = None
  private var startDragging = false
  private val selectionPane = new Pane
  self.children.add(selectionPane)
  this.handleEvent(MouseEvent.Any){
  me : MouseEvent =>
    if(me.button == MouseButton.Primary) {
      val pointClick = new Point2D(me.x,me.y)
      me.eventType match  {
        //store the point of start circle or start moving logic
        case MouseEvent.MousePressed =>
          if(this.circle.isDefined) {
            if(!this.circle.get.contains(pointClick)) {
              startPoint = pointClick
              startDragging = false
            } else {
              this._selected = Set.empty
              startDragging = true
            }
          } else {
            startPoint = pointClick
            startDragging = false
          }
        //draw the circle or move it
        case MouseEvent.MouseDragged =>
          if(circle.isEmpty) {
            circle = Some(new Circle {
              this.centerX = startPoint.x
              this.centerY = startPoint.y
              this.radius.bind(radiusProperty)
              this.fill = FXSelectionArea.CircleColor
              this.stroke = Color.Black
            })
            selectionPane.children.add(circle.get)
          }
          if(!startDragging) {
            radiusProperty.value = startPoint.distance(pointClick)
          } else {
            this.circle.get.translateX = pointClick.x - startPoint.x
            this.circle.get.translateY = pointClick.y - startPoint.y
          }
        //circle created or moved
        case MouseEvent.MouseReleased =>
          if(this.circle.isDefined && this.moved.isEmpty) {
            this.moved = this.nodes.filter{x => this.circle.get.contains(nodeToPosition(x._2))}
              .map {x => x._1 -> RichNode(x._2).cloneNode()}
            this.moved.foreach(x => {
              x._2.translateX.bind(circle.get.translateX)
              x._2.translateY.bind(circle.get.translateY)
              x._2.opacity = FXSelectionArea.ClonedOpacity
            })
            this.moved.values foreach {x => selectionPane.children += x}
            this._selected = this.moved.keySet
          } else if(this.moved.nonEmpty && (this.circle.get.translateX.value != 0 || this.circle.get.translateY.value != 0)) {
            val toMove = moved.map {x => x._1 -> view.scalaFX.nodeToPosition(x._2)} map {x => x._1 -> pointToWorldPosition(x._2)}
            if(argumentName.isDefined && factory.isDefined) {
              InputCommandController.virtualMachine.process((factory.get,Map(argumentName.get -> toMove)))
            }
            clearSelected()
          } else {
            clearSelected()
          }
        case _ =>
      }
    }
  }
  private def clearSelected() : Unit = {
    this.circle match {
      case Some(c) =>
        this._selected = Set.empty
        selectionPane.children.remove(c)
        this.circle = None
        Platform.runLater {
          selectionPane.children.clear()
          this.moved = Map.empty
        }
      case _ =>
    }
  }
  override def selected : Set[Any] = this._selected
}

object FXSelectionArea {
  private [FXSelectionArea] lazy val CircleColor = Color.rgb(255,0,0,0.2)
  private [FXSelectionArea] lazy val ClonedOpacity = 0.2
}