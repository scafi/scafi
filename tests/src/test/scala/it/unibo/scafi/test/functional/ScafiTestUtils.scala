package it.unibo.scafi.test.functional

import it.unibo.scafi.test.FunctionalTestIncarnation._
import org.scalactic.Equality

import scala.collection.Map

/**
 * Created by: Roberto Casadei
 * Created on date: 13/11/15
 */

object ScafiTestUtils {

  def runProgram(exp: => Any, ntimes: Int = 500)
                (net: Network with SimulatorOps)
                (implicit node: AggregateInterpreter): Network ={
    var endNet: Network = null
    net.execMany(
      node = node,
      exp = exp,
      size = ntimes,
      action = (n,i) => {
        if (i % ntimes == 0) {
          endNet = net
        }})
    endNet
  }

  def runProgramInOrder(firingSeq: Seq[ID])
                       (exp: => Any)
                       (net: Network with SimulatorOps)
                       (implicit node: AggregateInterpreter): Network ={
    net.execInOrderAndReturn(node, exp, firingSeq)
  }

}
