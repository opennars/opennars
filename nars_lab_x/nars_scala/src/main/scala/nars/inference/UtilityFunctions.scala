package nars.logic

import nars.main.Parameters
//remove if not needed
import scala.collection.JavaConversions._

object UtilityFunctions {

  /**
   * A function where the output is conjunctively determined by the inputs
   * @param arr The inputs, each in [0, 1]
   * @return The output that is no larger than each input
   */
  def and(arr: Float*): Float = {
    var product: Float = 1
    for (f <- arr) {
      product *= f
    }
    product
  }

  /**
   * A function where the output is disjunctively determined by the inputs
   * @param arr The inputs, each in [0, 1]
   * @return The output that is no smaller than each input
   */
  def or(arr: Float*): Float = {
    var product: Float = 1
    for (f <- arr) {
      product *= (1 - f)
    }
    1 - product
  }

  /**
   * A function where the output is the arithmetic average the inputs
   * @param arr The inputs, each in [0, 1]
   * @return The arithmetic average the inputs
   */
  def aveAri(arr: Float*): Float = {
    var sum: Float = 0
    for (f <- arr) {
      sum += f
    }
    sum / arr.length
  }

  /**
   * A function where the output is the geometric average the inputs
   * @param arr The inputs, each in [0, 1]
   * @return The geometric average the inputs
   */
  def aveGeo(arr: Float*): Float = {
    var product: Float = 1
    for (f <- arr) {
      product *= f
    }
    Math.pow(product, 1.00 / arr.length).toFloat
  }

  /**
   * A function to convert weight to confidence
   * @param w Weight of evidence, a non-negative real number
   * @return The corresponding confidence, in [0, 1)
   */
  def w2c(w: Float): Float = w / (w + Parameters.HORIZON)

  /**
   * A function to convert confidence to weight
   * @param c confidence, in [0, 1)
   * @return The corresponding weight of evidence, a non-negative real number
   */
  def c2w(c: Float): Float = Parameters.HORIZON * c / (1 - c)
}
