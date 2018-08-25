package it.unibo.scafi.simulation.gui.view.scalaFX.common

import it.unibo.scafi.simulation.gui.controller.input.InputCommandController
import it.unibo.scafi.simulation.gui.view
import it.unibo.scafi.simulation.gui.view.scalaFX._
import it.unibo.scafi.simulation.gui.view.AbstractSelectionArea

import scalafx.application.Platform
import scalafx.beans.property.DoubleProperty
import scalafx.geometry.Point2D
import scalafx.scene.Node
import scalafx.scene.input.{MouseButton, MouseEvent}
import scalafx.scene.paint.Color
import scalafx.scene.shape.Circle

/**
  * the logic of selection a set of node
  */
trait FXSelectionArea extends AbstractSelectionArea {
  self : AbstractFXSimulationPane =>
  private var moved : Map[Any,(Node,Point2D)] = Map.empty
  private var _selected : Set[Any] = Set.empty
  private var startPoint : Point2D = new Point2D(0,0)
  private val radiusProperty : DoubleProperty = DoubleProperty(0)
  private var circle : Option[Circle] = None
  private var startDragging = false
  import scalafx.Includes._
  this.handleEvent(MouseEvent.Any){
    me : MouseEvent =>{
      if(me.button == MouseButton.Primary) {
        val pointClick = new Point2D(me.x,me.y)
        me.eventType match  {
          //store the point of start cirlce or start moving logic
          case MouseEvent.MousePressed => {
            if(this.circle.isDefined) {
              if(!this.circle.get.contains(pointClick)) {
                clearSelected()
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
          }
          //draw the circle or move it
          case MouseEvent.MouseDragged => {
            if(circle.isEmpty) {
              circle = Some(new Circle {
                this.centerX = startPoint.x
                this.centerY = startPoint.y
                this.radius.bind(radiusProperty)
                this.fill = Color.rgb(255,0,0,0.2)
                this.stroke = Color.Black
              })
              this.children.add(circle.get)
            }
            if(startDragging == false) {
              radiusProperty.value = startPoint.distance(pointClick)
            } else {
              this.circle.get.translateX = pointClick.x - startPoint.x
              this.circle.get.translateY = pointClick.y - startPoint.y
            }
          }

          //circle created or moved
          case MouseEvent.MouseReleased => {
            if(this.circle.isDefined && this.moved.isEmpty) {
              this.moved = this.nodes.map {x => x -> nodeTranslationToPosition(x._2._1,x._2._2)}
                .filter{x => this.circle.get.contains(x._2)}
                .map {x => x._1._1 -> (RichNode(x._1._2._1).cloneNode(),x._2)}
              this.moved.foreach(x => {
                x._2._1.translateX.bind(circle.get.translateX)
                x._2._1.translateY.bind(circle.get.translateY)
              })
              this.moved.values foreach {x => this.children += x._1}

              this._selected = this.moved.keySet
            } else if(!this.moved.isEmpty) {
              val toMove = moved.map {x => x._1 -> view.scalaFX.nodeToWorldPosition(x._2._1,x._2._2)}
              if(argumentName.isDefined && factory.isDefined) {
                InputCommandController.virtualMachine.process((factory.get,Map(argumentName.get -> toMove)))
              }
              clearSelected()
            }
          }
          case _ => {}
        }
        me.consume()
      }
    }
  }
  private def clearSelected() = {
    this.children.remove(this.circle.get)
    this.circle = None
    Platform.runLater {
      this.moved.values.foreach { x => this.children -= x._1}
      this.moved = Map.empty
    }

  }

  override def selected : Set[Any] = this._selected
}