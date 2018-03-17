package it.unibo.scafi.simulation.gui.view



import it.unibo.scafi.simulation.gui.launcher.scalaFX.WorldConfig
import it.unibo.scafi.simulation.gui.model.core.{Shape => InternalShape}
import it.unibo.scafi.simulation.gui.model.graphics2D.BasicShape2D.{Circle => InternalCircle, Polygon => InternalPolygon, Rectangle => InternalRectangle}
import it.unibo.scafi.simulation.gui.model.space.{Point, Point2D, Point3D}

import scalafx.geometry.{Point2D => FXPoint}
import scalafx.scene.Node
import scalafx.scene.paint.Color
//TODO THINK WHERE ADD COLOR (SIMPLE TEST)
//TODO FOR PERFORMANCE USE IMAGE INSTEAD OF SHAPE -> TO DRAW IMAGE JAVAFX DON'T USE CPU , ONLY GPU
package object scalaFX {
  //TODO CHANGE METHOD TO DRAW SENSOR
  val colors : Map[String,Color] = Map(WorldConfig.source.name -> Color.Red,
                                        WorldConfig.destination.name -> Color.Yellow,
                                        WorldConfig.obstacle.name -> Color.Blue,
                                        WorldConfig.gsensor.name -> Color.Pink)

  /**
    * convert a point in a point2D in javafx
    * @param p the point
    * @return the fx point
    */
  implicit def PointToScalaFXPoint(p : Point) : FXPoint = {
    p match {
      case p: Point3D => new FXPoint(p.x,p.y)
      case p : Point2D => new FXPoint(p.x,p.y)
    }
  }
  def nodeToAbsolutePosition(n : Node) : FXPoint = {
    val bounds = n.boundsInLocal.value
    val middleWidth = bounds.getWidth / 2
    val middleHeight = bounds.getHeight / 2
    new FXPoint(bounds.getMinX + middleWidth, bounds.getMinY + middleHeight)
  }
  def nodeToWorldPosition(n: Node, start : FXPoint) : Point3D = {
    //TODO THINK WHAT TO DO HERE
    val defaultZ = 0
    Point3D(start.x + n.translateX.value, start.y + n.translateY.value, defaultZ)
  }

}
