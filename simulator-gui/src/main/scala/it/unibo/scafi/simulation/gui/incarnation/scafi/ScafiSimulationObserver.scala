package it.unibo.scafi.simulation.gui.incarnation.scafi


import it.unibo.scafi.incarnations.{BasicAbstractSpatialSimulationIncarnation => ExternSimulation}
import it.unibo.scafi.simulation.gui.controller.logical.ExternalSimulation
import it.unibo.scafi.simulation.gui.incarnation.scafi.ScafiWorldIncarnation._
import it.unibo.scafi.simulation.gui.model.aggregate.AggregateEvent.{NodesDeviceChanged, NodesMoved}
import it.unibo.scafi.simulation.gui.model.common.world.CommonWorldEvent.{NodesAdded, NodesRemoved}
//TODO RIGUARDARE COMPLETAMENTE q
class ScafiSimulationObserver[W <: ScafiLikeWorld](override protected val world : W,
                                                   val contract : ScafiSimulationContract[W,ScafiPrototype],
                                                   override protected val minDelta : Int,
                                                   override protected val maxDelta : Int,
                                                   override val simulationPrototype : ScafiPrototype,
                                                   val program : Any) extends ExternalSimulation[W]{

  //TODO BRIDGE
  //val ap = (new Simple).asInstanceOf[CONTEXT=>EXPORT]
  private val checkMoved = world.createObserver(Set(NodesMoved))
  private val checkRemoved = world.createObserver(Set(NodesRemoved))
  private val checkChanged = world.createObserver(Set(NodesDeviceChanged))
  private val checkAdded = world.createObserver(Set(NodesAdded))
  private var exportProduced : Set[(world.ID,EXPORT)] = Set()
  world <-- checkAdded <-- checkRemoved <-- checkChanged <-- checkAdded


  override type EXTERNAL_SIMULATION = SpaceAwareSimulator
  override type SIMULATION_PROTOTYPE = ScafiPrototype
  override type SIMULATION_CONTRACT = ScafiSimulationContract[W,SIMULATION_PROTOTYPE]

  override protected var delta: Int = (minDelta + maxDelta) / 2
  override protected var currentExecutor: ActorExecutor = _

  override protected def AsyncLogicExecution(): Unit = {

    println(contract.getSimulation.isDefined)
    if(contract.getSimulation.isDefined) {
      val net = contract.getSimulation.get
      /*val result = net.exec(ap)
      exportProduced += result._1 -> result._2*/
    }
  }

  override def onTick(float: Float): Unit = {
    import it.unibo.scafi.space.{Point3D => ExternalPoint}
    val removed = checkRemoved.nodeChanged()
    val moved = filterRemoved(removed,checkMoved.nodeChanged())
    val devs = filterRemoved(removed,checkChanged.nodeChanged())
    val added = filterRemoved(removed,checkAdded.nodeChanged())
    if(contract.getSimulation.isDefined) {
      val extern = contract.getSimulation.get
      //TODO AGGIUNGI ANCHE IL COMPORTAMENTO DEL MOVIMENTO, PENSA SE USARE UNA STRATEGY ESTERNA
      devs map {world(_).get} foreach {x => x.devices.foreach(y => extern.chgSensorValue(y.name,Set(x.id),y.value))}
    }
    /*println(exportProduced)
    exportProduced = Set.empty*/
  }

  private def filterRemoved(removed : Set[world.ID], another : Set[world.ID]) : Set[world.ID] = another filter { removed.contains(_)}
}


