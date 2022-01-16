package it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge

import it.unibo.scafi.simulation.SimulationObserver
import it.unibo.scafi.simulation.s2.frontend.controller.logical.ExternalSimulation
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.ScafiWorldIncarnation._
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.world.ScafiLikeWorld
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.world.scafiWorld
import it.unibo.scafi.space.{Point3D => ScafiPoint}
/**
 * define a generic bridge with scafi
 */
abstract class ScafiBridge extends ExternalSimulation[ScafiLikeWorld]("scafi-bridge") {
  override type EXTERNAL_SIMULATION = SpaceAwareSimulator
  override type SIMULATION_PROTOTYPE = () => EXTERNAL_SIMULATION
  override type SIMULATION_CONTRACT = ExternalSimulationContract
  protected var idsObserved: Set[world.ID] = Set.empty
  protected var simulationObserver = new SimulationObserver[ID, CNAME]
  val world: ScafiLikeWorld = scafiWorld
  /**
   * current simulation prototype, at begging no prototype defined
   */
  var simulationPrototype: Option[SIMULATION_PROTOTYPE] = None
  // scafi execution context
  private var context: Option[CONTEXT => EXPORT] = None

  def observeExport(id: world.ID): Unit = {
    if (idsObserved.contains(id)) {
      idsObserved -= id
    } else {
      idsObserved += id
    }
  }
  /**
   * @return
   *   current running context (if it is defined)
   */
  protected def runningContext: CONTEXT => EXPORT = {
    require(context.isDefined)
    context.get
  }

  private var simSeed: Option[SimulationInfo] = None

  /**
   * @return
   *   the current simulation seed
   */
  def simulationInfo: Option[SimulationInfo] = simSeed

  /**
   * @param simulationSeed
   *   the simulation seed used to initialize the simulation
   */
  def simulationInfo_=(simulationSeed: SimulationInfo): Unit = {
    require(simulationSeed != null)
    simSeed = Some(simulationSeed)
  }
  // describe scafi contract like
  override val contract: ExternalSimulationContract = new ExternalSimulationContract {
    private var currentSimulation: Option[EXTERNAL_SIMULATION] = None
    override def simulation: Option[SpaceAwareSimulator] = this.currentSimulation
    override def initialize(prototype: SIMULATION_PROTOTYPE): Unit = {
      // to initialize the simulation, current simulation must be empty and program must be defined
      require(currentSimulation.isEmpty && simulationInfo.isDefined)
      // create context by program passed
      context = Some(simulationInfo.get.program.newInstance().asInstanceOf[CONTEXT => EXPORT])
      // create new simulation
      this.currentSimulation = Some(prototype())
      this.currentSimulation.get.attach(simulationObserver)
    }
    override def restart(prototype: SIMULATION_PROTOTYPE): Unit = {
      // to restart simulation current simulation must be defined
      require(currentSimulation.isDefined)
      // create the instance of program
      context = Some(simulationInfo.get.program.newInstance().asInstanceOf[CONTEXT => EXPORT])
      // set current simulation to another
      this.currentSimulation = Some(prototype())
      simulationObserver.idMoved
      this.currentSimulation.get.attach(simulationObserver)
    }
  }
}

object ScafiBridge {

  var Instance: Option[ScafiBridge] = None
  /**
   * implicit class used to compute the path level in the tree
   * @param path
   *   the path passed
   */
  implicit class RichPath(path: Path) {
    def level: Int = if (path.isRoot) {
      0
    } else {
      path.toString.split("/").length + 1
    }
  }
}
