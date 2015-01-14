package nars.logic.entity

object ShortFloat {
  def computeValue(v: Float): Short = {
    if ((v < 0) || (v > 1)) {
      throw new ArithmeticException("ShortFloat: Invalid value: " + v)
    } else {
      (v * 10000.0 + 0.5).toShort
    }
  }
}

import ShortFloat._

/**
 * A float value in [0, 1], with 4 digits accuracy.
 */
class ShortFloat(var value: Short) extends Cloneable {

  /**
   * Constructor
   * @param v The initial value in float
   */
  def this(v: Float) {
    this( computeValue(v) )
  }

  /**
   * To access the value as float
   * @return The current value in float
   */
  def getValue(): Float = value * 0.0001f

  /**
   * To access the value as short
   * @return The current value in short
   */
  private def getShortValue(): Short = value

  /**
   * Set new value, rounded, with validity checking
   * @param v The new value
   */
  def setValue(v: Float):Unit = {
    value = computeValue(v)
  }

  /**
   * Compare two ShortFloat values
   * @param that The other value to be compared
   * @return Whether the two have the same value
   */
  override def equals(that: Any): Boolean = {
    ((that.isInstanceOf[ShortFloat]) && 
      (value == that.asInstanceOf[ShortFloat].getShortValue))
  }

  /**
   * The hash code of the ShortFloat
   * @return The hash code
   */
  override def hashCode(): Int = this.value + 17

  /**
   * To create an identical copy of the ShortFloat
   * @return A cloned ShortFloat
   */
  override def clone(): AnyRef = new ShortFloat(value)

  /**
   * Convert the value into a String
   * @return The String representation, with 4 digits accuracy
   */
  override def toString(): String = {
    if (value >= 10000) {
      "1.0000"
    } else {
      var s = String.valueOf(value)
      while (s.length < 4) {
        s = "0" + s
      }
      "0." + s
    }
  }

  /**
   * Round the value into a short String
   * @return The String representation, with 2 digits accuracy
   */
  def toStringBrief(): String = {
    value = ( value+50 ).asInstanceOf[Short]
    val s = toString
    value = ( value-50 ).asInstanceOf[Short]
    if (s.length > 4) {
      s.substring(0, 4)
    } else {
      s
    }
  }
}
