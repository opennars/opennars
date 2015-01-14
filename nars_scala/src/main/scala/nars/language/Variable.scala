package nars.logic.language

import java.util._
import nars.io.Symbols
import Variable._
//remove if not needed
import scala.collection.JavaConversions._

object Variable {

  /**
   * Check whether a string represent a name of a term that contains an independent variable
   * @param n The string name to be checked
   * @return Whether the name contains an independent variable
   */
  def containVarIndep(n: String): Boolean = n.indexOf(Symbols.VAR_INDEPENDENT) >= 0

  /**
   * Check whether a string represent a name of a term that contains a dependent variable
   * @param n The string name to be checked
   * @return Whether the name contains a dependent variable
   */
  def containVarDep(n: String): Boolean = n.indexOf(Symbols.VAR_DEPENDENT) >= 0

  /**
   * Check whether a string represent a name of a term that contains a query variable
   * @param n The string name to be checked
   * @return Whether the name contains a query variable
   */
  def containVarQuery(n: String): Boolean = n.indexOf(Symbols.VAR_QUERY) >= 0

  /**
   * Check whether a string represent a name of a term that contains a variable
   * @param n The string name to be checked
   * @return Whether the name contains a variable
   */
  def containVar(n: String): Boolean = {
    containVarIndep(n) || containVarDep(n) || containVarQuery(n)
  }

  /**
   * To unify two terms
   * @param type The type of variable that can be substituted
   * @param t1 The first term
   * @param t2 The second term
   * @return Whether the unification is possible
   */
  def unify(`type`: Char, t1: Term, t2: Term): Boolean = unify(`type`, t1, t2, t1, t2)

  /**
   * To unify two terms
   * @param type The type of variable that can be substituted
   * @param t1 The first term to be unified
   * @param t2 The second term to be unified
   * @param compound1 The compound contermaining the first term
   * @param compound2 The compound contermaining the second term
   * @return Whether the unification is possible
   */
  def unify(`type`: Char, 
      t1: Term, 
      t2: Term, 
      compound1: Term, 
      compound2: Term): Boolean = {
    val map1 = new HashMap[Term, Term]()
    val map2 = new HashMap[Term, Term]()
    val hasSubs = findSubstitute(`type`, t1, t2, map1, map2)
    if (hasSubs) {
      if (!map1.isEmpty) {
        compound1.asInstanceOf[CompoundTerm].applySubstitute(map1)
      }
      if (!map2.isEmpty) {
        compound2.asInstanceOf[CompoundTerm].applySubstitute(map2)
      }
    }
    hasSubs
  }

  /**
   * To recursively find a substitution that can unify two Terms without changing them
   * @param type The type of Variable to be substituted
   * @param term1 The first Term to be unified
   * @param term2 The second Term to be unified
   * @param subs The substitution formed so far
   * @return The substitution that unifies the two Terms
   */
  private def findSubstitute(`type`: Char, 
      term1: Term, 
      term2: Term, 
      map1: HashMap[Term, Term], 
      map2: HashMap[Term, Term]): Boolean = {
    var t: Term = null
    if ((term1.isInstanceOf[Variable]) && term1.asInstanceOf[Variable].getType == `type`) {
      t = map1.get(term1.asInstanceOf[Variable])
      if (t != null) {
        return t == term2
      } else {
        map1.put(term1.asInstanceOf[Variable], term2)
        return true
      }
    }
    if ((term2.isInstanceOf[Variable]) && term2.asInstanceOf[Variable].getType == `type`) {
      t = map2.get(term2.asInstanceOf[Variable])
      if (t != null) {
        return t == term1
      } else {
        map2.put(term2.asInstanceOf[Variable], term1)
        return true
      }
    }
    if ((term1.isInstanceOf[CompoundTerm]) && term1.getClass == term2.getClass) {
      val cTerm1 = term1.asInstanceOf[CompoundTerm]
      val cTerm2 = term2.asInstanceOf[CompoundTerm]
      if (cTerm1.size != (cTerm2).size) {
        return false
      }
      for (i <- 0 until cTerm1.size) {
        val t1 = cTerm1.componentAt(i)
        val t2 = cTerm2.componentAt(i)
        val haveSub = findSubstitute(`type`, t1, t2, map1, map2)
        if (!haveSub) {
          return false
        }
      }
      return true
    }
    if (!(term1.isInstanceOf[Variable]) && !(term2.isInstanceOf[Variable]) && 
      term1 == term2) {
      return true
    }
    false
  }

  /**
   * Check if two terms can be unified
   * @param type The type of variable that can be substituted
   * @param term1 The first term to be unified
   * @param term2 The second term to be unified
   * @return Whether there is a substitution
   */
  def hasSubstitute(`type`: Char, term1: Term, term2: Term): Boolean = {
    findSubstitute(`type`, term1, term2, new HashMap[Term, Term](), new HashMap[Term, Term]())
  }
}

/**
 * A variable term, which does not correspond to a concept
 */
class Variable(s: String) extends Term(s) {

  /**
   * Clone a Variable
   * @return The cloned Variable
   */
  override def clone(): AnyRef = new Variable(name)

  /**
   * Get the type of the variable
   * @return The variable type
   */
  def getType(): Char = name.charAt(0)

  /**
   * A variable is not constant
   * @return false
   */
  override def isConstant(): Boolean = false
}
