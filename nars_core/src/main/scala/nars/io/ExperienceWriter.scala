package nars.io

import java.awt.FileDialog
import java.io._
import java.util._
import nars.main._
//remove if not needed
import scala.collection.JavaConversions._

/**
 * To read and write experience as Task streams
 */
class ExperienceWriter(var reasoner: Reasoner) extends OutputChannel {

  /**
   Input experience from a file
   */
  private var outExp: PrintWriter = _

  def this(reasoner: Reasoner, outExp: PrintWriter) {
    this(reasoner)
    this.outExp = outExp
  }

  /**
   * Open an output experience file
   */
  def openSaveFile() {
    val dialog = new FileDialog(null.asInstanceOf[FileDialog], "Save experience", FileDialog.SAVE)
    dialog.setVisible(true)
    val directoryName = dialog.getDirectory
    val fileName = dialog.getFile
    try {
      outExp = new PrintWriter(new FileWriter(directoryName + fileName))
    } catch {
      case ex: IOException => println("i/o error: " + ex.getMessage)
    }
    reasoner.addOutputChannel(this)
  }

  /**
   * Close an output experience file
   */
  def closeSaveFile() {
    outExp.close()
    reasoner.removeOutputChannel(this)
  }

  /**
   * Process the next chunk of output data
   * @param lines The text to be displayed
   */
  def nextOutput(lines: ArrayList[String]) {
    if (outExp != null) {
      for (line <- lines) {
        outExp.println(line.toString)
      }
    }
  }
}
