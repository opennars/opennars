package nars.main

import java.applet.Applet
import nars.io.ExperienceReader
import NARS._
import scala.reflect.{BeanProperty, BooleanBeanProperty}
//remove if not needed
import scala.collection.JavaConversions._

object NARS {

  /**
   * The information about the version and date of the project.
   */
  val INFO = "     Open-NARS     Version 1.5.1     February 2013  \n"

  /**
   * The project websites.
   */
  val WEBSITE = " Open-NARS website:  http://code.google.com/p/open-nars/ \n" + 
    "      NARS website:  http://sites.google.com/site/narswang/ "

  /**
   * Flag to distinguish the two running modes of the project.
   */
  @BooleanBeanProperty
  var standAlone: Boolean = false

  /**
   * The entry point of the standalone application.
   * <p>
   * Create an instance of the class, then run the init and start methods.
   * @param args optional argument used : one input file
   */
  def main(args : Array[String] ) {
    standAlone = true
    val nars = new NARS()
    nars.init(args)
    nars.start()
  }
}

/**
 * The main class of the project.
 * <p>
 * Define an application with full funcationality and an applet with partial functionality.
 * <p>
 * Manage the internal working thread. Communicate with Reasoner only.
 */
class NARS extends Applet with Runnable {

  /**
   * The internal working thread of the system.
   */
  var narsThread: Thread = null

  /**
   * The reasoner
   */
  var reasoner: Reasoner = _

  def init(args: Array[String]) {
    init()
    if (args.length > 0) {
      val experienceReader = new ExperienceReader(reasoner)
      experienceReader.openLoadFile(args(0))
    }
  }

  /**
   * Initialize the system at the control center.<p>
   * Can instantiate multiple reasoners
   */
  override def init() {
    reasoner = new Reasoner("NARS Reasoner")
  }

  /**
   * Start the thread if necessary, called when the page containing the applet first appears on the screen.
   */
  override def start() {
    if (narsThread == null) {
      narsThread = new Thread(this)
      narsThread.start()
    }
  }

  /**
   * Called when the page containing the applet is no longer on the screen.
   */
  override def stop() {
    narsThread = null
  }

  /**
   * Repeatedly execute NARS working cycle. This method is called when the Runnable's thread is started.
   */
  override def run() {
    val thisThread = Thread.currentThread()
    while (narsThread == thisThread) {
      try {
        Thread.sleep(1)
      } catch {
        case e: InterruptedException => 
      }
      try {
        reasoner.tick()
      } catch {
        case e: Exception => e.printStackTrace()
      }
    }
  }

  /**
   * Provide system information for the applet.
   * @return The string containing the information about the applet.
   */
  override def getAppletInfo(): String = INFO
}
