package nars.logic.entity

import nars.logic._
import nars.io.Symbols
import nars.main._
import BudgetValue._
//remove if not needed
import scala.collection.JavaConversions._

object BudgetValue {

  /**
   The character that marks the two ends of a budget value
   */
  private val MARK = Symbols.BUDGET_VALUE_MARK

  /**
   The character that separates the factors in a budget value
   */
  private val SEPARATOR = Symbols.VALUE_SEPARATOR
}

/**
 * A triple of priority (current), durability (decay), and quality (long-term average).
 */
class BudgetValue extends Cloneable {

  /**
   The relative share of time resource to be allocated
   */
  protected var priority: ShortFloat = new ShortFloat(0.01f)

  /**
   The percent of priority to be kept in a constant period
   */
  protected var durability: ShortFloat = new ShortFloat(0.01f)

  /**
   The overall (context-independent) evaluation
   */
  protected var quality: ShortFloat = new ShortFloat(0.01f)

  /**
   * Constructor with initialization
   * @param p Initial priority
   * @param d Initial durability
   * @param q Initial quality
   */
  def this(p: Float, d: Float, q: Float) {
    this()
    priority = new ShortFloat(p)
    durability = new ShortFloat(d)
    quality = new ShortFloat(q)
  }

  /**
   * Cloning constructor
   * @param v Budget value to be cloned
   */
  def this(v: BudgetValue) {
    this()
    priority = new ShortFloat(v.getPriority)
    durability = new ShortFloat(v.getDurability)
    quality = new ShortFloat(v.getQuality)
  }

  /**
   * Cloning method
   */
  override def clone(): AnyRef = {
    new BudgetValue(this.getPriority, this.getDurability, this.getQuality)
  }

  /**
   * Get priority value
   * @return The current priority
   */
  def getPriority(): Float = priority.getValue

  /**
   * Change priority value
   * @param v The new priority
   */
  def setPriority(v: Float) {
    priority.setValue(v)
  }

  /**
   * Increase priority value by a percentage of the remaining range
   * @param v The increasing percent
   */
  def incPriority(v: Float) {
    priority.setValue(UtilityFunctions.or(priority.getValue, v))
  }

  /**
   * Decrease priority value by a percentage of the remaining range
   * @param v The decreasing percent
   */
  def decPriority(v: Float) {
    priority.setValue(UtilityFunctions.and(priority.getValue, v))
  }

  /**
   * Get durability value
   * @return The current durability
   */
  def getDurability(): Float = durability.getValue

  /**
   * Change durability value
   * @param v The new durability
   */
  def setDurability(v: Float) {
    durability.setValue(v)
  }

  /**
   * Increase durability value by a percentage of the remaining range
   * @param v The increasing percent
   */
  def incDurability(v: Float) {
    durability.setValue(UtilityFunctions.or(durability.getValue, v))
  }

  /**
   * Decrease durability value by a percentage of the remaining range
   * @param v The decreasing percent
   */
  def decDurability(v: Float) {
    durability.setValue(UtilityFunctions.and(durability.getValue, v))
  }

  /**
   * Get quality value
   * @return The current quality
   */
  def getQuality(): Float = quality.getValue

  /**
   * Change quality value
   * @param v The new quality
   */
  def setQuality(v: Float) {
    quality.setValue(v)
  }

  /**
   * Increase quality value by a percentage of the remaining range
   * @param v The increasing percent
   */
  def incQuality(v: Float) {
    quality.setValue(UtilityFunctions.or(quality.getValue, v))
  }

  /**
   * Decrease quality value by a percentage of the remaining range
   * @param v The decreasing percent
   */
  def decQuality(v: Float) {
    quality.setValue(UtilityFunctions.and(quality.getValue, v))
  }

  /**
   * Merge one BudgetValue into another
   * @param that The other Budget
   */
  def merge(that: BudgetValue) {
    BudgetFunctions.merge(this, that)
  }

  /**
   * To summarize a BudgetValue into a single number in [0, 1]
   * @return The summary value
   */
  def summary(): Float = {
    UtilityFunctions.aveGeo(priority.getValue, durability.getValue, quality.getValue)
  }

  /**
   * Whether the budget should get any processing at all
   * <p>
   * to be revised to depend on how busy the system is
   * @return The decision on whether to process the Item
   */
  def aboveThreshold(): Boolean = {
    (summary() >= Parameters.BUDGET_THRESHOLD)
  }

  /**
   * Fully display the BudgetValue
   * @return String representation of the value
   */
  override def toString(): String = {
    MARK + priority.toString + SEPARATOR + durability.toString + 
      SEPARATOR + 
      quality.toString + 
      MARK
  }

  /**
   * Briefly display the BudgetValue
   * @return String representation of the value with 2-digit accuracy
   */
  def toStringBrief(): String = {
    MARK + priority.toStringBrief() + SEPARATOR + durability.toStringBrief() + 
      SEPARATOR + 
      quality.toStringBrief() + 
      MARK
  }
}
