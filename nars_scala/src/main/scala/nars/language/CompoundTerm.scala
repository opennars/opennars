package nars.logic.language

import java.util._
import nars.logic.entity._
import nars.storage._
import nars.io.Symbols
import CompoundTerm._
//remove if not needed
import scala.collection.JavaConversions._

object CompoundTerm {

  /**
   * Try to make a compound term from a template and a list of components
   * @param compound The template
   * @param components The components
   * @param memory Reference to the memory
   * @return A compound term or null
   */
  def make(compound: CompoundTerm, components: ArrayList[Term], memory: Memory): Term = {
    if (compound.isInstanceOf[ImageExt]) {
      ImageExt.make(components, compound.asInstanceOf[ImageExt].getRelationIndex, memory)
    } else if (compound.isInstanceOf[ImageInt]) {
      ImageInt.make(components, compound.asInstanceOf[ImageInt].getRelationIndex, memory)
    } else {
      make(compound.operator(), components, memory)
    }
  }

  /**
   * Try to make a compound term from an operator and a list of components
   * <p>
   * Called from StringParser
   * @param op Term operator
   * @param arg Component list
   * @param memory Reference to the memory
   * @return A compound term or null
   */
  def make(op: String, arg: ArrayList[Term], memory: Memory): Term = {
    if (op.length == 1) {
      if (op.charAt(0) == Symbols.SET_EXT_OPENER) {
        return SetExt.make(arg, memory)
      }
      if (op.charAt(0) == Symbols.SET_INT_OPENER) {
        return SetInt.make(arg, memory)
      }
      if (op == Symbols.INTERSECTION_EXT_OPERATOR) {
        return IntersectionExt.make(arg, memory)
      }
      if (op == Symbols.INTERSECTION_INT_OPERATOR) {
        return IntersectionInt.make(arg, memory)
      }
      if (op == Symbols.DIFFERENCE_EXT_OPERATOR) {
        return DifferenceExt.make(arg, memory)
      }
      if (op == Symbols.DIFFERENCE_INT_OPERATOR) {
        return DifferenceInt.make(arg, memory)
      }
      if (op == Symbols.PRODUCT_OPERATOR) {
        return Product.make(arg, memory)
      }
      if (op == Symbols.IMAGE_EXT_OPERATOR) {
        return ImageExt.make(arg, memory)
      }
      if (op == Symbols.IMAGE_INT_OPERATOR) {
        return ImageInt.make(arg, memory)
      }
    }
    if (op.length == 2) {
      if (op == Symbols.NEGATION_OPERATOR) {
        return Negation.make(arg, memory)
      }
      if (op == Symbols.DISJUNCTION_OPERATOR) {
        return Disjunction.make(arg, memory)
      }
      if (op == Symbols.CONJUNCTION_OPERATOR) {
        return Conjunction.make(arg, memory)
      }
    }
    null
  }

  /**
   * Check CompoundTerm operator symbol
   * @return if the given String is an operator symbol
   * @param s The String to be checked
   */
  def isOperator(s: String): Boolean = {
    if (s.length == 1) {
      return (s == Symbols.INTERSECTION_EXT_OPERATOR || s == Symbols.INTERSECTION_INT_OPERATOR || 
        s == Symbols.DIFFERENCE_EXT_OPERATOR || 
        s == Symbols.DIFFERENCE_INT_OPERATOR || 
        s == Symbols.PRODUCT_OPERATOR || 
        s == Symbols.IMAGE_EXT_OPERATOR || 
        s == Symbols.IMAGE_INT_OPERATOR)
    }
    if (s.length == 2) {
      return (s == Symbols.NEGATION_OPERATOR || s == Symbols.DISJUNCTION_OPERATOR || 
        s == Symbols.CONJUNCTION_OPERATOR)
    }
    false
  }

  /**
   * build a component list from two terms
   * @param t1 the first component
   * @param t2 the second component
   * @return the component list
   */
//  protected jmv
  def argumentsToList(t1: Term, t2: Term): ArrayList[Term] = {
    val list = new ArrayList[Term](2)
    list.add(t1)
    list.add(t2)
    list
  }

  /**
   * default method to make the oldName of a compound term from given fields
   * @param op the term operator
   * @param arg the list of components
   * @return the oldName of the term
   */
//  protected jmv
  def makeCompoundName(op: String, arg: ArrayList[Term]): String = {
    val name = new StringBuffer()
    name.append(Symbols.COMPOUND_TERM_OPENER)
    name.append(op)
    for (t <- arg) {
      name.append(Symbols.ARGUMENT_SEPARATOR)
      if (t.isInstanceOf[CompoundTerm]) {
        t.asInstanceOf[CompoundTerm].setName(t.asInstanceOf[CompoundTerm].makeName())
      }
      name.append(t.getName)
    }
    name.append(Symbols.COMPOUND_TERM_CLOSER)
    name.toString
  }

  /**
   * make the oldName of an ExtensionSet or IntensionSet
   * @param opener the set opener
   * @param closer the set closer
   * @param arg the list of components
   * @return the oldName of the term
   */
//  protected jmv 
  def makeSetName(opener: Char, arg: ArrayList[Term], closer: Char): String = {
    val name = new StringBuffer()
    name.append(opener)
    name.append(arg.get(0).getName)
    for (i <- 1 until arg.size) {
      name.append(Symbols.ARGUMENT_SEPARATOR)
      name.append(arg.get(i).getName)
    }
    name.append(closer)
    name.toString
  }

  /**
   * default method to make the oldName of an image term from given fields
   * @param op the term operator
   * @param arg the list of components
   * @param relationIndex the location of the place holder
   * @return the oldName of the term
   */
  // protected jmv
  def makeImageName(op: String, arg: ArrayList[Term], relationIndex: Int): String = {
    val name = new StringBuffer()
    name.append(Symbols.COMPOUND_TERM_OPENER)
    name.append(op)
    name.append(Symbols.ARGUMENT_SEPARATOR)
    name.append(arg.get(relationIndex).getName)
    for (i <- 0 until arg.size) {
      name.append(Symbols.ARGUMENT_SEPARATOR)
      if (i == relationIndex) {
        name.append(Symbols.IMAGE_PLACE_HOLDER)
      } else {
        name.append(arg.get(i).getName)
      }
    }
    name.append(Symbols.COMPOUND_TERM_CLOSER)
    name.toString
  }

  /**
   * Deep clone an array list of terms
   * @param original The original component list
   * @return an identical and separate copy of the list
   */
  def cloneList(original: ArrayList[Term]): ArrayList[Term] = {
    if (original == null) {
      return null
    }
    val arr = new ArrayList[Term](original.size)
    for (i <- 0 until original.size) {
      arr.add(original.get(i).asInstanceOf[Term].clone().asInstanceOf[Term])
    }
    arr
  }

  /**
   * Try to add a component into a compound
   * @param t1 The compound
   * @param t2 The component
   * @param memory Reference to the memory
   * @return The new compound
   */
  def addComponents(t1: CompoundTerm, t2: Term, memory: Memory): Term = {
    if (t2 == null) {
      return t1
    }
    val list = t1.cloneComponents()
    var success =
    	if (t1.getClass == t2.getClass) list.addAll(t2.asInstanceOf[CompoundTerm].getComponents) else list.add(t2)
    (if (success) make(t1, list, memory) else null)
  }

  /**
   * Try to remove a component from a compound
   * @param t1 The compound
   * @param t2 The component
   * @param memory Reference to the memory 
   * @return The new compound
   */
  def reduceComponents(t1: CompoundTerm, t2: Term, memory: Memory): Term = {
    val list = t1.cloneComponents()
    var success =
    	if (t1.getClass == t2.getClass) list.removeAll(t2.asInstanceOf[CompoundTerm].getComponents) else list.remove(t2)
    (if (success) make(t1, list, memory) else null)
  }

  /**
   * Try to replace a component in a compound at a given index by another one
   * @param compound The compound
   * @param index The location of replacement
   * @param t The new component
   * @param memory Reference to the memory 
   * @return The new compound
   */
  def setComponent(compound: CompoundTerm, 
      index: Int, 
      t: Term, 
      memory: Memory): Term = {
    val list = compound.cloneComponents()
    list.remove(index)
    if (t != null) {
      if (compound.getClass != t.getClass) {
        list.add(index, t)
      } else {
        val list2 = t.asInstanceOf[CompoundTerm].cloneComponents()
        for (i <- 0 until list2.size) {
          list.add(index + i, list2.get(i))
        }
      }
    }
    make(compound, list, memory)
  }
}

/**
 * A CompoundTerm is a Term with internal (syntactic) structure
 * <p>
 * A CompoundTerm consists of a term operator with one or more component Terms.
 * <p>
 * This abstract class contains default methods for all CompoundTerms.
 */
abstract class CompoundTerm protected (protected val components: ArrayList[Term]) extends Term {

  /**
   list of (direct) components
   */
//  protected var components: ArrayList[Term] = _
	
  /**
   syntactic complexity of the compound, the sum of those of its components plus 1
   */
  protected var complexity: Short = _

  /**
   Whether the term names a concept
   */
  protected var isConstant_ : Boolean = true

  /**
   * Abstract method to get the operator of the compound
   * @return The operator in a String
   */
  def operator(): String

  /**
   * Abstract clone method
   * @return A clone of the compound term
   */
  override def clone(): AnyRef

  /**
   * Constructor called from subclasses constructors to initialize the fields
   * @param components Component list
   */
//  protected def this(components: ArrayList[Term]) {
//    this()
//    this.components = components
    calcComplexity()
    name = makeName()
    isConstant_ = !Variable.containVar(name)
//  }
  
  /**
   * Constructor called from subclasses constructors to clone the fields
   * @param name Name
   * @param components Component list
   * @param isConstant Whether the term refers to a concept
   * @param complexity Complexity of the compound term
   */
  protected def this(name: String, 
      components: ArrayList[Term], 
      isConstant: Boolean, 
      complexity: Short) {
//    super(name)
    this(components)
    setName(name);
//    this.components = components
    this.isConstant_ = isConstant
    this.complexity = complexity
  }

  /**
   * Constructor called from subclasses constructors to initialize the fields
   * @param name Name of the compound
   * @param components Component list
   */
  protected def this(name: String, components: ArrayList[Term]) {
//    super(name)
    this(components)
    setName(name);
    isConstant_ = !Variable.containVar(name)
//    this.components = components
    calcComplexity()
  }

  /**
   * Change the oldName of a CompoundTerm, called after variable substitution
   * @param s The new oldName
   */
  protected def setName(s: String) {
    name = s
  }

  /**
   * The complexity of the term is the sum of those of the components plus 1
   */
  private def calcComplexity() {
    complexity = 1
    for (t <- components) {
      complexity = ( complexity + t.getComplexity ) . asInstanceOf[Short]
    }
  }

  /**
   * default method to make the oldName of the current term from existing fields
   * @return the oldName of the term
   */
//  protected jmv 
  def makeName(): String = {
    makeCompoundName(operator(), components)
  }

  /**
   * report the term's syntactic complexity
   * @return the complexity value
   */
  override def getComplexity(): Int = complexity

  /**
   * check if the term contains free variable
   * @return if the term is a constant
   */
  override def isConstant(): Boolean = isConstant_

  /**
   * Set the constant status
   * @param isConstant
   */
  def setConstant(isConstant: Boolean) {
    this.isConstant_ = isConstant
  }

  /**
   * Check if the order of the components matters
   * <p>
   * commutative CompoundTerms: Sets, Intersections
   * commutative Statements: Similarity, Equivalence (except the one with a temporal order)
   * commutative CompoundStatements: Disjunction, Conjunction (except the one with a temporal order)
   * @return The default value is false
   */
  def isCommutative(): Boolean = false

  /**
   * get the number of components
   * @return the size of the component list
   */
  def size(): Int = components.size

  /**
   * get a component by index
   * @param i index of the component
   * @return the component
   */
  def componentAt(i: Int): Term = components.get(i)

  /**
   * Get the component list
   * @return The component list
   */
  def getComponents(): ArrayList[Term] = components

  /**
   * Clone the component list
   * @return The cloned component list
   */
  def cloneComponents(): ArrayList[Term] = cloneList(components)

  /**
   * Check whether the compound contains a certain component
   * @param t The component to be checked
   * @return Whether the component is in the compound
   */
  def containComponent(t: Term): Boolean = components.contains(t)

  /**
   * Recursively check if a compound contains a term
   * @param target The term to be searched
   * @return Whether the terget is in the current term
   */
  override def containTerm(target: Term): Boolean = {
    components.exists(_.containTerm(target))
//    components.find(_.containTerm(target)).map(true).getOrElse(false)
  }

  /**
   * Check whether the compound contains all components of another term, or that term as a whole
   * @param t The other term
   * @return Whether the components are all in the compound
   */
  def containAllComponents(t: Term): Boolean = {
    if (getClass == t.getClass) {
      components.containsAll(t.asInstanceOf[CompoundTerm].getComponents)
    } else {
      components.contains(t)
    }
  }

  /**
   * Whether this compound term contains any variable term
   * @return Whether the name contains a variable
   */
  def containVar(): Boolean = Variable.containVar(name)

  /**
   * Rename the variables in the compound
   */
  override def renameVariables() {
    setName(makeName())	// jmv
    if (containVar()) {
      renameVariables(new HashMap[Variable, Variable]())
    }
    setConstant(true)
//    setName(makeName())	// jmv
  }

  /**
   * Rename the variables in the compound
   * @param map The substitution established so far
   */
  def renameVariables(map: HashMap[Variable, Variable]) {
    if (containVar()) {
      for (i <- 0 until components.size) {
        val term = componentAt(i)
        if (term.isInstanceOf[Variable]) {
          var `var` =
          if (term.getName.length == 1) {
            new Variable(term.getName.charAt(0) + "" + (map.size + 1))
          } else {
            var var1 = map.get(term.asInstanceOf[Variable]).asInstanceOf[Variable]
            if (var1 == null) {
              var1 = new Variable(term.getName.charAt(0) + "" + (map.size + 1))
            }
            var1
          }
          if (term != `var`) {
            components.set(i, `var`)
          }
          map.put(term.asInstanceOf[Variable], `var`)
        } else if (term.isInstanceOf[CompoundTerm]) {
          term.asInstanceOf[CompoundTerm].renameVariables(map)
          term.asInstanceOf[CompoundTerm].setName(term.asInstanceOf[CompoundTerm].makeName())
        }
      }
    }
  }

  /**
   * Recursively apply a substitute to the current CompoundTerm
   * @param subs
   */
  def applySubstitute(subs: HashMap[Term, Term]) {
    var t1: Term = null
    var t2: Term = null
    for (i <- 0 until size) {
      t1 = componentAt(i)
      t2 = subs.get(t1)
      if (t2 != null) {
        components.set(i, t2.clone().asInstanceOf[Term])
      } else if (t1.isInstanceOf[CompoundTerm]) {
        t1.asInstanceOf[CompoundTerm].applySubstitute(subs)
      }
    }
    if (this.isCommutative) {
      val s = new TreeSet[Term](components)
//      components = new ArrayList[Term](s)
      components.clear()
      components.addAll(s)	// TODO jmv TreeSet type is lost here
    }
    name = makeName()
  }

  /**
   * Build TermLink templates to constant components and subcomponents
   * <p>
   * The compound type determines the link type; the component type determines whether to build the link.
   * @return A list of TermLink templates
   */
  def prepareComponentLinks(): ArrayList[TermLink] = {
    val componentLinks = new ArrayList[TermLink]()
    val `type` = if ((this.isInstanceOf[Statement])) TermLink.COMPOUND_STATEMENT else TermLink.COMPOUND
    prepareComponentLinks(componentLinks, `type`.asInstanceOf[Short], this)
    componentLinks
  }

  /**
   * Collect TermLink templates into a list, go down one level except in special cases
   * <p>
   * @param componentLinks The list of TermLink templates built so far
   * @param type The type of TermLink to be built
   * @param term The CompoundTerm for which the links are built
   */
  private def prepareComponentLinks(componentLinks: ArrayList[TermLink], `type`: Short, term: CompoundTerm) {
    var t1: Term = null
    var t2: Term = null
    var t3: Term = null
    for (i <- 0 until term.size) {
      t1 = term.componentAt(i)
      if (t1.isConstant) {
        componentLinks.add(new TermLink(t1, `type`, i))
      }
      if ((t1.isInstanceOf[Conjunction]) && 
        ((this.isInstanceOf[Equivalence]) || ((this.isInstanceOf[Implication]) && (i == 0)))) {
        t1.asInstanceOf[CompoundTerm].prepareComponentLinks(componentLinks, TermLink.COMPOUND_CONDITION.asInstanceOf[Short], 
          t1.asInstanceOf[CompoundTerm])
      } else if (t1.isInstanceOf[CompoundTerm]) {
        for (j <- 0 until t1.asInstanceOf[CompoundTerm].size) {
          t2 = t1.asInstanceOf[CompoundTerm].componentAt(j)
          if (t2.isConstant) {
            if ((t1.isInstanceOf[Product]) || (t1.isInstanceOf[ImageExt]) || 
              (t1.isInstanceOf[ImageInt])) {
              if (`type` == TermLink.COMPOUND_CONDITION) {
                componentLinks.add(new TermLink(t2, TermLink.TRANSFORM.asInstanceOf[Short], 0, i, j))
              } else {
                componentLinks.add(new TermLink(t2, TermLink.TRANSFORM.asInstanceOf[Short], i, j))
              }
            } else {
              componentLinks.add(new TermLink(t2, `type`, i, j))
            }
          }
          if ((t2.isInstanceOf[Product]) || (t2.isInstanceOf[ImageExt]) || 
            (t2.isInstanceOf[ImageInt])) {
            for (k <- 0 until t2.asInstanceOf[CompoundTerm].size) {
              t3 = t2.asInstanceOf[CompoundTerm].componentAt(k)
              if (t3.isConstant) {
                if (`type` == TermLink.COMPOUND_CONDITION) {
                  componentLinks.add(new TermLink(t3, TermLink.TRANSFORM.asInstanceOf[Short], 0, i, j, k))
                } else {
                  componentLinks.add(new TermLink(t3, TermLink.TRANSFORM.asInstanceOf[Short], i, j, k))
                }
              }
            }
          }
        }
      }
    }
  }
}
