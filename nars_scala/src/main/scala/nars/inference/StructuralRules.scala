package nars.logic

import nars.storage.Memory
import java.util.ArrayList
import nars.logic.entity._
import nars.logic.language._
import nars.main._
//remove if not needed
import scala.collection.JavaConversions._

object StructuralRules {

  private val RELIANCE = Parameters.RELIANCE

  /**
   * {<S --> P>, S@(S&T)} |- <(S&T) --> (P&T)>
   * {<S --> P>, S@(M-S)} |- <(M-P) --> (M-S)>
   * @param compound The compound term
   * @param index The location of the indicated term in the compound
   * @param statement The premise
   * @param side The location of the indicated term in the premise
   * @param memory Reference to the memory
   */
  def structuralCompose2(compound: CompoundTerm, 
      index: Short, 
      statement: Statement, 
      side: Short, 
      memory: Memory) {
    if (compound == statement.componentAt(side)) {
      return
    }
    var sub = statement.getSubject
    var pred = statement.getPredicate
    val components = compound.cloneComponents()
    if (((side == 0) && components.contains(pred)) || ((side == 1) && components.contains(sub))) {
      return
    }
    if (side == 0) {
      if (components.contains(sub)) {
        if (pred.isInstanceOf[CompoundTerm]) {
          return
        }
        sub = compound
        components.set(index, pred)
        pred = CompoundTerm.make(compound, components, memory)
      }
    } else {
      if (components.contains(pred)) {
        if (sub.isInstanceOf[CompoundTerm]) {
          return
        }
        components.set(index, sub)
        sub = CompoundTerm.make(compound, components, memory)
        pred = compound
      }
    }
    if ((sub == null) || (pred == null)) {
      return
    }
    var content: Term = null
    content = if (switchOrder(compound, index)) Statement.make(statement, pred, sub, memory) else Statement.make(statement, 
      sub, pred, memory)
    if (content == null) {
      return
    }
    val task = memory.currentTask
    val sentence = task.getSentence
    var truth = sentence.getTruth
    var budget: BudgetValue = null
    if (sentence.isQuestion) {
      budget = BudgetFunctions.compoundBackwardWeak(content, memory)
    } else {
      if (compound.size > 1) {
        if (sentence.isJudgment) {
          truth = TruthFunctions.deduction(truth, RELIANCE)
        } else {
          return
        }
      }
      budget = BudgetFunctions.compoundForward(truth, content, memory)
    }
    memory.singlePremiseTask(content, truth, budget)
  }

  /**
   * {<(S&T) --> (P&T)>, S@(S&T)} |- <S --> P>
   * @param statement The premise
   * @param memory Reference to the memory
   */
  def structuralDecompose2(statement: Statement, memory: Memory) {
    val subj = statement.getSubject
    val pred = statement.getPredicate
    if (subj.getClass != pred.getClass) {
      return
    }
    val sub = subj.asInstanceOf[CompoundTerm]
    val pre = pred.asInstanceOf[CompoundTerm]
    if (sub.size != pre.size) {
      return
    }
    var index = -1
    var t1: Term = null
    var t2: Term = null
    for (i <- 0 until sub.size) {
      t1 = sub.componentAt(i)
      t2 = pre.componentAt(i)
      if (t1 != t2) {
        if (index < 0) {
          index = i
        } else {
          return
        }
      }
    }
    t1 = sub.componentAt(index)
    t2 = pre.componentAt(index)
    var content: Term = null
    content = if (switchOrder(sub, index.toShort)) Statement.make(statement, t2, t1, memory) else Statement.make(statement, 
      t1, t2, memory)
    if (content == null) {
      return
    }
    val task = memory.currentTask
    val sentence = task.getSentence
    val truth = sentence.getTruth
    var budget: BudgetValue = null
    if (sentence.isQuestion) {
      budget = BudgetFunctions.compoundBackward(content, memory)
    } else {
      if ((sub.size > 1) && (sentence.isJudgment)) {
        return
      }
      budget = BudgetFunctions.compoundForward(truth, content, memory)
    }
    memory.singlePremiseTask(content, truth, budget)
  }

  /**
   * List the cases where the direction of inheritance is revised in conclusion
   * @param compound The compound term
   * @param index The location of focus in the compound
   * @return Whether the direction of inheritance should be revised
   */
  private def switchOrder(compound: CompoundTerm, index: Short): Boolean = {
    ((((compound.isInstanceOf[DifferenceExt]) || (compound.isInstanceOf[DifferenceInt])) && 
      (index == 1)) || 
      ((compound.isInstanceOf[ImageExt]) && 
      (index != compound.asInstanceOf[ImageExt].getRelationIndex)) || 
      ((compound.isInstanceOf[ImageInt]) && 
      (index != compound.asInstanceOf[ImageInt].getRelationIndex)))
  }

  /**
   * {<S --> P>, P@(P&Q)} |- <S --> (P&Q)>
   * @param compound The compound term
   * @param index The location of the indicated term in the compound
   * @param statement The premise
   * @param memory Reference to the memory
   */
  def structuralCompose1(compound: CompoundTerm, 
      index: Short, 
      statement: Statement, 
      memory: Memory) {
    if (!memory.currentTask.getSentence.isJudgment) {
      return
    }
    val component = compound.componentAt(index)
    val task = memory.currentTask
    val sentence = task.getSentence
    val truth = sentence.getTruth
    val truthDed = TruthFunctions.deduction(truth, RELIANCE)
    val truthNDed = TruthFunctions.negation(TruthFunctions.deduction(truth, RELIANCE))
    val subj = statement.getSubject
    val pred = statement.getPredicate
    if (component == subj) {
      if (compound.isInstanceOf[IntersectionExt]) {
        structuralStatement(compound, pred, truthDed, memory)
      } else if (compound.isInstanceOf[IntersectionInt]) {
        return
      } else if ((compound.isInstanceOf[DifferenceExt]) && (index == 0)) {
        structuralStatement(compound, pred, truthDed, memory)
      } else if (compound.isInstanceOf[DifferenceInt]) {
        if (index == 0) {
          return
        } else {
          structuralStatement(compound, pred, truthNDed, memory)
        }
      }
    } else if (component == pred) {
      if (compound.isInstanceOf[IntersectionExt]) {
        return
      } else if (compound.isInstanceOf[IntersectionInt]) {
        structuralStatement(subj, compound, truthDed, memory)
      } else if (compound.isInstanceOf[DifferenceExt]) {
        if (index == 0) {
          return
        } else {
          structuralStatement(subj, compound, truthNDed, memory)
        }
      } else if ((compound.isInstanceOf[DifferenceInt]) && (index == 0)) {
        structuralStatement(subj, compound, truthDed, memory)
      }
    }
  }

  /**
   * {<(S&T) --> P>, S@(S&T)} |- <S --> P>
   * @param compound The compound term
   * @param index The location of the indicated term in the compound
   * @param statement The premise
   * @param memory Reference to the memory
   */
  def structuralDecompose1(compound: CompoundTerm, 
      index: Short, 
      statement: Statement, 
      memory: Memory) {
    if (!memory.currentTask.getSentence.isJudgment) {
      return
    }
    val component = compound.componentAt(index)
    val task = memory.currentTask
    val sentence = task.getSentence
    val truth = sentence.getTruth
    val truthDed = TruthFunctions.deduction(truth, RELIANCE)
    val truthNDed = TruthFunctions.negation(TruthFunctions.deduction(truth, RELIANCE))
    val subj = statement.getSubject
    val pred = statement.getPredicate
    if (compound == subj) {
      if (compound.isInstanceOf[IntersectionExt]) {
        return
      } else if (compound.isInstanceOf[IntersectionInt]) {
        structuralStatement(component, pred, truthDed, memory)
      } else if ((compound.isInstanceOf[DifferenceExt]) && (index == 0)) {
        return
      } else if (compound.isInstanceOf[DifferenceInt]) {
        if (index == 0) {
          structuralStatement(component, pred, truthDed, memory)
        } else {
          structuralStatement(component, pred, truthNDed, memory)
        }
      }
    } else if (compound == pred) {
      if (compound.isInstanceOf[IntersectionExt]) {
        structuralStatement(subj, component, truthDed, memory)
      } else if (compound.isInstanceOf[IntersectionInt]) {
        return
      } else if (compound.isInstanceOf[DifferenceExt]) {
        if (index == 0) {
          structuralStatement(subj, component, truthDed, memory)
        } else {
          structuralStatement(subj, component, truthNDed, memory)
        }
      } else if ((compound.isInstanceOf[DifferenceInt]) && (index == 0)) {
        return
      }
    }
  }

  /**
   * Common final operations of the above two methods
   * @param subject The subject of the new task
   * @param predicate The predicate of the new task
   * @param truth The truth value of the new task
   * @param memory Reference to the memory
   */
  private def structuralStatement(subject: Term, 
      predicate: Term, 
      truth: TruthValue, 
      memory: Memory) {
    val task = memory.currentTask
    val oldContent = task.getContent
    if (oldContent.isInstanceOf[Statement]) {
      val content = Statement.make(oldContent.asInstanceOf[Statement], subject, predicate, memory)
      if (content != null) {
        val budget = BudgetFunctions.compoundForward(truth, content, memory)
        memory.singlePremiseTask(content, truth, budget)
      }
    }
  }

  /**
   * {<S --> {P}>} |- <S <-> {P}>
   * @param compound The set compound
   * @param statement The premise
   * @param side The location of the indicated term in the premise
   * @param memory Reference to the memory
   */
  def transformSetRelation(compound: CompoundTerm, 
      statement: Statement, 
      side: Short, 
      memory: Memory) {
    if (compound.size > 1) {
      return
    }
    if (statement.isInstanceOf[Inheritance]) {
      if (((compound.isInstanceOf[SetExt]) && (side == 0)) || ((compound.isInstanceOf[SetInt]) && (side == 1))) {
        return
      }
    }
    val sub = statement.getSubject
    val pre = statement.getPredicate
    var content: Term = null
    content = if (statement.isInstanceOf[Inheritance]) Similarity.make(sub, pre, memory) else if (((compound.isInstanceOf[SetExt]) && (side == 0)) || ((compound.isInstanceOf[SetInt]) && (side == 1))) Inheritance.make(pre, 
      sub, memory) else Inheritance.make(sub, pre, memory)
    val task = memory.currentTask
    val sentence = task.getSentence
    val truth = sentence.getTruth
    var budget: BudgetValue = null
    budget = if (sentence.isQuestion) BudgetFunctions.compoundBackward(content, memory) else BudgetFunctions.compoundForward(truth, 
      content, memory)
    memory.singlePremiseTask(content, truth, budget)
  }

  /**
   * Equivalent transformation between products and images
   * {<(*, S, M) --> P>, S@(*, S, M)} |- <S --> (/, P, _, M)>
   * {<S --> (/, P, _, M)>, P@(/, P, _, M)} |- <(*, S, M) --> P>
   * {<S --> (/, P, _, M)>, M@(/, P, _, M)} |- <M --> (/, P, S, _)>
   * @param inh An Inheritance statement
   * @param oldContent The whole content
   * @param indices The indices of the TaskLink
   * @param task The task
   * @param memory Reference to the memory
   */
  def transformProductImage(inh: Inheritance, 
      oldContent: CompoundTerm, 
      indices: Array[Short], 
      memory: Memory) {
    var subject = inh.getSubject
    var predicate = inh.getPredicate
    if (inh == oldContent) {
      if (subject.isInstanceOf[CompoundTerm]) {
        transformSubjectPI(subject.asInstanceOf[CompoundTerm], predicate, memory)
      }
      if (predicate.isInstanceOf[CompoundTerm]) {
        transformPredicatePI(subject, predicate.asInstanceOf[CompoundTerm], memory)
      }
      return
    }
    val index = indices(indices.length - 1)
    val side = indices(indices.length - 2)
    val comp = inh.componentAt(side).asInstanceOf[CompoundTerm]
    if (comp.isInstanceOf[Product]) {
      if (side == 0) {
        subject = comp.componentAt(index)
        predicate = ImageExt.make(comp.asInstanceOf[Product], inh.getPredicate, index, memory)
      } else {
        subject = ImageInt.make(comp.asInstanceOf[Product], inh.getSubject, index, memory)
        predicate = comp.componentAt(index)
      }
    } else if ((comp.isInstanceOf[ImageExt]) && (side == 1)) {
      if (index == comp.asInstanceOf[ImageExt].getRelationIndex) {
        subject = Product.make(comp, inh.getSubject, index, memory)
        predicate = comp.componentAt(index)
      } else {
        subject = comp.componentAt(index)
        predicate = ImageExt.make(comp.asInstanceOf[ImageExt], inh.getSubject, index, memory)
      }
    } else if ((comp.isInstanceOf[ImageInt]) && (side == 0)) {
      if (index == comp.asInstanceOf[ImageInt].getRelationIndex) {
        subject = comp.componentAt(index)
        predicate = Product.make(comp, inh.getPredicate, index, memory)
      } else {
        subject = ImageInt.make(comp.asInstanceOf[ImageInt], inh.getPredicate, index, memory)
        predicate = comp.componentAt(index)
      }
    } else {
      return
    }
    val newInh = Inheritance.make(subject, predicate, memory)
    var content: Term = null
    if (indices.length == 2) {
      content = newInh
    } else if ((oldContent.isInstanceOf[Statement]) && (indices(0) == 1)) {
      content = Statement.make(oldContent.asInstanceOf[Statement], oldContent.componentAt(0), newInh, 
        memory)
    } else {
      var componentList: ArrayList[Term] = null
      val condition = oldContent.componentAt(0)
      if (((oldContent.isInstanceOf[Implication]) || (oldContent.isInstanceOf[Equivalence])) && 
        (condition.isInstanceOf[Conjunction])) {
        componentList = condition.asInstanceOf[CompoundTerm].cloneComponents()
        componentList.set(indices(1), newInh)
        val newCond = CompoundTerm.make(condition.asInstanceOf[CompoundTerm], componentList, memory)
        content = Statement.make(oldContent.asInstanceOf[Statement], newCond, oldContent.asInstanceOf[Statement].getPredicate, 
          memory)
      } else {
        componentList = oldContent.cloneComponents()
        componentList.set(indices(0), newInh)
        if (oldContent.isInstanceOf[Conjunction]) {
          content = CompoundTerm.make(oldContent, componentList, memory)
        } else if ((oldContent.isInstanceOf[Implication]) || (oldContent.isInstanceOf[Equivalence])) {
          content = Statement.make(oldContent.asInstanceOf[Statement], componentList.get(0), componentList.get(1), 
            memory)
        }
      }
    }
    if (content == null) {
      return
    }
    val sentence = memory.currentTask.getSentence
    val truth = sentence.getTruth
    var budget: BudgetValue = null
    budget = if (sentence.isQuestion) BudgetFunctions.compoundBackward(content, memory) else BudgetFunctions.compoundForward(truth, 
      content, memory)
    memory.singlePremiseTask(content, truth, budget)
  }

  /**
   * Equivalent transformation between products and images when the subject is a compound
   * {<(*, S, M) --> P>, S@(*, S, M)} |- <S --> (/, P, _, M)>
   * {<S --> (/, P, _, M)>, P@(/, P, _, M)} |- <(*, S, M) --> P>
   * {<S --> (/, P, _, M)>, M@(/, P, _, M)} |- <M --> (/, P, S, _)>
   * @param subject The subject term
   * @param predicate The predicate term
   * @param memory Reference to the memory
   */
  private def transformSubjectPI(subject: CompoundTerm, predicate: Term, memory: Memory) {
    val truth = memory.currentTask.getSentence.getTruth
    var budget: BudgetValue = null
    var inheritance: Inheritance = null
    var newSubj: Term = null
    var newPred: Term = null
    if (subject.isInstanceOf[Product]) {
      val product = subject.asInstanceOf[Product]
      for (i <- 0 until product.size) {
        newSubj = product.componentAt(i)
        newPred = ImageExt.make(product, predicate, i.asInstanceOf[Short], memory)
        inheritance = Inheritance.make(newSubj, newPred, memory)
        budget = if (truth == null) BudgetFunctions.compoundBackward(inheritance, memory) else BudgetFunctions.compoundForward(truth, 
          inheritance, memory)
        memory.singlePremiseTask(inheritance, truth, budget)
      }
    } else if (subject.isInstanceOf[ImageInt]) {
      val image = subject.asInstanceOf[ImageInt]
      val relationIndex = image.getRelationIndex
      for (i <- 0 until image.size) {
        if (i == relationIndex) {
          newSubj = image.componentAt(relationIndex)
          newPred = Product.make(image, predicate, relationIndex, memory)
        } else {
          newSubj = ImageInt.make(image.asInstanceOf[ImageInt], predicate, i.asInstanceOf[Short], memory)
          newPred = image.componentAt(i)
        }
        inheritance = Inheritance.make(newSubj, newPred, memory)
        budget = if (truth == null) BudgetFunctions.compoundBackward(inheritance, memory) else BudgetFunctions.compoundForward(truth, 
          inheritance, memory)
        memory.singlePremiseTask(inheritance, truth, budget)
      }
    }
  }

  /**
   * Equivalent transformation between products and images when the predicate is a compound
   * {<(*, S, M) --> P>, S@(*, S, M)} |- <S --> (/, P, _, M)>
   * {<S --> (/, P, _, M)>, P@(/, P, _, M)} |- <(*, S, M) --> P>
   * {<S --> (/, P, _, M)>, M@(/, P, _, M)} |- <M --> (/, P, S, _)>
   * @param subject The subject term
   * @param predicate The predicate term
   * @param memory Reference to the memory
   */
  private def transformPredicatePI(subject: Term, predicate: CompoundTerm, memory: Memory) {
    val truth = memory.currentTask.getSentence.getTruth
    var budget: BudgetValue = null
    var inheritance: Inheritance = null
    var newSubj: Term = null
    var newPred: Term = null
    if (predicate.isInstanceOf[Product]) {
      val product = predicate.asInstanceOf[Product]
      for (i <- 0 until product.size) {
        newSubj = ImageInt.make(product, subject, i.asInstanceOf[Short], memory)
        newPred = product.componentAt(i)
        inheritance = Inheritance.make(newSubj, newPred, memory)
        budget = if (truth == null) BudgetFunctions.compoundBackward(inheritance, memory) else BudgetFunctions.compoundForward(truth, 
          inheritance, memory)
        memory.singlePremiseTask(inheritance, truth, budget)
      }
    } else if (predicate.isInstanceOf[ImageExt]) {
      val image = predicate.asInstanceOf[ImageExt]
      val relationIndex = image.getRelationIndex
      for (i <- 0 until image.size) {
        if (i == relationIndex) {
          newSubj = Product.make(image, subject, relationIndex, memory)
          newPred = image.componentAt(relationIndex)
        } else {
          newSubj = image.componentAt(i)
          newPred = ImageExt.make(image.asInstanceOf[ImageExt], subject, i.asInstanceOf[Short], memory)
        }
        inheritance = Inheritance.make(newSubj, newPred, memory)
        budget = if (truth == null) BudgetFunctions.compoundBackward(inheritance, memory) else BudgetFunctions.compoundForward(truth, 
          inheritance, memory)
        memory.singlePremiseTask(inheritance, truth, budget)
      }
    }
  }

  /**
   * {(&&, A, B), A@(&&, A, B)} |- A
   * {(||, A, B), A@(||, A, B)} |- A
   * @param compound The premise
   * @param component The recognized component in the premise
   * @param compoundTask Whether the compound comes from the task
   * @param memory Reference to the memory
   */
  def structuralCompound(compound: CompoundTerm, 
      component: Term, 
      compoundTask: Boolean, 
      memory: Memory) {
    if (!component.isConstant) {
      return
    }
    val content = (if (compoundTask) component else compound)
    val task = memory.currentTask
    if (task.isStructural) {
      return
    }
    val sentence = task.getSentence
    var truth = sentence.getTruth
    var budget: BudgetValue = null
    if (sentence.isQuestion) {
      budget = BudgetFunctions.compoundBackward(content, memory)
    } else {
      if ((sentence.isJudgment) == 
        (compoundTask == (compound.isInstanceOf[Conjunction]))) {
        truth = TruthFunctions.deduction(truth, RELIANCE)
      } else {
        return
      }
      budget = BudgetFunctions.forward(truth, memory)
    }
    memory.singlePremiseTask(content, truth, budget)
  }

  /**
   * {A, A@(--, A)} |- (--, A)
   * @param content The premise
   * @param memory Reference to the memory
   */
  def transformNegation(content: Term, memory: Memory) {
    val task = memory.currentTask
    val sentence = task.getSentence
    var truth = sentence.getTruth
    if (sentence.isJudgment) {
      truth = TruthFunctions.negation(truth)
    }
    var budget: BudgetValue = null
    budget = if (sentence.isQuestion) BudgetFunctions.compoundBackward(content, memory) else BudgetFunctions.compoundForward(truth, 
      content, memory)
    memory.singlePremiseTask(content, truth, budget)
  }

  /**
   * {<A ==> B>, A@(--, A)} |- <(--, B) ==> (--, A)>
   * @param statement The premise
   * @param memory Reference to the memory
   */
  def contraposition(statement: Statement, memory: Memory) {
    val subj = statement.getSubject
    val pred = statement.getPredicate
    val task = memory.currentTask
    val sentence = task.getSentence
    val content = Statement.make(statement, Negation.make(pred, memory), Negation.make(subj, memory), 
      memory)
    var truth = sentence.getTruth
    var budget: BudgetValue = null
    if (sentence.isQuestion) {
      budget = if (content.isInstanceOf[Implication]) BudgetFunctions.compoundBackwardWeak(content, memory) else BudgetFunctions.compoundBackward(content, 
        memory)
    } else {
      if (content.isInstanceOf[Implication]) {
        truth = TruthFunctions.contraposition(truth)
      }
      budget = BudgetFunctions.compoundForward(truth, content, memory)
    }
    memory.singlePremiseTask(content, truth, budget)
  }
}
