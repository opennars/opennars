package nars.logic.entity

//remove if not needed
import scala.collection.JavaConversions._

/**
 * An item is an object that can be put into a Bag,
 * to participate in the resource competation of the system.
 * <p>
 * It has a key and a budget. Cannot be cloned
 */
abstract class Item protected () {

  /**
   The key of the Item, unique in a Bag
   */
  protected var key: String = _

  /**
   The budget of the Item, consisting of 3 numbers
   */
  protected var budget: BudgetValue = _

  /**
   * Constructor with default budget
   * @param key The key value
   */
  protected def this(key: String) {
    this()
    this.key = key
    this.budget = new BudgetValue()
  }

  /**
   * Constructor with initial budget
   * @param key The key value
   * @param budget The initial budget
   */
  protected def this(key: String, budget: BudgetValue) {
    this()
    this.key = key
    this.budget = new BudgetValue(budget)
  }

  /**
   * Constructor with initial budget
   * @param budget The initial budget
   */
  protected def setBudget(budget: BudgetValue) {
    this.budget = budget
  }

  /**
   * Get the current key
   * @return Current key value
   */
  def getKey(): String = key

  /**
   * Get BudgetValue
   * @return Current BudgetValue
   */
  def getBudget(): BudgetValue = budget

  /**
   * Get priority value
   * @return Current priority value
   */
  def getPriority(): Float = budget.getPriority

  /**
   * Set priority value
   * @param v Set a new priority value
   */
  def setPriority(v: Float) {
    budget.setPriority(v)
  }

  /**
   * Increase priority value
   * @param v The amount of increase
   */
  def incPriority(v: Float) {
    budget.incPriority(v)
  }

  /**
   * Decrease priority value
   * @param v The amount of decrease
   */
  def decPriority(v: Float) {
    budget.decPriority(v)
  }

  /**
   * Get durability value
   * @return Current durability value
   */
  def getDurability(): Float = budget.getDurability

  /**
   * Set durability value
   * @param v The new durability value
   */
  def setDurability(v: Float) {
    budget.setDurability(v)
  }

  /**
   * Increase durability value
   * @param v The amount of increase
   */
  def incDurability(v: Float) {
    budget.incDurability(v)
  }

  /**
   * Decrease durability value
   * @param v The amount of decrease
   */
  def decDurability(v: Float) {
    budget.decDurability(v)
  }

  /**
   * Get quality value
   * @return The quality value
   */
  def getQuality(): Float = budget.getQuality

  /**
   * Set quality value
   * @param v The new quality value
   */
  def setQuality(v: Float) {
    budget.setQuality(v)
  }

  /**
   * Merge with another Item with identical key
   * @param that The Item to be merged
   */
  def merge(that: Item) {
    budget.merge(that.getBudget)
  }

  /**
   * Return a String representation of the Item
   * @return The String representation of the full content
   */
  override def toString(): String = budget + " " + key

  /**
   * Return a String representation of the Item after simplification
   * @return A simplified String representation of the content
   */
  def toStringBrief(): String = budget.toStringBrief() + " " + key

  def toStringLong(): String = toString
}
