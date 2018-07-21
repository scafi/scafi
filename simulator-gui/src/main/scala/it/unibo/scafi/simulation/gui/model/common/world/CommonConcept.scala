package it.unibo.scafi.simulation.gui.model.common.world

import it.unibo.scafi.simulation.gui.model.core.World

/**
  * Some concept use in observable word
  */
trait CommonConcept {
  self : World =>
  /**
    * the world has an internal representation of node that is mutable
    * outside user can see only a view on node.
    * the view object can be linked with internal representation(outside user
    * can't change the state of object but can only see internal state changes)
    * or not (view is an immutable copy of the state of node at particular moment)
    *
    * the type is protected because only the trait or class that extend or mix this trait
    * can see internal definition
    */

  protected type MUTABLE_NODE <: RootMutableNode

  override type NODE <: Node
  /**
    * the type of node producer uses by client to give a skeleton of node representation
    *
    */
  type NODE_PRODUCER <: RootNodeProducer
  /**
    * an implicit method used to transform mutable node object in
    * a node view
    * @param node the mutable node
    * @return an instance of node
    */
  protected implicit def mutableToNode(node : MUTABLE_NODE) : NODE = node.view

  /**
    * roo trait of mutable node
    */
  trait RootMutableNode <: Node {
    /**
      * @return a view of mutable node, can be linked with this instance or not
      */
    def view : NODE
  }

  /**
    * the root of all node producer
    */
  trait RootNodeProducer {
    /**
      * create an internal node
      * @return the node created
      */
    def build() : MUTABLE_NODE
  }

}

object CommonConcept {
  type Dependency = AbstractObservableWorld
}
