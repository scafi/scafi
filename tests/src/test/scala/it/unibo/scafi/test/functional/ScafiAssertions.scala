package it.unibo.scafi.test.functional

/**
 * Created by: Roberto Casadei
 * Created on date: 30/10/15
 */

import it.unibo.scafi.test.FunctionalTestIncarnation._
import org.scalatest.Matchers

object ScafiAssertions extends Matchers {

  def assertForAllNodes[T](f: T => Boolean, okWhenNotComputed: Boolean = false)
                          (implicit net: Network): Unit ={
    withClue("Actual network: " + net) {
      net.exports.forall {
        case (id, Some(e)) => f(e.root[T]())
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
                             customEq:Option[(T,T)=>Boolean] = None)
                            (implicit net: Network): Unit ={
    withClue("Actual network: " + net + "\nExpected values:"+vals) {
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

  def assertNetworkValuesWithPredicate[T](pred: (ID,T)=>Boolean)
                                         (passNotComputed: Boolean = true)
                                         (implicit net: Network): Unit ={
    withClue("Actual network: " + net) {
      net.ids.forall(id => {
        val actualExport = net.export(id)
        actualExport match {
          case Some(v) => pred(id,v.root[T]())
          case None => passNotComputed
        }
      }) shouldBe true
    }
  }
}
