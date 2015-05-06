package nars.io

import java.awt.FileDialog
import java.io._
import nars.gui.InferenceWindow
//remove if not needed
import scala.collection.JavaConversions._

/**
 * Inference log, which record input/output of each inference step
 * jmv: make it an interface, with 2 implementations: GUI or batch, or apply MVC design pattern : append() is the event
 */
class InferenceRecorder {

  /**
   the display window
   */
  private var window: InferenceWindow = new InferenceWindow(this)

  /**
   whether to display
   */
  private var isReporting: Boolean = false

  /**
   the log file
   */
  private var logFile: PrintWriter = null

  /**
   * Initialize the window and the file
   */
  def init() {
    window.clear()
  }

  /**
   * Show the window
   */
  def show() {
    window.setVisible(true)
  }

  /**
   * Begin the display
   */
  def play() {
    isReporting = true
  }

  /**
   * Stop the display
   */
  def stop() {
    isReporting = false
  }

  /**
   * Add new text to display
   * @param s The line to be displayed
   */
  def append(s: String) {
    if (isReporting) {
      window.append(s)
    }
    if (logFile != null) {
      logFile.println(s)
    }
  }

  /**
   * Open the log file
   */
  def openLogFile() {
    val dialog = new FileDialog(null.asInstanceOf[FileDialog], "Inference Log", FileDialog.SAVE)
    dialog.setVisible(true)
    val directoryName = dialog.getDirectory
    val fileName = dialog.getFile
    try {
      logFile = new PrintWriter(new FileWriter(directoryName + fileName))
    } catch {
      case ex: IOException => println("i/o error: " + ex.getMessage)
    }
    window.switchBackground()
    window.setVisible(true)
  }

  /**
   * Close the log file
   */
  def closeLogFile() {
    logFile.close()
    logFile = null
    window.resetBackground()
  }

  /**
   * Check file logging
   * @return If the file logging is going on
   */
  def isLogging(): Boolean = (logFile != null)
}
