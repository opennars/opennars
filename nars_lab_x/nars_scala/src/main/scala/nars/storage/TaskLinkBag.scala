package nars.storage

import nars.logic.entity.TaskLink
import nars.main.Parameters
//remove if not needed
import scala.collection.JavaConversions._

/**
 * TaskLinkBag contains links to tasks.
 */
class TaskLinkBag(memory: Memory) extends Bag[TaskLink](memory) {

  /**
   * Get the (constant) capacity of TaskLinkBag
   * @return The capacity of TaskLinkBag
   */
  protected def capacity(): Int = Parameters.TASK_LINK_BAG_SIZE

  /**
   * Get the (adjustable) forget rate of TaskLinkBag
   * @return The forget rate of TaskLinkBag
   */
  protected def forgetRate(): Int = memory.getMainWindow.forgetTW.value()
}
