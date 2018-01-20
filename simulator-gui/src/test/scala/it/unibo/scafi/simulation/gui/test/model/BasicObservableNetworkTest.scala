package it.unibo.scafi.simulation.gui.test.model
/* TO DELETE
import it.unibo.scafi.simulation.gui.model.space.Point
import it.unibo.scafi.simulation.gui.test.help.{BasicTestableDevice, BasicTestableNode, BasicTestableObservableNetwork, Utils}
import org.scalatest.{FunSpec, Matchers}

class BasicObservableNetworkTest extends FunSpec with Matchers{
  val checkThat = new ItWord
  //DEFINITION OF SOME DEVICE
  //FIRST
  val deviceName = "simple"
  val simpleDevice = new BasicTestableDevice(deviceName)
  //SECOND
  val advanceName = "advance"
  val advanceDevice = new BasicTestableDevice(advanceName)
  //DEFINITION OF SOME NODE
  //FIRST(WITHOUT DEVICE)
  val simpleId = 1
  val simpleNode = new BasicTestableNode(simpleId,Point.ZERO,Map())
  //SECOND(WITH ONE DEVICE)
  val middleId = 2
  val middleNode = new BasicTestableNode(middleId,Point.ZERO,Map(deviceName -> simpleDevice))
  //THIRD(WITH ONE ADVANCE DEVICE)
  val lastId = 3
  val lastNode = new BasicTestableNode(lastId,Point.ZERO,Map(advanceName -> advanceDevice))

  val network = new BasicTestableObservableNetwork {
    override type NODE = BasicTestableNode
  }

  network + simpleNode + middleNode

  checkThat("network at begging must be empty") {
    network.neighbours().isEmpty
  }

  checkThat("I can't add a node that isn't in the network") {
    try {
      network.addNeighbours(lastNode,Set(simpleNode))
    } catch {
      case x : IllegalArgumentException => assert(true)
      case _ => assert(false)
    }
  }
  network + lastNode

  checkThat("I can add a neighbour ") {
    try {
      network.addNeighbours(simpleNode,Set(lastNode,middleNode))
    } catch {
      case x : IllegalArgumentException => assert(false)
      case _ => assert(false)
    }
  }

  checkThat("network has a neighbour") {
    assert(network.neighbours(simpleNode).size == 2)
    assert(network.neighbours(simpleNode).find(_ == lastNode).isDefined)
  }
  checkThat("I can remove a neighbour") {
    assert(network.removeNeighbours(simpleNode,Set(lastNode)))
  }
  val bigNumber = 100000
  checkThat("add or remove neighbour in big network doesn't take a lot of time") {

    network ++ (lastId to bigNumber).map(x => new BasicTestableNode(x,Point.ZERO,Map(simpleDevice.name ->simpleDevice))).toSet
    Utils.timeTest(0.5f) {
      network.clearNeighbours(simpleNode)
      network.addNeighbours(simpleNode,network.nodes)
      network.removeNeighbours(simpleNode,network.nodes)
    }
  }
}
*/