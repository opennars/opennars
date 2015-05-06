package nars.logic.entity

import nars.logic.entity.Term
import nars.main._
import scala.reflect.{BeanProperty, BooleanBeanProperty}
//remove if not needed
import scala.collection.JavaConversions._

/**
 * Reference to a Task.
 * <p>
 * The reason to separate a Task and a TaskLink is that the same Task can be linked from
 * multiple Concepts, with different BudgetValue.
 */
class TaskLink(@BeanProperty var targetTask: Task, template: TermLink, v: BudgetValue)
    extends TermLink("", v) {

  /**
   Remember the TermLinks that has been used recently with this TaskLink
   */
  private var recordedLinks = new Array[String](Parameters.TERM_LINK_RECORD_LENGTH)

  /**
   Remember the time when each TermLink is used with this TaskLink
   */
  private var recordingTime = new Array[Long](Parameters.TERM_LINK_RECORD_LENGTH)

  /**
   The number of TermLinks remembered
   */
  var counter: Int = 0

//  super("", v)

  if (template == null) {
    `type` = TermLink.SELF.asInstanceOf[Short]
    index = null
  } else {
    `type` = template.getType
    index = template.getIndices
  }

  setKey()

  key += targetTask.getKey

  /**
   * To check whether a TaskLink should use a TermLink, return false if they
   * interacted recently
   * <p>
   * called in TermLinkBag only
   * @param termLink The TermLink to be checked
   * @param currentTime The current time
   * @return Whether they are novel to each other
   */
  def novel(termLink: TermLink, currentTime: Long): Boolean = {
    val bTerm = termLink.getTarget
    if (bTerm == targetTask.getSentence.getContent) {
      return false
    }
    val linkKey = termLink.getKey
    var next = 0
    var i: Int = 0
    while (i < counter) {
      next = i % Parameters.TERM_LINK_RECORD_LENGTH
      if (linkKey == recordedLinks(next)) {
        if (currentTime < 
          recordingTime(next) + Parameters.TERM_LINK_RECORD_LENGTH) {
          return false
        } else {
          recordingTime(next) = currentTime
          return true
        }
      }
      i += 1
    }
    next = i % Parameters.TERM_LINK_RECORD_LENGTH
    recordedLinks(next) = linkKey
    recordingTime(next) = currentTime
    if (counter < Parameters.TERM_LINK_RECORD_LENGTH) {
      counter += 1
    }
    true
  }
}
