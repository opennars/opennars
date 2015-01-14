package nars.storage

import java.util._
import nars.logic.entity.Item
import nars.gui.BagWindow
import nars.logic.BudgetFunctions
import nars.main.Parameters
import Bag._
//remove if not needed
import scala.collection.JavaConversions._

object Bag {

  /**
   priority levels
   */
  private val TOTAL_LEVEL = Parameters.BAG_LEVEL

  /**
   firing threshold
   */
  private val THRESHOLD = Parameters.BAG_THRESHOLD

  /**
   relative threshold, only calculate once
   */
  private val RELATIVE_THRESHOLD = THRESHOLD.toFloat / TOTAL_LEVEL.toFloat

  /**
   hashtable load factor
   */
  private val LOAD_FACTOR = Parameters.LOAD_FACTOR

  /**
   shared DISTRIBUTOR that produce the probability distribution
   */
  private val DISTRIBUTOR = new Distributor(TOTAL_LEVEL)
}

/**
 * A Bag is a storage with a constant capacity and maintains an internal priority
 * distribution for retrieval.
 * <p>
 * Each entity in a bag must extend Item, which has a BudgetValue and a key.
 * <p>
 * A name table is used to merge duplicate items that have the same key.
 * <p>
 * The bag space is divided by a threshold, above which is mainly time management,
 * and below, space management.
 * Differences: (1) level selection vs. item selection, (2) decay rate
 * @param <Type>  The type of the Item in the Bag
 */
abstract class Bag[Type <: Item] protected (protected var memory: Memory) {

  /**
   mapping from key to item
   */
  private var nameTable: HashMap[String, Type] = _

  /**
   array of lists of items, for items on different level
   */
  private var itemTable: ArrayList[ArrayList[Type]] = _

  /**
   defined in different bags
   */
  var capacity_ : Int = capacity()

  /**
   current sum of occupied level
   */
  private var mass: Int = _

  /**
   index to get next level, kept in individual objects
   */
  private var levelIndex: Int = _

  /**
   current take out level
   */
  private var currentLevel: Int = _

  /**
   maximum number of items to be taken out at current level
   */
  private var currentCounter: Int = _

  /**
   whether this bag has an active window
   */
  private var showing: Boolean = false

  /**
   display window TODO : remove GUI dependency
   */
  private var window: BagWindow = _

  init()

  def init() {
    itemTable = new ArrayList[ArrayList[Type]](TOTAL_LEVEL)
    for (i <- 0 until TOTAL_LEVEL) {
      itemTable.add(new ArrayList[Type]())
    }
    nameTable = new HashMap[String, Type]((capacity_ / LOAD_FACTOR).toInt, LOAD_FACTOR)
    currentLevel = TOTAL_LEVEL - 1
    levelIndex = capacity_ % TOTAL_LEVEL
    mass = 0
    currentCounter = 0
  }

      /**
     * To get the capacity of the concrete subclass
     * @return Bag capacity, in number of Items allowed
     */
    protected def capacity(): Int
    
  /**
   * Get the item decay rate, which differs in difference subclass, and can be
   * changed in run time by the user, so not a constant.
   * @return The number of times for a decay factor to be fully applied
   */
  protected def forgetRate(): Int

  /**
   * Get the average priority of Items
   * @return The average priority of Items in the bag
   */
  def averagePriority(): Float = {
    if (nameTable.size == 0) {
      return 0.01f
    }
    val f = mass.toFloat / (nameTable.size * TOTAL_LEVEL)
    if (f > 1) {
      return 1.0f
    }
    f
  }

  /**
   * Check if an item is in the bag
   * @param it An item
   * @return Whether the Item is in the Bag
   */
  def contains(it: Type): Boolean = nameTable.containsValue(it)

  /**
   * Get an Item by key
   * @param key The key of the Item
   * @return The Item with the given key
   */
  def get(key: String): Type = nameTable.get(key)

  /**
   * Add a new Item into the Bag
   * @param newItem The new Item
   * @return Whether the new Item is added into the Bag
   */
  def putIn(newItem: Type): Boolean = {
    val newKey = newItem.getKey
    val oldItem = nameTable.put(newKey, newItem)
    if (oldItem != null) {
      outOfBase(oldItem)
      newItem.merge(oldItem)
    }
    val overflowItem = intoBase(newItem)
    if (overflowItem != null) {
      val overflowKey = overflowItem.getKey
      nameTable.remove(overflowKey)
      (overflowItem != newItem)
    } else {
      true
    }
  }

  /**
   * Put an item back into the itemTable
   * <p>
   * The only place where the forgetting rate is applied
   * @param oldItem The Item to put back
   * @return Whether the new Item is added into the Bag
   */
  def putBack(oldItem: Type): Boolean = {
    BudgetFunctions.forget(oldItem.getBudget, forgetRate(), RELATIVE_THRESHOLD)
    putIn(oldItem)
  }

  /**
   * Choose an Item according to priority distribution and take it out of the Bag
   * @return The selected Item
   */
  def takeOut(): Type = {
    if (nameTable.isEmpty) {
      return null.asInstanceOf[Type]
    }
    if (emptyLevel(currentLevel) || (currentCounter == 0)) {
      currentLevel = DISTRIBUTOR.pick(levelIndex)
      levelIndex = DISTRIBUTOR.next(levelIndex)
      while (emptyLevel(currentLevel)) {
        currentLevel = DISTRIBUTOR.pick(levelIndex)
        levelIndex = DISTRIBUTOR.next(levelIndex)
      }
      currentCounter = if (currentLevel < THRESHOLD) 1 else itemTable.get(currentLevel).size
    }
    val selected = takeOutFirst(currentLevel)
    currentCounter -= 1
    nameTable.remove(selected.getKey)
    refresh()
    selected
  }

  /**
   * Pick an item by key, then remove it from the bag
   * @param key The given key
   * @return The Item with the key
   */
  def pickOut(key: String): Type = {
    val picked = nameTable.get(key)
    if (picked != null) {
      outOfBase(picked)
      nameTable.remove(key)
    }
    picked
  }

  /**
   * Check whether a level is empty
   * @param n The level index
   * @return Whether that level is empty
   */
  protected def emptyLevel(n: Int): Boolean = {
    ((itemTable.get(n) == null) || itemTable.get(n).isEmpty)
  }

  /**
   * Decide the put-in level according to priority
   * @param item The Item to put in
   * @return The put-in level
   */
  private def getLevel(item: Type): Int = {
    val fl = item.getPriority * TOTAL_LEVEL
    val level = Math.ceil(fl).toInt - 1
    if ((level < 0)) 0 else level
  }

  /**
   * Insert an item into the itemTable, and return the overflow
   * @param newItem The Item to put in
   * @return The overflow Item
   */
  private def intoBase(newItem: Type): Type = {
    var oldItem: Type = null.asInstanceOf[Type]
    val inLevel = getLevel(newItem)
    if (nameTable.size > capacity_) {
      var outLevel = 0
      while (emptyLevel(outLevel)) {
        outLevel += 1
      }
      if (outLevel > inLevel) {
        return newItem
      } else {
        oldItem = takeOutFirst(outLevel)
      }
    }
    itemTable.get(inLevel).add(newItem)
    mass += (inLevel + 1)
    refresh()
    oldItem
  }

  /**
   * Take out the first or last Type in a level from the itemTable
   * @param level The current level
   * @return The first Item
   */
  private def takeOutFirst(level: Int): Type = {
    val selected = itemTable.get(level).get(0)
    itemTable.get(level).remove(0)
    mass -= (level + 1)
    refresh()
    selected
  }

  /**
   * Remove an item from itemTable, then adjust mass
   * @param oldItem The Item to be removed
   */
  protected def outOfBase(oldItem: Type) {
    val level = getLevel(oldItem)
    itemTable.get(level).remove(oldItem)
    mass -= (level + 1)
    refresh()
  }

  /**
   * To start displaying the Bag in a BagWindow
   * TODO these 4 GUI methods should be moved in class {@link BagWindow}
   * @param title The title of the window
   */
  def startPlay(title: String) {
    window = new BagWindow(this, title)
    showing = true
    window.post(toString)
  }

  /**
   * Resume display
   */
  def play() {
    showing = true
    window.post(toString)
  }

  /**
   * Refresh display
   */
  def refresh() {
    if (showing) {
      window.post(toString)
    }
  }

  /**
   * Stop display
   */
  def stop() {
    showing = false
  }

  /**
   * Collect Bag content into a String for display
   */
  override def toString(): String = {
    var buf = new StringBuffer(" ")
    val showLevel = if (window == null) 1 else window.showLevel
    var i = TOTAL_LEVEL
    while (i >= showLevel) {
      if (!emptyLevel(i - 1)) {
        buf = buf.append("\n --- Level " + i + ":\n ")
        for (j <- 0 until itemTable.get(i - 1).size) {
          buf = buf.append(itemTable.get(i - 1).get(j).toStringBrief() + "\n ")
        }
      }
      i -= 1
    }
    buf.toString
  }

  /**
   TODO bad paste from preceding
   */
  def toStringLong(): String = {
    var buf = new StringBuffer(" BAG " + getClass.getSimpleName)
    buf.append(" ").append(showSizes())
    val showLevel = if (window == null) 1 else window.showLevel
    var i = TOTAL_LEVEL
    while (i >= showLevel) {
      if (!emptyLevel(i - 1)) {
        buf = buf.append("\n --- LEVEL " + i + ":\n ")
        for (j <- 0 until itemTable.get(i - 1).size) {
          buf = buf.append(itemTable.get(i - 1).get(j).toStringLong() + "\n ")
        }
      }
      i -= 1
    }
    buf.append(">>>> end of Bag").append(getClass.getSimpleName)
    buf.toString
  }

  def showSizes(): String = {
    val buf = new StringBuilder(" ")
    var levels = 0
    for (items <- itemTable if (items != null) && !items.isEmpty) {
      levels += 1
      buf.append(items.size).append(" ")
    }
    "Levels: " + (java.lang.Integer toString (levels)) + ", sizes: " + buf
  }
}
