/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.test.functional

import it.unibo.scafi.test.FunctionalTestIncarnation._
import it.unibo.scafi.test.functional.ScafiTestUtils.exec
import org.scalatest.Matchers

object ScafiAssertions extends Matchers {

  def assertForAllNodes[T](f: (ID,T) => Boolean, okWhenNotComputed: Boolean = false)
                          (implicit net: Network): Unit ={
    withClue("Actual network:\n" + net + "\n\n Sample exports:\n" + net.export(0) + "\n" + net.export(1)) {
      net.exports.forall {
        case (id, Some(e)) => f(id, e.root[T]())
        case (id, None) => okWhenNotComputed
      } shouldBe true
    }
  }

  /**
   * Asserts the value of only a subset of the nodes of the network.
   */
  def assertKnownNetworkValues[T](vals: Map[ID,Option[T]])(implicit net: Network): Unit ={
    vals.keys.forall(id => {
      val actualExport = net.export(id)
      var expected = vals(id)
      (actualExport, expected) match {
        case (Some(e), Some(v)) => e.root[T]() == v
        case (None, None) => true
        case _ => false
      }
    }) shouldBe true
  }

  /**
   * Asserts the value of all the nodes of the network.
   */
  def assertNetworkValues[T](vals: Map[ID, T],
                             customEq: Option[(T,T)=>Boolean] = None,
                             msg: String = "Assert Network Values")
                            (implicit net: Network): Unit ={
    withClue(s"""
              | ${msg}
              | Actual network: ${net}
              | Sensor state: ${net.sensorState()}
              | Neighborhoods: ${net.ids.map(id => id -> net.neighbourhood(id))}
              | Sample exports
              | ID=0 => ${net.export(0)}
              | ID=1 => ${net.export(1)}
              |
              | Expected values: ${vals}
              """.stripMargin) {
      net.ids.forall(id => {
        val actualExport = net.export(id)
        var expected = vals.get(id)
        (actualExport, expected) match {
          case (Some(e), Some(v)) => if(customEq.isDefined) customEq.get(e.root[T](), v)
            else e.root[T]() == v
          case (None, None) => true
          case (None, _) => false
          case _ => false
        }
      }) shouldBe true
    }
  }

  def assertNetworkValuesWithPredicate[T](pred: (ID,T)=>Boolean, msg: String = "Assert network values with predicate")
                                         (passNotComputed: Boolean = true)
                                         (implicit net: Network): Unit ={
    withClue(
      s"""
         | ${msg}
         | Actual network: ${net}
         | Sensor state: ${net.sensorState()}
         | Neighborhoods: ${net.ids.map(id => id -> net.neighbourhood(id))}
         | Sample exports
         | ID=0 => ${net.export(0)}
         | ID=1 => ${net.export(1)}
        |""".stripMargin) {
      net.ids.forall(id => {
        val actualExport = net.export(id)
        actualExport match {
          case Some(v) => pred(id,v.root[T]())
          case None => passNotComputed
        }
      }) shouldBe true
    }
  }

  def assertAlways[T](ap: AggregateProgram, ntimes: Int)
                     (pred: (ID,T)=>Boolean)
                     (net: Network with SimulatorOps) = {
    for(i <- 1 to ntimes){
      exec(ap, ntimes=1)(net)
      assertNetworkValuesWithPredicate[T](pred)(true)(net)
    }
  }
}
