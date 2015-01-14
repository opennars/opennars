package nars.logic.language

import nars.storage.Memory
//remove if not needed
import scala.collection.JavaConversions._

object Property {

  /**
   * Try to make a new compound from two components. Called by the inference rules.
   * <p>
   *  A --] B becomes A --> [B]
   * @param subject The first compoment
   * @param predicate The second compoment
   * @param memory Reference to the memeory
   * @return A compound generated or null
   */
  def make(subject: Term, predicate: Term, memory: Memory): Statement = {
    Inheritance.make(subject, SetInt.make(predicate, memory), memory)
  }
}
