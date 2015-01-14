package nars.logic.language

import java.util._
import nars.io.Symbols
import nars.storage.Memory
import Negation._
//remove if not needed
import scala.collection.JavaConversions._
import CompoundTerm._

object Negation {

  /**
   * Try to make a Negation of one component. Called by the inference rules.
   * @param t The compoment
   * @param memory Reference to the memory
   * @return A compound generated or a term it reduced to
   */
  def make(t: Term, memory: Memory): Term = {
    if (t.isInstanceOf[Negation]) {
      return t.asInstanceOf[CompoundTerm].cloneComponents().get(0)
    }
    val argument = new ArrayList[Term]()
    argument.add(t)
    make(argument, memory)
  }

  /**
   * Try to make a new SetExt. Called by StringParser.
   * @return the Term generated from the arguments
   * @param argument The list of components
   * @param memory Reference to the memory
   */
  def make(argument: ArrayList[Term], memory: Memory): Term = {
    if (argument.size != 1) {
      return null
    }
    val name = makeCompoundName(Symbols.NEGATION_OPERATOR, argument)
    val t = memory.nameToListedTerm(name)
    if ((t != null)) t else new Negation(argument)
  }
}

/**
 * A negation of a Statement.
 */
class Negation private (arg: ArrayList[Term]) extends CompoundTerm(arg) {

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
   * Clone an object
   * @return A new object
   */
  override def clone(): AnyRef = {
    new Negation(name, cloneList(components).asInstanceOf[ArrayList[Term]], isConstant_, complexity)
  }

  /**
   * Get the operator of the term.
   * @return the operator of the term
   */
  def operator(): String = Symbols.NEGATION_OPERATOR
}
