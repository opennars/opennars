package nars.logic.language

import java.util.ArrayList
import nars.io.Symbols
import nars.storage.Memory
import Statement._
//remove if not needed
import scala.collection.JavaConversions._

object Statement {
//
//    protected def Statement( name: String, 
//      components: ArrayList[Term], 
//      isConstant: Boolean, 
//      complexity: Short ) :Statement = {
//      new Statement()
////    new CompoundTerm(n, cs, con, i)
//  }

  /**
   * Make a Statement from String, called by StringParser
   * @param relation The relation String
   * @param subject The first component
   * @param predicate The second component
   * @param memory Reference to the memory
   * @return The Statement built
   */
  def make(relation: String, 
      subject: Term, 
      predicate: Term, 
      memory: Memory): Statement = {
    if (invalidStatement(subject, predicate)) {
      return null
    }
    if (relation == Symbols.INHERITANCE_RELATION) {
      return Inheritance.make(subject, predicate, memory)
    }
    if (relation == Symbols.SIMILARITY_RELATION) {
      return Similarity.make(subject, predicate, memory)
    }
    if (relation == Symbols.INSTANCE_RELATION) {
      return Instance.make(subject, predicate, memory)
    }
    if (relation == Symbols.PROPERTY_RELATION) {
      return Property.make(subject, predicate, memory)
    }
    if (relation == Symbols.INSTANCE_PROPERTY_RELATION) {
      return InstanceProperty.make(subject, predicate, memory)
    }
    if (relation == Symbols.IMPLICATION_RELATION) {
      return Implication.make(subject, predicate, memory)
    }
    if (relation == Symbols.EQUIVALENCE_RELATION) {
      return Equivalence.make(subject, predicate, memory)
    }
    null
  }

  /**
   * Make a Statement from given components, called by the rules
   * @return The Statement built
   * @param subj The first component
   * @param pred The second component
   * @param statement A sample statement providing the class type
   * @param memory Reference to the memeory
   */
  def make(statement: Statement, 
      subj: Term, 
      pred: Term, 
      memory: Memory): Statement = {
    if (statement.isInstanceOf[Inheritance]) {
      return Inheritance.make(subj, pred, memory)
    }
    if (statement.isInstanceOf[Similarity]) {
      return Similarity.make(subj, pred, memory)
    }
    if (statement.isInstanceOf[Implication]) {
      return Implication.make(subj, pred, memory)
    }
    if (statement.isInstanceOf[Equivalence]) {
      return Equivalence.make(subj, pred, memory)
    }
    null
  }

  /**
   * Make a symmetric Statement from given components and temporal information, called by the rules
   * @param statement A sample asymmetric statement providing the class type
   * @param subj The first component
   * @param pred The second component
   * @param memory Reference to the memeory
   * @return The Statement built
   */
  def makeSym(statement: Statement, 
      subj: Term, 
      pred: Term, 
      memory: Memory): Statement = {
    if (statement.isInstanceOf[Inheritance]) {
      return Similarity.make(subj, pred, memory)
    }
    if (statement.isInstanceOf[Implication]) {
      return Equivalence.make(subj, pred, memory)
    }
    null
  }

  /**
   * Check Statement relation symbol, called in StringPaser
   * @param s0 The String to be checked
   * @return if the given String is a relation symbol
   */
  def isRelation(s0: String): Boolean = {
    val s = s0.trim()
    if (s.length != 3) {
      return false
    }
    (s == Symbols.INHERITANCE_RELATION || s == Symbols.SIMILARITY_RELATION || 
      s == Symbols.INSTANCE_RELATION || 
      s == Symbols.PROPERTY_RELATION || 
      s == Symbols.INSTANCE_PROPERTY_RELATION || 
      s == Symbols.IMPLICATION_RELATION || 
      s == Symbols.EQUIVALENCE_RELATION)
  }

  /**
   * Default method to make the nameStr of an image term from given fields
   * @param subject The first component
   * @param predicate The second component
   * @param relation The relation operator
   * @return The nameStr of the term
   */
//  protected jmv
  def makeStatementName(subject: Term, relation: String, predicate: Term): String = {
    val nameStr = new StringBuffer()
    nameStr.append(Symbols.STATEMENT_OPENER)
    nameStr.append(subject.getName)
    nameStr.append(' ' + relation + ' ')
    nameStr.append(predicate.getName)
    nameStr.append(Symbols.STATEMENT_CLOSER)
    nameStr.toString
  }

  /**
   * Check the validity of a potential Statement. [To be refined]
   * <p>
   * Minimum requirement: the two terms cannot be the same, or containing each other as component
   * @param subject The first component
   * @param predicate The second component
   * @return Whether The Statement is invalid
   */
  def invalidStatement(subject: Term, predicate: Term): Boolean = {
    if (subject == predicate) {
      return true
    }
    if ((subject.isInstanceOf[CompoundTerm]) && 
      subject.asInstanceOf[CompoundTerm].containComponent(predicate)) {
      return true
    }
    if ((predicate.isInstanceOf[CompoundTerm]) && 
      predicate.asInstanceOf[CompoundTerm].containComponent(subject)) {
      return true
    }
    if ((subject.isInstanceOf[Statement]) && (predicate.isInstanceOf[Statement])) {
      val s1 = subject.asInstanceOf[Statement]
      val s2 = predicate.asInstanceOf[Statement]
      val t11 = s1.getSubject
      val t12 = s1.getPredicate
      val t21 = s2.getSubject
      val t22 = s2.getPredicate
      if (t11 == t22 && t12 == t21) {
        return true
      }
    }
    false
  }
}

/**
 * A statement is a compound term, consisting of a subject, a predicate,
 * and a relation symbol in between. It can be of either first-order or higher-order.
 */
abstract class Statement protected (components: ArrayList[Term]) extends CompoundTerm(components) {

  /**
   * Constructor with partial values, called by make
   * @param arg The component list of the term
   */
//  protected def this(arg: ArrayList[Term]) {
////    super(arg) 
//    this()
//    this.components = arg
//  }

  /**
   * Constructor with full values, called by clone
   * @param n The nameStr of the term
   * @param cs Component list
   * @param con Constant indicator
   * @param i Syntactic complexity of the compound
   */
//  protected def this(n: String, 
//      cs: ArrayList[Term], 
//      con: Boolean, 
//      i: Short) {
//    super(n, cs, con, i)
//  }

  /**
   * Override the default in making the nameStr of the current term from existing fields
   * @return the nameStr of the term
   */
  override def makeName(): String = {
    makeStatementName(getSubject, operator(), getPredicate)
  }

  /**
   * Check the validity of a potential Statement. [To be refined]
   * <p>
   * Minimum requirement: the two terms cannot be the same, or containing each other as component
   * @return Whether The Statement is invalid
   */
  def invalid(): Boolean = {
    invalidStatement(getSubject, getPredicate)
  }

  /**
   * Return the first component of the statement
   * @return The first component
   */
  def getSubject(): Term = components.get(0)

  /**
   * Return the second component of the statement
   * @return The second component
   */
  def getPredicate(): Term = components.get(1)
}
