package nars.logic.entity

import nars.io.Symbols
import nars.logic.entity.Term
import scala.reflect.{BeanProperty, BooleanBeanProperty}
//remove if not needed
import scala.collection.JavaConversions._

/**
 * A Sentence is an abstract class, mainly containing a Term, a TruthValue, and a Stamp.
 *<p>
 * It is used as the premises and conclusions of all inference rules.
 */
class Sentence(@BeanProperty var content: Term, 
    @BeanProperty var punctuation: Char, 
    @BeanProperty var truth: TruthValue, 
    @BeanProperty var stamp: Stamp) extends Cloneable {

  /**
   Whether the sentence can be revised
   */
  @BeanProperty
  var revisible: Boolean = true

  this.content.renameVariables()

  /**
   * Create a Sentence with the given fields
   * @param content The Term that forms the content of the sentence
   * @param punctuation The punctuation indicating the type of the sentence
   * @param truth The truth value of the sentence, null for question
   * @param stamp The stamp of the sentence indicating its derivation time and base
   * @param revisible Whether the sentence can be revised
   */
  def this(content: Term, 
      punctuation: Char, 
      truth: TruthValue, 
      stamp: Stamp, 
      revisible: Boolean) {
    this( content, punctuation, truth, stamp )
    this.revisible = revisible
  }

  /**
   * To check whether two sentences are equal
   * @param that The other sentence
   * @return Whether the two sentences have the same content
   */
  override def equals(that: Any): Boolean = {
    if (that.isInstanceOf[Sentence]) {
      val t = that.asInstanceOf[Sentence]
      return content == t.getContent && punctuation == t.getPunctuation && 
        truth == t.getTruth && 
        stamp == t.getStamp
    }
    false
  }

  /**
   * To produce the hashcode of a sentence
   * @return A hashcode
   */
  override def hashCode(): Int = {
    var hash = 5
    hash = 67 * hash + 
      (if (this.content != null) this.content.hashCode else 0)
    hash = 67 * hash + this.punctuation
    hash = 67 * hash + 
      (if (this.truth != null) this.truth.hashCode else 0)
    hash = 67 * hash + 
      (if (this.stamp != null) this.stamp.hashCode else 0)
    hash
  }

  /**
   * Check whether the judgment is equivalent to another one
   * <p>
   * The two may have different keys
   * @param that The other judgment
   * @return Whether the two are equivalent
   */
  def equivalentTo(that: Sentence): Boolean = {
    assert ( content == that.getContent && punctuation == that.getPunctuation )
    (truth == that.getTruth && stamp == that.getStamp)
  }

  /**
   * Clone the Sentence
   * @return The clone
   */
  override def clone(): AnyRef = {
    if (truth == null) {
      return new Sentence(content.clone().asInstanceOf[Term], punctuation, null, stamp.clone().asInstanceOf[Stamp])
    }
    new Sentence(content.clone().asInstanceOf[Term], punctuation, new TruthValue(truth), stamp.clone().asInstanceOf[Stamp], 
      revisible)
  }

  /**
   * Clone the content of the sentence
   * @return A clone of the content Term
   */
  def cloneContent(): Term = content.clone().asInstanceOf[Term]

  /**
   * Distinguish Judgment from Goal ("instanceof Judgment" doesn't work)
   * @return Whether the object is a Judgment
   */
  def isJudgment(): Boolean = (punctuation == Symbols.JUDGMENT)

  /**
   * Distinguish Question from Quest ("instanceof Question" doesn't work)
   * @return Whether the object is a Question
   */
  def isQuestion(): Boolean = (punctuation == Symbols.QUESTION)

  /**
   * Get a String representation of the sentence
   * @return The String
   */
  override def toString(): String = {
    val s = new StringBuffer()
    s.append(content.toString)
    s.append(punctuation + " ")
    if (truth != null) {
      s.append(truth.toString)
    }
    s.append(stamp.toString)
    s.toString
  }

  /**
   * Get a String representation of the sentence, with 2-digit accuracy
   * @return The String
   */
  def toStringBrief(): String = toKey() + stamp.toString

  /**
   * Get a String representation of the sentence for key of Task and TaskLink
   * @return The String
   */
  def toKey(): String = {
    val s = new StringBuffer()
    s.append(content.toString)
    s.append(punctuation + " ")
    if (truth != null) {
      s.append(truth.toStringBrief())
    }
    s.toString
  }
}
