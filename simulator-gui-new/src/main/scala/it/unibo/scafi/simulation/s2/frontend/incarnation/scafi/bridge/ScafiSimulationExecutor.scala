package it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge

import it.unibo.scafi.simulation.s2.frontend.controller.logger.LogManager
import it.unibo.scafi.simulation.s2.frontend.controller.logger.LogManager.Channel
import it.unibo.scafi.simulation.s2.frontend.controller.logger.LogManager.TreeLog
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.ScafiWorldIncarnation._
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.monitoring.MonitoringExecutor.exportProduced

import java.util.function.UnaryOperator

/**
 * scafi bridge implementation, this object execute each tick scafi logic
 */
object ScafiSimulationExecutor extends SimulationExecutor {
  import ScafiBridge._
  override protected def asyncLogicExecution(): Unit = {
    if (contract.simulation.isDefined) {
      val net = contract.simulation.get
      val result = net.exec(runningContext)
      val safeUpdate = new UnaryOperator[Map[ID, EXPORT]] {
        override def apply(map: Map[ID, EXPORT]): Map[ID, EXPORT] = map + (result._1 -> result._2)
      }
      exportProduced.updateAndGet(safeUpdate)
      // verify it there are some id observed to put export
      if (idsObserved.contains(result._1)) {
        // get the path associated to the node
        val mapped = result._2.paths.toSeq.map { x =>
          {
            if (x._1.isRoot) {
              (None, x._1, x._2)
            } else {
              (Some(x._1.pull()), x._1, x._2)
            }
          }
        }.sortWith((x, y) => x._2.level < y._2.level)
        LogManager.notify(TreeLog[Path](Channel.Export, result._1.toString, mapped))
      }
      // an the meta actions associated to this simulation
      val metaActions = this.simulationInfo.get.metaActions
      metaActions.filter(x => x.valueParser(result._2.root()).isDefined).foreach(x => net.add(x(result._1, result._2)))
      net.process()
    }
  }

  override def toString: String = "simulation bridge"
}
