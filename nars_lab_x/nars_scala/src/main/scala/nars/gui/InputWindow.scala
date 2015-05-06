package nars.gui

import java.awt.Button
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.TextArea
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.WindowEvent
import nars.io.ExperienceReader
import nars.io.InputChannel
import nars.main.Reasoner
//remove if not needed
import scala.collection.JavaConversions._

/**
 * Input window, accepting user tasks
 */
class InputWindow(var reasoner: Reasoner, title: String) extends NarsFrame(title + " - Input Window") with ActionListener with InputChannel {

  /**
   Control buttons
   */
  private var okButton: Button = new Button("OK")

  private var holdButton: Button = new Button("Hold")

  private var clearButton: Button = new Button("Clear")

  private var closeButton: Button = new Button("Hide")

  /**
   Input area
   */
  private var inputText: TextArea = new TextArea("")

  /**
   Whether the window is ready to accept new input (in fact whether the Reasoner will read the content of {@link #inputText} )
   */
  private var ready: Boolean = _

  /**
   number of cycles between experience lines
   */
  private var timer: Int = _

//  super(title + " - Input Window")

  setBackground(NarsFrame.SINGLE_WINDOW_COLOR)

  val gridbag = new GridBagLayout()

  val c = new GridBagConstraints()

  setLayout(gridbag)

  c.ipadx = 3

  c.ipady = 3

  c.insets = new Insets(5, 5, 5, 5)

  c.fill = GridBagConstraints.BOTH

  c.gridwidth = GridBagConstraints.REMAINDER

  c.weightx = 1.0

  c.weighty = 1.0

  gridbag.setConstraints(inputText, c)

  add(inputText)

  c.weighty = 0.0

  c.gridwidth = 1

  okButton.addActionListener(this)

  gridbag.setConstraints(okButton, c)

  add(okButton)

  holdButton.addActionListener(this)

  gridbag.setConstraints(holdButton, c)

  add(holdButton)

  clearButton.addActionListener(this)

  gridbag.setConstraints(clearButton, c)

  add(clearButton)

  closeButton.addActionListener(this)

  gridbag.setConstraints(closeButton, c)

  add(closeButton)

  setBounds(0, 40, 400, 210)

  setVisible(true)

  /**
   * Initialize the window
   */
  def init() {
    ready = false
    inputText.setText("")
  }

  /**
   * Handling button click
   * @param e The ActionEvent
   */
  def actionPerformed(e: ActionEvent) {
    val b = e.getSource.asInstanceOf[Button]
    if (b == okButton) {
      ready = true
    } else if (b == holdButton) {
      ready = false
    } else if (b == clearButton) {
      inputText.setText("")
    } else if (b == closeButton) {
      close()
    }
  }

  private def close() {
    setVisible(false)
  }

  override def windowClosing(arg0: WindowEvent) {
    close()
  }

  /**
   * Accept text input in a tick, which can be multiple lines
   * TODO duplicated code with {@link ExperienceReader}
   * @return Whether to check this channel again
   */
  def nextInput(): Boolean = {
    if (timer > 0) {
      timer -= 1
      return true
    }
    if (!ready) {
      return false
    }
    var text = inputText.getText.trim()
    var line: String = ""
    var endOfLine: Int = 0
    while ((text.length > 0) && (timer == 0)) {
      endOfLine = text.indexOf('\n')
      if (endOfLine < 0) {
        line = text
        text = ""
      } else {
        line = text.substring(0, endOfLine).trim()
        text = text.substring(endOfLine + 1)
      }
      try {
        timer = Integer.parseInt(line)
        reasoner.walk(timer)
      } catch {
        case e: NumberFormatException => try {
          reasoner.textInputLine(line)
        } catch {
          case e1: NullPointerException => {
            println("InputWindow.nextInput() - NullPointerException: please correct the input")
            ready = false
            return false
          }
        }
      }
      inputText.setText(text)
      if (text.isEmpty) {
        ready = false
      }
    }
    ((text.length > 0) || (timer > 0))
  }
}
