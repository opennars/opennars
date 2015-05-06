package nars.logic.entity

import nars.io.Symbols
import nars.logic.entity.Term
import TermLink._
import scala.reflect.{BeanProperty, BooleanBeanProperty}
//remove if not needed
import scala.collection.JavaConversions._

object TermLink {

  /**
   At C, point to C; TaskLink only
   */
  val SELF = 0

  /**
   At (&&, A, C), point to C
   */
  val COMPONENT = 1

  /**
   At C, point to (&&, A, C)
   */
  val COMPOUND = 2

  /**
   At <C --> A>, point to C
   */
  val COMPONENT_STATEMENT = 3

  /**
   At C, point to <C --> A>
   */
  val COMPOUND_STATEMENT = 4

  /**
   At <(&&, C, B) ==> A>, point to C
   */
  val COMPONENT_CONDITION = 5

  /**
   At C, point to <(&&, C, B) ==> A>
   */
  val COMPOUND_CONDITION = 6

  /**
   At C, point to <(*, C, B) --> A>; TaskLink only
   */
  val TRANSFORM = 8
}

/**
 * A link between a compound term and a component term
 * <p>
 * A TermLink links the current Term to a target Term, which is
 * either a component of, or compound made from, the current term.
 * <p>
 * Neither of the two terms contain variable shared with other terms.
 * <p>
 * The index value(s) indicates the location of the component in the compound.
 * <p>
 * This class is mainly used in inference.RuleTable to dispatch premises to inference rules
 */
class TermLink(@BeanProperty var target: Term, protected var `type`: Short, indices: Int*)
    extends Item {

  /**
   The index of the component in the component list of the compound, may have up to 4 levels
   */
  protected var index: Array[Short] = _

  assert (`type` % 2 == 0)

  if (`type` == TermLink.COMPOUND_CONDITION) {
    index = new Array[Short](indices.length + 1)
    index(0) = 0
    for (i <- 0 until indices.length) {
      index(i + 1) = indices(i).toShort
    }
  } else {
    index = new Array[Short](indices.length)
    for (i <- 0 until index.length) {
      index(i) = indices(i).toShort
    }
  }

  /**
   called from TaskLink
   * @param s The key of the TaskLink
   * @param v The budget value of the TaskLink
   */
  protected def this(s: String, v: BudgetValue) {
//    super(s, v)
    // jmv : have to write this, as this is what the Java code really does :(
    // cf http://stackoverflow.com/questions/1438762/how-can-scala-receive-multiple-parameters-in-a-method-definition
    this( null.asInstanceOf[Term] , 0.asInstanceOf[Short], List[Int]():_* )
    this.key = s;
    this.budget = new BudgetValue(v);  // clone, not assignment
  }

  /**
   * Constructor to make actual TermLink from a template
   * <p>
   * called in Concept.buildTermLinks only
   * @param t Target Term
   * @param template TermLink template previously prepared
   * @param v Budget value of the link
   */
  def this(t: Term, template: TermLink, v: BudgetValue) {
    this(t, template.getType)
//    super(t.getName, v)
//    target = t
//    `type` = template.getType
    this.budget = new BudgetValue(v);  // clone, not assignment
    if (template.getTarget == t) {
//      `type` -= 1
      `type` = ( `type` - 1 ).asInstanceOf[Short] 
    }
    index = template.getIndices
    setKey()
  }

  /**
   * Set the key of the link
   */
  protected def setKey() {
    var at1: String = ""
    var at2: String = ""
    if ((`type` % 2) == 1) {
      at1 = Symbols.TO_COMPONENT_1
      at2 = Symbols.TO_COMPONENT_2
    } else {
      at1 = Symbols.TO_COMPOUND_1
      at2 = Symbols.TO_COMPOUND_2
    }
    var in = "T" + `type`
    if (index != null) {
      for (i <- 0 until index.length) {
        in += "-" + (index(i) + 1)
      }
    }
    key = at1 + in + at2
    if (target != null) {
      key += target
    }
  }

  /**
   * Get the link type
   * @return Type of the link
   */
  def getType(): Short = `type`

  /**
   * Get all the indices
   * @return The index array
   */
  def getIndices(): Array[Short] = index

  /**
   * Get one index by level
   * @param i The index level
   * @return The index value
   */
  def getIndex(i: Int): Short = {
    if ((index != null) && (i < index.length)) {
      index(i)
    } else {
      -1
    }
  }
}
