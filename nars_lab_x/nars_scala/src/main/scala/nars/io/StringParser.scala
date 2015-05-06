package nars.io

import java.util._
import nars.logic.entity._
import nars.logic._
import nars.logic.language._
import nars.main._
import nars.storage.Memory
//remove if not needed
import scala.collection.JavaConversions._
import Symbols._
import java.lang.Float

object StringParser {

  /**
   * All kinds of invalid input lines
   */
  private class InvalidInputException(s: String) extends Exception(s)

  /**
   * Parse a line of input experience
   * <p>
   * called from ExperienceIO.loadLine
   * @param buffer The line to be parsed
   * @param memory Reference to the memory
   * @param time The current time
   * @return An experienced task
   */
  def parseExperience(buffer: StringBuffer, memory: Memory, time: Long): Task = {
    val i = buffer.indexOf(PREFIX_MARK + "")
    if (i > 0) {
      val prefix = buffer.substring(0, i).trim()
      if (prefix == OUTPUT_LINE) {
        return null
      } else if (prefix == INPUT_LINE) {
        buffer.delete(0, i + 1)
      }
    }
    val c = buffer.charAt(buffer.length - 1)
    if (c == STAMP_CLOSER) {
      val j = buffer.lastIndexOf(STAMP_OPENER + "")
      buffer.delete(j - 1, buffer.length)
    }
    parseTask(buffer.toString.trim(), memory, time)
  }

  /**
   * Enter a new Task in String into the memory, called from InputWindow or locally.
   * @param s the single-line input String
   * @param memory Reference to the memory
   * @param time The current time
   * @return An experienced task
   */
  def parseTask(s: String, memory: Memory, time: Long): Task = {
    val buffer = new StringBuffer(s)
    var task: Task = null
    try {
      val budgetString = getBudgetString(buffer)
      val truthString = getTruthString(buffer)
      val str = buffer.toString.trim()
      val last = str.length - 1
      val punc = str.charAt(last)
      val stamp = new Stamp(time)
      val truth = parseTruth(truthString, punc)
      val content = parseTerm(str.substring(0, last), memory)
      val sentence = new Sentence(content, punc, truth, stamp)
      if ((content.isInstanceOf[Conjunction]) && Variable.containVarDep(content.getName)) {
        sentence.setRevisible(false)
      }
      val budget = parseBudget(budgetString, punc, truth)
      task = new Task(sentence, budget)
    } catch {
      case e: InvalidInputException => println(" !!! INVALID INPUT: " + buffer + " --- " + e.getMessage)
    }
    task
  }

  /**
   * Return the prefex of a task string that contains a BudgetValue
   * @param s the input in a StringBuffer
   * @return a String containing a BudgetValue
   * @throws nars.io.StringParser.InvalidInputException if the input cannot be parsed into a BudgetValue
   */
  private def getBudgetString(s: StringBuffer): String = {
    if (s.charAt(0) != BUDGET_VALUE_MARK) {
      return null
    }
    val i = s.indexOf(BUDGET_VALUE_MARK + "", 1)
    if (i < 0) {
      throw new InvalidInputException("missing budget closer")
    }
    val budgetString = s.substring(1, i).trim()
    if (budgetString.length == 0) {
      throw new InvalidInputException("empty budget")
    }
    s.delete(0, i + 1)
    budgetString
  }

  /**
   * Return the postfix of a task string that contains a TruthValue
   * @return a String containing a TruthValue
   * @param s the input in a StringBuffer
   * @throws nars.io.StringParser.InvalidInputException if the input cannot be parsed into a TruthValue
   */
  private def getTruthString(s: StringBuffer): String = {
    val last = s.length - 1
    if (s.charAt(last) != TRUTH_VALUE_MARK) {
      return null
    }
    val first = s.indexOf(TRUTH_VALUE_MARK + "")
    if (first == last) {
      throw new InvalidInputException("missing truth mark")
    }
    val truthString = s.substring(first + 1, last).trim()
    if (truthString.length == 0) {
      throw new InvalidInputException("empty truth")
    }
    s.delete(first, last + 1)
    s.trimToSize()
    truthString
  }

  /**
   * parse the input String into a TruthValue (or DesireValue)
   * @param s input String
   * @param type Task type
   * @return the input TruthValue
   */
  private def parseTruth(s: String, `type`: Char): TruthValue = {
    if (`type` == QUESTION) {
      return null
    }
    var frequency = 1.0f
    var confidence = Parameters.DEFAULT_JUDGMENT_CONFIDENCE
    if (s != null) {
      val i = s.indexOf(VALUE_SEPARATOR)
      if (i < 0) {
        frequency = Float.parseFloat(s)
      } else {
        frequency = Float.parseFloat(s.substring(0, i))
        confidence = Float.parseFloat(s.substring(i + 1))
      }
    }
    new TruthValue(frequency, confidence)
  }

  /**
   * parse the input String into a BudgetValue
   *
   * @param truth the TruthValue of the task
   * @param s input String
   * @param punctuation Task punctuation
   * @return the input BudgetValue
   * @throws nars.io.StringParser.InvalidInputException If the String cannot be parsed into a BudgetValue
   */
  private def parseBudget(s: String, punctuation: Char, truth: TruthValue): BudgetValue = {
    var priority: Float = 0
    var durability: Float = 0
    punctuation match {
      case JUDGMENT =>
        priority = Parameters.DEFAULT_JUDGMENT_PRIORITY
        durability = Parameters.DEFAULT_JUDGMENT_DURABILITY

      case QUESTION =>
        priority = Parameters.DEFAULT_QUESTION_PRIORITY
        durability = Parameters.DEFAULT_QUESTION_DURABILITY

      case _ => throw new InvalidInputException("unknown punctuation: '" + punctuation + "'")
    }
    if (s != null) {
      val i = s.indexOf(VALUE_SEPARATOR)
      if (i < 0) {
        priority = Float.parseFloat(s)
      } else {
        priority = Float.parseFloat(s.substring(0, i))
        durability = Float.parseFloat(s.substring(i + 1))
      }
    }
    val quality = if ((truth == null)) 1 else BudgetFunctions.truthToQuality(truth)
    new BudgetValue(priority, durability, quality)
  }

  /**
   * Top-level method that parse a Term in general, which may recursively call itself.
   * <p>
   * There are 5 valid cases:
   * 1. (Op, A1, ..., An) is a CompoundTerm if Op is a built-in operator
   * 2. {A1, ..., An} is an SetExt;
   * 3. [A1, ..., An] is an SetInt;
   * 4. <T1 Re T2> is a Statement (including higher-order Statement);
   * 5. otherwise it is a simple term.
   * @param s0 the String to be parsed
   * @param memory Reference to the memory
   * @return the Term generated from the String
   */
  def parseTerm(s0: String, memory: Memory): Term = {
    val s = s0.trim()
    try {
      if (s.length == 0) {
        throw new InvalidInputException("missing content")
      }
      val t = memory.nameToListedTerm(s)
      if (t != null) {
        return t
      }
      val index = s.length - 1
      val first = s.charAt(0)
      val last = s.charAt(index)
      first match {
        case COMPOUND_TERM_OPENER => if (last == COMPOUND_TERM_CLOSER) {
          return parseCompoundTerm(s.substring(1, index), memory)
        } else {
          throw new InvalidInputException("missing CompoundTerm closer")
        }
        case SET_EXT_OPENER => if (last == SET_EXT_CLOSER) {
          return SetExt.make(parseArguments(s.substring(1, index) + ARGUMENT_SEPARATOR, memory), memory)
        } else {
          throw new InvalidInputException("missing ExtensionSet closer")
        }
        case SET_INT_OPENER => if (last == SET_INT_CLOSER) {
          return SetInt.make(parseArguments(s.substring(1, index) + ARGUMENT_SEPARATOR, memory), memory)
        } else {
          throw new InvalidInputException("missing IntensionSet closer")
        }
        case STATEMENT_OPENER => if (last == STATEMENT_CLOSER) {
          return parseStatement(s.substring(1, index), memory)
        } else {
          throw new InvalidInputException("missing Statement closer")
        }
        case _ => return parseAtomicTerm(s)
      }
    } catch {
      case e: InvalidInputException => println(" !!! INVALID INPUT: " + s + " --- " + e.getMessage)
    }
    null
  }

  /**
   * Parse a Term that has no internal structure.
   * <p>
   * The Term can be a constant or a variable.
   * @param s0 the String to be parsed
   * @throws nars.io.StringParser.InvalidInputException the String cannot be parsed into a Term
   * @return the Term generated from the String
   */
  private def parseAtomicTerm(s0: String): Term = {
    val s = s0.trim()
    if (s.length == 0) {
      throw new InvalidInputException("missing term")
    }
    if (s.contains(" ")) {
      throw new InvalidInputException("invalid term")
    }
    if (Variable.containVar(s)) {
      new Variable(s)
    } else {
      new Term(s)
    }
  }

  /**
   * Parse a String to create a Statement.
   * @return the Statement generated from the String
   * @param s0 The input String to be parsed
   * @throws nars.io.StringParser.InvalidInputException the String cannot be parsed into a Term
   */
  private def parseStatement(s0: String, memory: Memory): Statement = {
    val s = s0.trim()
    val i = topRelation(s)
    if (i < 0) {
      throw new InvalidInputException("invalid statement")
    }
    val relation = s.substring(i, i + 3)
    val subject = parseTerm(s.substring(0, i), memory)
    val predicate = parseTerm(s.substring(i + 3), memory)
    val t = Statement.make(relation, subject, predicate, memory)
    if (t == null) {
      throw new InvalidInputException("invalid statement")
    }
    t
  }

  /**
   * Parse a String to create a CompoundTerm.
   * @return the Term generated from the String
   * @param s0 The String to be parsed
   * @throws nars.io.StringParser.InvalidInputException the String cannot be parsed into a Term
   */
  private def parseCompoundTerm(s0: String, memory: Memory): Term = {
    val s = s0.trim()
    val firstSeparator = s.indexOf(ARGUMENT_SEPARATOR)
    val op = s.substring(0, firstSeparator).trim()
    if (!CompoundTerm.isOperator(op)) {
      throw new InvalidInputException("unknown operator: " + op)
    }
    val arg = parseArguments(s.substring(firstSeparator + 1) + ARGUMENT_SEPARATOR, memory)
    val t = CompoundTerm.make(op, arg, memory)
    if (t == null) {
      throw new InvalidInputException("invalid compound term")
    }
    t
  }

  /**
   * Parse a String into the argument get of a CompoundTerm.
   * @return the arguments in an ArrayList
   * @param s0 The String to be parsed
   * @throws nars.io.StringParser.InvalidInputException the String cannot be parsed into an argument get
   */
  private def parseArguments(s0: String, memory: Memory): ArrayList[Term] = {
    val s = s0.trim()
    val list = new ArrayList[Term]()
    var start = 0
    var end = 0
    while (end < s.length - 1) {
      end = nextSeparator(s, start)
      val t = parseTerm(s.substring(start, end), memory)
      list.add(t)
      start = end + 1
    }
    if (list.isEmpty) {
      throw new InvalidInputException("null argument")
    }
    list
  }

  /**
   * Locate the first top-level separator in a CompoundTerm
   * @return the index of the next seperator in a String
   * @param s The String to be parsed
   * @param first The starting index
   */
  private def nextSeparator(s: String, first: Int): Int = {
    var levelCounter = 0
    var i = first
    while (i < s.length - 1) {
      if (isOpener(s, i)) {
        levelCounter += 1
      } else if (isCloser(s, i)) {
        levelCounter -= 1
      } else if (s.charAt(i) == ARGUMENT_SEPARATOR) {
        if (levelCounter == 0) {
          //break
        }
      }
      i += 1
    }
    i
  }

  /**
   * locate the top-level relation in a statement
   * @return the index of the top-level relation
   * @param s The String to be parsed
   */
  private def topRelation(s: String): Int = {
    var levelCounter = 0
    var i = 0
    while (i < s.length - 3) {
      if ((levelCounter == 0) && (Statement.isRelation(s.substring(i, i + 3)))) {
        return i
      }
      if (isOpener(s, i)) {
        levelCounter += 1
      } else if (isCloser(s, i)) {
        levelCounter -= 1
      }
      i += 1
    }
    -1
  }

  /**
   * Check CompoundTerm opener symbol
   * @return if the given String is an opener symbol
   * @param s The String to be checked
   * @param i The starting index
   */
  private def isOpener(s: String, i: Int): Boolean = {
    val c = s.charAt(i)
    val b = (c == COMPOUND_TERM_OPENER) || (c == SET_EXT_OPENER) || 
      (c == SET_INT_OPENER) || 
      (c == STATEMENT_OPENER)
    if (!b) {
      return false
    }
    if (i + 3 <= s.length && Statement.isRelation(s.substring(i, i + 3))) {
      return false
    }
    true
  }

  /**
   * Check CompoundTerm closer symbol
   * @return if the given String is a closer symbol
   * @param s The String to be checked
   * @param i The starting index
   */
  private def isCloser(s: String, i: Int): Boolean = {
    val c = s.charAt(i)
    val b = (c == COMPOUND_TERM_CLOSER) || (c == SET_EXT_CLOSER) || 
      (c == SET_INT_CLOSER) || 
      (c == STATEMENT_CLOSER)
    if (!b) {
      return false
    }
    if (i >= 2 && Statement.isRelation(s.substring(i - 2, i + 1))) {
      return false
    }
    true
  }
}
