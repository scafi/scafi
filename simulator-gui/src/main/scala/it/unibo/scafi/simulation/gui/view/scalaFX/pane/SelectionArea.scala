package it.unibo.scafi.simulation.gui.view.scalaFX.pane

import it.unibo.scafi.simulation.gui.model.core.World
import it.unibo.scafi.simulation.gui.view.scalaFX.AbstractFXSimulationPane

import scalafx.beans.property.DoubleProperty
import scalafx.geometry.Point2D
import scalafx.scene.Node
import scalafx.scene.input.{MouseButton, MouseEvent}
import scalafx.scene.paint.Color
import scalafx.scene.shape.Circle

/**
  * the logic of selection a set of node
  */
trait SelectionArea extends AbstractSelectionArea{
  self : AbstractFXSimulationPane =>
  private var _moved : Map[World#ID,Node] = Map()
  private var startPoint : Point2D = new Point2D(0,0)
  private var r : DoubleProperty = DoubleProperty(0)
  private var circle : Option[Circle] = None
  private var startDragging = false

  import scalafx.Includes._
  this.handleEvent(MouseEvent.Any){
    me : MouseEvent =>{
      if(me.button == MouseButton.Primary) {
        val pointClick = new Point2D(me.x,me.y)
        me.eventType match  {
          case MouseEvent.MousePressed => {
            if(this.circle.isDefined) {
              if(!this.circle.get.contains(pointClick)) {
                this.children.remove(this.circle.get)
                this.circle = None
                startPoint = pointClick
                startDragging = false
                this._moved.values.foreach { x => this.children -= x}
                this._moved = Map()
                println("Here")
              } else {
                startDragging = true
              }
            } else {
              startPoint = pointClick
              startDragging = false
            }
          }
          case MouseEvent.MouseDragged => {
            if(circle.isEmpty) {
              circle = Some(new Circle {
                this.centerX = startPoint.x
                this.centerY = startPoint.y
                this.radius.bind(r)
                this.fill = Color.Transparent
                this.stroke = Color.Black
              })
              this.children.add(circle.get)
            }
            if(startDragging == false) {
              r.value = startPoint.distance(pointClick)
            } else {
              this.circle.get.translateX = pointClick.x - startPoint.x
              this.circle.get.translateY = pointClick.y - startPoint.y
            }
          }
          case MouseEvent.MouseReleased => {
            if(this.circle.isDefined && this._moved.isEmpty) {
              _moved = this.nodes.filter {x => this.circle.get.contains(x._2._2.x + x._2._1.translateX.value, x._2._2.y + x._2._1.translateY.value)} map {x => x._1 -> new Circle {
                this.centerX = x._2._2.x + x._2._1.translateX.value
                this.centerY = x._2._2.y + x._2._1.translateY.value
                //HERE TO CHANGE
                this.radius = 5
                this.translateX.bind(circle.get.translateX)
                this.translateY.bind(circle.get.translateY)
              }}

              this._moved.values foreach {this.children += _}
            }
          }

          case _ => {}
        }
        me.consume()
      }
    }
  }
  def moved : Set[World#ID] = this._moved.keySet
}