package nars.io

//remove if not needed
import scala.collection.JavaConversions._

/**
 * An interface to be implemented in all input channels
 * To get the input for the next moment from an input channel
 * The return value indicating whether the reasoner should run
 */
trait InputChannel {

  def nextInput(): Boolean
}
