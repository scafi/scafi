package it.unibo.scafi.simulation.gui.incarnation.scafi
import it.unibo.scafi.simulation.gui.incarnation.scafi.ScafiWorldIncarnation._
/**
class SimulationScafi(protected val world : ScafiLikeWorld) extends AbstractScafiSimulation[ScafiLikeWorld] {
  override type SIMULATION_CONTRACT = ExternalSimulationContract[EXTERNAL_SIMULATION]

  private var scafiPrototype : SIMULATION_PROTOTYPE = ???
  /**
    * start the external simulation with the default seed
    */
  override def contract: SIMULATION_CONTRACT = new ScafiSimulationContract

  override def simulationPrototype: SIMULATION_PROTOTYPE = scafiPrototype

  override protected var delta: Int = _
  override protected val minDelta: Int = _
  override protected val maxDelta: Int = _
  override protected var currentExecutor: ActorExecutor = _

  override protected def AsyncLogicExecution(): Unit = ???

  override def onTick(float: Float): Unit = ???

  override type EXTERNAL_SIMULATION = SpaceAwareSimulator

  override def setPrototype(s: SIMULATION_PROTOTYPE): Unit = this.scafiPrototype = s

  class ScafiSimulationContract extends ExternalSimulationContract[EXTERNAL_SIMULATION] {
    private var currentSimulation : Option[SpaceAwareSimulator] = None
    override def getSimulation: Option[SpaceAwareSimulator] = this.currentSimulation

    override def initialize(world: ScafiLikeWorld, prototype: SIMULATION_PROTOTYPE): Unit = {
      require(currentSimulation.isEmpty)
      this.currentSimulation = Some(createSimulation(world,prototype))
    }
    /**
      * restart the external simulation
      *
      * @param world the internal representation of the world
      */
    override def restart(world: ScafiLikeWorld, prototype: SIMULATION_PROTOTYPE): Unit = {
      require(currentSimulation.isDefined)
      this.currentSimulation = Some(createSimulation(world,prototype))
    }

    private def createSimulation(w : ScafiLikeWorld, p : SIMULATION_PROTOTYPE) : SpaceAwareSimulator = {
      p.create(w)
    }
  }
}
**/