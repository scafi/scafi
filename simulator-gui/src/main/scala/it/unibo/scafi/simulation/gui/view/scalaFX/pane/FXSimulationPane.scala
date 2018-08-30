package it.unibo.scafi.simulation.gui.view.scalaFX.pane

import it.unibo.scafi.simulation.gui.view.scalaFX.common.{AbstractFXSimulationPane, FXSelectionArea, KeyboardManager}
import it.unibo.scafi.simulation.gui.view.scalaFX.drawer.FXOutputPolicy
import it.unibo.scafi.simulation.gui.view.scalaFX.{NodeLine, _}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.geometry.Point2D
import scalafx.scene.Node
import scalafx.scene.layout.Priority

private [scalaFX] class FXSimulationPane (override val drawer : FXOutputPolicy) extends AbstractFXSimulationPane  with FXSelectionArea with KeyboardManager{
  //internal representation of node
  private val _nodes : mutable.Map[ID,drawer.OUTPUT_NODE] = mutable.Map()
  //internal representation of neighbour
  private val neighbours : mutable.Map[ID,mutable.Map[ID,NodeLine]] = mutable.Map()
  //internal representation of devices
  private val devices : mutable.Map[ID,mutable.Map[Any,drawer.OUTPUT_NODE]] = mutable.Map()
  //used to flush method
  type ACTION = () => Unit
  //a list of changes that are show only when a presenter call flush
  private var changes : List[ACTION] = List.empty

  private var neighbourToRemove : mutable.ListBuffer[javafx.scene.Node] = ListBuffer()
  def nodes : Map[ID,drawer.OUTPUT_NODE] =_nodes.toMap

  override def outNode(node: NODE): Unit = {
    //take the current node position
    val p : Point2D = node.position
    if(_nodes.contains(node.id)) {
      //if the node is already show, the view change the node position
      val n = _nodes(node.id)
      //with bind propriety, i can moved the node
      val oldP = nodeToAbsolutePosition(n)
      val action : ACTION = () => {
        n.translateX = p.x - oldP.x
        n.translateY = p.y - oldP.y
        if(neighbours.get(node.id).isDefined) {
          val map = neighbours(node.id)
          map.foreach(_._2.update())
          neighbours(node.id) foreach {x => {
            neighbours.get(x._1) match {
              case Some(map : mutable.Map[_,_]) => map.get(node.id) foreach {x => x.update()}
            }
          }}
        }

      }
      changes = action :: changes
    } else {
      //if the node isn't show, with drawer i create new graphics instanced associated with the node
      val shape : drawer.OUTPUT_NODE = drawer.nodeGraphicsNode(node)
      this._nodes += node.id -> shape
      //add the new shape in the view
      val action : ACTION = () => {
        this.children += shape
      }
      changes = action :: changes
    }
  }
  //TODO
  override def removeNode(node: ID): Unit = {}

  override def outNeighbour(node : (ID,Set[_ <: ID])): Unit = {
    val map : mutable.Map[ID,NodeLine] = this.neighbours.get(node._1) match {
      case Some(_) => this.neighbours(node._1)
      case _ => {
        this.neighbours += node._1 -> mutable.Map()
        this.neighbours(node._1)
      }
    }
    //i take the current graphics node
    val gnode = this._nodes(node._1)
    //foreach neighbour i create a new link
    node._2 foreach { x => {
      val endGnode = this._nodes(x)
      val link : NodeLine = new NodeLine(gnode,endGnode,lineColor)
      val add = () => {
        val action : ACTION = (() => this.children.add(link))
        changes = action :: changes
      }
      this.neighbours.get(x) match {
        case Some(_) => if(!this.neighbours(x).contains(node._1)) {
          add()
        }
        case _ => add()
      }
      //add each new link
      map.put(x,link)
    }}
  }

  def removeNeighbour(nodes : (ID,Set[_ <: ID])) = {
    var nodeToRemove : List[javafx.scene.Node] = List()
    //utility method used to remove a neigbour
    def erase(start: ID, end: ID): Unit = {
      //take the neighbour of current node
      val map = this.neighbours(start)
      val toRemove = map(end)
      //add te node to remove to removing list
      nodeToRemove = toRemove :: nodeToRemove
      map -= end
    }
    //verify if neighbour is already show or not
    def checkPresence(start: ID, end: ID) = this.neighbours.get(start).isDefined && this.neighbours(start).contains(end)
    //remove the linking
    nodes._2.foreach { x => {
      if (checkPresence(nodes._1, x)) {
        erase(nodes._1, x)
      }
      if (checkPresence(x, nodes._1)) {
        erase(x, nodes._1)
      }
    }}
    val removing = nodeToRemove
    val action = () => neighbourToRemove.append(removing:_*)
    changes = action :: changes
  }
  override def outDevice(id: ID, dev : DEVICE): Unit = {
    if(!devices.get(id).isDefined) devices += id -> mutable.Map()
    val oldDev = devices(id)

    val current : drawer.OUTPUT_NODE = _nodes(id)
    val oldPrintedDev = oldDev.get(dev.name)
    val newDev : Option[drawer.OUTPUT_NODE] = if(!oldPrintedDev.isDefined) {
      drawer.deviceToGraphicsNode(current,dev)
    } else {
      None
    }

    if(newDev.isDefined){
      oldDev += dev.name -> newDev.get
      val action : ACTION = (() => this.children.add(newDev.get))
      changes = action :: changes
    }

    val action : ACTION = (() => drawer.updateDevice(current,dev,oldDev.get(dev.name)))
    changes = action :: changes

  }

  override def clearDevice(node: ID): Unit = {
    //remove all device associated with a id
    val toRemove : List[Node] = List()
    if (this.devices.get(node).isDefined) {
      this.devices(node) foreach {x => this.changes = (() => this.children -= x._2).asInstanceOf[ACTION] :: changes}
      this.devices -= node
    }
  }

  override def flush(): Unit = {
    val changeToApply = changes
    changes = List.empty
    Platform.runLater {
      changeToApply foreach {_()}
      val removing = neighbourToRemove
      this.children.removeAll(removing:_*)
      neighbourToRemove.clear()
    }
  }
}
