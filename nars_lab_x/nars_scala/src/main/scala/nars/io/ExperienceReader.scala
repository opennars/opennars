package nars.io

import java.awt.FileDialog
import java.io._
import nars.gui.InputWindow
import nars.main._
//remove if not needed
import scala.collection.JavaConversions._

/**
 * To read and write experience as Task streams
 */
class ExperienceReader(var reasoner: Reasoner) extends InputChannel {

  /**
   Input experience from a file
   */
  private var inExp: BufferedReader = null

  /**
   Remaining working cycles before reading the next line
   */
  private var timer: Int = _

  /**
   Open an input experience file with a FileDialog
   */
  def openLoadFile() {
    val dialog = new FileDialog(null.asInstanceOf[FileDialog], "Load experience", FileDialog.LOAD)
    dialog.setVisible(true)
    val directoryName = dialog.getDirectory
    val fileName = dialog.getFile
    val filePath = directoryName + fileName
    openLoadFile(filePath)
  }

  /**
   Open an input experience file from given file Path
   * @param filePath File to be read as experience
   */
  def openLoadFile(filePath: String) {
    try {
      inExp = new BufferedReader(new FileReader(filePath))
    } catch {
      case ex: IOException => println("i/o error: " + ex.getMessage)
    }
    reasoner.addInputChannel(this)
  }

  /**
   * Close an input experience file
   */
  def closeLoadFile() {
    try {
      inExp.close()
    } catch {
      case ex: IOException => println("i/o error: " + ex.getMessage)
    }
    reasoner.removeInputChannel(this)
  }

  /**
   * Process the next chunk of input data
   * TODO duplicated code with {@link InputWindow}
   * @return Whether the input channel should be checked again
   */
  def nextInput(): Boolean = {
    if (timer > 0) {
      timer -= 1
      return true
    }
    if (inExp == null) {
      return false
    }
    var line: String = null
    while (timer == 0) {
      try {
        line = inExp.readLine()
        if (line == null) {
          inExp.close()
          inExp = null
          return false
        }
      } catch {
        case ex: IOException => println("i/o error: " + ex.getMessage)
      }
      line = line.trim()
      if (line.length > 0) {
        try {
          timer = Integer.parseInt(line)
          reasoner.walk(timer)
        } catch {
          case e: NumberFormatException => reasoner.textInputLine(line)
        }
      }
    }
    true
  }
}
