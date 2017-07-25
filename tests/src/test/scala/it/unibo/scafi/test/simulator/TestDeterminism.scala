package it.unibo.scafi.test.simulator

import it.unibo.scafi.test.FunctionalTestIncarnation._
import org.scalatest.{FunSuite, Matchers}

/**
  * @author Roberto Casadei
  *
  */

class TestDeterminism extends FunSuite with Matchers {

  test("Determinism in network creation"){
    /**
      * gridLike uses 'eps' param to determine the extent of random displacements.
      * Here, we verify that a system is configurated at the same way as others when their factories
      * use the same random seed for configuration, and they differ otherwise.
      */
    val net1 = simulatorFactory.gridLike(20, 20, 1, 1, eps = 5, 1.5, seeds = Seeds(configSeed = 5L))
    val net2 = simulatorFactory.gridLike(20, 20, 1, 1, eps = 5, 1.5, seeds = Seeds(configSeed = 5L))
    val net3 = simulatorFactory.gridLike(20, 20, 1, 1, eps = 5, 1.5, seeds = Seeds(configSeed = 10101010L))

    net1.ids.forall(id =>
      net1.neighbourhood(id) == net2.neighbourhood(id)
    ) shouldBe(true)

    net1.ids.exists(id =>
      net1.neighbourhood(id) != net3.neighbourhood(id)
    ) shouldBe(true)
  }

}
