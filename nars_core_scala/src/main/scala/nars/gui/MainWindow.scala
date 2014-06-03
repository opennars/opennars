package nars.gui

import java.awt._
import java.awt.event._
import java.util.ArrayList
import nars.io._
import nars.main._
import nars.storage.Memory
//remove if not needed
import scala.collection.JavaConversions._
import NarsFrame._

/**
 * Main window of NARS GUI
 */
class MainWindow(var reasoner: Reasoner, title: String) extends NarsFrame(title) with ActionListener with OutputChannel {

  /**
   Reference to the memory
   */
  private var memory: Memory = reasoner.getMemory

  /**
   Reference to the inference recorder
   */
  private var record: InferenceRecorder = memory.getRecorder

  /**
   Reference to the experience reader
   */
  private var experienceReader: ExperienceReader = _

  /**
   Reference to the experience writer
   */
  private var experienceWriter: ExperienceWriter = new ExperienceWriter(reasoner)

  /**
   Experience display area
   */
  private var ioText: TextArea = new TextArea("")

  /**
   Control buttons
   */
  private var stopButton: Button = new Button(" Stop ")

  private var walkButton: Button = new Button(" Walk ")

  private var runButton: Button = new Button(" Run ")

  private var exitButton: Button = new Button(" Exit ")

  /**
   Clock display field
   */
  private var timerText: TextField = new TextField("")

  /**
   Label of the clock
   */
  private var timerLabel: Label = new Label("Clock:", Label.RIGHT)

  /**
   System clock
   */
  private var timer: Long = _

  /**
   Whether the experience is saving into a file
   */
  private var savingExp: Boolean = false

  /**
   Input experience window
   */
  var inputWindow: InputWindow = reasoner.getInputWindow

  /**
   Window to accept a Term to be looked into
   */
  var conceptWin: TermWindow = new TermWindow(memory)

  /**
   Windows for run-time parameter adjustment
   */
  var forgetTW: ParameterWindow = new ParameterWindow("Task Forgetting Rate", Parameters.TASK_LINK_FORGETTING_CYCLE)

  var forgetBW: ParameterWindow = new ParameterWindow("Belief Forgetting Rate", Parameters.TERM_LINK_FORGETTING_CYCLE)

  var forgetCW: ParameterWindow = new ParameterWindow("Concept Forgetting Rate", Parameters.CONCEPT_FORGETTING_CYCLE)

  var silentW: ParameterWindow = new ParameterWindow("Report Silence Level", Parameters.SILENT_LEVEL)

//  super(title)

  setBackground(NarsFrame.MAIN_WINDOW_COLOR)

  {
  val menuBar = new MenuBar()

  var m = new Menu("File")

  m.add(new MenuItem("Load Experience"))

  m.add(new MenuItem("Save Experience"))

  m.addSeparator()

  m.add(new MenuItem("Record Inference"))

  m.addActionListener(this)

  menuBar.add(m)

  m = new Menu("Memory")

  m.add(new MenuItem("Initialize"))

  m.addActionListener(this)

  menuBar.add(m)

  m = new Menu("View")

  m.add(new MenuItem("Concepts"))

  m.add(new MenuItem("Buffered Tasks"))

  m.add(new MenuItem("Concept Content"))

  m.add(new MenuItem("Inference Log"))

  m.add(new MenuItem("Input Window"))

  m.addActionListener(this)

  menuBar.add(m)

  m = new Menu("Parameter")

  m.add(new MenuItem("Concept Forgetting Rate"))

  m.add(new MenuItem("Task Forgetting Rate"))

  m.add(new MenuItem("Belief Forgetting Rate"))

  m.addSeparator()

  m.add(new MenuItem("Report Silence Level"))

  m.addActionListener(this)

  menuBar.add(m)

  m = new Menu("Help")

  m.add(new MenuItem("Related Information"))

  m.add(new MenuItem("About NARS"))

  m.addActionListener(this)

  menuBar.add(m)

  setMenuBar(menuBar)
  }

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

  ioText.setBackground(DISPLAY_BACKGROUND_COLOR)

  ioText.setEditable(false)

  gridbag.setConstraints(ioText, c)

  add(ioText)

  c.weightx = 0.0

  c.weighty = 0.0

  c.gridwidth = 1

  gridbag.setConstraints(runButton, c)

  runButton.addActionListener(this)

  add(runButton)

  gridbag.setConstraints(walkButton, c)

  walkButton.addActionListener(this)

  add(walkButton)

  gridbag.setConstraints(stopButton, c)

  stopButton.addActionListener(this)

  add(stopButton)

  timerLabel.setBackground(MAIN_WINDOW_COLOR)

  gridbag.setConstraints(timerLabel, c)

  add(timerLabel)

  c.weightx = 1.0

  timerText.setBackground(DISPLAY_BACKGROUND_COLOR)

  timerText.setEditable(false)

  gridbag.setConstraints(timerText, c)

  add(timerText)

  c.weightx = 0.0

  gridbag.setConstraints(exitButton, c)

  exitButton.addActionListener(this)

  add(exitButton)

  setBounds(0, 250, 400, 350)

  setVisible(true)

  initTimer()

  /**
   * Initialize the system for a new run
   */
  def init() {
    initTimer()
    ioText.setText("")
  }

  /**
   * Reset timer and its display
   */
  def initTimer() {
    timer = 0
    timerText.setText(memory.getTime + " :: " + timer)
  }

  /**
   * Update timer and its display
   */
  def tickTimer() {
    timer += 1
    timerText.setText(memory.getTime + " :: " + timer)
  }

  /**
   * Handling button click
   * @param e The ActionEvent
   */
  def actionPerformed(e: ActionEvent) {
    val obj = e.getSource
    if (obj.isInstanceOf[Button]) {
      if (obj == runButton) {
        reasoner.run()
      } else if (obj == stopButton) {
        reasoner.stop()
      } else if (obj == walkButton) {
        reasoner.walk(1)
      } else if (obj == exitButton) {
        close()
      }
    } else if (obj.isInstanceOf[MenuItem]) {
      val label = e.getActionCommand
      if (label == "Load Experience") {
        experienceReader = new ExperienceReader(reasoner)
        experienceReader.openLoadFile()
      } else if (label == "Save Experience") {
        if (savingExp) {
          ioText.setBackground(DISPLAY_BACKGROUND_COLOR)
          experienceWriter.closeSaveFile()
        } else {
          ioText.setBackground(SAVING_BACKGROUND_COLOR)
          experienceWriter.openSaveFile()
        }
        savingExp = !savingExp
      } else if (label == "Record Inference") {
        if (record.isLogging) {
          record.closeLogFile()
        } else {
          record.openLogFile()
        }
      } else if (label == "Initialize") {
        reasoner.reset()
        memory.getExportStrings.add("*****RESET*****")
      } else if (label == "Concepts") {
        memory.conceptsStartPlay("Active Concepts")
      } else if (label == "Buffered Tasks") {
        memory.taskBuffersStartPlay("Buffered Tasks")
      } else if (label == "Concept Content") {
        conceptWin.setVisible(true)
      } else if (label == "Inference Log") {
        record.show()
        record.play()
      } else if (label == "Input Window") {
        inputWindow.setVisible(true)
      } else if (label == "Task Forgetting Rate") {
        forgetTW.setVisible(true)
      } else if (label == "Belief Forgetting Rate") {
        forgetBW.setVisible(true)
      } else if (label == "Concept Forgetting Rate") {
        forgetCW.setVisible(true)
      } else if (label == "Report Silence Level") {
        silentW.setVisible(true)
      } else if (label == "Related Information") {
        new MessageDialog(this, NARS.WEBSITE)
      } else if (label == "About NARS") {
        new MessageDialog(this, NARS.INFO)
      } else {
        new MessageDialog(this, UNAVAILABLE)
      }
    }
  }

  /**
   * Close the whole system
   */
  private def close() {
    setVisible(false)
    System.exit(0)
  }

  override def windowClosing(arg0: WindowEvent) {
    close()
  }

  /**
   * To process the next chunk of output data
   * @param lines The text lines to be displayed
   */
  def nextOutput(lines: ArrayList[String]) {
    if (!lines.isEmpty) {
      var text = ""
      for (line <- lines) {
        text += line + "\n"
      }
      ioText.append(text)
    }
  }

  /**
   * To get the timer value and then to reset it
   * @return The previous timer value
   */
  def updateTimer(): Long = {
    val i = timer
    initTimer()
    i
  }
}
