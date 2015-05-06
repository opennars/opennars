package nars.logic.entity

import java.util._
import nars.io.Symbols
import nars.main._
import Stamp._
import scala.reflect.{BeanProperty, BooleanBeanProperty}
//remove if not needed
import scala.collection.JavaConversions._

object Stamp {

  /**
   serial number, for the whole system
   */
  private var currentSerial: Long = 0

  /**
   * Try to merge two Stamps, return null if have overlap
   * <p>
   * By default, the event time of the first stamp is used in the result
   * @param first The first Stamp
   * @param second The second Stamp
   * @param time The new creation time
   * @return The merged Stamp, or null
   */
  def make(first: Stamp, second: Stamp, time: Long): Stamp = {
    for (i <- 0 until first.length; j <- 0 until second.length if first.get(i) == second.get(j)) {
      return null
    }
    if (first.length > second.length) {
      new Stamp(first, second, time)
    } else {
      new Stamp(second, first, time)
    }
  }

  /**
   * Initialize the stamp machenism of the system, called in Reasoner
   */
  def init() {
    currentSerial = 0
  }
}

/**
 * Each Sentence has a time stamp, consisting the following components:
 * (1) The creation time of the sentence,
 * (2) A evidentialBase of serial numbers of sentence, from which the sentence is derived.
 * Each input sentence gets a unique serial number, though the creation time may be not unique.
 * The derived sentences inherits serial numbers from its parents, cut at the baseLength limit.
 */
class Stamp(@BeanProperty var creationTime: Long) extends Cloneable {

    /** evidentialBase baseLength */
  private var baseLength: Int = 1
  
  /** serial numbers */
  private var evidentialBase: Array[Long] = new Array[Long](baseLength)

  currentSerial += 1
  evidentialBase(0) = currentSerial

  /**
   * Generate a new stamp identical with a given one
   * @param old The stamp to be cloned
   */
  private def this(old: Stamp) {
    this(old.getCreationTime)
    baseLength = old.length
    evidentialBase = old.getBase
    creationTime = old.getCreationTime
  }

  /**
   * Generate a new stamp from an existing one, with the same evidentialBase but different creation time
   * <p>
   * For single-premise rules
   * @param old The stamp of the single premise
   * @param time The current time
   */
  def this(old: Stamp, time: Long) {
    this(time)
    baseLength = old.length
    evidentialBase = old.getBase
  }

  /**
   * Generate a new stamp for derived sentence by merging the two from parents
   * the first one is no shorter than the second
   * @param first The first Stamp
   * @param second The second Stamp
   */
  private def this(first: Stamp, second: Stamp, time: Long) {
    this(time)
    var i1: Int = 0
    var i2: Int = 0
    var j: Int = 0
    baseLength = Math.min(first.length + second.length, Parameters.MAXIMUM_STAMP_LENGTH)
    evidentialBase = new Array[Long](baseLength)
    while (i2 < second.length && j < baseLength) {
      evidentialBase(j) = first.get(i1)
      i1 += 1
      j += 1
      evidentialBase(j) = second.get(i2)
      i2 += 1
      j += 1
    }
    while (i1 < first.length && j < baseLength) {
      evidentialBase(j) = first.get(i1)
      i1 += 1
      j += 1
    }
    creationTime = time
  }

  /**
   * Clone a stamp
   * @return The cloned stamp
   */
  override def clone(): AnyRef = new Stamp(this)

  /**
   * Return the baseLength of the evidentialBase
   * @return Length of the Stamp
   */
  def length(): Int = baseLength

  /**
   * Get a number from the evidentialBase by index, called in this class only
   * @param i The index
   * @return The number at the index
   */
  def get(i: Int): Long = evidentialBase(i)

  /**
   * Get the evidentialBase, called in this class only
   * @return The evidentialBase of numbers
   */
  private def getBase(): Array[Long] = evidentialBase

  /**
   * Convert the evidentialBase into a set
   * @return The TreeSet representation of the evidential base
   */
  private def toSet(): TreeSet[Long] = {
    val set = new TreeSet[Long]()
    for (i <- 0 until baseLength) {
      set.add(evidentialBase(i))
    }
    set
  }

  /**
   * Check if two stamps contains the same content
   * @param that The Stamp to be compared
   * @return Whether the two have contain the same elements
   */
  override def equals(that: Any): Boolean = {
    if (!(that.isInstanceOf[Stamp])) {
      return false
    }
    val set1 = toSet()
    val set2 = that.asInstanceOf[Stamp].toSet()
    (set1.containsAll(set2) && set2.containsAll(set1))
  }

  /**
   * The hash code of Stamp
   * @return The hash code
   */
  override def hashCode(): Int = toString.hashCode

  /**
   * Get a String form of the Stamp for display
   * Format: {creationTime [: eventTime] : evidentialBase}
   * @return The Stamp as a String
   */
  override def toString(): String = {
    val buffer = new StringBuffer(" " + Symbols.STAMP_OPENER + creationTime)
    buffer.append(" " + Symbols.STAMP_STARTER + " ")
    for (i <- 0 until baseLength) {
      buffer.append(java.lang.Long toString evidentialBase(i))
      if (i < (baseLength - 1)) {
        buffer.append(Symbols.STAMP_SEPARATOR)
      } else {
        buffer.append(Symbols.STAMP_CLOSER + " ")
      }
    }
    buffer.toString
  }
}
