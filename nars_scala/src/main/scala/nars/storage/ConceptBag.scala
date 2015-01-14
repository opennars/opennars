package nars.storage

import nars.logic.entity.Concept
import nars.main.Parameters
//remove if not needed
import scala.collection.JavaConversions._

/**
 * Contains Concepts.
 */
class ConceptBag(memory: Memory) extends Bag[Concept](memory) {

  /**
   *
   * Get the (constant) capacity of ConceptBag
   * @return The capacity of ConceptBag
   */
  protected def capacity(): Int = Parameters.CONCEPT_BAG_SIZE

  /**
   * Get the (adjustable) forget rate of ConceptBag
   * @return The forget rate of ConceptBag
   */
  protected def forgetRate(): Int = memory.getMainWindow.forgetCW.value()
}
