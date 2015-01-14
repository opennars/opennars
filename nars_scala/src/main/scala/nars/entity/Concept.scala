package nars.logic.entity

import java.util.ArrayList
import nars.gui.ConceptWindow
import nars.logic.BudgetFunctions
import nars.logic.nal1.LocalRules
import nars.logic.RuleTables
import nars.logic.UtilityFunctions
import nars.logic.entity.CompoundTerm
import nars.logic.entity.Term
import nars.main.NARS
import nars.main.Parameters
import nars.main_nogui.NARSBatch
import nars.storage.Memory
import nars.storage.TaskLinkBag
import nars.storage.TermLinkBag
import scala.reflect.{BeanProperty, BooleanBeanProperty}
//remove if not needed
import scala.collection.JavaConversions._

/**
 * A concept contains information associated with a term, including directly
 * and indirectly related tasks and beliefs.
 * <p>
 * To make sure the space will be released, the only allowed reference to a concept are
 * those in a ConceptBag. All other access go through the Term that names the concept.
 */
class Concept(@BeanProperty var term: Term, var memory: Memory) extends Item(term.getName) {

  /**
   Task links for indirect processing
   */
  private var taskLinks: TaskLinkBag = new TaskLinkBag(memory)

  /**
   Term links between the term and its components and compounds
   */
  private var termLinks: TermLinkBag = new TermLinkBag(memory)

  /**
   Link templates of TermLink, only in concepts with CompoundTerm
   * jmv TODO explain more
   */
  @BeanProperty
  var termLinkTemplates: ArrayList[TermLink] = _

  /**
   Question directly asked about the term
   */
  private var questions: ArrayList[Task] = new ArrayList[Task]()

  /**
   Sentences directly made about the term, with non-future tense
   */
  private var beliefs: ArrayList[Sentence] = new ArrayList[Sentence]()

  /**
   Whether the content of the concept is being displayed
   */
  private var showing: Boolean = false

  /**
   The display window
   */
  private var window: ConceptWindow = null

//  super(tm.getName)

  if (term.isInstanceOf[CompoundTerm]) {
    termLinkTemplates = term.asInstanceOf[CompoundTerm].prepareComponentLinks()
  }

  /**
   * Directly process a new task. Called exactly once on each task.
   * Using local information and finishing in a constant time.
   * Provide feedback in the taskBudget value of the task.
   * <p>
   * called in Memory.immediateProcess only
   * @param task The task to be processed
   */
  def directProcess(task: Task) {
    if (task.getSentence.isJudgment) {
      processJudgment(task)
    } else {
      processQuestion(task)
    }
    if (task.getBudget.aboveThreshold()) {
      linkToTask(task)
    }
    if (showing) {
      window.post(displayContent())
    }
  }

  /**
   * To accept a new judgment as isBelief, and check for revisions and solutions
   * @param judg The judgment to be accepted
   * @param task The task to be processed
   * @return Whether to continue the processing of the task
   */
  private def processJudgment(task: Task) {
    val judg = task.getSentence
    val oldBelief = evaluation(judg, beliefs)
    if (oldBelief != null) {
      val newStamp = judg.getStamp
      val oldStamp = oldBelief.getStamp
      if (newStamp == oldStamp) {
        task.getBudget.decPriority(0)
        return
      } else if (LocalRules.revisible(judg, oldBelief)) {
        memory.newStamp = Stamp.make(newStamp, oldStamp, memory.getTime)
        if (memory.newStamp != null) {
          memory.currentBelief = oldBelief
          LocalRules.revision(judg, oldBelief, false, memory)
        }
      }
    }
    if (task.getBudget.aboveThreshold()) {
      for (ques <- questions) {
        LocalRules.trySolution(ques.getSentence, judg, ques, memory)
      }
      addToTable(judg, beliefs, Parameters.MAXIMUM_BELIEF_LENGTH)
    }
  }

  /**
   * To answer a question by existing beliefs
   * @param task The task to be processed
   * @return Whether to continue the processing of the task
   */
  def processQuestion(task: Task): Float = {
    var ques = task.getSentence
    var newQuestion = true
    if (questions != null) {
      for (t <- questions) {
        val q = t.getSentence
        if (q.getContent == ques.getContent) {
          ques = q
          newQuestion = false
          //break
        }
      }
    }
    if (newQuestion) {
      questions.add(task)
    }
    if (questions.size > Parameters.MAXIMUM_QUESTIONS_LENGTH) {
      questions.remove(0)
    }
    val newAnswer = evaluation(ques, beliefs)
    if (newAnswer != null) {
      LocalRules.trySolution(ques, newAnswer, task, memory)
      newAnswer.getTruth.getExpectation
    } else {
      0.5f
    }
  }

  /**
   * Link to a new task from all relevant concepts for continued processing in
   * the near future for unspecified time.
   * <p>
   * The only method that calls the TaskLink constructor.
   * @param task The task to be linked
   * @param content The content of the task
   */
  private def linkToTask(task: Task) {
    val taskBudget = task.getBudget
    var taskLink = new TaskLink(task, null, taskBudget)
    insertTaskLink(taskLink)
    if (term.isInstanceOf[CompoundTerm]) {
      if (termLinkTemplates.size > 0) {
        val subBudget = BudgetFunctions.distributeAmongLinks(taskBudget, termLinkTemplates.size)
        if (subBudget.aboveThreshold()) {
          var componentTerm: Term = null
          var componentConcept: Concept = null
          for (termLink <- termLinkTemplates) {
            taskLink = new TaskLink(task, termLink, subBudget)
            componentTerm = termLink.getTarget
            componentConcept = memory.getConcept(componentTerm)
            if (componentConcept != null) {
              componentConcept.insertTaskLink(taskLink)
            }
          }
          buildTermLinks(taskBudget)
        }
      }
    }
  }

  /**
   * Add a new belief (or goal) into the table
   * Sort the beliefs/goals by rank, and remove redundant or low rank one
   * @param newSentence The judgment to be processed
   * @param table The table to be revised
   * @param capacity The capacity of the table
   */
  private def addToTable(newSentence: Sentence, table: ArrayList[Sentence], capacity: Int) {
    val rank1 = BudgetFunctions.rankBelief(newSentence)
    var judgment2: Sentence = null
    var rank2: Float = 0
    var i: Int = 0
    while (i < table.size) {
      judgment2 = table.get(i).asInstanceOf[Sentence]
      rank2 = BudgetFunctions.rankBelief(judgment2)
      if (rank1 >= rank2) {
        if (newSentence.equivalentTo(judgment2)) {
          return
        }
        table.add(i, newSentence)
        //break
      }
      i += 1
    }
    if (table.size >= capacity) {
      while (table.size > capacity) {
        table.remove(table.size - 1)
      }
    } else if (i == table.size) {
      table.add(newSentence)
    }
  }

  /**
   * Evaluate a query against beliefs (and desires in the future)
   * @param query The question to be processed
   * @param list The list of beliefs to be used
   * @return The best candidate belief selected
   */
  private def evaluation(query: Sentence, list: ArrayList[Sentence]): Sentence = {
    if (list == null) {
      return null
    }
    var currentBest: Float = 0
    var beliefQuality: Float = 0
    var candidate: Sentence = null
    for (judg <- list) {
      beliefQuality = LocalRules.solutionQuality(query, judg)
      if (beliefQuality > currentBest) {
        currentBest = beliefQuality
        candidate = judg
      }
    }
    candidate
  }

  /**
   * Insert a TaskLink into the TaskLink bag
   * <p>
   * called only from Memory.continuedProcess
   * @param taskLink The termLink to be inserted
   */
  def insertTaskLink(taskLink: TaskLink) {
    val taskBudget = taskLink.getBudget
    taskLinks.putIn(taskLink)
    memory.activateConcept(this, taskBudget)
  }

  /**
   * Recursively build TermLinks between a compound and its components
   * <p>
   * called only from Memory.continuedProcess
   * and #linkToTask
   * @param taskBudget The BudgetValue of the task
   */
  def buildTermLinks(taskBudget: BudgetValue) {
    var t: Term = null
    var concept: Concept = null
    var termLink1: TermLink = null
    var termLink2: TermLink = null
    if (termLinkTemplates.size > 0) {
      val subBudget = BudgetFunctions.distributeAmongLinks(taskBudget, termLinkTemplates.size)
      if (subBudget.aboveThreshold()) {
        for (template <- termLinkTemplates if template.getType != TermLink.TRANSFORM) {
          t = template.getTarget
          concept = memory.getConcept(t)
          if (concept != null) {
            termLink1 = new TermLink(t, template, subBudget)
            insertTermLink(termLink1)
            termLink2 = new TermLink(term, template, subBudget)
            concept.insertTermLink(termLink2)
            if (t.isInstanceOf[CompoundTerm]) {
              concept.buildTermLinks(subBudget)
            }
          }
        }
      }
    }
  }

  /**
   * Insert a TermLink into the TermLink bag
   * <p>
   * called from buildTermLinks only
   * @param termLink The termLink to be inserted
   */
  def insertTermLink(termLink: TermLink) {
    termLinks.putIn(termLink)
  }

  /**
   * Return a string representation of the concept, called in ConceptBag only
   * @return The concept name, with taskBudget in the full version
   */
  override def toString(): String = {
    if (NARS.isStandAlone) {
      (super.toStringBrief() + " " + key)
    } else {
      key
    }
  }

  /**
   called from {@link NARSBatch}
   */
  override def toStringLong(): String = {
    var res = toStringBrief() + " " + key + toStringIfNotNull(termLinks, "termLinks") + 
      toStringIfNotNull(taskLinks, "taskLinks")
    res += toStringIfNotNull(null, "questions")
    for (t <- questions) {
      res += t.toString
    }
    res
  }

  private def toStringIfNotNull(item: AnyRef, title: String): String = {
    if (item == null) "" else "\n " + title + ":" + item.toString
  }

  /**
   * Recalculate the quality of the concept [to be refined to show extension/intension balance]
   * @return The quality value
   */
  override def getQuality(): Float = {
    val linkPriority = termLinks.averagePriority()
    val termComplexityFactor = 1.0f / term.getComplexity
    UtilityFunctions.or(linkPriority, termComplexityFactor)
  }

  /**
   * Select a isBelief to interact with the given task in inference
   * <p>
   * get the first qualified one
   * <p>
   * only called in RuleTables.reason
   * @param task The selected task
   * @return The selected isBelief
   */
  def getBelief(task: Task): Sentence = {
    val taskSentence = task.getSentence
    var belief: Sentence = null
    for (i <- 0 until beliefs.size) {
      belief = beliefs.get(i)
      memory.getRecorder.append(" * Selected Belief: " + belief + "\n")
      memory.newStamp = Stamp.make(taskSentence.getStamp, belief.getStamp, memory.getTime)
      if (memory.newStamp != null) {
        val belief2 = belief.clone().asInstanceOf[Sentence]
        return belief2
      }
    }
    null
  }

  /**
   * An atomic step in a concept, only called in Memory.processConcept
   */
  def fire() {
    val tLink = taskLinks.takeOut()
    if (tLink == null) {
      return
    }
    memory.currentTaskLink = tLink
    memory.currentBeliefLink = null
    memory.getRecorder.append(" * Selected TaskLink: " + tLink + "\n")
    val task = tLink.getTargetTask
    memory.currentTask = task
    if (tLink.getType == TermLink.TRANSFORM) {
      RuleTables.transformTask(tLink, memory)
    }
    var termLinkCount = Parameters.MAX_REASONED_TERM_LINK
    while (memory.noResult() && (termLinkCount > 0)) {
      val termLink = termLinks.takeOut(tLink, memory.getTime)
      if (termLink != null) {
        memory.getRecorder.append(" * Selected TermLink: " + termLink + "\n")
        memory.currentBeliefLink = termLink
        RuleTables.reason(tLink, termLink, memory)
        termLinks.putBack(termLink)
        termLinkCount -= 1
      } else {
        termLinkCount = 0
      }
    }
    taskLinks.putBack(tLink)
  }

  /**
   * Start displaying contents and links, called from ConceptWindow,
   * TermWindow
   * or Memory.processTask only
   * @param showLinks Whether to display the task links
   */
  def startPlay(showLinks: Boolean) {
    if (window != null && window.isVisible) {
      window.detachFromConcept()
    }
    window = new ConceptWindow(this)
    showing = true
    window.post(displayContent())
    if (showLinks) {
      taskLinks.startPlay("Task Links in " + term)
      termLinks.startPlay("Term Links in " + term)
    }
  }

  /**
   * Resume display, called from ConceptWindow only
   */
  def play() {
    showing = true
    window.post(displayContent())
  }

  /**
   * Stop display, called from ConceptWindow only
   */
  def stop() {
    showing = false
  }

  /**
   * Collect direct isBelief, questions, and goals for display
   * @return String representation of direct content
   */
  def displayContent(): String = {
    val buffer = new StringBuilder()
    if (beliefs.size > 0) {
      buffer.append("\n  Beliefs:\n")
      for (s <- beliefs) {
        buffer.append(s).append("\n")
      }
    }
    if (questions.size > 0) {
      buffer.append("\n  Question:\n")
      for (t <- questions) {
        buffer.append(t).append("\n")
      }
    }
    buffer.toString
  }
}
