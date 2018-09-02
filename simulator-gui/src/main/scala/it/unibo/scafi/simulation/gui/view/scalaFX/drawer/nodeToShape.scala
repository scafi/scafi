package it.unibo.scafi.simulation.gui.view.scalaFX.drawer

import javafx.scene.shape._

import it.unibo.scafi.simulation.gui.model.core.World
import it.unibo.scafi.simulation.gui.model.graphics2D.BasicShape2D.{Circle => InternalCircle, Polygon => InternalPolygon, Rectangle => InternalRectangle}
import it.unibo.scafi.simulation.gui.model.space.Point2D
import it.unibo.scafi.simulation.gui.view.scalaFX._

import scalafx.geometry.{Point2D => FXPoint}
import scalafx.scene.paint.Color
import scalafx.Includes._

//TODO REFACTOR
private[drawer] object nodeToShape {
  def create (node: World#Node): Shape = {
    val p: FXPoint = node.position
    node.shape match {
      case Some(InternalRectangle(w,h,_)) => new Rectangle {
        this.x = p.x - w / 2
        this.y = p.y - h / 2
        this.width = w
        this.height = h
      }

      case Some(InternalCircle(r,_)) => new Circle {
        this.centerX = p.x
        this.centerY = p.y
        this.radius = r
        this.smooth = false
      }

      case Some(InternalPolygon(_,polyPoints @ _*)) => new Polyline {
        polyPoints.foreach {internalPoint => this.points.addAll(internalPoint.x + p.x, p.y + internalPoint.y)}
      }

      case _ => new Rectangle {
        this.x = p.x
        this.y = p.y
        this.width = 10
        this.height = 10
        this.fill = Color.Red
      }
    }
  }
}
