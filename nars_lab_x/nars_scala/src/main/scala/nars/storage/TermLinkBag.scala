package nars.storage

import nars.logic.entity._
import nars.main.Parameters
//remove if not needed
import scala.collection.JavaConversions._

/**
 * Contains TermLinks to relevant (compound or component) Terms.
 */
class TermLinkBag(memory: Memory) extends Bag[TermLink](memory) {

  /**
   * Get the (constant) capacity of TermLinkBag
   * @return The capacity of TermLinkBag
   */
  protected def capacity(): Int = Parameters.TERM_LINK_BAG_SIZE

  /**
   * Get the (adjustable) forget rate of TermLinkBag
   * @return The forget rate of TermLinkBag
   */
  protected def forgetRate(): Int = memory.getMainWindow.forgetBW.value()

  /**
   * Replace defualt to prevent repeated inference, by checking TaskLink
   * @param taskLink The selected TaskLink
   * @param time The current time
   * @return The selected TermLink
   */
  def takeOut(taskLink: TaskLink, time: Long): TermLink = {
    for (i <- 0 until Parameters.MAX_MATCHED_TERM_LINK) {
      val termLink = takeOut()
      if (termLink == null) {
        return null
      }
      if (taskLink.novel(termLink, time)) {
        return termLink
      }
      putBack(termLink)
    }
    null
  }
}
