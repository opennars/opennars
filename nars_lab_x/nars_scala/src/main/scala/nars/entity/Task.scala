package nars.logic.entity

import nars.logic.entity.Term
import scala.reflect.{BeanProperty, BooleanBeanProperty}
//remove if not needed
import scala.collection.JavaConversions._

/**
 * A task to be processed, consists of a Sentence and a BudgetValue
 */
class Task(@BeanProperty var sentence: Sentence, b: BudgetValue) extends Item(sentence.toKey(), b) {

  /**
   Task from which the Task is derived, or null if input
   */
  private var parentTask: Task = _

  /**
   Belief from which the Task is derived, or null if derived from a theorem
   */
  @BeanProperty
  var parentBelief: Sentence = _

  /**
   For Question and Goal: best solution found so far
   */
  @BeanProperty
  var bestSolution: Sentence = _

//  super(s.toKey(), b)

  key = sentence.toKey()

  /**
   * Constructor for a derived task
   * @param s The sentence
   * @param b The budget
   * @param parentTask The task from which this new task is derived
   * @param parentBelief The belief from which this new task is derived
   */
  def this(s: Sentence, 
      b: BudgetValue, 
      parentTask: Task, 
      parentBelief: Sentence) {
    this(s, b)
    this.parentTask = parentTask
    this.parentBelief = parentBelief
  }

  /**
   * Constructor for an activated task
   * @param s The sentence
   * @param b The budget
   * @param parentTask The task from which this new task is derived
   * @param parentBelief The belief from which this new task is derived
   * @param solution The belief to be used in future inference
   */
  def this(s: Sentence, 
      b: BudgetValue, 
      parentTask: Task, 
      parentBelief: Sentence, 
      solution: Sentence) {
    this(s, b, parentTask, parentBelief)
    this.bestSolution = solution
  }

  /**
   * Directly get the content of the sentence
   * @return The content of the sentence
   */
  def getContent(): Term = sentence.getContent

  /**
   * Directly get the creation time of the sentence
   * @return The creation time of the sentence
   */
  def getCreationTime(): Long = sentence.getStamp.getCreationTime

  /**
   * Check if a Task is a direct input
   * @return Whether the Task is derived from another task
   */
  def isInput(): Boolean = parentTask == null

  /**
   * Check if a Task is derived by a StructuralRule
   * @return Whether the Task is derived by a StructuralRule
   */
  def isStructural(): Boolean = {
    (parentBelief == null) && (parentTask != null)
  }

  /**
   * Merge one Task into another
   * @param that The other Task
   */
  override def merge(that: Item) {
    if (getCreationTime >= that.asInstanceOf[Task].getCreationTime) {
      super.merge(that)
    } else {
      that.merge(this)
    }
  }

  /**
   * Get a String representation of the Task
   * @return The Task as a String
   */
  override def toString(): String = {
    val s = new StringBuffer()
    s.append(super.toString + " ")
    if (parentTask != null) {
      s.append("  \n from task: " + parentTask.toStringBrief())
      if (parentBelief != null) {
        s.append("  \n from belief: " + parentBelief.toStringBrief())
      }
    }
    if (bestSolution != null) {
      s.append("  \n solution: " + bestSolution.toStringBrief())
    }
    s.append("\n>>>> end of Task")
    s.toString
  }
}
