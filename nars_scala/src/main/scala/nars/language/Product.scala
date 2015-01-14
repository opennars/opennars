package nars.logic.language

import java.util._
import nars.io.Symbols
import nars.storage.Memory
import Product._
//remove if not needed
import scala.collection.JavaConversions._
import CompoundTerm._

object Product {

  /**
   * Try to make a new compound. Called by StringParser.
   * @return the Term generated from the arguments
   * @param argument The list of components
   * @param memory Reference to the memeory
   */
  def make(argument: ArrayList[Term], memory: Memory): Term = {
    val name = makeCompoundName(Symbols.PRODUCT_OPERATOR, argument)
    val t = memory.nameToListedTerm(name)
    if ((t != null)) t else new Product(argument)
  }

  /**
   * Try to make a Product from an ImageExt/ImageInt and a component. Called by the inference rules.
   * @param image The existing Image
   * @param component The component to be added into the component list
   * @param index The index of the place-holder in the new Image -- optional parameter
   * @param memory Reference to the memeory
   * @return A compound generated or a term it reduced to
   */
  def make(image: CompoundTerm, 
      component: Term, 
      index: Int, 
      memory: Memory): Term = {
    val argument = image.cloneComponents()
    argument.set(index, component)
    make(argument, memory)
  }
}

/**
 * A Product is a sequence of terms.
 */
class Product private (arg: ArrayList[Term]) extends CompoundTerm(arg) {

  /**
   * Constructor with full values, called by clone
   * @param n The name of the term
   * @param cs Component list
   * @param open Open variable list
   * @param complexity Syntactic complexity of the compound
   */
  private def this(n: String, 
      cs: ArrayList[Term], 
      con: Boolean, 
      complexity: Short) {
//    super(n, cs, con, complexity)
    this(cs)
    setName(n)
    this.isConstant_ = con
    this.complexity = complexity
  }

  /**
   * Clone a Product
   * @return A new object, to be casted into an ImageExt
   */
  override def clone(): AnyRef = {
    new Product(name, cloneList(components).asInstanceOf[ArrayList[Term]], isConstant_, complexity)
  }

  /**
   * Get the operator of the term.
   * @return the operator of the term
   */
  def operator(): String = Symbols.PRODUCT_OPERATOR
}
