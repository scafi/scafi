package it.unibo.scafi.simulation.gui.utility

/**
  * Created by Varini on 15/11/16.
  * Converted/refactored to Scala by Casadei on 05/02/17
  */

import javax.swing.filechooser.FileFilter
import java.io.File

/* ImageFilter.java is used by FileChooserDemo2.java. */
class ImageFilter extends FileFilter {
  //Accept all directories and all gif, jpg, tiff, or png files.
  def accept(f: File): Boolean = {
    if (f.isDirectory) {
      return true
    }
    val extension: String = Utils.getExtension(f)
    if (extension != null) {
      if (extension == Utils.tiff || extension == Utils.tif || extension == Utils.gif || extension == Utils.jpeg || extension == Utils.jpg || extension == Utils.png) {
        return true
      }
      else {
        return false
      }
    }
    return false
  }

  //The description of this filter
  def getDescription: String = {
    return "Just Images"
  }
}