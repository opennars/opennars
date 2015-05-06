package nars.main

//remove if not needed
import scala.collection.JavaConversions._

object Parameters {

  /**
   Concept decay rate in ConceptBag, in [1, 99].
   */
  val CONCEPT_FORGETTING_CYCLE = 15

  /**
   TaskLink decay rate in TaskLinkBag, in [1, 99].
   */
  val TASK_LINK_FORGETTING_CYCLE = 20

  /**
   TermLink decay rate in TermLinkBag, in [1, 99].
   */
  val TERM_LINK_FORGETTING_CYCLE = 50

  /**
   Silent threshold for task reporting, in [0, 100].
   */
  val SILENT_LEVEL = 1

  /**
   Task decay rate in TaskBuffer, in [1, 99].
   */
  val NEW_TASK_FORGETTING_CYCLE = 1

  /**
   Maximum TermLinks checked for novelity for each TaskLink in TermLinkBag
   */
  val MAX_MATCHED_TERM_LINK = 10

  /**
   Maximum TermLinks used in reasoning for each Task in Concept
   */
  val MAX_REASONED_TERM_LINK = 3

  /**
   Evidential Horizon, the amount of future evidence to be considered.
   */
  val HORIZON = 1

  /**
   Reliance factor, the empirical confidence of analytical truth.
   */
  val RELIANCE = 1.0.toFloat

  /**
   The budget threthold rate for task to be accepted.
   */
  val BUDGET_THRESHOLD = 0.1.toFloat

  /**
   Default expectation for conformation.
   */
  val DEFAULT_CONFIRMATION_EXPECTATION = 0.8.toFloat

  /**
   Default expectation for conformation.
   */
  val DEFAULT_CREATION_EXPECTATION = 0.66.toFloat

  /**
   Default confidence of input judgment.
   */
  val DEFAULT_JUDGMENT_CONFIDENCE = 0.9.toFloat

  /**
   Default priority of input judgment
   */
  val DEFAULT_JUDGMENT_PRIORITY = 0.8.toFloat

  /**
   Default durability of input judgment
   */
  val DEFAULT_JUDGMENT_DURABILITY = 0.8.toFloat

  /**
   Default priority of input question
   */
  val DEFAULT_QUESTION_PRIORITY = 0.9.toFloat

  /**
   Default durability of input question
   */
  val DEFAULT_QUESTION_DURABILITY = 0.7.toFloat

  /**
   Level granularity in Bag, two digits
   */
  val BAG_LEVEL = 100

  /**
   Level separation in Bag, one digit, for display (run-time adjustable) and management (fixed)
   */
  val BAG_THRESHOLD = 10

  /**
   Hashtable load factor in Bag
   */
  val LOAD_FACTOR = 0.5.toFloat

  /**
   Size of ConceptBag
   */
  val CONCEPT_BAG_SIZE = 1000

  /**
   Size of TaskLinkBag
   */
  val TASK_LINK_BAG_SIZE = 20

  /**
   Size of TermLinkBag
   */
  val TERM_LINK_BAG_SIZE = 100

  /**
   Size of TaskBuffer
   */
  val TASK_BUFFER_SIZE = 20

  /**
   Maximum length of Stamp, a power of 2
   */
  val MAXIMUM_STAMP_LENGTH = 8

  /**
   Remember recently used TermLink on a Task
   */
  val TERM_LINK_RECORD_LENGTH = 10

  /**
   Maximum number of beliefs kept in a Concept
   */
  val MAXIMUM_BELIEF_LENGTH = 7

  /**
   Maximum number of goals kept in a Concept
   */
  val MAXIMUM_QUESTIONS_LENGTH = 5
}
