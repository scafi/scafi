package it.unibo.scafi.simulation.gui.view.scalaFX
//TODO TRY TO CONVERT LIST[NODE] TO LIST[JAVAFX.NODE}
import it.unibo.scafi.simulation.gui.model.core
import it.unibo.scafi.simulation.gui.model.core.World
import it.unibo.scafi.simulation.gui.view.scalaFX.pane.SelectionArea

import scala.collection.mutable
import scalafx.beans.property.DoubleProperty
import scalafx.geometry.Point2D
import scalafx.scene.Node
import scalafx.scene.input.{MouseButton, MouseEvent}
import scalafx.scene.paint.Color
import scalafx.scene.shape.Circle
import scalafx.Includes._
class FXSimulationPane extends AbstractFXSimulationPane with SelectionArea {
  private val _nodes : mutable.Map[World#ID,(Node,Point2D)] = mutable.Map()
  private val neighbours : mutable.Map[World#ID,mutable.Map[World#ID,Node]] = mutable.Map()
  private val devices : mutable.Map[World#ID,Set[Node]] = mutable.Map()
  /*private var moved : Map[World#ID,Node] = Map()
  private var startPoint : Point2D = new Point2D(0,0)
  private var r : DoubleProperty = DoubleProperty(0)
  private var circle : Option[Circle] = None
  private var startDragging = false*/

  def nodes : Map[World#ID,(Node,Point2D)] =_nodes.toMap
  // THE LOGIC OF DRAG ELEMENT -> REMOVE HERE
  /*
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
                this.moved.values.foreach { x => this.children -= x}
                this.moved = Map()
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
            if(this.circle.isDefined && this.moved.isEmpty) {
              moved = this._nodes.filter {x => this.circle.get.contains(x._2._2.x + x._2._1.translateX.value, x._2._2.y + x._2._1.translateY.value)} map {x => x._1 -> new Circle {
                this.centerX = x._2._2.x + x._2._1.translateX.value
                this.centerY = x._2._2.y + x._2._1.translateY.value
                //HERE TO CHANGE
                this.radius = 5
                this.translateX.bind(circle.get.translateX)
                this.translateY.bind(circle.get.translateY)
              }} toMap

              this.moved.values foreach {this.children += _}
            }
          }

          case _ => {}
        }
        me.consume()
      }
    }
  }*/
  override def outNode[N <: World#Node](node: Set[N]): Unit = {
    node foreach  { x => {
        val p : Point2D = x.position
        if(_nodes.contains(x.id)) {
          val (node,oldP) = _nodes(x.id)
          node.translateX = p.x - oldP.x
          node.translateY = p.y - oldP.y
        } else {
          val shape = nodeToScalaFXNode(x)
          this._nodes += x.id -> (shape,p)
          this.children += shape
        }
      }
    }
  }
  //TODO
  override def removeNode[ID <: World#ID](node: Set[ID]): Unit = {
    node foreach  {x => {this.children -= (_nodes(x)._1 )}}
    this.requestLayout()
  }

  override def outNeighbour[N <: World#Node](node: N, neighbour: Set[N]): Unit = {
    val gnode = this._nodes(node.id)._1
    neighbour foreach {x => {
      val endGnode = this._nodes(x.id)._1
      val link = new NodeLine(gnode,endGnode,Color.web("rgba(125,125,125,0.1)"))
      if(!this.neighbours.get(node.id).isDefined) {
        this.neighbours += node.id -> mutable.Map()
      }
      val s : mutable.Map[World#ID,Node] = this.neighbours(node.id)
      //CHECK IF THE LINK IS ALREADY SHOW
      if(!s.contains(x.id)) {
        this.children += link
        s += (x.id -> link)
      }
    }}
  }

  override def removeNeighbour[ID <: World#ID](node: ID, neighbour: Set[ID]): Unit = {
    def erase(start : ID, end : ID): Unit = {
      val map = this.neighbours(start)
      val toRemove = map(end)
      this.children -= toRemove
      map -= end
    }
    def checkPresence(start :ID, end : ID) = this.neighbours.get(start).isDefined && this.neighbours(start).contains(end)
    neighbour foreach { x =>
      if(checkPresence(node,x)) {
        erase(node,x)
      }
      if (checkPresence(x,node)) {
        erase(x,node)
      }
    }
  }

  override def outDevice[N <: World#Node](node: N): Unit = {
    val devs = deviceToNode(node.devices,_nodes(node.id)._1)
    devs.foreach(this.children += _)
    this.devices += node.id -> devs
  }

  /**
    * remove all devices associated to a node
    *
    * @param node the node
    * @tparam N the type of node
    */
  override def clearDevice[N <: World#ID](node: N): Unit = {
    if(this.devices.get(node).isDefined) {
      this.devices(node) foreach {this.children -= _ }
      this.devices -= node
    }
  }
}
