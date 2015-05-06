package nars.logic.language

//remove if not needed
import scala.collection.JavaConversions._

/**
 * Term is the basic component of Narsese, and the object of processing in NARS.
 * <p>
 * A Term may have an associated Concept containing relations with other Terms. It
 * is not linked in the Term, because a Concept may be forgot while the Term exists.
 * Multiple objects may represent the same Term.
 */
class Term protected () extends Cloneable with Comparable[Term] {

  /**
   * A Term is identified uniquely by its name, a sequence of characters in a
   * given alphabet (ASCII or Unicode)
   */
  protected var name: String = _

  /**
   * Constructor with a given name
   * @param name A String as the name of the Term
   */
  def this(name: String) {
    this()
    this.name = name
  }

  /**
   * Reporting the name of the current Term.
   * @return The name of the term as a String
   */
  def getName(): String = name

  /**
   * Make a new Term with the same name.
   * @return The new Term
   */
  override def clone(): AnyRef = new Term(name)

  /**
   * Equal terms have identical name, though not necessarily the same reference.
   * @return Whether the two Terms are equal
   * @param that The Term to be compared with the current Term
   */
  override def equals(that: Any): Boolean = {
    (that.isInstanceOf[Term]) && name == that.asInstanceOf[Term].getName
  }

  /**
   * Produce a hash code for the term
   * @return An integer hash code
   */
  override def hashCode(): Int = {
    (if (name != null) name.hashCode else 7)
  }

  /**
   * Check whether the current Term can name a Concept.
   * @return A Term is constant by default
   */
  def isConstant(): Boolean = true

  /**
   * Blank method to be override in CompoundTerm
   */
  def renameVariables() {
  }

  /**
   * The syntactic complexity, for constant automic Term, is 1.
   * @return The conplexity of the term, an integer
   */
  def getComplexity(): Int = 1

  /**
   * Check the relative order of two Terms.
   * @param that The Term to be compared with the current Term
   * @return The same as compareTo as defined on Strings
   */
  def compareTo(that: Term): Int = name.compareTo(that.getName)

  /**
   * Recursively check if a compound contains a term
   * @param target The term to be searched
   * @return Whether the two have the same content
   */
  def containTerm(target: Term): Boolean = this == target

  /**
   * The same as getName by default, used in display only.
   * @return The name of the term as a String
   */
  override def toString(): String = name
}
