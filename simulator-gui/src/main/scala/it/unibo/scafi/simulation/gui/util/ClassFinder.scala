package it.unibo.scafi.simulation.gui.util

import java.io.{File, IOException}

/**
  * allow to find clss in a package
  */
object ClassFinder {
  /**
    * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
    *
    * @param packageName The base package
    * @return The classes
    * @throws ClassNotFoundException
    * @throws IOException
    */
  @throws[ClassNotFoundException]
  @throws[IOException]
  def getClasses(packageName: String): List[Class[_]] = {
    val classLoader = Thread.currentThread.getContextClassLoader
    assert(classLoader != null)
    val path = packageName.replace('.', '/')
    val resources = classLoader.getResources(path)
    var dirs = List.empty[File]
    while ( {
      resources.hasMoreElements
    }) {
      val resource = resources.nextElement
      val file : File = new File(resource.getFile)
      dirs = file :: dirs
    }
    var classes = List.empty[Class[_]]
    for (directory <- dirs) {
      classes = (findClasses(directory, packageName)) ::: classes
    }
    classes
  }

  /**
    * Recursive method used to find all classes in a given directory and subdirs.
    *
    * @param directory   The base directory
    * @param packageName The package name for classes found inside the base directory
    * @return The classes
    * @throws ClassNotFoundException
    */
  @throws[ClassNotFoundException]
  private def findClasses(directory: File, packageName: String): List[Class[_]] = {
    var classes = List.empty[Class[_]]
    if (!directory.exists) return classes
    val files = directory.listFiles
    for (file <- files) {
      if (file.isDirectory) {
        assert(!file.getName.contains("."))
        classes = classes ::: (findClasses(file, packageName + "." + file.getName))
      } else if (file.getName.endsWith(".class")) {
        classes = Class.forName(packageName + '.' + file.getName.substring(0, file.getName.length - 6)) :: classes
      }
    }
    classes
  }
}
