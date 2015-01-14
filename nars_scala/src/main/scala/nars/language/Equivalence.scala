package nars.logic.language

import java.util._
import nars.io.Symbols
import nars.storage.Memory
import Equivalence._
//remove if not needed
import scala.collection.JavaConversions._
import CompoundTerm._
import Statement._

object Equivalence {

  /**
   * Try to make a new compound from two components. Called by the inference rules.
   * @param subject The first component
   * @param predicate The second component
   * @param memory Reference to the memory
   * @return A compound generated or null
   */
  def make(subject0: Term, predicate0: Term, memory: Memory): Equivalence = {
    var subject = subject0
    var predicate = predicate0
    if ((subject.isInstanceOf[Implication]) || (subject.isInstanceOf[Equivalence])) {
      return null
    }
    if ((predicate.isInstanceOf[Implication]) || (predicate.isInstanceOf[Equivalence])) {
      return null
    }
    if (invalidStatement(subject, predicate)) {
      return null
    }
    if (subject.compareTo(predicate) > 0) {
      val interm = subject
      subject = predicate
      predicate = interm
    }
    val name = makeStatementName(subject, Symbols.EQUIVALENCE_RELATION, predicate)
    val t = memory.nameToListedTerm(name)
    if (t != null) {
      return t.asInstanceOf[Equivalence]
    }
    val argument = argumentsToList(subject, predicate)
    new Equivalence(argument)
  }
}

/**
 * A Statement about an Equivalence relation.
 */
class Equivalence protected (components: ArrayList[Term]) extends Statement(components) {

  /**
   * Constructor with full values, called by clone
   * @param n The name of the term
   * @param components Component list
   * @param constant Whether the statement contains open variable
   * @param complexity Syntactic complexity of the compound
   */
  protected def this( name: String, 
      components: ArrayList[Term], 
      isConstant: Boolean, 
      complexity: Short ) {
    this(components)
    setName(name)
    this.complexity = complexity
    this.isConstant_ = isConstant
//    super(n, components, constant, complexity)
  }

  /**
   * Clone an object
   * @return A new object
   */
  override def clone(): AnyRef = {
    new Equivalence(name, cloneList(components).asInstanceOf[ArrayList[Term]], isConstant_, complexity)
  }

  /**
   * Get the operator of the term.
   * @return the operator of the term
   */
  def operator(): String = Symbols.EQUIVALENCE_RELATION

  /**
   * Check if the compound is commutative.
   * @return true for commutative
   */
  override def isCommutative(): Boolean = true
}
