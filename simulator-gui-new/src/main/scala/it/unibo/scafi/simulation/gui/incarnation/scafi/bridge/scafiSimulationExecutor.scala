package it.unibo.scafi.simulation.gui.incarnation.scafi.bridge

import it.unibo.scafi.simulation.gui.controller.logger.LogManager
import it.unibo.scafi.simulation.gui.controller.logger.LogManager.{Channel, TreeLog}
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiWorldIncarnation._
import ScafiBridge._

object scafiSimulationExecutor extends SimulationExecutor {
  override protected def asyncLogicExecution(): Unit = {
    if (contract.simulation.isDefined) {
      val net = contract.simulation.get
      val result = net.exec(runningContext)
      exportProduced += result._1 -> result._2
      //verify it there are some id observed to put export
      if (idsObserved.contains(result._1)) {
        //get the path associated to the node
        val mapped = result._2.paths.toSeq.map { x => {
          if (x._1.isRoot) {
            (None, x._1, x._2)
          } else {
            (Some(x._1.pull()), x._1, x._2)
          }
        }
        }.sortWith((x, y) => x._2.level < y._2.level)
        LogManager.notify(TreeLog[Path](Channel.Export, result._1.toString, mapped))
      }
      //an the meta actions associated to this simulation
      val metaActions = this.simulationInfo.get.metaActions
      metaActions.filter(x => x.valueParser(result._2.root()).isDefined).foreach(x => net.add(x(result._1, result._2)))
      net.process()
    }
  }
}
