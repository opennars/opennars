package nars.logic

import nars.storage.Memory
import nars.logic.entity._
import nars.logic.language._
import nars.io.Symbols
//remove if not needed
import scala.collection.JavaConversions._

object LocalRules {

  /**
   * The task and belief have the same content
   * <p>
   * called in RuleTables.reason
   * @param task The task
   * @param belief The belief
   * @param memory Reference to the memory
   */
  def `match`(task: Task, belief: Sentence, memory: Memory) {
    val sentence = task.getSentence.clone().asInstanceOf[Sentence]
    if (sentence.isJudgment) {
      if (revisible(sentence, belief)) {
        revision(sentence, belief, true, memory)
      }
    } else if (Variable.unify(Symbols.VAR_QUERY, sentence.getContent, belief.getContent.clone().asInstanceOf[Term])) {
      trySolution(sentence, belief, task, memory)
    }
  }

  /**
   * Check whether two sentences can be used in revision
   * @param s1 The first sentence
   * @param s2 The second sentence
   * @return If revision is possible between the two sentences
   */
  def revisible(s1: Sentence, s2: Sentence): Boolean = {
    (s1.getContent == s2.getContent && s1.getRevisible)
  }

  /**
   * Belief revision
   * <p>
   * called from Concept.reviseTable and match
   * @param newBelief The new belief in task
   * @param oldBelief The previous belief with the same content
   * @param feedbackToLinks Whether to send feedback to the links
   * @param memory Reference to the memory
   */
  def revision(newBelief: Sentence, 
      oldBelief: Sentence, 
      feedbackToLinks: Boolean, 
      memory: Memory) {
    val newTruth = newBelief.getTruth
    val oldTruth = oldBelief.getTruth
    val truth = TruthFunctions.revision(newTruth, oldTruth)
    val budget = BudgetFunctions.revise(newTruth, oldTruth, truth, feedbackToLinks, memory)
    val content = newBelief.getContent
    memory.doublePremiseTask(content, truth, budget)
  }

  /**
   * Check if a Sentence provide a better answer to a Question or Goal
   * @param problem The Goal or Question to be answered
   * @param belief The proposed answer
   * @param task The task to be processed
   * @param memory Reference to the memory
   */
  def trySolution(problem: Sentence, 
      belief: Sentence, 
      task: Task, 
      memory: Memory) {
    val oldBest = task.getBestSolution
    val newQ = solutionQuality(problem, belief)
    if (oldBest != null) {
      val oldQ = solutionQuality(problem, oldBest)
      if (oldQ >= newQ) {
        return
      }
    }
    task.setBestSolution(belief)
    if (task.isInput) {
      memory.report(belief, false)
    }
    val budget = BudgetFunctions.solutionEval(problem, belief, task, memory)
    if ((budget != null) && budget.aboveThreshold()) {
      memory.activatedTask(budget, belief, task.getParentBelief)
    }
  }

  /**
   * Evaluate the quality of the judgment as a solution to a problem
   * @param problem A goal or question
   * @param solution The solution to be evaluated
   * @return The quality of the judgment as the solution
   */
  def solutionQuality(problem: Sentence, solution: Sentence): Float = {
    if (problem == null) {
      return solution.getTruth.getExpectation
    }
    val truth = solution.getTruth
    if (problem.getContent.isConstant) {
      truth.getConfidence
    } else {
      truth.getExpectation / solution.getContent.getComplexity
    }
  }

  /**
   * The task and belief match reversely
   * @param memory Reference to the memory
   */
  def matchReverse(memory: Memory) {
    val task = memory.currentTask
    val belief = memory.currentBelief
    val sentence = task.getSentence
    if (sentence.isJudgment) {
      inferToSym(sentence.asInstanceOf[Sentence], belief, memory)
    } else {
      conversion(memory)
    }
  }

  /**
   * Inheritance/Implication matches Similarity/Equivalence
   * @param asym A Inheritance/Implication sentence
   * @param sym A Similarity/Equivalence sentence
   * @param figure location of the shared term
   * @param memory Reference to the memory
   */
  def matchAsymSym(asym: Sentence, 
      sym: Sentence, 
      figure: Int, 
      memory: Memory) {
    if (memory.currentTask.getSentence.isJudgment) {
      inferToAsym(asym.asInstanceOf[Sentence], sym.asInstanceOf[Sentence], memory)
    } else {
      convertRelation(memory)
    }
  }

  /**
   * {<S --> P>, <P --> S} |- <S <-> p>
   * Produce Similarity/Equivalence from a pair of reversed Inheritance/Implication
   * @param judgment1 The first premise
   * @param judgment2 The second premise
   * @param memory Reference to the memory
   */
  private def inferToSym(judgment1: Sentence, judgment2: Sentence, memory: Memory) {
    val s1 = judgment1.getContent.asInstanceOf[Statement]
    val t1 = s1.getSubject
    val t2 = s1.getPredicate
    var content: Term = null
    content = if (s1.isInstanceOf[Inheritance]) Similarity.make(t1, t2, memory) else Equivalence.make(t1, 
      t2, memory)
    val value1 = judgment1.getTruth
    val value2 = judgment2.getTruth
    val truth = TruthFunctions.intersection(value1, value2)
    val budget = BudgetFunctions.forward(truth, memory)
    memory.doublePremiseTask(content, truth, budget)
  }

  /**
   * {<S <-> P>, <P --> S>} |- <S --> P>
   * Produce an Inheritance/Implication from a Similarity/Equivalence and a reversed Inheritance/Implication
   * @param asym The asymmetric premise
   * @param sym The symmetric premise
   * @param memory Reference to the memory
   */
  private def inferToAsym(asym: Sentence, sym: Sentence, memory: Memory) {
    val statement = asym.getContent.asInstanceOf[Statement]
    val sub = statement.getPredicate
    val pre = statement.getSubject
    val content = Statement.make(statement, sub, pre, memory)
    val truth = TruthFunctions.reduceConjunction(sym.getTruth, asym.getTruth)
    val budget = BudgetFunctions.forward(truth, memory)
    memory.doublePremiseTask(content, truth, budget)
  }

  /**
   * {<P --> S>} |- <S --> P>
   * Produce an Inheritance/Implication from a reversed Inheritance/Implication
   * @param memory Reference to the memory
   */
  private def conversion(memory: Memory) {
    val truth = TruthFunctions.conversion(memory.currentBelief.getTruth)
    val budget = BudgetFunctions.forward(truth, memory)
    convertedJudgment(truth, budget, memory)
  }

  /**
   * {<S --> P>} |- <S <-> P>
   * {<S <-> P>} |- <S --> P>
   * Switch between Inheritance/Implication and Similarity/Equivalence
   * @param memory Reference to the memory
   */
  private def convertRelation(memory: Memory) {
    var truth = memory.currentBelief.getTruth
    truth = if (memory.currentTask.getContent.asInstanceOf[Statement]
      .isCommutative) TruthFunctions.abduction(truth, 1.0f) else TruthFunctions.deduction(truth, 1.0f)
    val budget = BudgetFunctions.forward(truth, memory)
    convertedJudgment(truth, budget, memory)
  }

  /**
   * Convert jusgment into different relation
   * <p>
   * called in MatchingRules
   * @param budget The budget value of the new task
   * @param truth The truth value of the new task
   * @param memory Reference to the memory
   */
  private def convertedJudgment(newTruth: TruthValue, newBudget: BudgetValue, memory: Memory) {
    var content = memory.currentTask.getContent.asInstanceOf[Statement]
    val beliefContent = memory.currentBelief.getContent.asInstanceOf[Statement]
    val subjT = content.getSubject
    val predT = content.getPredicate
    val subjB = beliefContent.getSubject
    val predB = beliefContent.getPredicate
    var otherTerm: Term = null
    if (Variable.containVarQuery(subjT.getName)) {
      otherTerm = if ((predT == subjB)) predB else subjB
      content = Statement.make(content, otherTerm, predT, memory)
    }
    if (Variable.containVarQuery(predT.getName)) {
      otherTerm = if ((subjT == subjB)) predB else subjB
      content = Statement.make(content, subjT, otherTerm, memory)
    }
    memory.singlePremiseTask(content, Symbols.JUDGMENT, newTruth, newBudget)
  }
}
