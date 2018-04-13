package it.unibo.scafi.simulation.gui.incarnation.scafi

import it.unibo.scafi.simulation.gui.controller.logical.{AsyncLogicController, LogicController}
import it.unibo.scafi.simulation.gui.model.space.Point3D

import scala.util.Random

class MovementSyncController[N <: ScafiLikeWorld#NODE] (velocity : Float, world : ScafiLikeWorld,nodes : Int) extends LogicController[ScafiLikeWorld]{
  var started= false
  val rand = new Random()
  val offsetValue = 1/2.0
  private var idVelocity = world.nodes map {x => x.id -> (math.random * offsetValue - (offsetValue/2), math.random * offsetValue - (offsetValue/2))} toMap
  override def onTick(float: Float): Unit = {
    if(started) {
      val vel = rand.nextFloat()
      val nodesToUpdate = (0 until nodes) map { x => rand.nextInt(world.nodes.size) }
      nodesToUpdate foreach {
        x => {
          val optionNode = world(x)
          if(optionNode.isDefined) {
            val Point3D(x,y,z) = optionNode.get.position
            val id = optionNode.get.id
            if(!world.moveNode(id, Point3D(x + idVelocity(id)._1 * float , y + idVelocity(id)._2 * float, z))) {
              val lastVelocity = idVelocity(id)
              idVelocity += id -> (-lastVelocity._1, -lastVelocity._2)
            }
          }
        }

      }
    }
  }

  /**
    * start the internal logic
    * @throws IllegalStateException if the simulation is started
    */
  def start() = {
    require(!started)
    started = true
  };

  /**
    * stop the internal logic
    * @throws IllegalStateException if the simulation is stopped
    */
  def stop() = {
    require(started)
    started = false;
  }

}
