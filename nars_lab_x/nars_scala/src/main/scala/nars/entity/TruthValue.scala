package nars.logic.entity

import nars.io.Symbols
import TruthValue._
//remove if not needed
import scala.collection.JavaConversions._

object TruthValue {

  /**
   The charactor that marks the two ends of a truth value
   */
  private val DELIMITER = Symbols.TRUTH_VALUE_MARK

  /**
   The charactor that separates the factors in a truth value
   */
  private val SEPARATOR = Symbols.VALUE_SEPARATOR
}

/**
 * Frequency and confidence.
 */
class TruthValue(f: Float, c: Float) extends Cloneable {

  /**
   The frequency factor of the truth value
   */
  private var frequency: ShortFloat = new ShortFloat(f)

  /**
   The confidence factor of the truth value
   */
  private var confidence: ShortFloat = if ((c < 1)) new ShortFloat(c) else new ShortFloat(0.9999f)

  /**
   * Constructor with a TruthValue to clone
   * @param v The truth value to be cloned
   */
  def this(v: TruthValue) {
    this( (v.getFrequency), v.getConfidence )
  }

  /**
   * Get the frequency value
   * @return The frequency value
   */
  def getFrequency(): Float = frequency.getValue

  /**
   * Get the confidence value
   * @return The confidence value
   */
  def getConfidence(): Float = confidence.getValue

  /**
   * Calculate the expectation value of the truth value
   * @return The expectation value
   */
  def getExpectation(): Float = {
    (confidence.getValue * (frequency.getValue - 0.5) + 0.5).toFloat
  }

  /**
   * Calculate the absolute difference of the expectation value and that of a given truth value
   * @param t The given value
   * @return The absolute difference
   */
  def getExpDifAbs(t: TruthValue): Float = {
    Math.abs(getExpectation - t.getExpectation)
  }

  /**
   * Check if the truth value is negative
   * @return True if the frequence is less than 1/2
   */
  def isNegative(): Boolean = getFrequency < 0.5

  /**
   * Compare two truth values
   * @param that The other TruthValue
   * @return Whether the two are equivalent
   */
  override def equals(that: Any): Boolean = {
    ((that.isInstanceOf[TruthValue]) && 
      (getFrequency == that.asInstanceOf[TruthValue].getFrequency) && 
      (getConfidence == that.asInstanceOf[TruthValue].getConfidence))
  }

  /**
   * The hash code of a TruthValue
   * @return The hash code
   */
  override def hashCode(): Int = (getExpectation * 37).toInt

  override def clone(): AnyRef = {
    new TruthValue(getFrequency, getConfidence)
  }

  /**
   * The String representation of a TruthValue
   * @return The String
   */
  override def toString(): String = {
    DELIMITER + frequency.toString + SEPARATOR + confidence.toString + 
      DELIMITER
  }

  /**
   * A simplified String representation of a TruthValue, where each factor is accruate to 1%
   * @return The String
   */
  def toStringBrief(): String = {
    val s1 = DELIMITER + frequency.toStringBrief() + SEPARATOR
    val s2 = confidence.toStringBrief()
    if (s2 == "1.00") {
      s1 + "0.99" + DELIMITER
    } else {
      s1 + s2 + DELIMITER
    }
  }
}
