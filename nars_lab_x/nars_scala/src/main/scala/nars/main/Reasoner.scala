package nars.main

import java.util.ArrayList
import javax.swing.SwingUtilities
import nars.storage._
import nars.io._
import nars.gui._
import nars.logic.entity._
import scala.reflect.{BeanProperty, BooleanBeanProperty}
//remove if not needed
import scala.collection.JavaConversions._

/**
 * A NARS Reasoner has its memory, I/O channels, and internal clock.
 * <p>
 * Create static main window and input channel, reset memory, and manage system clock.
 */
class Reasoner(var name: String) {

  /**
   The memory of the reasoner
   */
  @BeanProperty
  var memory: Memory = new Memory(this)

  /**
   The input channels of the reasoner
   */
  private var inputChannels: ArrayList[InputChannel] = new ArrayList[InputChannel]()

  /**
   The output channels of the reasoner
   */
  private var outputChannels: ArrayList[OutputChannel] = new ArrayList[OutputChannel]()

  /**
   The unique main window
   */
  @BeanProperty
  var mainWindow: MainWindow = new MainWindow(this, name)

  /**
   Input experience from a window
   */
  @BeanProperty
  var inputWindow: InputWindow = new InputWindow(this, name)

  /**
   System clock, relatively defined to guarantee the repeatability of behaviors
   */
  private var clock: Long = _

  /**
   Flag for running continuously
   */
  private var running: Boolean = _

  /**
   The number of steps to be carried out
   */
  private var walkingSteps: Int = _

  @BooleanBeanProperty
  var finishedInputs: Boolean = _

  inputChannels.add(inputWindow)

  outputChannels.add(mainWindow)

  /**
   * Reset the system with an empty memory and reset clock. Called locally and from MainWindow.
   */
  def reset() {
    running = false
    walkingSteps = 0
    clock = 0
    memory.init()
    Stamp.init()
  }

  def addInputChannel(channel: InputChannel) {
    inputChannels.add(channel)
  }

  def removeInputChannel(channel: InputChannel) {
    inputChannels.remove(channel)
  }

  def addOutputChannel(channel: OutputChannel) {
    outputChannels.add(channel)
  }

  def removeOutputChannel(channel: OutputChannel) {
    outputChannels.remove(channel)
  }

  /**
   * Get the current time from the clock
   * Called in nars.logic.entity.Stamp
   * @return The current time
   */
  def getTime(): Long = clock

  /**
   * Start the inference process
   */
  def run() {
    running = true
  }

  /**
   * Carry the inference process for a certain number of steps
   * @param n The number of inference steps to be carried
   */
  def walk(n: Int) {
    walkingSteps = n
  }

  /**
   * Stop the inference process
   */
  def stop() {
    running = false
  }

  /**
   * A clock tick. Run one working workCycle or read input. Called from NARS only.
   */
  def tick() {
    SwingUtilities.invokeLater(new Runnable() {

      override def run() {
        if (walkingSteps == 0) {
          var reasonerShouldRun = false
          for (channelIn <- inputChannels) {
            reasonerShouldRun = reasonerShouldRun || channelIn.nextInput()
          }
          finishedInputs = !reasonerShouldRun
        }
        val output = memory.getExportStrings
        if (!output.isEmpty) {
          for (channelOut <- outputChannels) {
            channelOut.nextOutput(output)
          }
          output.clear()
        }
        if (running || walkingSteps > 0) {
          clock += 1
          mainWindow.tickTimer()
          memory.workCycle(clock)
          if (walkingSteps > 0) {
            walkingSteps -= 1
          }
        }
      }
    })
  }

  /**
   * To process a line of input text
   * @param text
   */
  def textInputLine(text: String) {
    if (text.isEmpty) {
      return
    }
    val c = text.charAt(0)
    if (c == Symbols.RESET_MARK) {
      reset()
      memory.getExportStrings.add(text)
    } else if (c == Symbols.COMMENT_MARK) {
      return
    } else {
      try {
        val i = Integer.parseInt(text)
        walk(i)
      } catch {
        case e: NumberFormatException => {
          val task = StringParser.parseExperience(new StringBuffer(text), memory, clock)
          if (task != null) {
            memory.inputTask(task)
          }
        }
      }
    }
  }

  override def toString(): String = memory.toString
}
