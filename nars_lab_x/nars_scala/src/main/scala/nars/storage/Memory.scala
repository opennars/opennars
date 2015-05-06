package nars.storage

import nars.main._
import java.util._
import nars.logic.entity._
import nars.logic._
import nars.io._
import nars.gui._
import nars.logic.language._
import scala.reflect.{BeanProperty, BooleanBeanProperty}
//remove if not needed
import scala.collection.JavaConversions._

/**
 * The memory of the system.
 */
class Memory(var reasoner: Reasoner) {

  /**
   Concept bag. Containing all Concepts of the system
   */
  private var concepts: ConceptBag = new ConceptBag(this)

  /**
   New tasks with novel composed terms, for delayed and selective processing
   */
  private var novelTasks: NovelTaskBag = new NovelTaskBag(this)

  /**
   Inference record text to be written into a log file
   */
  @BeanProperty
  var recorder: InferenceRecorder = new InferenceRecorder()

  /**
   List of new tasks accumulated in one cycle, to be processed in the next cycle
   */
  private var newTasks: ArrayList[Task] = new ArrayList[Task]()

  /**
   List of Strings or Tasks to be sent to the output channels
   */
  @BeanProperty
  var exportStrings: ArrayList[String] = new ArrayList[String]()

  /**
   The selected Term
   */
  var currentTerm: Term = _

  /**
   The selected Concept
   */
  var currentConcept: Concept = _

  /**
   The selected TaskLink
   */
  var currentTaskLink: TaskLink = _

  /**
   The selected Task
   */
  var currentTask: Task = _

  /**
   The selected TermLink
   */
  var currentBeliefLink: TermLink = _

  /**
   The selected belief
   */
  var currentBelief: Sentence = _

  /**
   The new Stamp
   */
  var newStamp: Stamp = _

  /**
   The substitution that unify the common term in the Task and the Belief
   * TODO unused
   */
  protected var substitute: HashMap[Term, Term] = _

  def init() {
    concepts.init()
    novelTasks.init()
    newTasks.clear()
    exportStrings.clear()
    reasoner.getMainWindow.initTimer()
    recorder.append("\n-----RESET-----\n")
  }

  def getTime(): Long = reasoner.getTime

  def getMainWindow(): MainWindow = reasoner.getMainWindow

  def noResult(): Boolean = newTasks.isEmpty

  /**
   * Get an existing Concept for a given name
   * <p>
   * called from Term and ConceptWindow.
   * @param name the name of a concept
   * @return a Concept or null
   */
  def nameToConcept(name: String): Concept = concepts.get(name)

  /**
   * Get a Term for a given name of a Concept or Operator
   * <p>
   * called in StringParser and the make methods of compound terms.
   * @param name the name of a concept or operator
   * @return a Term or null (if no Concept/Operator has this name)
   */
  def nameToListedTerm(name: String): Term = {
    val concept = concepts.get(name)
    if (concept != null) {
      return concept.getTerm
    }
    null
  }

  /**
   * Get an existing Concept for a given Term.
   * @param term The Term naming a concept
   * @return a Concept or null
   */
  def termToConcept(term: Term): Concept = nameToConcept(term.getName)

  /**
   * Get the Concept associated to a Term, or create it.
   * @param term indicating the concept
   * @return an existing Concept, or a new one
   */
  def getConcept(term: Term): Concept = {
    if (!term.isConstant) {
      return null
    }
    val n = term.getName
    var concept = concepts.get(n)
    if (concept == null) {
      concept = new Concept(term, this)
      val created = concepts.putIn(concept)
      if (!created) {
        return null
      }
    }
    concept
  }

  /**
   * Adjust the activation level of a Concept
   * <p>
   * called in Concept.insertTaskLink only
   * @param c the concept to be adjusted
   * @param b the new BudgetValue
   */
  def activateConcept(c: Concept, b: BudgetValue) {
    concepts.pickOut(c.getKey)
    BudgetFunctions.activate(c, b)
    concepts.putBack(c)
  }

  /**
   * Input task processing. Invoked by the outside or inside environment.
   * Outside: StringParser (input); Inside: Operator (feedback).
   * Input tasks with low priority are ignored, and the others are put into task buffer.
   * @param task The input task
   */
  def inputTask(task: Task) {
    if (task.getBudget.aboveThreshold()) {
      recorder.append("!!! Perceived: " + task + "\n")
      report(task.getSentence, true)
      newTasks.add(task)
    } else {
      recorder.append("!!! Neglected: " + task + "\n")
    }
  }

  /**
   * Activated task called in MatchingRules.trySolution and Concept.processGoal
   * @param budget The budget value of the new Task
   * @param sentence The content of the new Task
   * @param candidateBelief The belief to be used in future inference, for forward/backward correspondence
   */
  def activatedTask(budget: BudgetValue, sentence: Sentence, candidateBelief: Sentence) {
    val task = new Task(sentence, budget, currentTask, sentence, candidateBelief)
    recorder.append("!!! Activated: " + task.toString + "\n")
    if (sentence.isQuestion) {
      val s = task.getBudget.summary()
      val minSilent = reasoner.getMainWindow.silentW.value() / 100.0f
      if (s > minSilent) {
        report(task.getSentence, false)
      }
    }
    newTasks.add(task)
  }

  /**
   * Derived task comes from the inference rules.
   * @param task the derived task
   */
  private def derivedTask(task: Task) {
    if (task.getBudget.aboveThreshold()) {
      recorder.append("!!! Derived: " + task + "\n")
      val budget = task.getBudget.summary()
      val minSilent = reasoner.getMainWindow.silentW.value() / 100.0f
      if (budget > minSilent) {
        report(task.getSentence, false)
      }
      newTasks.add(task)
    } else {
      recorder.append("!!! Ignored: " + task + "\n")
    }
  }

  /**
   * Shared final operations by all double-premise rules, called from the rules except StructuralRules
   * @param newContent The content of the sentence in task
   * @param newTruth The truth value of the sentence in task
   * @param newBudget The budget value in task
   */
  def doublePremiseTask(newContent: Term, newTruth: TruthValue, newBudget: BudgetValue) {
    if (newContent != null) {
      val newSentence = new Sentence(newContent, currentTask.getSentence.getPunctuation, newTruth, newStamp)
      val newTask = new Task(newSentence, newBudget, currentTask, currentBelief)
      derivedTask(newTask)
    }
  }

  /**
   * Shared final operations by all double-premise rules, called from the rules except StructuralRules
   * @param newContent The content of the sentence in task
   * @param newTruth The truth value of the sentence in task
   * @param newBudget The budget value in task
   * @param revisible Whether the sentence is revisible
   */
  def doublePremiseTask(newContent: Term, 
      newTruth: TruthValue, 
      newBudget: BudgetValue, 
      revisible: Boolean) {
    if (newContent != null) {
      val taskSentence = currentTask.getSentence
      val newSentence = new Sentence(newContent, taskSentence.getPunctuation, newTruth, newStamp, revisible)
      val newTask = new Task(newSentence, newBudget, currentTask, currentBelief)
      derivedTask(newTask)
    }
  }

  /**
   * Shared final operations by all single-premise rules, called in StructuralRules
   * @param newContent The content of the sentence in task
   * @param newTruth The truth value of the sentence in task
   * @param newBudget The budget value in task
   */
  def singlePremiseTask(newContent: Term, newTruth: TruthValue, newBudget: BudgetValue) {
    singlePremiseTask(newContent, currentTask.getSentence.getPunctuation, newTruth, newBudget)
  }

  /**
   * Shared final operations by all single-premise rules, called in StructuralRules
   * @param newContent The content of the sentence in task
   * @param punctuation The punctuation of the sentence in task
   * @param newTruth The truth value of the sentence in task
   * @param newBudget The budget value in task
   */
  def singlePremiseTask(newContent: Term, 
      punctuation: Char, 
      newTruth: TruthValue, 
      newBudget: BudgetValue) {
    val taskSentence = currentTask.getSentence
    newStamp = if (taskSentence.isJudgment || currentBelief == null) new Stamp(taskSentence.getStamp, 
      getTime) else new Stamp(currentBelief.getStamp, getTime)
    val newSentence = new Sentence(newContent, punctuation, newTruth, newStamp, taskSentence.getRevisible)
    val newTask = new Task(newSentence, newBudget, currentTask, null)
    derivedTask(newTask)
  }

  /**
   * An atomic working workCycle of the system: process new Tasks, then fire a concept
   * <p>
   * Called from Reasoner.tick only
   * @param clock The current time to be displayed
   */
  def workCycle(clock: Long) {
    recorder.append(" --- " + clock + " ---\n")
    processNewTask()
    if (noResult()) {
      processNovelTask()
    }
    if (noResult()) {
      processConcept()
    }
    novelTasks.refresh()
  }

  /**
   * Process the newTasks accumulated in the previous workCycle, accept input ones
   * and those that corresponding to existing concepts, plus one from the buffer.
   */
  private def processNewTask() {
    var task: Task = null
    var counter = newTasks.size
    //    while (counter-- > 0) { // Java
//    while (counter -= 1 > 0) {
    while (counter > 1) {
      counter -= 1
      task = newTasks.remove(0)
      if (task.isInput || (termToConcept(task.getContent) != null)) {
        immediateProcess(task)
      } else {
        val s = task.getSentence
        if (s.isJudgment) {
          val d = s.getTruth.getExpectation
          if (d > Parameters.DEFAULT_CREATION_EXPECTATION) {
            novelTasks.putIn(task)
          } else {
            recorder.append("!!! Neglected: " + task + "\n")
          }
        }
      }
    }
  }

  /**
   * Select a novel task to process.
   */
  private def processNovelTask() {
    val task = novelTasks.takeOut()
    if (task != null) {
      immediateProcess(task)
    }
  }

  /**
   * Select a concept to fire.
   */
  private def processConcept() {
    currentConcept = concepts.takeOut()
    if (currentConcept != null) {
      currentTerm = currentConcept.getTerm
      recorder.append(" * Selected Concept: " + currentTerm + "\n")
      concepts.putBack(currentConcept)
      currentConcept.fire()
    }
  }

  /**
   * Immediate processing of a new task, in constant time
   * Local processing, in one concept only
   * @param task the task to be accepted
   */
  private def immediateProcess(task: Task) {
    currentTask = task
    recorder.append("!!! Insert: " + task + "\n")
    currentTerm = task.getContent
    currentConcept = getConcept(currentTerm)
    if (currentConcept != null) {
      currentConcept.directProcess(task)
    }
  }

  /**
   * Display active concepts, called from MainWindow.
   * @param s the window title
   */
  def conceptsStartPlay(s: String) {
    concepts.startPlay(s)
  }

  /**
   * Display new tasks, called from MainWindow.
   * @param s the window title
   */
  def taskBuffersStartPlay(s: String) {
    novelTasks.startPlay(s)
  }

  /**
   * Display input/output sentence in the output channels.
   * The only place to add Objects into exportStrings. Currently only Strings
   * are added, though in the future there can be outgoing Tasks
   * @param sentence the sentence to be displayed
   * @param input whether the task is input
   */
  def report(sentence: Sentence, input: Boolean) {
    var s: String = ""
    s = if (input) "  IN: " else " OUT: "
    s += sentence.toStringBrief()
    if (exportStrings.isEmpty) {
      val timer = reasoner.getMainWindow.updateTimer()
      if (timer > 0) {
        exportStrings.add(String.valueOf(timer))
      }
    }
    exportStrings.add(s)
  }

  override def toString(): String = {
    toStringLongIfNotNull(concepts, "concepts") + toStringLongIfNotNull(novelTasks, "novelTasks") + 
      toStringIfNotNull(newTasks, "newTasks") + 
      toStringLongIfNotNull(currentTask, "currentTask") + 
      toStringLongIfNotNull(currentBeliefLink, "currentBeliefLink") + 
      toStringIfNotNull(currentBelief, "currentBelief")
  }

  private def toStringLongIfNotNull(item: Bag[_], title: String): String = {
    if (item == null) "" else "\n " + title + ":\n" + item.toStringLong()
  }

  private def toStringLongIfNotNull(item: Item, title: String): String = {
    if (item == null) "" else "\n " + title + ":\n" + item.toStringLong()
  }

  private def toStringIfNotNull(item: AnyRef, title: String): String = {
    if (item == null) "" else "\n " + title + ":\n" + item.toString
  }
}
