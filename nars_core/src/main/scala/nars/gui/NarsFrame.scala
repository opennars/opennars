package nars.gui

import java.awt._
import java.awt.event._
import NarsFrame._
//remove if not needed
import scala.collection.JavaConversions._

object NarsFrame {

  /**
   Color for the background of the main window
   */
  val MAIN_WINDOW_COLOR = new Color(120, 120, 255)

  /**
   Color for the background of the windows with unique instantiation
   */
  val SINGLE_WINDOW_COLOR = new Color(180, 100, 230)

  /**
   Color for the background of the windows with multiple instantiations
   */
  val MULTIPLE_WINDOW_COLOR = new Color(100, 220, 100)

  /**
   Color for the background of the text components that are read-only
   */
  val DISPLAY_BACKGROUND_COLOR = new Color(200, 230, 220)

  /**
   Color for the background of the text components that are being saved into a file
   */
  val SAVING_BACKGROUND_COLOR = new Color(216, 216, 128)

  /**
   Font for NARS GUI
   */
  val NarsFont = new Font("Helvetica", Font.PLAIN, 11)

  /**
   Message for unimplemented functions
   */
  val UNAVAILABLE = "\n Not implemented in this demo applet."
}

/**
 * Specify shared properties of NARS windows
 */
abstract class NarsFrame() extends Frame() with WindowListener {

//  super()

  addWindowListener(this)

  /**
   * Constructor with title and font setting
   * @param title The title displayed by the window
   */
  def this(title: String) {
//    super(" " + title)
    this()
    setTitle(" " + title)
    setFont(NarsFont)
    addWindowListener(this)
  }

  override def windowActivated(arg0: WindowEvent) {
  }

  override def windowClosed(arg0: WindowEvent) {
  }

  override def windowClosing(arg0: WindowEvent) {
  }

  override def windowDeactivated(arg0: WindowEvent) {
  }

  override def windowDeiconified(arg0: WindowEvent) {
  }

  override def windowIconified(arg0: WindowEvent) {
  }

  override def windowOpened(arg0: WindowEvent) {
  }
}
