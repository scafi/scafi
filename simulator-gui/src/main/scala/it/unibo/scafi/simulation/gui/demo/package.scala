package it.unibo.scafi.simulation.gui

import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.reflection.Demo
import it.unibo.scafi.simulation.gui.util.ClassFinder

package object demo {
  /**
    * get all demo defined
    */
  lazy val demos : List[Class[_]] = ClassFinder.getClasses("sims").filter(x => x.isAnnotationPresent(classOf[Demo]))
  /**
    * allow to change a string value to class value
    */
  lazy val nameToDemoClass : Map[String,Class[_]] = demos.map(x => x.getSimpleName -> x).toMap
}
