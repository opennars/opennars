package nars.storage

//remove if not needed
import scala.collection.JavaConversions._

/**
 * A pseudo-random number generator, used in Bag.
 */
class Distributor(range: Int) {

    /**
   Capacity of the array
   */
  private var capacity: Int = (range * (range + 1)) / 2
  
  /**
   Shuffled sequence of index numbers
   */
  private var order = new Array[Int](capacity)

  var index: Int = _
  var rank: Int = _
  var time: Int = _

  index = 0
  while (index < capacity) {
    order(index) = -1
    index += 1
  }

  rank = range
  while (rank > 0) {
    time = 0
    while (time < rank) {
      index = ((capacity / rank) + index) % capacity
      while (order(index) >= 0) {
        index = (index + 1) % capacity
      }
      order(index) = rank - 1
      time += 1
    }
    rank -= 1
  }

  /**
   * Get the next number according to the given index
   * @param index The current index
   * @return the random value
   */
  def pick(index: Int): Int = order(index)

  /**
   * Advance the index
   * @param index The current index
   * @return the next index
   */
  def next(index: Int): Int = (index + 1) % capacity
}
