package nars.logic.language

import java.util._
import nars.io.Symbols
import nars.storage.Memory
import Disjunction._
//remove if not needed
import scala.collection.JavaConversions._
import CompoundTerm._

object Disjunction {

  /**
   * Try to make a new Disjunction from two components. Called by the inference rules.
   * @param term1 The first component
   * @param term2 The first component
   * @param memory Reference to the memory
   * @return A Disjunction generated or a Term it reduced to
   */
  def make(term1: Term, term2: Term, memory: Memory): Term = {
    var set: TreeSet[Term] = null
    if (term1.isInstanceOf[Disjunction]) {
      set = new TreeSet[Term](term1.asInstanceOf[CompoundTerm].cloneComponents())
      if (term2.isInstanceOf[Disjunction]) {
        set.addAll(term2.asInstanceOf[CompoundTerm].cloneComponents())
      } else {
        set.add(term2.clone().asInstanceOf[Term])
      }
    } else if (term2.isInstanceOf[Disjunction]) {
      set = new TreeSet[Term](term2.asInstanceOf[CompoundTerm].cloneComponents())
      set.add(term1.clone().asInstanceOf[Term])
    } else {
      set = new TreeSet[Term]()
      set.add(term1.clone().asInstanceOf[Term])
      set.add(term2.clone().asInstanceOf[Term])
    }
    make(set, memory)
  }

  /**
   * Try to make a new IntersectionExt. Called by StringParser.
   * @param argList a list of Term as compoments
   * @param memory Reference to the memory
   * @return the Term generated from the arguments
   */
  def make(argList: ArrayList[Term], memory: Memory): Term = {
    val set = new TreeSet[Term](argList)
    make(set, memory)
  }

  /**
   * Try to make a new Disjunction from a set of components. Called by the public make methods.
   * @param set a set of Term as components
   * @param memory Reference to the memory
   * @return the Term generated from the arguments
   */
  def make(set: TreeSet[Term], memory: Memory): Term = {
    if (set.size == 1) {
      return set.first()
    }
    val argument = new ArrayList[Term](set)
    val name = makeCompoundName(Symbols.DISJUNCTION_OPERATOR, argument)
    val t = memory.nameToListedTerm(name)
    if ((t != null)) t else new Disjunction(argument)
  }
}

/**
 * A disjunction of Statements.
 */
class Disjunction private (arg: ArrayList[Term]) extends CompoundTerm(arg) {

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
   * @return A new object
   */
  override def clone(): AnyRef = {
    new Disjunction(name, cloneList(components).asInstanceOf[ArrayList[Term]], isConstant_, complexity)
  }

  /**
   * Get the operator of the term.
   * @return the operator of the term
   */
  def operator(): String = Symbols.DISJUNCTION_OPERATOR

  /**
   * Disjunction is commutative.
   * @return true for commutative
   */
  override def isCommutative(): Boolean = true
}
