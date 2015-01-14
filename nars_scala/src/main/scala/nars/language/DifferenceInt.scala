package nars.logic.language

import java.util._
import nars.io.Symbols
import nars.storage.Memory
import DifferenceInt._
//remove if not needed
import scala.collection.JavaConversions._
import CompoundTerm._

object DifferenceInt {

  /**
   * Try to make a new DifferenceExt. Called by StringParser.
   * @return the Term generated from the arguments
   * @param argList The list of components
   * @param memory Reference to the memory
   */
  def make(argList: ArrayList[Term], memory: Memory): Term = {
    if (argList.size == 1) {
      return argList.get(0)
    }
    if (argList.size != 2) {
      return null
    }
    val name = makeCompoundName(Symbols.DIFFERENCE_INT_OPERATOR, argList)
    val t = memory.nameToListedTerm(name)
    if ((t != null)) t else new DifferenceInt(argList)
  }

  /**
   * Try to make a new compound from two components. Called by the inference rules.
   * @param t1 The first compoment
   * @param t2 The second compoment
   * @param memory Reference to the memory
   * @return A compound generated or a term it reduced to
   */
  def make(t1: Term, t2: Term, memory: Memory): Term = {
    if (t1 == t2) {
      return null
    }
    if ((t1.isInstanceOf[SetInt]) && (t2.isInstanceOf[SetInt])) {
      val set = new TreeSet[Term](t1.asInstanceOf[CompoundTerm].cloneComponents())
      set.removeAll(t2.asInstanceOf[CompoundTerm].cloneComponents())
      return SetInt.make(set, memory)
    }
    val list = argumentsToList(t1, t2)
    make(list, memory)
  }
}

/**
 * A compound term whose extension is the difference of the intensions of its components
 */
class DifferenceInt private (arg: ArrayList[Term]) extends CompoundTerm(arg) {

  /**
   * Constructor with full values, called by clone
   * @param n The name of the term
   * @param cs Component list
   * @param open Open variable list
   * @param i Syntactic complexity of the compound
   */
  private def this( name: String, 
      components: ArrayList[Term], 
      isConstant: Boolean, 
      complexity: Short ) {
//    super(n, cs, con, i)
    this(components)
    setName(name)
    this.complexity = complexity
    this.isConstant_ = isConstant
  }

  /**
   * Clone an object
   * @return A new object, to be casted into a DifferenceInt
   */
  override def clone(): AnyRef = {
    new DifferenceInt(name, cloneList(components).asInstanceOf[ArrayList[Term]], isConstant_, complexity)
  }

  /**
   * Get the operator of the term.
   * @return the operator of the term
   */
  def operator(): String = Symbols.DIFFERENCE_INT_OPERATOR
}
