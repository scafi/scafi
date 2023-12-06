package it.unibo.scafi.simulation.s2.frontend.view.scalaFX.pane

import java.util.function.Predicate
import javafx.scene.Node
import javafx.scene.paint.ImagePattern
import com.sun.javafx.binding.ExpressionHelper
import it.unibo.scafi.simulation.s2.frontend.view.ViewSetting
import it.unibo.scafi.simulation.s2.frontend.view.ViewSetting._
import it.unibo.scafi.simulation.s2.frontend.view.scalaFX.common.AbstractFXSimulationPane
import it.unibo.scafi.simulation.s2.frontend.view.scalaFX.common.FXSelectionArea
import it.unibo.scafi.simulation.s2.frontend.view.scalaFX.common.KeyboardManager
import it.unibo.scafi.simulation.s2.frontend.view.scalaFX.drawer.FXOutputPolicy
import it.unibo.scafi.simulation.s2.frontend.view.scalaFX.NodeLine
import it.unibo.scafi.simulation.s2.frontend.view.scalaFX._
import it.unibo.scafi.space.graphics2D.BasicShape2D.Circle
import it.unibo.scafi.space.graphics2D.BasicShape2D.Rectangle
import it.unibo.scafi.space.Point3D
import it.unibo.scafi.space.Shape

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.geometry.Point2D
import scalafx.scene.image.Image
import scalafx.scene.layout.Pane
import scalafx.scene.paint.Color
import scalafx.scene.paint.Paint

import java.util.concurrent.Semaphore

private[scalaFX] class FXSimulationPane(override val drawer: FXOutputPolicy)
    extends AbstractFXSimulationPane
    with FXSelectionArea
    with KeyboardManager {
  // layer used to put background
  private val zeroLayer = 0
  // internal representation of node
  private val _nodes: mutable.Map[ID, drawer.OUTPUT_NODE] = mutable.Map()
  // internal representation of neighbour
  private val neighbours: mutable.Map[ID, mutable.Map[ID, NodeLine]] = mutable.Map()
  // internal representation of devices
  private val devices: mutable.Map[ID, mutable.Map[NAME, drawer.OUTPUT_NODE]] = mutable.Map()
  // used to flush method
  type ACTION = () => Unit
  // a list of changes that are show only when a presenter call flush
  private var changes: List[ACTION] = List.empty
  // pane used to show network
  private val network = new Pane
  this.children.add(network)

  private val neighbourToRemove: mutable.ListBuffer[NodeLine] = ListBuffer()
  def nodes: Map[ID, drawer.OUTPUT_NODE] = _nodes.toMap

  override def outNode(node: NODE): Unit = {
    // take the current node position
    val p: Point2D = node.position
    if (_nodes.contains(node.id)) {
      // if the node is already show, the view change the node position
      val n = _nodes(node.id)
      val oldP = nodeToAbsolutePosition(n)
      val translation = (p.x - oldP.x, p.y - oldP.y)
      val action: ACTION = () => {
        n.translateX = translation._1
        n.translateY = translation._2
      }
      changes = action :: changes
    } else {
      // if the node isn't show, with drawer i create new graphics instanced associated with the node
      val shape: drawer.OUTPUT_NODE = drawer.nodeGraphicsNode(node)
      this._nodes += node.id -> shape
      // add the new shape in the view
      val action: ACTION = () => this.children += shape
      changes = action :: changes
    }
  }
  // TODO
  override def removeNode(node: ID): Unit = {}

  override def outNeighbour(node: (ID, Set[_ <: ID])): Unit = {
    val map: mutable.Map[ID, NodeLine] = this.neighbours.get(node._1) match {
      case Some(_) => this.neighbours(node._1)
      case _ =>
        this.neighbours += node._1 -> mutable.Map()
        this.neighbours(node._1)
    }
    // i take the current graphics node
    val gnode = this._nodes(node._1)
    // foreach neighbour i create a new link
    node._2 foreach { x =>
      val endGnode = this._nodes(x)
      val link: NodeLine = new NodeLine(gnode, endGnode, lineColor)
      val add = () => {
        val action: ACTION = () => network.children.add(link)
        changes = action :: changes
      }
      this.neighbours.get(x) match {
        case Some(_) =>
          if (!this.neighbours(x).contains(node._1)) {
            add()
            // add each new link
            map.put(x, link)
          }
        case _ =>
          add()
          // add each new link
          map.put(x, link)
      }
    }
  }

  def removeNeighbour(nodes: (ID, Set[_ <: ID])): Unit = {
    var nodeToRemove: List[NodeLine] = List()
    // utility method used to remove a neigbour
    def erase(start: ID, end: ID): Unit = {
      // take the neighbour of current node
      val map = this.neighbours(start)
      val toRemove = map(end)
      // add te node to remove to removing list
      nodeToRemove = toRemove :: nodeToRemove
      map -= end
    }
    // verify if neighbour is already show or not
    def checkPresence(start: ID, end: ID) = this.neighbours.get(start).isDefined && this.neighbours(start).contains(end)
    // remove the linking
    nodes._2.foreach { x =>
      if (checkPresence(nodes._1, x)) {
        erase(nodes._1, x)
      }
      if (checkPresence(x, nodes._1)) {
        erase(x, nodes._1)
      }
    }
    val removing = nodeToRemove
    val action = () => neighbourToRemove.append(removing: _*)
    changes = (action.asInstanceOf[ACTION]) :: changes
  }
  override def outDevice(id: ID, dev: DEVICE): Unit = {
    // create new mutable map if no device is defined
    if (devices.get(id).isEmpty) devices += id -> mutable.Map()
    val oldDev = devices(id)
    // get the node where the device is attached
    val current: drawer.OUTPUT_NODE = _nodes(id)
    // get old printed dev (if it is defined)
    val oldPrintedDev = oldDev.get(dev.name)
    // try to create new dev (if it is defined)
    val newDev: Option[drawer.OUTPUT_NODE] = if (oldPrintedDev.isEmpty) {
      drawer.deviceToGraphicsNode(current, dev)
    } else {
      None
    }
    // if it is defined create a new device associated to node
    if (newDev.isDefined) {
      oldDev += dev.name -> newDev.get
      val action: ACTION = () => this.children.add(newDev.get)
      changes = action :: changes
    }
    // add action to produce, each time try to update device status
    val action: ACTION = () => drawer.updateDevice(current, dev, oldDev.get(dev.name))
    changes = action :: changes

  }

  override def clearDevice(node: ID, devName: NAME): Unit = {
    if (devices.get(node).isDefined && devices(node).get(devName).isDefined) {
      val guiNode = devices(node)(devName)
      devices(node) -= devName
      val action: ACTION = () => {
        guiNode.visible = false
        this.children -= guiNode
      }
      changes = action :: changes

    }
  }

  override def flush(): Unit = {
    val lock = new Semaphore(0)
    val changeToApply = changes
    changes = List.empty
    val removing = neighbourToRemove
    Platform.runLater {
      changeToApply foreach { _() }
      if (removing.nonEmpty) {
        removing.foreach(_.unbind())
        this.network.children.removeIf(new Predicate[Node] {
          override def test(t: Node): Boolean = !t.visibleProperty().get()
        })
      }
      neighbourToRemove.clear()
      lock.release()
    }
    lock.acquire()
  }

  override def boundary_=(boundary: Shape): Unit = {
    val shape = modelShapeToFXShape.apply(Some(boundary), Point3D.Zero)
    boundary match {
      case Rectangle(_, _, _) | Circle(_, _) => shape.relocate(0, 0)
      case _ =>
    }
    shape.strokeWidth = 1
    shape.stroke = Color.Black
    val fill: Paint = ViewSetting.backgroundImage match {
      case Some(value) => new ImagePattern(new Image(value))
      case _ => ViewSetting.backgroundColor
    }
    shape.fill = fill
    Platform.runLater(this.children.add(zeroLayer, shape))
  }

  override def walls_=(walls: Seq[(Shape, Point3D)]): Unit = {
    for (wall <- walls) {
      val shape = modelShapeToFXShape(Some(wall._1), Point3D.Zero)
      val fxp: Point2D = wall._2
      shape.relocate(fxp.x, fxp.y)

      shape.strokeWidth = 1
      shape.stroke = Color.Black
      shape.fill = new ImagePattern(new Image("wall.jpg"))
      Platform.runLater(this.children.add(shape))
    }
  }
}
