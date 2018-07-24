package it.unibo.scafi.simulation.gui.incarnation.scafi.bridge

import it.unibo.scafi.simulation.gui.controller.logical.ExternalSimulation
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiWorldIncarnation._
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiLikeWorld

import scala.util.Random
/**
  * define a generic bridge with scafi
  * @tparam W the internal world representation
  */
abstract class ScafiBridge [W <: ScafiLikeWorld](protected val world : W) extends ExternalSimulation[W] {
  override type EXTERNAL_SIMULATION = SpaceAwareSimulator
  override type SIMULATION_PROTOTYPE = () => EXTERNAL_SIMULATION
  override type SIMULATION_CONTRACT = ScafiSimulationContract

  /**
    * define the execution context
    */
  var simulationPrototype: Option[SIMULATION_PROTOTYPE] = None
  override protected val threadName: String = "scafi-bridge"
  //TODO remember to add a defualt operation
  protected var action : PartialFunction[EXPORT,(W,ID)=>Unit] = _
  private var context : Option[CONTEXT=>EXPORT] = None
  protected def runningContext : CONTEXT=>EXPORT = {
    require(context.isDefined)
    context.get
  }
  private var program : Option[Class[_]] = None
  override protected val maxDelta: Option[Int] = None
  override protected var delta: Int = 0
  override val contract = new ScafiSimulationContract
  override protected var currentExecutor: ActorExecutor = _

  def setProgramm(program : Class[_]): Unit = {
    if(program.newInstance().isInstanceOf[CONTEXT=>EXPORT]) {
      this.program = Some(program)
    }
  }

  def setAction(operation : PartialFunction[EXPORT,(W,ID)=>Unit]): Unit = action = operation
  class ScafiSimulationContract extends ExternalSimulationContract{

    private var currentSimulation : Option[EXTERNAL_SIMULATION] = None
    override def getSimulation: Option[SpaceAwareSimulator] = this.currentSimulation

    override def initialize(prototype: SIMULATION_PROTOTYPE): Unit = {
      require(currentSimulation.isEmpty && program.isDefined)
      context = Some(program.get.newInstance().asInstanceOf[CONTEXT=>EXPORT])
      this.currentSimulation = Some(createSimulation(world,prototype))
    }

    override def restart(prototype: SIMULATION_PROTOTYPE): Unit = {
      require(currentSimulation.isDefined)
      context = Some(program.get.newInstance().asInstanceOf[CONTEXT=>EXPORT])
      this.currentSimulation = Some(createSimulation(world,prototype))
    }
    private def createSimulation(w : W, p : SIMULATION_PROTOTYPE) : SpaceAwareSimulator = {
      p()
    }
  }
}

object ScafiBridge {
  val rand = new Random
  def createRadiusPrototype[W <: ScafiLikeWorld](radius: Double)(implicit bridged: ScafiBridge[W]): () => bridged.EXTERNAL_SIMULATION = {
    () => {
      val w = bridged.world
      val nodes: Map[ID, P] = w.nodes map {n => n.id -> new P(n.position.x,n.position.y,n.position.z)} toMap
      val createdSpace  = new QuadTreeSpace(nodes,radius)
      val createdDevs =  nodes.map { case (d, p) => d -> new DevInfo(d, p,
        lsns => if (lsns == "sensor") 1 else 0,
        nsns => nbr => null)
      }
      val res : SpaceAwareSimulator = new SpaceAwareSimulator(simulationSeed = rand.nextInt(),randomSensorSeed = rand.nextInt(),
        space = createdSpace,
        devs = createdDevs)
      nodes map {x => w(x._1).get} foreach {x => x.devices.foreach(y => res.chgSensorValue(y.name,Set(x.id),y.value))}
      w.nodes  foreach { x =>
        x.devices foreach {y => res.chgSensorValue(y.name,Set(x.id),y.value)}
      }
      res
    }
  }
  def apply(w : ScafiLikeWorld) : ScafiBridge[ScafiLikeWorld] = new ScafiSimulationObserver(w)
}