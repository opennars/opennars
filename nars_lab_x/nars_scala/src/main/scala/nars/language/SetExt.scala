package nars.logic.language

import java.util._
import nars.io.Symbols
import nars.storage.Memory
import SetExt._
//remove if not needed
import scala.collection.JavaConversions._
import CompoundTerm._

object SetExt {

  /**
   * Try to make a new set from one component. Called by the inference rules.
   * @param t The compoment
   * @param memory Reference to the memeory
   * @return A compound generated or a term it reduced to
   */
  def make(t: Term, memory: Memory): Term = {
    val set = new TreeSet[Term]()
    set.add(t)
    make(set, memory)
  }

  /**
   * Try to make a new SetExt. Called by StringParser.
   * @return the Term generated from the arguments
   * @param argList The list of components
   * @param memory Reference to the memeory
   */
  def make(argList: ArrayList[Term], memory: Memory): Term = {
    val set = new TreeSet[Term](argList)
    make(set, memory)
  }

  /**
   * Try to make a new compound from a set of components. Called by the public make methods.
   * @param set a set of Term as compoments
   * @param memory Reference to the memeory
   * @return the Term generated from the arguments
   */
  def make(set: TreeSet[Term], memory: Memory): Term = {
    if (set.isEmpty) {
      return null
    }
    val argument = new ArrayList[Term](set)
    val name = makeSetName(Symbols.SET_EXT_OPENER, argument, Symbols.SET_EXT_CLOSER)
    val t = memory.nameToListedTerm(name)
    if ((t != null)) t else new SetExt(argument)
  }
}

/**
 * An extensionally defined set, which contains one or more instances.
 */
class SetExt private (arg: ArrayList[Term]) extends CompoundTerm(arg) {

  /**
   * Constructor with full values, called by clone
   * @param n The name of the term
   * @param cs Component list
   * @param open Open variable list
   * @param i Syntactic complexity of the compound
   */
  private def this(n: String, 
      cs: ArrayList[Term], 
      con: Boolean, 
      i: Short) {
//    super(n, cs, con, i)
    this(cs)
    setName(n)
    this.isConstant_ = con
    this.complexity = i
  }

  /**
   * Clone a SetExt
   * @return A new object, to be casted into a SetExt
   */
  override def clone(): AnyRef = {
    new SetExt(name, cloneList(components).asInstanceOf[ArrayList[Term]], isConstant_, complexity)
  }

  /**
   * Get the operator of the term.
   * @return the operator of the term
   */
  def operator(): String = "" + Symbols.SET_EXT_OPENER

  /**
   * Check if the compound is communitative.
   * @return true for communitative
   */
  override def isCommutative(): Boolean = true

  /**
   * Make a String representation of the set, override the default.
   * @return true for communitative
   */
  override def makeName(): String = {
    makeSetName(Symbols.SET_EXT_OPENER, components, Symbols.SET_EXT_CLOSER)
  }
}
