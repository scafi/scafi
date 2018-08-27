package it.unibo.scafi.simulation.gui.util

import java.io.FileWriter

import scala.reflect.io.Directory

object FileUtil {
  lazy val homeDirectory = System.getProperty("user.home")
  lazy val outputDirectory = Directory(homeDirectory + "\\" + ".scafi-output")

  def path (file : String)(implicit home : String) : String = home + "\\" + file
  def append(file : String, value : String) : Unit = {
    val fw = new FileWriter(file,true)
    try {
      fw.write(value + "\n")
    } finally fw.close()
  }

  def write(file : String, value : String ) : Unit = try {
    val fw = new FileWriter(file,false)
    try {
      fw.append(value + "\n")
    } finally fw.close()
  }

}
