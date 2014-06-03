package nars.main_nogui

import java.io.PrintStream
import java.io.PrintWriter
import nars.io.ExperienceReader
import nars.io.ExperienceWriter
import nars.main.Reasoner
import NARSBatch._
//remove if not needed
import scala.collection.JavaConversions._

object NARSBatch {

  /**
   The entry point of the standalone application.
   * <p>
   * Create an instance of the class, then run the {@link #init(String[])} and {@link #run()} methods.
   * @param args optional argument used : one input file
   */
  def main(args: Array[String]) {
    val nars = new NARSBatch()
    nars.runInference(args)
    if (nars.dumpLastState) println("==== Dump Last State ====\n" + nars.reasoner.toString)
  }
}

/**
 * The main class of the project.
 * <p>
 * Define an application with batch functionality;
 * TODO duplicated code with {@link nars.main.NARS}
 * TODO still instantiates windows
 * <p>
 * Manage the internal working thread. Communicate with Reasoner only.
 */
class NARSBatch {

  /**
   The reasoner
   */
  var reasoner: Reasoner = _

  private var logging: Boolean = _

  private var out: PrintStream = System.out

  private var dumpLastState: Boolean = true

  /**
   non-static equivalent to {@link #main(String[])}
   */
  def runInference(args: Array[String]) {
    init(args)
    run()
  }

  def init(args: Array[String]) {
    init()
    if (args.length > 0) {
      val experienceReader = new ExperienceReader(reasoner)
      experienceReader.openLoadFile(args(0))
    }
    reasoner.addOutputChannel(new ExperienceWriter(reasoner, new PrintWriter(out, true)))
  }

  /**
   Initialize the system at the control center.<p>
   * Can instantiate multiple reasoners
   */
  def init() {
    reasoner = new Reasoner("NARS Batch Reasoner")
    reasoner.getMainWindow.setVisible(false)
    reasoner.getInputWindow.setVisible(false)
    reasoner.removeInputChannel(reasoner.getInputWindow)
  }

  /**
   Repeatedly execute NARS working cycle, until Inputs are Finished, or 1000 steps.
   * This method is called when the Runnable's thread is started.
   */
  def run() {
//    while (true) {
    while ( ! reasoner.isFinishedInputs && (reasoner.getTime <= 1000) ) {
      log("NARSBatch.run():" + " step " + reasoner.getTime + " " + 
        reasoner.isFinishedInputs)
      reasoner.tick()
      log("NARSBatch.run(): after tick" + " step " + reasoner.getTime + 
        " " + 
        reasoner.isFinishedInputs)
//      if (reasoner.isFinishedInputs || reasoner.getTime == 1000) //break
    }
    reasoner.getMainWindow.dispose()
    reasoner.getInputWindow.dispose()
  }

  def setPrintStream(out: PrintStream) {
    this.out = out
  }

  private def log(mess: String) {
    if (logging) println("/ " + mess)
  }
}
