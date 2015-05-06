package nars.logic

import nars.logic.entity._
import nars.logic.language._
import nars.storage.Memory
//remove if not needed
import scala.collection.JavaConversions._
import UtilityFunctions._

object BudgetFunctions {

  /**
   * Determine the quality of a judgment by its truth value alone
   * <p>
   * Mainly decided by confidence, though binary judgment is also preferred
   * @param t The truth value of a judgment
   * @return The quality of the judgment, according to truth value only
   */
  def truthToQuality(t: TruthValue): Float = {
    val freq = t.getFrequency
    val conf = t.getConfidence
    aveGeo(conf, Math.abs(freq - 0.5f) + freq * 0.5f)
  }

  /**
   * Determine the rank of a judgment by its confidence and originality (stamp length)
   * @param judg The judgment to be ranked
   * @return The rank of the judgment, according to truth value only
   */
  def rankBelief(judg: Sentence): Float = {
    val confidence = judg.getTruth.getConfidence
    val originality = 1.0f / (judg.getStamp.length + 1)
    or(confidence, originality)
  }

  /**
   * Evaluate the quality of a belief as a solution to a problem, then reward
   * the belief and de-prioritize the problem
   * @param problem The problem (question or goal) to be solved
   * @param solution The belief as solution
   * @param task The task to be immediately processed, or null for continued process
   * @return The budget for the new task which is the belief activated, if necessary
   */
  def solutionEval(problem: Sentence, 
      solution: Sentence, 
      task: Task, 
      memory: Memory): BudgetValue = {
    var budget: BudgetValue = null
    var feedbackToLinks = false
    var task2 = task

    if (task == null) {
      task2 = memory.currentTask
      feedbackToLinks = true
    }
    val judgmentTask = task.getSentence.isJudgment
    val quality = LocalRules.solutionQuality(problem, solution)
    if (judgmentTask) {
      task2.incPriority(quality)
    } else {
      task2.setPriority(Math.min(1 - quality, task2.getPriority))
      budget = new BudgetValue(quality, task2.getDurability, truthToQuality(solution.getTruth))
    }
    if (feedbackToLinks) {
      val tLink = memory.currentTaskLink
      tLink.setPriority(Math.min(1 - quality, tLink.getPriority))
      val bLink = memory.currentBeliefLink
      bLink.incPriority(quality)
    }
    budget
  }

  /**
   * Evaluate the quality of a revision, then de-prioritize the premises
   * @param tTruth The truth value of the judgment in the task
   * @param bTruth The truth value of the belief
   * @param truth The truth value of the conclusion of revision
   * @return The budget for the new task
   */
  def revise(tTruth: TruthValue, 
      bTruth: TruthValue, 
      truth: TruthValue, 
      feedbackToLinks: Boolean, 
      memory: Memory): BudgetValue = {
    val difT = truth.getExpDifAbs(tTruth)
    val task = memory.currentTask
    task.decPriority(1 - difT)
    task.decDurability(1 - difT)
    if (feedbackToLinks) {
      val tLink = memory.currentTaskLink
      tLink.decPriority(1 - difT)
      tLink.decDurability(1 - difT)
      val bLink = memory.currentBeliefLink
      val difB = truth.getExpDifAbs(bTruth)
      bLink.decPriority(1 - difB)
      bLink.decDurability(1 - difB)
    }
    val dif = truth.getConfidence - 
      Math.max(tTruth.getConfidence, bTruth.getConfidence)
    val priority = or(dif, task.getPriority)
    val durability = or(dif, task.getDurability)
    val quality = truthToQuality(truth)
    new BudgetValue(priority, durability, quality)
  }

  /**
   * Update a belief
   * @param task The task containing new belief
   * @param bTruth Truth value of the previous belief
   * @return Budget value of the updating task
   */
  def update(task: Task, bTruth: TruthValue): BudgetValue = {
    val tTruth = task.getSentence.getTruth
    val dif = tTruth.getExpDifAbs(bTruth)
    val priority = or(dif, task.getPriority)
    val durability = or(dif, task.getDurability)
    val quality = truthToQuality(bTruth)
    new BudgetValue(priority, durability, quality)
  }

  /**
   * Distribute the budget of a task among the links to it
   * @param b The original budget
   * @param n Number of links
   * @return Budget value for each link
   */
  def distributeAmongLinks(b: BudgetValue, n: Int): BudgetValue = {
    val priority = (b.getPriority / Math.sqrt(n)).toFloat
    new BudgetValue(priority, b.getDurability, b.getQuality)
  }

  /**
   * Activate a concept by an incoming TaskLink
   * @param concept The concept
   * @param budget The budget for the new item
   */
  def activate(concept: Concept, budget: BudgetValue) {
    val oldPri = concept.getPriority
    val priority = or(oldPri, budget.getPriority)
    val durability = aveAri(concept.getDurability, budget.getDurability, oldPri / priority)
    val quality = concept.getQuality
    concept.setPriority(priority)
    concept.setDurability(durability)
    concept.setQuality(quality)
  }

  /**
   * Decrease Priority after an item is used, called in Bag
   * <p>
   * After a constant time, p should become d*p.  Since in this period, the item is accessed c*p times,
   * each time p-q should multiple d^(1/(c*p)).
   * The intuitive meaning of the parameter "forgetRate" is: after this number of times of access,
   * priority 1 will become d, it is a system parameter adjustable in run time.
   *
   * @param budget The previous budget value
   * @param forgetRate The budget for the new item
   * @param relativeThreshold The relative threshold of the bag
   */
  def forget(budget: BudgetValue, forgetRate: Float, relativeThreshold: Float) {
    var quality = budget.getQuality * relativeThreshold
    val p = budget.getPriority - quality
    if (p > 0) {
      quality += p * 
        Math.pow(budget.getDurability, 1.0 / (forgetRate * p)).asInstanceOf[Float]
    }
    budget.setPriority(quality.toFloat)
  }

  /**
   * Merge an item into another one in a bag, when the two are identical except in budget values
   * @param baseValue The budget value to be modified
   * @param adjustValue The budget doing the adjusting
   */
  def merge(baseValue: BudgetValue, adjustValue: BudgetValue) {
    baseValue.incPriority(adjustValue.getPriority)
    baseValue.setDurability(Math.max(baseValue.getDurability, adjustValue.getDurability))
    baseValue.setQuality(Math.max(baseValue.getQuality, adjustValue.getQuality))
  }

  /**
   * Forward inference result and adjustment
   * @param truth The truth value of the conclusion
   * @return The budget value of the conclusion
   */
  def forward(truth: TruthValue, memory: Memory): BudgetValue = {
    budgetInference(truthToQuality(truth), 1, memory)
  }

  /**
   * Backward inference result and adjustment, stronger case
   * @param truth The truth value of the belief deriving the conclusion
   * @param memory Reference to the memory
   * @return The budget value of the conclusion
   */
  def backward(truth: TruthValue, memory: Memory): BudgetValue = {
    budgetInference(truthToQuality(truth), 1, memory)
  }

  /**
   * Backward inference result and adjustment, weaker case
   * @param truth The truth value of the belief deriving the conclusion
   * @param memory Reference to the memory
   * @return The budget value of the conclusion
   */
  def backwardWeak(truth: TruthValue, memory: Memory): BudgetValue = {
    budgetInference(w2c(1) * truthToQuality(truth), 1, memory)
  }

  /**
   * Forward inference with CompoundTerm conclusion
   * @param truth The truth value of the conclusion
   * @param content The content of the conclusion
   * @param memory Reference to the memory
   * @return The budget of the conclusion
   */
  def compoundForward(truth: TruthValue, content: Term, memory: Memory): BudgetValue = {
    budgetInference(truthToQuality(truth), content.getComplexity, memory)
  }

  /**
   * Backward inference with CompoundTerm conclusion, stronger case
   * @param content The content of the conclusion
   * @param memory Reference to the memory
   * @return The budget of the conclusion
   */
  def compoundBackward(content: Term, memory: Memory): BudgetValue = {
    budgetInference(1, content.getComplexity, memory)
  }

  /**
   * Backward inference with CompoundTerm conclusion, weaker case
   * @param content The content of the conclusion
   * @param memory Reference to the memory
   * @return The budget of the conclusion
   */
  def compoundBackwardWeak(content: Term, memory: Memory): BudgetValue = {
    budgetInference(w2c(1), content.getComplexity, memory)
  }

  /**
   * Common processing for all inference step
   * @param qual Quality of the inference
   * @param complexity Syntactic complexity of the conclusion
   * @param memory Reference to the memory
   * @return Budget of the conclusion task
   */
  private def budgetInference(qual: Float, complexity: Int, memory: Memory): BudgetValue = {
    var t:Item = memory.currentTaskLink
    if (t == null) t = memory.currentTask
    var priority = t.getPriority
    var durability = t.getDurability
    val quality = (qual / Math.sqrt(complexity)).toFloat
    val bLink = memory.currentBeliefLink
    if (bLink != null) {
      priority = aveAri(priority, bLink.getPriority)
      durability = aveAri(durability, bLink.getDurability)
      bLink.incPriority(quality)
    }
    new BudgetValue(and(priority, quality), and(durability, quality), quality)
  }
}
