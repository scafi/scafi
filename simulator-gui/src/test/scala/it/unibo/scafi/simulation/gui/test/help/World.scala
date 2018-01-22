package it.unibo.scafi.simulation.gui.test.help

trait World {
  type NODE <: Node
  var nodes : Map[NODE#Id,NODE] = Map[NODE#Id,NODE]()
  trait Node {
    trait Id {
      val value : Int
    }
    val id : Id
  }

  def addNode(n: Node)(implicit ev : n.Id <:< NODE#Id) = {
    val id : NODE#Id = n.id
  }
}

object x extends App {
  class MyWorld extends World {
    override type NODE = MyNode
    class MyNode extends Node {
      override val id: Id = new Id {
        override val value: Int = 1
      }
    }
    def addNode(n: MyNode) = {
      val id : Node#Id = n.id
    }
  }
  val w : MyWorld = new MyWorld
  w.addNode(new w.MyNode())
}
