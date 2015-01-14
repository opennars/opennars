package nars.logic.language

import java.util._
import nars.io.Symbols
import nars.storage.Memory
import Implication._
//remove if not needed
import scala.collection.JavaConversions._
import CompoundTerm._
import Statement._

object Implication {

  /**
   * Try to make a new compound from two components. Called by the inference rules.
   * @param subject The first component
   * @param predicate The second component
   * @param memory Reference to the memory
   * @return A compound generated or a term it reduced to
   */
  def make(subject: Term, predicate: Term, memory: Memory): Implication = {
    if ((subject == null) || (predicate == null)) {
      return null
    }
    if ((subject == null) || (predicate == null)) {
      return null
    }
    if ((subject.isInstanceOf[Implication]) || (subject.isInstanceOf[Equivalence]) || 
      (predicate.isInstanceOf[Equivalence])) {
      return null
    }
    if (invalidStatement(subject, predicate)) {
      return null
    }
    val name = makeStatementName(subject, Symbols.IMPLICATION_RELATION, predicate)
    val t = memory.nameToListedTerm(name)
    if (t != null) {
      return t.asInstanceOf[Implication]
    }
    if (predicate.isInstanceOf[Implication]) {
      val oldCondition = predicate.asInstanceOf[Implication].getSubject
      if ((oldCondition.isInstanceOf[Conjunction]) && 
        oldCondition.asInstanceOf[Conjunction].containComponent(subject)) {
        return null
      }
      val newCondition = Conjunction.make(subject, oldCondition, memory)
      make(newCondition, predicate.asInstanceOf[Implication].getPredicate, memory)
    } else {
      val argument = argumentsToList(subject, predicate)
      new Implication(argument)
    }
  }
}

/**
 * A Statement about an Inheritance relation.
 */
class Implication protected (arg: ArrayList[Term]) extends Statement(arg) {

  /**
   * Constructor with full values, called by clone
   * @param n The name of the term
   * @param cs Component list
   * @param con Whether it is a constant term
   * @param i Syntactic complexity of the compound
   */
  protected def this(n: String, 
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
   * @return A new object
   */
  override def clone(): AnyRef = {
    new Implication(name, cloneList(components).asInstanceOf[ArrayList[Term]], isConstant_, complexity)
  }

  /**
   * Get the operator of the term.
   * @return the operator of the term
   */
  def operator(): String = Symbols.IMPLICATION_RELATION
}
