package nars.logic

import nars.logic.entity._
import nars.logic.language._
import nars.storage.Memory
import nars.io.Symbols
//remove if not needed
import scala.collection.JavaConversions._

object RuleTables {

  /**
   * Entry point of the inference engine
   *
   * @param tLink The selected TaskLink, which will provide a task
   * @param bLink The selected TermLink, which may provide a belief
   * @param memory Reference to the memory
   */
  def reason(tLink: TaskLink, bLink: TermLink, memory: Memory) {
    val task = memory.currentTask
    val taskSentence = task.getSentence
    val taskTerm = taskSentence.getContent.clone().asInstanceOf[Term]
    val beliefTerm = bLink.getTarget.clone().asInstanceOf[Term]
    val beliefConcept = memory.termToConcept(beliefTerm)
    var belief: Sentence = null
    if (beliefConcept != null) {
      belief = beliefConcept.getBelief(task)
    }
    memory.currentBelief = belief
    if (belief != null) {
      LocalRules.`match`(task, belief, memory)
    }
    if (!memory.noResult()) {
      return
    }
    val tIndex = tLink.getIndex(0)
    var bIndex = bLink.getIndex(0)
    tLink.getType match {
      case TermLink.SELF => bLink.getType match {
        case TermLink.COMPONENT => compoundAndSelf(taskTerm.asInstanceOf[CompoundTerm], beliefTerm, true, 
          memory)
        case TermLink.COMPOUND => compoundAndSelf(beliefTerm.asInstanceOf[CompoundTerm], taskTerm, false, 
          memory)
        case TermLink.COMPONENT_STATEMENT => if (belief != null) {
          SyllogisticRules.detachment(task.getSentence, belief, bIndex, memory)
        }
        case TermLink.COMPOUND_STATEMENT => if (belief != null) {
          SyllogisticRules.detachment(belief, task.getSentence, bIndex, memory)
        }
        case TermLink.COMPONENT_CONDITION => if (belief != null) {
          bIndex = bLink.getIndex(1)
          SyllogisticRules.conditionalDedInd(taskTerm.asInstanceOf[Implication], bIndex, beliefTerm, 
            tIndex, memory)
        }
        case TermLink.COMPOUND_CONDITION => if (belief != null) {
          bIndex = bLink.getIndex(1)
          SyllogisticRules.conditionalDedInd(beliefTerm.asInstanceOf[Implication], bIndex, taskTerm, 
            tIndex, memory)
        }
      }
      case TermLink.COMPOUND => bLink.getType match {
        case TermLink.COMPOUND => compoundAndCompound(taskTerm.asInstanceOf[CompoundTerm], beliefTerm.asInstanceOf[CompoundTerm], 
          memory)
        case TermLink.COMPOUND_STATEMENT => compoundAndStatement(taskTerm.asInstanceOf[CompoundTerm], 
          tIndex, beliefTerm.asInstanceOf[Statement], bIndex, beliefTerm, memory)
        case TermLink.COMPOUND_CONDITION => if (belief != null) {
          if (beliefTerm.isInstanceOf[Implication]) {
            SyllogisticRules.conditionalDedInd(beliefTerm.asInstanceOf[Implication], bIndex, taskTerm, 
              -1, memory)
          } else if (beliefTerm.isInstanceOf[Equivalence]) {
            SyllogisticRules.conditionalAna(beliefTerm.asInstanceOf[Equivalence], bIndex, taskTerm, -1, 
              memory)
          }
        }
      }
      case TermLink.COMPOUND_STATEMENT => bLink.getType match {
        case TermLink.COMPONENT => componentAndStatement(memory.currentTerm.asInstanceOf[CompoundTerm], 
          bIndex, taskTerm.asInstanceOf[Statement], tIndex, memory)
        case TermLink.COMPOUND => compoundAndStatement(beliefTerm.asInstanceOf[CompoundTerm], bIndex, 
          taskTerm.asInstanceOf[Statement], tIndex, beliefTerm, memory)
        case TermLink.COMPOUND_STATEMENT => if (belief != null) {
          syllogisms(tLink, bLink, taskTerm, beliefTerm, memory)
        }
        case TermLink.COMPOUND_CONDITION => if (belief != null) {
          bIndex = bLink.getIndex(1)
          if (beliefTerm.isInstanceOf[Implication]) {
            conditionalDedIndWithVar(beliefTerm.asInstanceOf[Implication], bIndex, taskTerm.asInstanceOf[Statement], 
              tIndex, memory)
          }
        }
      }
      case TermLink.COMPOUND_CONDITION => bLink.getType match {
        case TermLink.COMPOUND_STATEMENT => if (belief != null) {
          conditionalDedIndWithVar(taskTerm.asInstanceOf[Implication], tIndex, beliefTerm.asInstanceOf[Statement], 
            bIndex, memory)
        }
      }
    }
  }

  /**
   * Meta-table of syllogistic rules, indexed by the content classes of the
   * taskSentence and the belief
   *
   * @param tLink The link to task
   * @param bLink The link to belief
   * @param taskTerm The content of task
   * @param beliefTerm The content of belief
   * @param memory Reference to the memory
   */
  private def syllogisms(tLink: TaskLink, 
      bLink: TermLink, 
      taskTerm: Term, 
      beliefTerm: Term, 
      memory: Memory) {
    val taskSentence = memory.currentTask.getSentence
    val belief = memory.currentBelief
    var figure: Int = 0
    if (taskTerm.isInstanceOf[Inheritance]) {
      if (beliefTerm.isInstanceOf[Inheritance]) {
        figure = indexToFigure(tLink, bLink)
        asymmetricAsymmetric(taskSentence, belief, figure, memory)
      } else if (beliefTerm.isInstanceOf[Similarity]) {
        figure = indexToFigure(tLink, bLink)
        asymmetricSymmetric(taskSentence, belief, figure, memory)
      } else {
        detachmentWithVar(belief, taskSentence, bLink.getIndex(0), memory)
      }
    } else if (taskTerm.isInstanceOf[Similarity]) {
      if (beliefTerm.isInstanceOf[Inheritance]) {
        figure = indexToFigure(bLink, tLink)
        asymmetricSymmetric(belief, taskSentence, figure, memory)
      } else if (beliefTerm.isInstanceOf[Similarity]) {
        figure = indexToFigure(bLink, tLink)
        symmetricSymmetric(belief, taskSentence, figure, memory)
      }
    } else if (taskTerm.isInstanceOf[Implication]) {
      if (beliefTerm.isInstanceOf[Implication]) {
        figure = indexToFigure(tLink, bLink)
        asymmetricAsymmetric(taskSentence, belief, figure, memory)
      } else if (beliefTerm.isInstanceOf[Equivalence]) {
        figure = indexToFigure(tLink, bLink)
        asymmetricSymmetric(taskSentence, belief, figure, memory)
      } else if (beliefTerm.isInstanceOf[Inheritance]) {
        detachmentWithVar(taskSentence, belief, tLink.getIndex(0), memory)
      }
    } else if (taskTerm.isInstanceOf[Equivalence]) {
      if (beliefTerm.isInstanceOf[Implication]) {
        figure = indexToFigure(bLink, tLink)
        asymmetricSymmetric(belief, taskSentence, figure, memory)
      } else if (beliefTerm.isInstanceOf[Equivalence]) {
        figure = indexToFigure(bLink, tLink)
        symmetricSymmetric(belief, taskSentence, figure, memory)
      } else if (beliefTerm.isInstanceOf[Inheritance]) {
        detachmentWithVar(taskSentence, belief, tLink.getIndex(0), memory)
      }
    }
  }

  /**
   * Decide the figure of syllogism according to the locations of the common
   * term in the premises
   *
   * @param link1 The link to the first premise
   * @param link2 The link to the second premise
   * @return The figure of the syllogism, one of the four: 11, 12, 21, or 22
   */
  private def indexToFigure(link1: TermLink, link2: TermLink): Int = {
    (link1.getIndex(0) + 1) * 10 + (link2.getIndex(0) + 1)
  }

  /**
   * Syllogistic rules whose both premises are on the same asymmetric relation
   *
   * @param sentence The taskSentence in the task
   * @param belief The judgment in the belief
   * @param figure The location of the shared term
   * @param memory Reference to the memory
   */
  private def asymmetricAsymmetric(sentence: Sentence, 
      belief: Sentence, 
      figure: Int, 
      memory: Memory) {
    val s1 = sentence.cloneContent().asInstanceOf[Statement]
    val s2 = belief.cloneContent().asInstanceOf[Statement]
    var t1: Term = null
    var t2: Term = null
    figure match {
      case 11 => if (Variable.unify(Symbols.VAR_INDEPENDENT, s1.getSubject, s2.getSubject, s1, s2)) {
        if (s1 == s2) {
          return
        }
        t1 = s2.getPredicate
        t2 = s1.getPredicate
        SyllogisticRules.abdIndCom(t1, t2, sentence, belief, figure, memory)
        CompositionalRules.composeCompound(s1, s2, 0, memory)
      }
      case 12 => if (Variable.unify(Symbols.VAR_INDEPENDENT, s1.getSubject, s2.getPredicate, s1, s2)) {
        if (s1 == s2) {
          return
        }
        t1 = s2.getSubject
        t2 = s1.getPredicate
        if (Variable.unify(Symbols.VAR_QUERY, t1, t2, s1, s2)) {
          LocalRules.matchReverse(memory)
        } else {
          SyllogisticRules.dedExe(t1, t2, sentence, belief, memory)
        }
      }
      case 21 => if (Variable.unify(Symbols.VAR_INDEPENDENT, s1.getPredicate, s2.getSubject, s1, s2)) {
        if (s1 == s2) {
          return
        }
        t1 = s1.getSubject
        t2 = s2.getPredicate
        if (Variable.unify(Symbols.VAR_QUERY, t1, t2, s1, s2)) {
          LocalRules.matchReverse(memory)
        } else {
          SyllogisticRules.dedExe(t1, t2, sentence, belief, memory)
        }
      }
      case 22 => if (Variable.unify(Symbols.VAR_INDEPENDENT, s1.getPredicate, s2.getPredicate, s1, s2)) {
        if (s1 == s2) {
          return
        }
        t1 = s1.getSubject
        t2 = s2.getSubject
        if (!SyllogisticRules.conditionalAbd(t1, t2, s1, s2, memory)) {
          SyllogisticRules.abdIndCom(t1, t2, sentence, belief, figure, memory)
          CompositionalRules.composeCompound(s1, s2, 1, memory)
        }
      }
      case _ => return
      }
  }

  /**
   * Syllogistic rules whose first premise is on an asymmetric relation, and
   * the second on a symmetric relation
   *
   * @param asym The asymmetric premise
   * @param sym The symmetric premise
   * @param figure The location of the shared term
   * @param memory Reference to the memory
   */
  private def asymmetricSymmetric(asym: Sentence, 
      sym: Sentence, 
      figure: Int, 
      memory: Memory) {
    val asymSt = asym.cloneContent().asInstanceOf[Statement]
    val symSt = sym.cloneContent().asInstanceOf[Statement]
    var t1: Term = null
    var t2: Term = null
    figure match {
      case 11 => if (Variable.unify(Symbols.VAR_INDEPENDENT, asymSt.getSubject, symSt.getSubject, asymSt, 
        symSt)) {
        t1 = asymSt.getPredicate
        t2 = symSt.getPredicate
        if (Variable.unify(Symbols.VAR_QUERY, t1, t2, asymSt, symSt)) {
          LocalRules.matchAsymSym(asym, sym, figure, memory)
        } else {
          SyllogisticRules.analogy(t2, t1, asym, sym, figure, memory)
        }
      }
      case 12 => if (Variable.unify(Symbols.VAR_INDEPENDENT, asymSt.getSubject, symSt.getPredicate, asymSt, 
        symSt)) {
        t1 = asymSt.getPredicate
        t2 = symSt.getSubject
        if (Variable.unify(Symbols.VAR_QUERY, t1, t2, asymSt, symSt)) {
          LocalRules.matchAsymSym(asym, sym, figure, memory)
        } else {
          SyllogisticRules.analogy(t2, t1, asym, sym, figure, memory)
        }
      }
      case 21 => if (Variable.unify(Symbols.VAR_INDEPENDENT, asymSt.getPredicate, symSt.getSubject, asymSt, 
        symSt)) {
        t1 = asymSt.getSubject
        t2 = symSt.getPredicate
        if (Variable.unify(Symbols.VAR_QUERY, t1, t2, asymSt, symSt)) {
          LocalRules.matchAsymSym(asym, sym, figure, memory)
        } else {
          SyllogisticRules.analogy(t1, t2, asym, sym, figure, memory)
        }
      }
      case 22 => if (Variable.unify(Symbols.VAR_INDEPENDENT, asymSt.getPredicate, symSt.getPredicate, 
        asymSt, symSt)) {
        t1 = asymSt.getSubject
        t2 = symSt.getSubject
        if (Variable.unify(Symbols.VAR_QUERY, t1, t2, asymSt, symSt)) {
          LocalRules.matchAsymSym(asym, sym, figure, memory)
        } else {
          SyllogisticRules.analogy(t1, t2, asym, sym, figure, memory)
        }
      }
    }
  }

  /**
   * Syllogistic rules whose both premises are on the same symmetric relation
   *
   * @param belief The premise that comes from a belief
   * @param taskSentence The premise that comes from a task
   * @param figure The location of the shared term
   * @param memory Reference to the memory
   */
  private def symmetricSymmetric(belief: Sentence, 
      taskSentence: Sentence, 
      figure: Int, 
      memory: Memory) {
    val s1 = belief.cloneContent().asInstanceOf[Statement]
    val s2 = taskSentence.cloneContent().asInstanceOf[Statement]
    figure match {
      case 11 => if (Variable.unify(Symbols.VAR_INDEPENDENT, s1.getSubject, s2.getSubject, s1, s2)) {
        SyllogisticRules.resemblance(s1.getPredicate, s2.getPredicate, belief, taskSentence, figure, 
          memory)
      }
      case 12 => if (Variable.unify(Symbols.VAR_INDEPENDENT, s1.getSubject, s2.getPredicate, s1, s2)) {
        SyllogisticRules.resemblance(s1.getPredicate, s2.getSubject, belief, taskSentence, figure, memory)
      }
      case 21 => if (Variable.unify(Symbols.VAR_INDEPENDENT, s1.getPredicate, s2.getSubject, s1, s2)) {
        SyllogisticRules.resemblance(s1.getSubject, s2.getPredicate, belief, taskSentence, figure, memory)
      }
      case 22 => if (Variable.unify(Symbols.VAR_INDEPENDENT, s1.getPredicate, s2.getPredicate, s1, s2)) {
        SyllogisticRules.resemblance(s1.getSubject, s2.getSubject, belief, taskSentence, figure, memory)
      }
    }
  }

  /**
   * The detachment rule, with variable unification
   *
   * @param originalMainSentence The premise that is an Implication or
   * Equivalence
   * @param subSentence The premise that is the subject or predicate of the
   * first one
   * @param index The location of the second premise in the first
   * @param memory Reference to the memory
   */
  private def detachmentWithVar(originalMainSentence: Sentence, 
      subSentence: Sentence, 
      index: Int, 
      memory: Memory) {
    val mainSentence = originalMainSentence.clone().asInstanceOf[Sentence]
    val statement = mainSentence.getContent.asInstanceOf[Statement]
    val component = statement.componentAt(index)
    val content = subSentence.getContent
    if ((component.isInstanceOf[Inheritance]) && (memory.currentBelief != null)) {
      if (component.isConstant) {
        SyllogisticRules.detachment(mainSentence, subSentence, index, memory)
      } else if (Variable.unify(Symbols.VAR_INDEPENDENT, component, content, statement, content)) {
        SyllogisticRules.detachment(mainSentence, subSentence, index, memory)
      } else if ((statement.isInstanceOf[Implication]) && (statement.getPredicate.isInstanceOf[Statement]) && 
        (memory.currentTask.getSentence.isJudgment)) {
        val s2 = statement.getPredicate.asInstanceOf[Statement]
        if (s2.getSubject == content.asInstanceOf[Statement].getSubject) {
          CompositionalRules.introVarInner(content.asInstanceOf[Statement], s2, statement, memory)
        }
      }
    }
  }

  /**
   * Conditional deduction or induction, with variable unification
   *
   * @param conditional The premise that is an Implication with a Conjunction
   * as condition
   * @param index The location of the shared term in the condition
   * @param statement The second premise that is a statement
   * @param side The location of the shared term in the statement
   * @param memory Reference to the memory
   */
  private def conditionalDedIndWithVar(conditional: Implication, 
      index: Short, 
      statement: Statement, 
      side: Short, 
      memory: Memory) {
    val condition = conditional.getSubject.asInstanceOf[CompoundTerm]
    val component = condition.componentAt(index)
    var component2: Term = null
    var side2 = side
    if (statement.isInstanceOf[Inheritance]) {
      component2 = statement
      side2 = -1
    } else if (statement.isInstanceOf[Implication]) {
      component2 = statement.componentAt(side)
    }
    if ((component2 != null) && 
      Variable.unify(Symbols.VAR_INDEPENDENT, component, component2, conditional, statement)) {
      SyllogisticRules.conditionalDedInd(conditional, index, statement, side2, memory)
    }
  }

  /**
   * Inference between a compound term and a component of it
   *
   * @param compound The compound term
   * @param component The component term
   * @param compoundTask Whether the compound comes from the task
   * @param memory Reference to the memory
   */
  private def compoundAndSelf(compound: CompoundTerm, 
      component: Term, 
      compoundTask: Boolean, 
      memory: Memory) {
    if ((compound.isInstanceOf[Conjunction]) || (compound.isInstanceOf[Disjunction])) {
      if (memory.currentBelief != null) {
        CompositionalRules.decomposeStatement(compound, component, compoundTask, memory)
      } else if (compound.containComponent(component)) {
        StructuralRules.structuralCompound(compound, component, compoundTask, memory)
      }
    } else if ((compound.isInstanceOf[Negation]) && !memory.currentTask.isStructural) {
      if (compoundTask) {
        StructuralRules.transformNegation(compound.asInstanceOf[Negation].componentAt(0), memory)
      } else {
        StructuralRules.transformNegation(compound, memory)
      }
    }
  }

  /**
   * Inference between two compound terms
   *
   * @param taskTerm The compound from the task
   * @param beliefTerm The compound from the belief
   * @param memory Reference to the memory
   */
  private def compoundAndCompound(taskTerm: CompoundTerm, beliefTerm: CompoundTerm, memory: Memory) {
    if (taskTerm.getClass == beliefTerm.getClass) {
      if (taskTerm.size > beliefTerm.size) {
        compoundAndSelf(taskTerm, beliefTerm, true, memory)
      } else if (taskTerm.size < beliefTerm.size) {
        compoundAndSelf(beliefTerm, taskTerm, false, memory)
      }
    }
  }

  /**
   * Inference between a compound term and a statement
   *
   * @param compound The compound term
   * @param index The location of the current term in the compound
   * @param statement The statement
   * @param side The location of the current term in the statement
   * @param beliefTerm The content of the belief
   * @param memory Reference to the memory
   */
  private def compoundAndStatement(compound: CompoundTerm, 
      index: Short, 
      statement: Statement, 
      side: Short, 
      beliefTerm: Term, 
      memory: Memory) {
    val component = compound.componentAt(index)
    val task = memory.currentTask
    if (component.getClass == statement.getClass) {
      if ((compound.isInstanceOf[Conjunction]) && (memory.currentBelief != null)) {
        if (Variable.unify(Symbols.VAR_DEPENDENT, component, statement, compound, statement)) {
          SyllogisticRules.elimiVarDep(compound, component, statement == beliefTerm, memory)
        } else if (task.getSentence.isJudgment) {
          CompositionalRules.introVarInner(statement, component.asInstanceOf[Statement], compound, memory)
        }
      }
    } else {
      if (!task.isStructural && task.getSentence.isJudgment) {
        if (statement.isInstanceOf[Inheritance]) {
          StructuralRules.structuralCompose1(compound, index, statement, memory)
          if (!(compound.isInstanceOf[SetExt] || compound.isInstanceOf[SetInt] || 
            compound.isInstanceOf[Negation])) {
            StructuralRules.structuralCompose2(compound, index, statement, side, memory)
          }
        } else if ((statement.isInstanceOf[Similarity]) && !(compound.isInstanceOf[Conjunction])) {
          StructuralRules.structuralCompose2(compound, index, statement, side, memory)
        }
      }
    }
  }

  /**
   * Inference between a component term (of the current term) and a statement
   *
   * @param compound The compound term
   * @param index The location of the current term in the compound
   * @param statement The statement
   * @param side The location of the current term in the statement
   * @param memory Reference to the memory
   */
  private def componentAndStatement(compound: CompoundTerm, 
      index: Short, 
      statement: Statement, 
      side: Short, 
      memory: Memory) {
    if (!memory.currentTask.isStructural) {
      if (statement.isInstanceOf[Inheritance]) {
        StructuralRules.structuralDecompose1(compound, index, statement, memory)
        if (!(compound.isInstanceOf[SetExt]) && !(compound.isInstanceOf[SetInt])) {
          StructuralRules.structuralDecompose2(statement, memory)
        } else {
          StructuralRules.transformSetRelation(compound, statement, side, memory)
        }
      } else if (statement.isInstanceOf[Similarity]) {
        StructuralRules.structuralDecompose2(statement, memory)
        if ((compound.isInstanceOf[SetExt]) || (compound.isInstanceOf[SetInt])) {
          StructuralRules.transformSetRelation(compound, statement, side, memory)
        }
      } else if ((statement.isInstanceOf[Implication]) && (compound.isInstanceOf[Negation])) {
        StructuralRules.contraposition(statement, memory)
      }
    }
  }

  /**
   * The TaskLink is of type TRANSFORM, and the conclusion is an equivalent
   * transformation
   *
   * @param tLink The task link
   * @param memory Reference to the memory
   */
  def transformTask(tLink: TaskLink, memory: Memory) {
    val content = memory.currentTask.getContent.clone().asInstanceOf[CompoundTerm]
    val indices = tLink.getIndices
    var inh: Term = null
    if ((indices.length == 2) || (content.isInstanceOf[Inheritance])) {
      inh = content
    } else if (indices.length == 3) {
      inh = content.componentAt(indices(0))
    } else if (indices.length == 4) {
      val component = content.componentAt(indices(0))
      if ((component.isInstanceOf[Conjunction]) && 
        (((content.isInstanceOf[Implication]) && (indices(0) == 0)) || 
        (content.isInstanceOf[Equivalence]))) {
        inh = component.asInstanceOf[CompoundTerm].componentAt(indices(1))
      } else {
        return
      }
    }
    if (inh.isInstanceOf[Inheritance]) {
      StructuralRules.transformProductImage(inh.asInstanceOf[Inheritance], content, indices, memory)
    }
  }
}
