package it.unibo.scafi.simulation.gui.launcher.scafi

import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.reflection.Demo
import it.unibo.scafi.simulation.gui.util.ClassFinder

/**
  * allow to find all demo class in the package specified
  */
object ListDemo {
  /**
    * the package name where demo are located
    */
  var packageName = ""

  /**
    * get all demo defined
    */
  lazy val demos : List[Class[_]] = ClassFinder.getClasses(packageName).filter(x => x.isAnnotationPresent(classOf[Demo]))
  /**
    * allow to change a string value to class value
    */
  lazy val nameToDemoClass : Map[String,Class[_]] = demos.map(x => x.getSimpleName -> x).toMap
}
