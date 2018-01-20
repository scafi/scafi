package it.unibo.scafi.simulation.gui.launcher.console

import it.unibo.scafi.simulation.gui.controller.LogicController
import it.unibo.scafi.simulation.gui.controller.logger.LogManager
import it.unibo.scafi.simulation.gui.controller.synchronization.Scheduler
import it.unibo.scafi.simulation.gui.incarnation.console.{ConsoleOutput, ConsoleWorld}
import it.unibo.scafi.simulation.gui.model.common.world.CommonWorldEvent.NodesRemoved
import it.unibo.scafi.simulation.gui.model.space.{Point, Point2D}

import scala.util.Random

object Main extends App {
  import Scheduler._
  val world = new ConsoleWorld
  val name = "simple"
  val bigNumber = 10
  (0 to bigNumber) foreach {x => {world + new world.RootNode(id = x, position = Point.ZERO)}}
  val output = new ConsoleOutput
  val controller = new FailureController(output,world)
  val movingController = new RandomMovementController(output,world)
  scheduler <-- controller
  scheduler <-- movingController
  scheduler.delta_=(1000)
  scheduler.start()
  LogManager <-- (new ConsoleLogger)
}

class FailureController(out : ConsoleOutput, world : ConsoleWorld) extends LogicController[ConsoleWorld]  {
  private val observer = world.createObserver(Set())
  world <-- observer
  override type OUTPUT = ConsoleOutput
  private val r = new Random()
  private val MAX = 10
  private val delta = 100
  private var nodeDelete : Set[ConsoleWorld#RootNode] = Set[ConsoleWorld#RootNode]()
  private[this] val thread = new Thread(){
    override def run(): Unit = {
      while(true) {
        val node = world.apply(r.nextInt(MAX))
        if(node isDefined) nodeDelete += node.get
        Thread.sleep(delta)
      }
    }
  }.start()


  override def onTick(float: Float): Unit = {
    if(!nodeDelete.isEmpty) {
      val toOut : Set[ConsoleWorld#RootNode] = nodeDelete
      world -- (nodeDelete map {_.id})
      out.out(toOut)

      LogManager.log("erasing..",LogManager.High)

      this.nodeDelete = this.nodeDelete.empty
    }
  }

}

class RandomMovementController(out: ConsoleOutput, world: ConsoleWorld) extends LogicController[ConsoleWorld] {
  private val observer = world.createObserver(Set(NodesRemoved))
  world <-- observer
  override type OUTPUT = ConsoleOutput
  private val r = new Random()
  private val MAX = 100
  private val delta = 10
  private var moving: Map[Int, Point2D] = Map[Int, Point2D]()
  private[this] val thread = new Thread() {
    override def run(): Unit = {
      while (true) {
        val node = world.apply(r.nextInt(MAX))
        if (node isDefined) moving += node.get.id -> Point2D(r.nextInt(MAX), r.nextInt(MAX))
        Thread.sleep(delta)
      }
    }
  }.start()

  override def onTick(float: Float): Unit = {
    if (!moving.isEmpty) {

      val nodeRemoved = this.observer.nodeChanged() map { _.id}
      moving = moving filter {x => !nodeRemoved.contains(x._1)}
      val count = moving.size
      LogManager.log(s"moving.. count = $count", LogManager.Middle)
      world.moveNodes(moving)
      moving = moving.empty
    }
  }
}

class ConsoleLogger extends LogManager.LogObserver {
  override protected def logging(message: String, priority: LogManager.Priority): Unit = println(s"[LOG $priority] msg := $message")
}