package nars.storage

import nars.logic.entity.Task
import nars.main.Parameters
//remove if not needed
import scala.collection.JavaConversions._

/**
 * New tasks that contain new Term.
 */
class NovelTaskBag(memory: Memory) extends Bag[Task](memory) {

  /**
   * Get the (constant) capacity of NovelTaskBag
   * @return The capacity of NovelTaskBag
   */
  protected def capacity(): Int = Parameters.TASK_BUFFER_SIZE

  /**
   * Get the (constant) forget rate in NovelTaskBag
   * @return The forget rate in NovelTaskBag
   */
  protected def forgetRate(): Int = Parameters.NEW_TASK_FORGETTING_CYCLE
}
