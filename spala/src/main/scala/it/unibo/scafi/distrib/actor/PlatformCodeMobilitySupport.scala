/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.distrib.actor

import it.unibo.scafi.distrib.actor.extensions.CodeMobilityExtension
import it.unibo.scafi.distrib.{CustomClassLoader, CustomClassLoaderRegistry, LoadClassBytes}
import akka.actor.{Actor, ActorRef}
import akka.util.Timeout
import it.unibo.scafi.distrib.actor.patterns.BasicActorBehavior
import it.unibo.scafi.distrib.actor.serialization.CustomAkkaSerializer

import scala.concurrent.duration._
import scala.util.Success

trait PlatformCodeMobilitySupport { self: Platform.Subcomponent =>

  /**
   * Behavior that should support the retrieval of missing class dependencies.
   * - Missing classes are identified through a [[SystemMsgClassNotFound]] msg
   *   which is produced by the [[CustomAkkaSerializer]] when deserialization
   *   fails due to ClassNotFoundException, and replaces the original message
   * - Then, requests for dependencies are carried out via [[MsgRequestClass]] messages
   * - Finally, responses for dependency requests consist in [[MsgWithClass]]
   *   or [[MsgWithClasses]] messages
   *
   * This behavior uses:
   * - The [[CodeMobilityExtension]] to dynamically access the classloader
   *   for loading classes at runtime
   * - The [[LoadClassBytes]] utility for getting the bytes of a class
   */
  trait MissingCodeManagementBehavior extends BasicActorBehavior { selfActor: Actor =>
    val mobilityExt = CodeMobilityExtension(selfActor.context.system)
    val classloader = mobilityExt.classloader

    override def inputManagementBehavior: Receive = super.inputManagementBehavior.orElse {
      // Management of missing classes
      case SystemMsgClassNotFound(className) => {
        logger.info(s"\nCLASS NOT FOUND: $className")
        sender ! MsgRequestClass(className)
      }
      case MsgRequestClass(className) => {
        sender ! MsgWithClasses(LoadClassBytes(Class.forName(className)), None)
      }
      case MsgWithClass(className, code) => {
        logger.debug("\nGOT CLASS: ${className}")
        //CustomClassLoaderRegistry.register(className, code)
        val kl = classloader.defineClass(className, code)
        logger.debug(s"\nCLASS DEFINED: ${kl}")
      }
    }

    override def commandManagementBehavior: Receive = super.commandManagementBehavior.orElse {
      case MsgWithClasses(classes,corr) => {
        logger.info(s"\nGOT CLASSES: ${classes.keySet}")
        classes.toVector.sortBy(_._1.size).foreach { case (clazz: String, classBytes: Array[Byte]) =>
          //CustomClassLoaderRegistry.register(clazz, classBytes)
          val kl = classloader.defineClass(clazz,classBytes)
          logger.debug(s"\nCLASS DEFINED: ${kl}")
        }
        sender ! MsgAck(corr)
      }
    }
  }

  /**
   * Behavior extension for a computation device that allows its aggregate computation
   *  to be updated.
   */
  trait UpdateableAggregateComputationBehavior
    extends BasicActorBehavior with MissingCodeManagementBehavior { sself: Actor =>
    def setProgram(et: ProgramContract): Unit

    override def inputManagementBehavior: Receive = super.inputManagementBehavior.orElse {
      // Program management
      case MsgProgram(et,_) => {
        setProgram(et)
        logger.info(s"\nGot my program: $et")
      }
    }
  }

  /**
   * Behavior for an actor that allows code to be shipped to some recipient actors.
   */
  trait CodeMobilitySupportBehavior { selfType: Actor =>
    def codeShippingRecipients(): Set[ActorRef]

    def isLocalActor(aRef: ActorRef): Boolean =
      aRef.path.root == self.path.root

    def codeMobilitySupportBehavior: Receive = {
      case MsgShipProgram(MsgProgram(program, deps)) =>
        shipProgram(program, deps, codeShippingRecipients())
    }

    import akka.pattern.ask
    import context.dispatcher
    implicit val timeout: Timeout = 2.seconds

    def shipProgram(program: ProgramContract,
                    dependencies: Set[Class[_]] = Set(),
                    recipients: Set[ActorRef]): Unit = {
      // Load the code associated to the program and its dependencies
      var classes = LoadClassBytes(program.getClass)
      dependencies.foreach(classes ++= LoadClassBytes(_))
      val msg = MsgProgram(program)

      // Ship the code
      recipients.foreach { aRef =>

        if(isLocalActor(aRef)) {
          // Local actor
          aRef ! msg
        } else {
          // Remote actor ref: the destination actor may not have the program
          val corr = aRef.hashCode() + "-" + System.currentTimeMillis()
          (aRef ? MsgWithClasses(classes,Some(corr))).onComplete {
            case Success(MsgAck(Some(corr))) =>
              aRef ! msg
          }
        }
      }
    }
  }

}
