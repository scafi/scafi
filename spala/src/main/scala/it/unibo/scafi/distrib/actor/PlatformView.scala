/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.distrib.actor

import java.awt.{Dimension, Toolkit}

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import it.unibo.scafi.space.Point2D
import javax.swing.{JComponent, JFrame, WindowConstants}

import scala.annotation.tailrec

trait PlatformView { self: Platform.Subcomponent =>
  class DevsGUIActor() extends Actor {
    val frame = new JFrame("Devices GUI")
    protected val Log = akka.event.Logging(context.system, this)
    private var devActors: Map[ActorRef, DevInfo] = Map()

    buildFrame()

    override def receive: Receive = {
      case MsgAddDevComponent(ref, comp) =>
        if (!frame.isVisible) frame.setVisible(true)
        devActors += ref -> devActors.getOrElse(ref, DevInfo()).copy(comp = Some(comp))
        frame.add(comp)
        frame.revalidate(); frame.repaint()
      case MsgDevName(ref, id) => devActors += ref -> devActors.getOrElse(ref, DevInfo()).copy(id = Some(id))
      case MsgDevPosition(ref, pos) => devActors += ref -> devActors.getOrElse(ref, DevInfo()).copy(pos = Some(pos))
      case MsgGetNeighborhood(id) => val nbrs: Map[ActorRef, DevInfo] = computeNeighborhood(id)
        sender ! MsgNeighborhoodUpdate(id, nbrs.map(d => d._2.id.get -> d._1))
      case msg => Log.debug("[DevsGUIActor] Message unhandled: " + msg); unhandled(msg)
    }

    private def buildFrame(): Unit = {
      val dim: Dimension = Toolkit.getDefaultToolkit.getScreenSize
      frame.setBounds(0, 0, dim.getWidth.toInt, dim.getHeight.toInt)
      frame.setLocationRelativeTo(null)
      frame.setLayout(null)
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
    }

    private def computeNeighborhood(id: UID): Map[ActorRef, DevInfo] = {
      def distance(p1: Option[Point2D], p2: Option[Point2D]): Double = {
        if (p1.isEmpty || p2.isEmpty) Double.PositiveInfinity else p1.get.distance(p2.get)
      }
      @tailrec
      def findNbrs(pos: Option[Point2D], devs: List[(ActorRef, DevInfo)], res: List[(ActorRef, DevInfo)]): List[(ActorRef, DevInfo)] =
        devs match {
          case h :: t if distance(pos, h._2.pos) <= 1  => findNbrs(pos, t, h::res)
          case _ :: t => findNbrs(pos, t, res)
          case Nil => res
        }

      val dev: Option[DevInfo] = devActors.values.toSet.find(_.id.contains(id))
      if (dev.isDefined) {
        val nbrsList = findNbrs(dev.get.pos, devActors.map(d => (d._1, d._2)).toList, List()).filterNot(_._2.id == dev.get.id)
        nbrsList.map(d => d._1 -> d._2).toMap
      } else {
        Map()
      }
    }

    private case class DevInfo(var id: Option[UID] = None,
                               var pos: Option[Point2D] = None,
                               var comp: Option[JComponent] = None)
  }

  object DevicesGUI {
    private var _actor: Option[ActorRef] = None
    def setupGui(actorSys: ActorSystem): Unit = if (_actor.isEmpty) _actor =
      Some(actorSys.actorOf(Props(classOf[DevsGUIActor], self)))
    def actor: Option[ActorRef] = _actor
  }
}
