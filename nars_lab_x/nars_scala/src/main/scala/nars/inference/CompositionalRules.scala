package nars.logic

import java.util._
import nars.logic.entity._
import nars.logic.language._
import nars.storage.Memory
//remove if not needed
import scala.collection.JavaConversions._

object CompositionalRules {

  /**
   * {<S ==> M>, <P ==> M>} |- {<(S|P) ==> M>, <(S&P) ==> M>, <(S-P) ==> M>, <(P-S) ==> M>}
   *
   * @param taskSentence The first premise
   * @param belief The second premise
   * @param index The location of the shared term
   * @param memory Reference to the memory
   */
  def composeCompound(taskContent: Statement, 
      beliefContent: Statement, 
      index: Int, 
      memory: Memory) {
    if ((!memory.currentTask.getSentence.isJudgment) || (taskContent.getClass != beliefContent.getClass)) {
      return
    }
    val componentT = taskContent.componentAt(1 - index)
    val componentB = beliefContent.componentAt(1 - index)
    val componentCommon = taskContent.componentAt(index)
    if ((componentT.isInstanceOf[CompoundTerm]) && 
      componentT.asInstanceOf[CompoundTerm].containAllComponents(componentB)) {
      decomposeCompound(componentT.asInstanceOf[CompoundTerm], componentB, componentCommon, index, true, 
        memory)
      return
    } else if ((componentB.isInstanceOf[CompoundTerm]) && 
      componentB.asInstanceOf[CompoundTerm].containAllComponents(componentT)) {
      decomposeCompound(componentB.asInstanceOf[CompoundTerm], componentT, componentCommon, index, false, 
        memory)
      return
    }
    val truthT = memory.currentTask.getSentence.getTruth
    val truthB = memory.currentBelief.getTruth
    val truthOr = TruthFunctions.union(truthT, truthB)
    var truthAnd = TruthFunctions.intersection(truthT, truthB)
    var termOr: Term = null
    var termAnd: Term = null
    if (index == 0) {
      if (taskContent.isInstanceOf[Inheritance]) {
        termOr = IntersectionInt.make(componentT, componentB, memory)
        if (truthB.isNegative) {
          if (!truthT.isNegative) {
            termAnd = DifferenceExt.make(componentT, componentB, memory)
            truthAnd = TruthFunctions.intersection(truthT, TruthFunctions.negation(truthB))
          }
        } else if (truthT.isNegative) {
          termAnd = DifferenceExt.make(componentB, componentT, memory)
          truthAnd = TruthFunctions.intersection(truthB, TruthFunctions.negation(truthT))
        } else {
          termAnd = IntersectionExt.make(componentT, componentB, memory)
        }
      } else if (taskContent.isInstanceOf[Implication]) {
        termOr = Disjunction.make(componentT, componentB, memory)
        termAnd = Conjunction.make(componentT, componentB, memory)
      }
      processComposed(taskContent, componentCommon, termOr, truthOr, memory)
      processComposed(taskContent, componentCommon, termAnd, truthAnd, memory)
    } else {
      if (taskContent.isInstanceOf[Inheritance]) {
        termOr = IntersectionExt.make(componentT, componentB, memory)
        if (truthB.isNegative) {
          if (!truthT.isNegative) {
            termAnd = DifferenceInt.make(componentT, componentB, memory)
            truthAnd = TruthFunctions.intersection(truthT, TruthFunctions.negation(truthB))
          }
        } else if (truthT.isNegative) {
          termAnd = DifferenceInt.make(componentB, componentT, memory)
          truthAnd = TruthFunctions.intersection(truthB, TruthFunctions.negation(truthT))
        } else {
          termAnd = IntersectionInt.make(componentT, componentB, memory)
        }
      } else if (taskContent.isInstanceOf[Implication]) {
        termOr = Conjunction.make(componentT, componentB, memory)
        termAnd = Disjunction.make(componentT, componentB, memory)
      }
      processComposed(taskContent, termOr, componentCommon, truthOr, memory)
      processComposed(taskContent, termAnd, componentCommon, truthAnd, memory)
    }
    if (taskContent.isInstanceOf[Inheritance]) {
      introVarOuter(taskContent, beliefContent, index, memory)
    }
  }

  /**
   * Finish composing implication term
   * @param premise1 Type of the contentInd
   * @param subject Subject of contentInd
   * @param predicate Predicate of contentInd
   * @param truth TruthValue of the contentInd
   * @param memory Reference to the memory
   */
  private def processComposed(statement: Statement, 
      subject: Term, 
      predicate: Term, 
      truth: TruthValue, 
      memory: Memory) {
    if ((subject == null) || (predicate == null)) {
      return
    }
    val content = Statement.make(statement, subject, predicate, memory)
    if ((content == null) || content == statement || content == memory.currentBelief.getContent) {
      return
    }
    val budget = BudgetFunctions.compoundForward(truth, content, memory)
    memory.doublePremiseTask(content, truth, budget)
  }

  /**
   * {<(S|P) ==> M>, <P ==> M>} |- <S ==> M>
   * @param implication The implication term to be decomposed
   * @param componentCommon The part of the implication to be removed
   * @param term1 The other term in the contentInd
   * @param index The location of the shared term: 0 for subject, 1 for predicate
   * @param compoundTask Whether the implication comes from the task
   * @param memory Reference to the memory
   */
  private def decomposeCompound(compound: CompoundTerm, 
      component: Term, 
      term1: Term, 
      index: Int, 
      compoundTask: Boolean, 
      memory: Memory) {
    if (compound.isInstanceOf[Statement]) {
      return
    }
    val term2 = CompoundTerm.reduceComponents(compound, component, memory)
    if (term2 == null) {
      return
    }
    val task = memory.currentTask
    val sentence = task.getSentence
    val belief = memory.currentBelief
    val oldContent = task.getContent.asInstanceOf[Statement]
    var v1: TruthValue = null
    var v2: TruthValue = null
    if (compoundTask) {
      v1 = sentence.getTruth
      v2 = belief.getTruth
    } else {
      v1 = belief.getTruth
      v2 = sentence.getTruth
    }
    var truth: TruthValue = null
    var content: Term = null
    if (index == 0) {
      content = Statement.make(oldContent, term1, term2, memory)
      if (content == null) {
        return
      }
      if (oldContent.isInstanceOf[Inheritance]) {
        if (compound.isInstanceOf[IntersectionExt]) {
          truth = TruthFunctions.reduceConjunction(v1, v2)
        } else if (compound.isInstanceOf[IntersectionInt]) {
          truth = TruthFunctions.reduceDisjunction(v1, v2)
        } else if ((compound.isInstanceOf[SetInt]) && (component.isInstanceOf[SetInt])) {
          truth = TruthFunctions.reduceConjunction(v1, v2)
        } else if ((compound.isInstanceOf[SetExt]) && (component.isInstanceOf[SetExt])) {
          truth = TruthFunctions.reduceDisjunction(v1, v2)
        } else if (compound.isInstanceOf[DifferenceExt]) {
          truth = if (compound.componentAt(0) == component) TruthFunctions.reduceDisjunction(v2, v1) else TruthFunctions.reduceConjunctionNeg(v1, 
            v2)
        }
      } else if (oldContent.isInstanceOf[Implication]) {
        if (compound.isInstanceOf[Conjunction]) {
          truth = TruthFunctions.reduceConjunction(v1, v2)
        } else if (compound.isInstanceOf[Disjunction]) {
          truth = TruthFunctions.reduceDisjunction(v1, v2)
        }
      }
    } else {
      content = Statement.make(oldContent, term2, term1, memory)
      if (content == null) {
        return
      }
      if (oldContent.isInstanceOf[Inheritance]) {
        if (compound.isInstanceOf[IntersectionInt]) {
          truth = TruthFunctions.reduceConjunction(v1, v2)
        } else if (compound.isInstanceOf[IntersectionExt]) {
          truth = TruthFunctions.reduceDisjunction(v1, v2)
        } else if ((compound.isInstanceOf[SetExt]) && (component.isInstanceOf[SetExt])) {
          truth = TruthFunctions.reduceConjunction(v1, v2)
        } else if ((compound.isInstanceOf[SetInt]) && (component.isInstanceOf[SetInt])) {
          truth = TruthFunctions.reduceDisjunction(v1, v2)
        } else if (compound.isInstanceOf[DifferenceInt]) {
          truth = if (compound.componentAt(1) == component) TruthFunctions.reduceDisjunction(v2, v1) else TruthFunctions.reduceConjunctionNeg(v1, 
            v2)
        }
      } else if (oldContent.isInstanceOf[Implication]) {
        if (compound.isInstanceOf[Disjunction]) {
          truth = TruthFunctions.reduceConjunction(v1, v2)
        } else if (compound.isInstanceOf[Conjunction]) {
          truth = TruthFunctions.reduceDisjunction(v1, v2)
        }
      }
    }
    if (truth != null) {
      val budget = BudgetFunctions.compoundForward(truth, content, memory)
      memory.doublePremiseTask(content, truth, budget)
    }
  }

  /**
   * {(||, S, P), P} |- S
   * {(&&, S, P), P} |- S
   * @param implication The implication term to be decomposed
   * @param componentCommon The part of the implication to be removed
   * @param compoundTask Whether the implication comes from the task
   * @param memory Reference to the memory
   */
  def decomposeStatement(compound: CompoundTerm, 
      component: Term, 
      compoundTask: Boolean, 
      memory: Memory) {
    val task = memory.currentTask
    val sentence = task.getSentence
    if (sentence.isQuestion) {
      return
    }
    val belief = memory.currentBelief
    val content = CompoundTerm.reduceComponents(compound, component, memory)
    if (content == null) {
      return
    }
    var v1: TruthValue = null
    var v2: TruthValue = null
    if (compoundTask) {
      v1 = sentence.getTruth
      v2 = belief.getTruth
    } else {
      v1 = belief.getTruth
      v2 = sentence.getTruth
    }
    var truth: TruthValue = null
    if (compound.isInstanceOf[Conjunction]) {
      if (sentence.isInstanceOf[Sentence]) {
        truth = TruthFunctions.reduceConjunction(v1, v2)
      }
    } else if (compound.isInstanceOf[Disjunction]) {
      if (sentence.isInstanceOf[Sentence]) {
        truth = TruthFunctions.reduceDisjunction(v1, v2)
      }
    } else {
      return
    }
    val budget = BudgetFunctions.compoundForward(truth, content, memory)
    memory.doublePremiseTask(content, truth, budget)
  }

  /**
   * Introduce a dependent variable in an outer-layer conjunction
   * @param taskContent The first premise <M --> S>
   * @param beliefContent The second premise <M --> P>
   * @param index The location of the shared term: 0 for subject, 1 for predicate
   * @param memory Reference to the memory
   */
  private def introVarOuter(taskContent: Statement, 
      beliefContent: Statement, 
      index: Int, 
      memory: Memory) {
    val truthT = memory.currentTask.getSentence.getTruth
    val truthB = memory.currentBelief.getTruth
    val varInd = new Variable("$varInd1")
    val varInd2 = new Variable("$varInd2")
    var term11: Term = null
    var term12: Term = null
    var term21: Term = null
    var term22: Term = null
    var commonTerm: Term = null
    val subs = new HashMap[Term, Term]()
    if (index == 0) {
      term11 = varInd
      term21 = varInd
      term12 = taskContent.getPredicate
      term22 = beliefContent.getPredicate
      if ((term12.isInstanceOf[ImageExt]) && (term22.isInstanceOf[ImageExt])) {
        commonTerm = term12.asInstanceOf[ImageExt].getTheOtherComponent
        if ((commonTerm == null) || 
          !term22.asInstanceOf[ImageExt].containTerm(commonTerm)) {
          commonTerm = term22.asInstanceOf[ImageExt].getTheOtherComponent
          if ((commonTerm == null) || 
            !term12.asInstanceOf[ImageExt].containTerm(commonTerm)) {
            commonTerm = null
          }
        }
        if (commonTerm != null) {
          subs.put(commonTerm, varInd2)
          term12.asInstanceOf[ImageExt].applySubstitute(subs)
          term22.asInstanceOf[ImageExt].applySubstitute(subs)
        }
      }
    } else {
      term11 = taskContent.getSubject
      term21 = beliefContent.getSubject
      term12 = varInd
      term22 = varInd
      if ((term11.isInstanceOf[ImageInt]) && (term21.isInstanceOf[ImageInt])) {
        commonTerm = term11.asInstanceOf[ImageInt].getTheOtherComponent
        if ((commonTerm == null) || 
          !term21.asInstanceOf[ImageInt].containTerm(commonTerm)) {
          commonTerm = term21.asInstanceOf[ImageInt].getTheOtherComponent
          if ((commonTerm == null) || 
            !term11.asInstanceOf[ImageExt].containTerm(commonTerm)) {
            commonTerm = null
          }
        }
        if (commonTerm != null) {
          subs.put(commonTerm, varInd2)
          term11.asInstanceOf[ImageInt].applySubstitute(subs)
          term21.asInstanceOf[ImageInt].applySubstitute(subs)
        }
      }
    }
    var state1 = Inheritance.make(term11, term12, memory)
    var state2 = Inheritance.make(term21, term22, memory)
    var content:Term = Implication.make(state1, state2, memory)
    var truth = TruthFunctions.induction(truthT, truthB)
    var budget = BudgetFunctions.compoundForward(truth, content, memory)
    memory.doublePremiseTask(content, truth, budget)
    content = Implication.make(state2, state1, memory)
    truth = TruthFunctions.induction(truthB, truthT)
    budget = BudgetFunctions.compoundForward(truth, content, memory)
    memory.doublePremiseTask(content, truth, budget)
    content = Equivalence.make(state1, state2, memory)
    truth = TruthFunctions.comparison(truthT, truthB)
    budget = BudgetFunctions.compoundForward(truth, content, memory)
    memory.doublePremiseTask(content, truth, budget)
    val varDep = new Variable("#varDep")
    if (index == 0) {
      state1 = Inheritance.make(varDep, taskContent.getPredicate, memory)
      state2 = Inheritance.make(varDep, beliefContent.getPredicate, memory)
    } else {
      state1 = Inheritance.make(taskContent.getSubject, varDep, memory)
      state2 = Inheritance.make(beliefContent.getSubject, varDep, memory)
    }
    content = Conjunction.make(state1, state2, memory)
    truth = TruthFunctions.intersection(truthT, truthB)
    budget = BudgetFunctions.compoundForward(truth, content, memory)
    memory.doublePremiseTask(content, truth, budget, false)
  }

  def introVarInner(premise1: Statement, 
      premise2: Statement, 
      oldCompound: CompoundTerm, 
      memory: Memory) {
    val task = memory.currentTask
    val taskSentence = task.getSentence
    if (!taskSentence.isJudgment || (premise1.getClass != premise2.getClass)) {
      return
    }
    val subject1 = premise1.getSubject
    val subject2 = premise2.getSubject
    val predicate1 = premise1.getPredicate
    val predicate2 = premise2.getPredicate
    var commonTerm1: Term = null
    var commonTerm2: Term = null
    if (subject1 == subject2) {
      commonTerm1 = subject1
      commonTerm2 = secondCommonTerm(predicate1, predicate2, 0)
    } else if (predicate1 == predicate2) {
      commonTerm1 = predicate1
      commonTerm2 = secondCommonTerm(subject1, subject2, 0)
    } else {
      return
    }
    val belief = memory.currentBelief
    val substitute = new HashMap[Term, Term]()
    substitute.put(commonTerm1, new Variable("#varDep2"))
    var content = Conjunction.make(premise1, oldCompound, memory).asInstanceOf[CompoundTerm]
    content.applySubstitute(substitute)
    var truth = TruthFunctions.intersection(taskSentence.getTruth, belief.getTruth)
    var budget = BudgetFunctions.forward(truth, memory)
    memory.doublePremiseTask(content, truth, budget, false)
    substitute.clear()
    substitute.put(commonTerm1, new Variable("$varInd1"))
    if (commonTerm2 != null) {
      substitute.put(commonTerm2, new Variable("$varInd2"))
    }
    content = Implication.make(premise1, oldCompound, memory)
    content.applySubstitute(substitute)
    truth = if (premise1 == taskSentence.getContent) TruthFunctions.induction(belief.getTruth, taskSentence.getTruth) else TruthFunctions.induction(taskSentence.getTruth, 
      belief.getTruth)
    budget = BudgetFunctions.forward(truth, memory)
    memory.doublePremiseTask(content, truth, budget)
  }

  /**
   * Introduce a second independent variable into two terms with a common component
   * @param term1 The first term
   * @param term2 The second term
   * @param index The index of the terms in their statement
   */
  private def secondCommonTerm(term1: Term, term2: Term, index: Int): Term = {
    var commonTerm: Term = null
    if (index == 0) {
      if ((term1.isInstanceOf[ImageExt]) && (term2.isInstanceOf[ImageExt])) {
        commonTerm = term1.asInstanceOf[ImageExt].getTheOtherComponent
        if ((commonTerm == null) || 
          !term2.asInstanceOf[ImageExt].containTerm(commonTerm)) {
          commonTerm = term2.asInstanceOf[ImageExt].getTheOtherComponent
          if ((commonTerm == null) || 
            !term1.asInstanceOf[ImageExt].containTerm(commonTerm)) {
            commonTerm = null
          }
        }
      }
    } else {
      if ((term1.isInstanceOf[ImageInt]) && (term2.isInstanceOf[ImageInt])) {
        commonTerm = term1.asInstanceOf[ImageInt].getTheOtherComponent
        if ((commonTerm == null) || 
          !term2.asInstanceOf[ImageInt].containTerm(commonTerm)) {
          commonTerm = term2.asInstanceOf[ImageInt].getTheOtherComponent
          if ((commonTerm == null) || 
            !term1.asInstanceOf[ImageExt].containTerm(commonTerm)) {
            commonTerm = null
          }
        }
      }
    }
    commonTerm
  }
}
