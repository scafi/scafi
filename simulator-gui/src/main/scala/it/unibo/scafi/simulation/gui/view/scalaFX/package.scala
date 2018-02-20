package it.unibo.scafi.simulation.gui.view


import java.util.concurrent.{CompletableFuture, CountDownLatch}

import it.unibo.scafi.simulation.gui.launcher.scalaFX.WorldConfig
import it.unibo.scafi.simulation.gui.model.core.{World, Shape => InternalShape}
import it.unibo.scafi.simulation.gui.model.graphics2D.BasicShape2D.{Circle => InternalCircle, Rectangle => InternalRectangle}
import it.unibo.scafi.simulation.gui.model.simulation.BasicPlatform.{OnOffSensor, TextSensor}
import it.unibo.scafi.simulation.gui.model.space.{Point, Point2D, Point3D}

import scalafx.application.Platform
import scalafx.geometry.{Point2D => FXPoint}
import scalafx.scene.{CacheHint, Node}
import scalafx.scene.control.Label
import scalafx.scene.image.Image
import scalafx.scene.paint.Color
import scalafx.scene.shape.{Circle, Ellipse, Rectangle}
//TODO THINK WHERE ADD COLOR (SIMPLE TEST)
//TODO FOR PERFORMANCE USE IMAGE INSTEAD OF SHAPE -> TO DRAW IMAGE JAVAFX DON'T USE CPU , ONLY GPU
package object scalaFX {
  //TODO CHANGE METHOD TO DRAW SENSOR
  val colors : Map[String,Color] = Map(WorldConfig.source.name -> Color.Red,
                                        WorldConfig.destination.name -> Color.Yellow,
                                        WorldConfig.obstacle.name -> Color.Blue,
                                        WorldConfig.gsensor.name -> Color.Pink)
  /**
    * create a fx node by a Node
    * @param node the node of the world
    * @tparam N the type of node
    * @return the node created
    */
  def nodeToScalaFXNode[N <: World#Node](node: N) : Node = {
    val p: FXPoint = node.position
    val defaultShape = new Rectangle {
      this.x = p.x
      this.y = p.y
      this.width = 10
      this.height = 10
      this.fill = Color.Red
    }
    var shape : Node = defaultShape
    if(node.shape.isDefined) {
      node.shape.get match {
        case r : InternalRectangle => shape = new Rectangle {
          this.x = p.x
          this.y = p.y
          this.width = r.w
          this.height = r.h
        }
        case c : InternalCircle => shape = new Circle {
          this.centerX = p.x
          this.centerY = p.y
          this.radius = c.r
          this.smooth = false
        }
        case _ =>
      }
    }
    shape
  }

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
  //TODO THINK TO A BETTER SOLUTION
  def deviceToNode[D <: World#Device](d : Set[D], n : Node) : Set[javafx.scene.Node] = {
    //the offset to the label of the y coordinate
    val yoffset = 10
    val label = new Label
    val nodeWidth = n.boundsInLocal.value.getWidth
    val increaseRadius = 2
    val lineWidth = 2
    var currentR = nodeWidth
    //TODO REMBER TO ADD COLOR IN MODEL
    val onColor = Color.Red
    val offColor = Color.Black
    val point = nodeToAbsolutePosition(n)
    label.layoutX.bind(n.translateX +point.x)
    label.layoutY.bind(n.translateY + point.y + yoffset)
    var res : Set[javafx.scene.Node] = Set(label)
    d foreach  { x =>
      x match {
        case TextSensor(value) => {
          label.text = label.text.value + "" + x.name.toString + " = " + value
        }
        case OnOffSensor(value) => {
          if(value) {
            val ellipse = new Ellipse{
              this.centerX.bind(n.translateX + point.x)
              this.centerY.bind(n.translateY + point.y)
              this.radiusX = currentR
              this.radiusY = currentR
              this.strokeWidth = lineWidth
              this.stroke = colors(x.name.toString)
              this.fill = Color.Transparent
              this.smooth = false
            }
            res += ellipse
          }
          currentR += increaseRadius
        }
        case _ => {
          None
        }
      }
    }

    res
  }
}
