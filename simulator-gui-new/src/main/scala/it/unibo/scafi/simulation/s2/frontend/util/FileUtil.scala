package it.unibo.scafi.simulation.s2.frontend.util

import java.io.FileWriter

import scala.reflect.io.Directory

/**
 * an object that describe a set of file util
 */
object FileUtil {
  lazy val homeDirectory: String = System.getProperty("user.home")
  lazy val separator: String = System.getProperty("file.separator")
  lazy val outputDirectory: Directory = Directory(homeDirectory + separator + ".scafi-output")

  /**
   * create a path start to home
   * @param file
   *   name file
   * @param home
   *   the home file
   * @return
   *   the string form of path created
   */
  def path(file: String)(implicit home: String): String = home + separator + file

  /**
   * append a value in file passed
   * @param file
   *   the file name
   * @param value
   *   the value to append
   */
  def append(file: String, value: String): Unit = {
    val fw = new FileWriter(file, true)
    try fw.write(value + "\n")
    finally fw.close()
  }

  /**
   * write a file with value passed
   * @param file
   *   the file name
   * @param value
   *   the value to write
   */
  def write(file: String, value: String): Unit = try {
    val fw = new FileWriter(file, false)
    try fw.append(value + "\n")
    finally fw.close()
  }

}
