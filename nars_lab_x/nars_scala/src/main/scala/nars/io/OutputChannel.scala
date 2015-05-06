package nars.io

import java.util.ArrayList
//remove if not needed
import scala.collection.JavaConversions._

/**
 * An interface to be implemented in all output channel
 */
trait OutputChannel {

  def nextOutput(output: ArrayList[String]): Unit
}
