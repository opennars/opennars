package nars.logic.language

import java.util.ArrayList
import nars.io.Symbols
import nars.storage.Memory
import Similarity._
//remove if not needed
import scala.collection.JavaConversions._
import CompoundTerm._
import Statement._

object Similarity {

  /**
   * Try to make a new compound from two components. Called by the inference rules.
   * @param subject The first component
   * @param predicate The second component
   * @param memory Reference to the memory
   * @return A compound generated or null
   */
  def make(subject: Term, predicate: Term, memory: Memory): Similarity = {
    if (invalidStatement(subject, predicate)) {
      return null
    }
    if (subject.compareTo(predicate) > 0) {
      return make(predicate, subject, memory)
    }
    val name = makeStatementName(subject, Symbols.SIMILARITY_RELATION, predicate)
    val t = memory.nameToListedTerm(name)
    if (t != null) {
      return t.asInstanceOf[Similarity]
    }
    val argument = argumentsToList(subject, predicate)
    new Similarity(argument)
  }
}

/**
 * A Statement about a Similarity relation.
 */
class Similarity private (arg: ArrayList[Term]) extends Statement(arg) {

  /**
   * Constructor with full values, called by clone
   * @param n The name of the term
   * @param cs Component list
   * @param open Open variable list
   * @param i Syntactic complexity of the compound
   */
  private def this(n: String, 
      cs: ArrayList[Term], 
      con: Boolean, 
      i: Short) {
//    super(n, cs, con, i)
    this(cs)
    setName(n)
    this.isConstant_ = con
    this.complexity = i
  }

  /**
   * Clone an object
   * @return A new object, to be casted into a Similarity
   */
  override def clone(): AnyRef = {
    new Similarity(name, cloneList(components).asInstanceOf[ArrayList[Term]], isConstant_, complexity)
  }

  /**
   * Get the operator of the term.
   * @return the operator of the term
   */
  def operator(): String = Symbols.SIMILARITY_RELATION

  /**
   * Check if the compound is communitative.
   * @return true for communitative
   */
  override def isCommutative(): Boolean = true
}
