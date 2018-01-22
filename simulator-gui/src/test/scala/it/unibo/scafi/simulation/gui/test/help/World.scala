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

  def addNode(n: NODE) = {
    val id : NODE#Id = n.id //ERRORE si aspetta World.this.NODE#Id e trova invece World.this.Node#Id
    nodes += id -> n
  }
}

object x extends App {
  class MyWorld extends World {
    override type NODE = MyNode
    class MyNode extends Node {
      override val id: ID = new Id {
        override val value: Int = 1
      }
    }
  }

  val w : MyWorld = new MyWorld
  w.addNode(new w.MyNode())
}
