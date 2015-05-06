package nars.logic

import nars.logic.entity._
import nars.logic.language._
import nars.io.Symbols
import nars.storage.Memory
//remove if not needed
import scala.collection.JavaConversions._

object SyllogisticRules {

  /**
   * {<S ==> M>, <M ==> P>} |- {<S ==> P>, <P ==> S>}
   * @param term1 Subject of the first new task
   * @param term2 Predicate of the first new task
   * @param sentence The first premise
   * @param belief The second premise
   * @param memory Reference to the memory
   */
  def dedExe(term1: Term, 
      term2: Term, 
      sentence: Sentence, 
      belief: Sentence, 
      memory: Memory) {
    if (Statement.invalidStatement(term1, term2)) {
      return
    }
    val value1 = sentence.getTruth
    val value2 = belief.getTruth
    var truth1: TruthValue = null
    var truth2: TruthValue = null
    var budget1: BudgetValue = null
    var budget2: BudgetValue = null
    if (sentence.isQuestion) {
      budget1 = BudgetFunctions.backwardWeak(value2, memory)
      budget2 = BudgetFunctions.backwardWeak(value2, memory)
    } else {
      truth1 = TruthFunctions.deduction(value1, value2)
      truth2 = TruthFunctions.exemplification(value1, value2)
      budget1 = BudgetFunctions.forward(truth1, memory)
      budget2 = BudgetFunctions.forward(truth2, memory)
    }
    val content = sentence.getContent.asInstanceOf[Statement]
    val content1 = Statement.make(content, term1, term2, memory)
    val content2 = Statement.make(content, term2, term1, memory)
    memory.doublePremiseTask(content1, truth1, budget1)
    memory.doublePremiseTask(content2, truth2, budget2)
  }

  /**
   * {<M ==> S>, <M ==> P>} |- {<S ==> P>, <P ==> S>, <S <=> P>}
   * @param term1 Subject of the first new task
   * @param term2 Predicate of the first new task
   * @param taskSentence The first premise
   * @param belief The second premise
   * @param figure Locations of the shared term in premises
   * @param memory Reference to the memory
   */
  def abdIndCom(term1: Term, 
      term2: Term, 
      taskSentence: Sentence, 
      belief: Sentence, 
      figure: Int, 
      memory: Memory) {
    if (Statement.invalidStatement(term1, term2)) {
      return
    }
    val taskContent = taskSentence.getContent.asInstanceOf[Statement]
    var truth1: TruthValue = null
    var truth2: TruthValue = null
    var truth3: TruthValue = null
    var budget1: BudgetValue = null
    var budget2: BudgetValue = null
    var budget3: BudgetValue = null
    val value1 = taskSentence.getTruth
    val value2 = belief.getTruth
    if (taskSentence.isQuestion) {
      budget1 = BudgetFunctions.backward(value2, memory)
      budget2 = BudgetFunctions.backwardWeak(value2, memory)
      budget3 = BudgetFunctions.backward(value2, memory)
    } else {
      truth1 = TruthFunctions.abduction(value1, value2)
      truth2 = TruthFunctions.abduction(value2, value1)
      truth3 = TruthFunctions.comparison(value1, value2)
      budget1 = BudgetFunctions.forward(truth1, memory)
      budget2 = BudgetFunctions.forward(truth2, memory)
      budget3 = BudgetFunctions.forward(truth3, memory)
    }
    val statement1 = Statement.make(taskContent, term1, term2, memory)
    val statement2 = Statement.make(taskContent, term2, term1, memory)
    val statement3 = Statement.makeSym(taskContent, term1, term2, memory)
    memory.doublePremiseTask(statement1, truth1, budget1)
    memory.doublePremiseTask(statement2, truth2, budget2)
    memory.doublePremiseTask(statement3, truth3, budget3)
  }

  /**
   * {<S ==> P>, <M <=> P>} |- <S ==> P>
   * @param term1 Subject of the new task
   * @param term2 Predicate of the new task
   * @param asym The asymmetric premise
   * @param sym The symmetric premise
   * @param figure Locations of the shared term in premises
   * @param memory Reference to the memory
   */
  def analogy(term1: Term, 
      term2: Term, 
      asym: Sentence, 
      sym: Sentence, 
      figure: Int, 
      memory: Memory) {
    if (Statement.invalidStatement(term1, term2)) {
      return
    }
    val asymSt = asym.getContent.asInstanceOf[Statement]
    var truth: TruthValue = null
    var budget: BudgetValue = null
    val sentence = memory.currentTask.getSentence
    val taskTerm = sentence.getContent.asInstanceOf[CompoundTerm]
    if (sentence.isQuestion) {
      budget = if (taskTerm.isCommutative) BudgetFunctions.backwardWeak(asym.getTruth, memory) else BudgetFunctions.backward(sym.getTruth, 
        memory)
    } else {
      truth = TruthFunctions.analogy(asym.getTruth, sym.getTruth)
      budget = BudgetFunctions.forward(truth, memory)
    }
    val content = Statement.make(asymSt, term1, term2, memory)
    memory.doublePremiseTask(content, truth, budget)
  }

  /**
   * {<S <=> M>, <M <=> P>} |- <S <=> P>
   * @param term1 Subject of the new task
   * @param term2 Predicate of the new task
   * @param belief The first premise
   * @param sentence The second premise
   * @param figure Locations of the shared term in premises
   * @param memory Reference to the memory
   */
  def resemblance(term1: Term, 
      term2: Term, 
      belief: Sentence, 
      sentence: Sentence, 
      figure: Int, 
      memory: Memory) {
    if (Statement.invalidStatement(term1, term2)) {
      return
    }
    val st1 = belief.getContent.asInstanceOf[Statement]
    var truth: TruthValue = null
    var budget: BudgetValue = null
    if (sentence.isQuestion) {
      budget = BudgetFunctions.backward(belief.getTruth, memory)
    } else {
      truth = TruthFunctions.resemblance(belief.getTruth, sentence.getTruth)
      budget = BudgetFunctions.forward(truth, memory)
    }
    val statement = Statement.make(st1, term1, term2, memory)
    memory.doublePremiseTask(statement, truth, budget)
  }

  /**
   * {<<M --> S> ==> <M --> P>>, <M --> S>} |- <M --> P>
   * {<<M --> S> ==> <M --> P>>, <M --> P>} |- <M --> S>
   * {<<M --> S> <=> <M --> P>>, <M --> S>} |- <M --> P>
   * {<<M --> S> <=> <M --> P>>, <M --> P>} |- <M --> S>
   * @param mainSentence The implication/equivalence premise
   * @param subSentence The premise on part of s1
   * @param side The location of s2 in s1
   * @param memory Reference to the memory
   */
  def detachment(mainSentence: Sentence, 
      subSentence: Sentence, 
      side: Int, 
      memory: Memory) {
    val statement = mainSentence.getContent.asInstanceOf[Statement]
    if (!(statement.isInstanceOf[Implication]) && !(statement.isInstanceOf[Equivalence])) {
      return
    }
    val subject = statement.getSubject
    val predicate = statement.getPredicate
    var content: Term = null
    if (side == 0) {
      content = predicate
    } else if (side == 1) {
      content = subject
    } else {
      return
    }
    if ((content.isInstanceOf[Statement]) && content.asInstanceOf[Statement].invalid()) {
      return
    }
    val taskSentence = memory.currentTask.getSentence
    val beliefSentence = memory.currentBelief
    val beliefTruth = beliefSentence.getTruth
    val truth1 = mainSentence.getTruth
    val truth2 = subSentence.getTruth
    var truth: TruthValue = null
    var budget: BudgetValue = new BudgetValue() // jmv _
    if (taskSentence.isQuestion) {
      budget = if (statement.isInstanceOf[Equivalence]) BudgetFunctions.backward(beliefTruth, memory) else if (side == 0) BudgetFunctions.backwardWeak(beliefTruth, 
        memory) else BudgetFunctions.backward(beliefTruth, memory)
    } else {
      truth = if (statement.isInstanceOf[Equivalence]) TruthFunctions.analogy(truth2, truth1) else if (side == 0) TruthFunctions.deduction(truth1, 
        truth2) else TruthFunctions.abduction(truth2, truth1)
      budget = BudgetFunctions.forward(truth, memory)
    }
    memory.doublePremiseTask(content, truth, budget)
  }

  /**
   * {<(&&, S1, S2, S3) ==> P>, S1} |- <(&&, S2, S3) ==> P>
   * {<(&&, S2, S3) ==> P>, <S1 ==> S2>} |- <(&&, S1, S3) ==> P>
   * {<(&&, S1, S3) ==> P>, <S1 ==> S2>} |- <(&&, S2, S3) ==> P>
   * @param premise1 The conditional premise
   * @param index The location of the shared term in the condition of premise1
   * @param premise2 The premise which, or part of which, appears in the condition of premise1
   * @param side The location of the shared term in premise2: 0 for subject, 1 for predicate, -1 for the whole term
   * @param memory Reference to the memory
   */
  def conditionalDedInd(premise1: Implication, 
      index: Short, 
      premise2: Term, 
      side: Int, 
      memory: Memory) {
    val task = memory.currentTask
    val taskSentence = task.getSentence
    val belief = memory.currentBelief
    val deduction = (side != 0)
    val conditionalTask = Variable.hasSubstitute(Symbols.VAR_INDEPENDENT, premise2, belief.getContent)
    var commonComponent: Term = null
    var newComponent: Term = null
    if (side == 0) {
      commonComponent = premise2.asInstanceOf[Statement].getSubject
      newComponent = premise2.asInstanceOf[Statement].getPredicate
    } else if (side == 1) {
      commonComponent = premise2.asInstanceOf[Statement].getPredicate
      newComponent = premise2.asInstanceOf[Statement].getSubject
    } else {
      commonComponent = premise2
    }
    val oldCondition = premise1.getSubject.asInstanceOf[Conjunction]
    val index2 = oldCondition.getComponents.indexOf(commonComponent)
    var newIndex = index
    if (index2 >= 0) {
      newIndex = index2.toShort
    } else {
      var `match` = Variable.unify(Symbols.VAR_INDEPENDENT, oldCondition.componentAt(index), commonComponent, 
        premise1, premise2)
      if (!`match` && 
        (commonComponent.getClass == oldCondition.getClass)) {
        `match` = Variable.unify(Symbols.VAR_INDEPENDENT, oldCondition.componentAt(index), commonComponent.asInstanceOf[CompoundTerm].componentAt(index), 
          premise1, premise2)
      }
      if (!`match`) {
        return
      }
    }
    var newCondition: Term = null
    newCondition = if (oldCondition == commonComponent) null else CompoundTerm.setComponent(oldCondition, 
      newIndex, newComponent, memory)
    var content: Term = null
    content = if (newCondition != null) Statement.make(premise1, newCondition, premise1.getPredicate, 
      memory) else premise1.getPredicate
    if (content == null) {
      return
    }
    val truth1 = taskSentence.getTruth
    val truth2 = belief.getTruth
    var truth: TruthValue = null
    var budget: BudgetValue = null
    if (taskSentence.isQuestion) {
      budget = BudgetFunctions.backwardWeak(truth2, memory)
    } else {
      truth = if (deduction) TruthFunctions.deduction(truth1, truth2) else if (conditionalTask) TruthFunctions.induction(truth2, 
        truth1) else TruthFunctions.induction(truth1, truth2)
      budget = BudgetFunctions.forward(truth, memory)
    }
    memory.doublePremiseTask(content, truth, budget)
  }

  /**
   * {<(&&, S1, S2) <=> P>, (&&, S1, S2)} |- P
   * @param premise1 The equivalence premise
   * @param index The location of the shared term in the condition of premise1
   * @param premise2 The premise which, or part of which, appears in the condition of premise1
   * @param side The location of the shared term in premise2: 0 for subject, 1 for predicate, -1 for the whole term
   * @param memory Reference to the memory
   */
  def conditionalAna(premise1: Equivalence, 
      index: Short, 
      premise2: Term, 
      side: Int, 
      memory: Memory) {
    val task = memory.currentTask
    val taskSentence = task.getSentence
    val belief = memory.currentBelief
    val conditionalTask = Variable.hasSubstitute(Symbols.VAR_INDEPENDENT, premise2, belief.getContent)
    var commonComponent: Term = null
    var newComponent: Term = null
    if (side == 0) {
      commonComponent = premise2.asInstanceOf[Statement].getSubject
      newComponent = premise2.asInstanceOf[Statement].getPredicate
    } else if (side == 1) {
      commonComponent = premise2.asInstanceOf[Statement].getPredicate
      newComponent = premise2.asInstanceOf[Statement].getSubject
    } else {
      commonComponent = premise2
    }
    val oldCondition = premise1.getSubject.asInstanceOf[Conjunction]
    var `match` = Variable.unify(Symbols.VAR_DEPENDENT, oldCondition.componentAt(index), commonComponent, 
      premise1, premise2)
    if (!`match` && 
      (commonComponent.getClass == oldCondition.getClass)) {
      `match` = Variable.unify(Symbols.VAR_DEPENDENT, oldCondition.componentAt(index), commonComponent.asInstanceOf[CompoundTerm].componentAt(index), 
        premise1, premise2)
    }
    if (!`match`) {
      return
    }
    var newCondition: Term = null
    newCondition = if (oldCondition == commonComponent) null else CompoundTerm.setComponent(oldCondition, 
      index, newComponent, memory)
    var content: Term = null
    content = if (newCondition != null) Statement.make(premise1, newCondition, premise1.getPredicate, 
      memory) else premise1.getPredicate
    if (content == null) {
      return
    }
    val truth1 = taskSentence.getTruth
    val truth2 = belief.getTruth
    var truth: TruthValue = null
    var budget: BudgetValue = null
    if (taskSentence.isQuestion) {
      budget = BudgetFunctions.backwardWeak(truth2, memory)
    } else {
      truth = if (conditionalTask) TruthFunctions.comparison(truth1, truth2) else TruthFunctions.analogy(truth1, 
        truth2)
      budget = BudgetFunctions.forward(truth, memory)
    }
    memory.doublePremiseTask(content, truth, budget)
  }

  /**
   * {<(&&, S2, S3) ==> P>, <(&&, S1, S3) ==> P>} |- <S1 ==> S2>
   * @param cond1 The condition of the first premise
   * @param cond2 The condition of the second premise
   * @param taskContent The first premise
   * @param st2 The second premise
   * @param memory Reference to the memory
   * @return Whether there are derived tasks
   */
  def conditionalAbd(cond1: Term, 
      cond2: Term, 
      st1: Statement, 
      st2: Statement, 
      memory: Memory): Boolean = {
    if (!(st1.isInstanceOf[Implication]) || !(st2.isInstanceOf[Implication])) {
      return false
    }
    if (!(cond1.isInstanceOf[Conjunction]) && !(cond2.isInstanceOf[Conjunction])) {
      return false
    }
    var term1: Term = null
    var term2: Term = null
    if (cond1.isInstanceOf[Conjunction]) {
      term1 = CompoundTerm.reduceComponents(cond1.asInstanceOf[Conjunction], cond2, memory)
    }
    if (cond2.isInstanceOf[Conjunction]) {
      term2 = CompoundTerm.reduceComponents(cond2.asInstanceOf[Conjunction], cond1, memory)
    }
    if ((term1 == null) && (term2 == null)) {
      return false
    }
    val task = memory.currentTask
    val sentence = task.getSentence
    val belief = memory.currentBelief
    val value1 = sentence.getTruth
    val value2 = belief.getTruth
    var content: Term = null
    var truth: TruthValue = null
    var budget: BudgetValue = null
    if (term1 != null) {
      content = if (term2 != null) Statement.make(st2, term2, term1, memory) else term1
      if (sentence.isQuestion) {
        budget = BudgetFunctions.backwardWeak(value2, memory)
      } else {
        truth = TruthFunctions.abduction(value2, value1)
        budget = BudgetFunctions.forward(truth, memory)
      }
      memory.doublePremiseTask(content, truth, budget)
    }
    if (term2 != null) {
      content = if (term1 != null) Statement.make(st1, term1, term2, memory) else term2
      if (sentence.isQuestion) {
        budget = BudgetFunctions.backwardWeak(value2, memory)
      } else {
        truth = TruthFunctions.abduction(value1, value2)
        budget = BudgetFunctions.forward(truth, memory)
      }
      memory.doublePremiseTask(content, truth, budget)
    }
    true
  }

  /**
   * {(&&, <#x() --> S>, <#x() --> P>>, <M --> P>} |- <M --> S>
   * @param compound The compound term to be decomposed
   * @param component The part of the compound to be removed
   * @param compoundTask Whether the compound comes from the task
   * @param memory Reference to the memory
   */
  def elimiVarDep(compound: CompoundTerm, 
      component: Term, 
      compoundTask: Boolean, 
      memory: Memory) {
    val content = CompoundTerm.reduceComponents(compound, component, memory)
    val task = memory.currentTask
    val sentence = task.getSentence
    val belief = memory.currentBelief
    val v1 = sentence.getTruth
    val v2 = belief.getTruth
    var truth: TruthValue = null
    var budget: BudgetValue = null
    if (sentence.isQuestion) {
      budget = (if (compoundTask) BudgetFunctions.backward(v2, memory) else BudgetFunctions.backwardWeak(v2, 
        memory))
    } else {
      truth = (if (compoundTask) TruthFunctions.anonymousAnalogy(v1, v2) else TruthFunctions.anonymousAnalogy(v2, 
        v1))
      budget = BudgetFunctions.compoundForward(truth, content, memory)
    }
    memory.doublePremiseTask(content, truth, budget)
  }
}
