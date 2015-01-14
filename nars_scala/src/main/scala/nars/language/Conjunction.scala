package nars.logic.language

import java.util._
import nars.io.Symbols
import nars.storage.Memory
import Conjunction._
//remove if not needed
import scala.collection.JavaConversions._
import CompoundTerm._

object Conjunction {

  /**
   * Try to make a new compound from a list of components. Called by StringParser.
   * @return the Term generated from the arguments
   * @param argList the list of arguments
   * @param memory Reference to the memory
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
  private def make(set: TreeSet[Term], memory: Memory): Term = {
    if (set.size == 0) {
      return null
    }
    if (set.size == 1) {
      return set.first()
    }
    val argument = new ArrayList[Term](set)
    val name = makeCompoundName(Symbols.CONJUNCTION_OPERATOR, argument)
    val t = memory.nameToListedTerm(name)
    if ((t != null)) t else new Conjunction(argument)
  }

  /**
   * Try to make a new compound from two components. Called by the inference rules.
   * @param term1 The first compoment
   * @param term2 The second compoment
   * @param memory Reference to the memory
   * @return A compound generated or a term it reduced to
   */
  def make(term1: Term, term2: Term, memory: Memory): Term = {
    var set: TreeSet[Term] = null
    if (term1.isInstanceOf[Conjunction]) {
      set = new TreeSet[Term](term1.asInstanceOf[CompoundTerm].cloneComponents())
      if (term2.isInstanceOf[Conjunction]) {
        set.addAll(term2.asInstanceOf[CompoundTerm].cloneComponents())
      } else {
        set.add(term2.clone().asInstanceOf[Term])
      }
    } else if (term2.isInstanceOf[Conjunction]) {
      set = new TreeSet[Term](term2.asInstanceOf[CompoundTerm].cloneComponents())
      set.add(term1.clone().asInstanceOf[Term])
    } else {
      set = new TreeSet[Term]()
      set.add(term1.clone().asInstanceOf[Term])
      set.add(term2.clone().asInstanceOf[Term])
    }
    make(set, memory)
  }
}

/**
 * Conjunction of statements
 */
class Conjunction protected (arg: ArrayList[Term]) extends CompoundTerm(arg) {

  /**
   * Constructor with full values, called by clone
   * @param n The name of the term
   * @param cs Component list
   * @param con Whether the term is a constant
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
    new Conjunction(name, cloneList(components).asInstanceOf[ArrayList[Term]], isConstant_, complexity)
  }

  /**
   * Get the operator of the term.
   * @return the operator of the term
   */
  def operator(): String = Symbols.CONJUNCTION_OPERATOR

  /**
   * Check if the compound is communitative.
   * @return true for communitative
   */
  override def isCommutative(): Boolean = true
}
