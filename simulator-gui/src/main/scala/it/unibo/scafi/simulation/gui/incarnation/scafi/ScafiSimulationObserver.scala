package it.unibo.scafi.simulation.gui.incarnation.scafi

import it.unibo.scafi.incarnations.{BasicAbstractSpatialSimulationIncarnation => ExternSimulation}
import it.unibo.scafi.simulation.gui.controller.ExternalSimulation
import it.unibo.scafi.simulation.gui.model.aggregate.AggregateEvent.{NodesDeviceChanged, NodesMoved}
import it.unibo.scafi.simulation.gui.model.common.world.CommonWorldEvent.{NodesAdded, NodesRemoved}
import it.unibo.scafi.simulation.gui.pattern.observer.Source
//TODO PENSA SE METTERE IL NOME DEL PROGRAMMA PLUGGABILE
class ScafiSimulationObserver[W <: ScafiLikeWorld with Source](override protected val world : W,
                                                   val contract : ScafiSimulationContract[W,ScafiPrototype],
                                                   override protected val minDelta : Int,
                                                   override protected val maxDelta : Int,
                                                   override val simulationPrototype : ScafiPrototype,
                                                   val program : Any) extends ExternalSimulation[W]{
  import it.unibo.scafi.incarnations.BasicSimulationIncarnation._
  val ap = Class.forName(program.toString).newInstance().asInstanceOf[CONTEXT=>EXPORT]
  private val checkMoved = world.createObserver(Set(NodesMoved))
  private val checkRemoved = world.createObserver(Set(NodesRemoved))
  private val checkChanged = world.createObserver(Set(NodesDeviceChanged))
  private val checkAdded = world.createObserver(Set(NodesAdded))
  private var exportProduced : Set[(world.ID,ExternSimulation#EXPORT)] = Set()
  world <-- checkAdded <-- checkRemoved <-- checkChanged <-- checkAdded


  override type EXTERNAL_SIMULATION = ExternSimulation#SpaceAwareSimulator
  override type SIMULATION_PROTOTYPE = ScafiPrototype
  override type SIMULATION_CONTRACT = ScafiSimulationContract[W,SIMULATION_PROTOTYPE]

  override protected var delta: Int = (minDelta + maxDelta) / 2
  override protected var currentExecutor: ActorExecutor = _

  override protected def AsyncLogicExecution(): Unit = {
    if(contract.getSimulation.isDefined) {
      val net = contract.getSimulation.get
      /*val result = net.exec(ap)
      exportProduced += result._1 -> result._2*/
    }
  }

  override def onTick(float: Float): Unit = {
    println(exportProduced)
    exportProduced = Set.empty
  }

}


