wpackage nars.logic.language

import java.util._
import nars.io.Symbols
import nars.storage.Memory
import ImageExt._
import scala.reflect.{BeanProperty, BooleanBeanProperty}
//remove if not needed
import scala.collection.JavaConversions._
import CompoundTerm._

object ImageExt {

  /**
   * Try to make a new ImageExt. Called by StringParser.
   * @return the Term generated from the arguments
   * @param argList The list of components
   * @param memory Reference to the memory
   */
  def make(argList: ArrayList[Term], memory: Memory): Term = {
    if (argList.size < 2) {
      return null
    }
    val relation = argList.get(0)
    val argument = new ArrayList[Term]()
    var index = 0
    for (j <- 1 until argList.size) {
      if (argList.get(j).getName.charAt(0) == Symbols.IMAGE_PLACE_HOLDER) {
        index = j - 1
        argument.add(relation)
      } else {
        argument.add(argList.get(j))
      }
    }
    make(argument, index.toShort, memory)
  }

  /**
   * Try to make an Image from a Product and a relation. Called by the inference rules.
   * @param product The product
   * @param relation The relation
   * @param index The index of the place-holder
   * @return A compound generated or a term it reduced to
   */
  def make(product: Product, 
      relation: Term, 
      index: Short, 
      memory: Memory): Term = {
    if (relation.isInstanceOf[Product]) {
      val p2 = relation.asInstanceOf[Product]
      if ((product.size == 2) && (p2.size == 2)) {
        if ((index == 0) && product.componentAt(1) == p2.componentAt(1)) {
          return p2.componentAt(0)
        }
        if ((index == 1) && product.componentAt(0) == p2.componentAt(0)) {
          return p2.componentAt(1)
        }
      }
    }
    val argument = product.cloneComponents()
    argument.set(index, relation)
    make(argument, index, memory)
  }

  /**
   * Try to make an Image from an existing Image and a component. Called by the inference rules.
   * @param oldImage The existing Image
   * @param component The component to be added into the component list
   * @param index The index of the place-holder in the new Image
   * @return A compound generated or a term it reduced to
   */
  def make(oldImage: ImageExt, 
      component: Term, 
      index: Short, 
      memory: Memory): Term = {
    val argList = oldImage.cloneComponents()
    val oldIndex = oldImage.getRelationIndex
    val relation = argList.get(oldIndex)
    argList.set(oldIndex, component)
    argList.set(index, relation)
    make(argList, index, memory)
  }

  /**
   * Try to make a new compound from a set of components. Called by the public make methods.
   * @param argument The argument list
   * @param index The index of the place-holder in the new Image
   * @return the Term generated from the arguments
   */
  def make(argument: ArrayList[Term], index: Short, memory: Memory): Term = {
    val name = makeImageName(Symbols.IMAGE_EXT_OPERATOR, argument, index)
    val t = memory.nameToListedTerm(name)
    if ((t != null)) t else new ImageExt(name, argument, index)
  }
}

/**
 * An extension image.
 * <p>
 * B --> (/,P,A,_)) iff (*,A,B) --> P
 * <p>
 * Internally, it is actually (/,A,P)_1, with an index.
 */
class ImageExt private (n: String, arg: ArrayList[Term], @BeanProperty var relationIndex: Short)
    extends CompoundTerm(n, arg) {

  /**
   * Constructor with full values, called by clone
   * @param n The name of the term
   * @param cs Component list
   * @param open Open variable list
   * @param complexity Syntactic complexity of the compound
   * @param index The index of relation in the component list
   */
  private def this( name: String, 
      components : ArrayList[Term], 
      isConstant : Boolean, 
      complexity: Short, 
      index: Short) {
    this(name, components, index)
    this.complexity = complexity
    this.isConstant_ = isConstant
//    super(n, cs, con, complexity)
  }

  /**
   * Clone an object
   * @return A new object, to be casted into an ImageExt
   */
  override def clone(): AnyRef = {
    new ImageExt(name, cloneList(components).asInstanceOf[ArrayList[Term]], isConstant_, complexity, relationIndex)
  }

  /**
   * Get the relation term in the Image
   * @return The term representing a relation
   */
  def getRelation(): Term = components.get(relationIndex)

  /**
   * Get the other term in the Image
   * @return The term related
   */
  def getTheOtherComponent(): Term = {
    if (components.size != 2) {
      return null
    }
    if ((relationIndex == 0)) components.get(1) else components.get(0)
  }

  /**
   * override the default in making the name of the current term from existing fields
   * @return the name of the term
   */
  override def makeName(): String = {
    makeImageName(Symbols.IMAGE_EXT_OPERATOR, components, relationIndex)
  }

  /**
   * get the operator of the term.
   * @return the operator of the term
   */
  def operator(): String = Symbols.IMAGE_EXT_OPERATOR
}
