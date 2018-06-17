/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.opennars.io;

import org.opennars.entity.*;
import org.opennars.io.Symbols.*;
import org.opennars.language.*;
import org.opennars.main.Nar;
import org.opennars.main.MiscFlags;
import org.opennars.operator.Operation;
import org.opennars.operator.Operator;
import org.opennars.storage.Memory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Float.parseFloat;
import static java.lang.String.valueOf;
import static org.opennars.inference.BudgetFunctions.truthToQuality;
import static org.opennars.io.Symbols.*;
import static org.opennars.io.Symbols.NativeOperator.*;
import static org.opennars.language.Variables.containVar;
import static org.opennars.operator.Operation.make;

/**
 * Utility methods for working and reacting to Narsese input.
 * This will eventually be integrated with NarseseParser for systematic
 * parsing and prediction of input.
 */
public class Narsese implements Serializable {
    
    public final Memory memory;

                
    /**
     * All kinds of invalid addInput lines
     */
    public static class InvalidInputException extends Exception {

        /**
         * An invalid addInput line.
         * @param s type of error
         */
        InvalidInputException(final String s) {
            super(s);
        }
    }    
    
    public Narsese(final Memory memory) {
        this.memory = memory;
    }

    public Narsese(final Nar n) {
        this(n.memory);
    }
    

    /**
     * Parse a line of addInput experience
     * <p>
     * called from ExperienceIO.loadLine
     *
     * @param buffer The line to be parsed
     * @return An experienced task
     */
    public Task parseNarsese(final StringBuilder buffer) throws InvalidInputException {
        
        final int i = buffer.indexOf(valueOf(PREFIX_MARK));
        if (i > 0) {
            final String prefix = buffer.substring(0, i).trim();
            if (prefix.equals(INPUT_LINE_PREFIX)) {
                buffer.delete(0, i + 1);                
            }
            else if (prefix.equals(OUTPUT_LINE_PREFIX)) {
                //ignore outputs
                return null;                
            }            
        }
        
        
        
        char c = buffer.charAt(buffer.length() - 1);
        if (c == STAMP_CLOSER) {
            //ignore stamp
            final int j = buffer.lastIndexOf(valueOf(STAMP_OPENER));
            buffer.delete(j - 1, buffer.length());
        }
        c = buffer.charAt(buffer.length() - 1);
        if (c == ']') {
            final int j = buffer.lastIndexOf(valueOf('['));
            buffer.delete(j-1, buffer.length());
        }
        return parseTask(buffer.toString().trim());
    }
    
    /**
     * Enter a new Task in String into the memory, called from InputWindow or
     * locally.
     *
     * @param s the single-line addInput String
     * @return An experienced task
     */    
    public Task parseTask(final String s) throws InvalidInputException {
        final StringBuilder buffer = new StringBuilder(Texts.escape(s));
        
        final String budgetString = getBudgetString(buffer);
        final String truthString = getTruthString(buffer);
        final Tense tense = parseTense(buffer);
        final String str = buffer.toString().trim();
        final int last = str.length() - 1;
        final char punc = str.charAt(last);
        
        final Stamp stamp = new Stamp(-1 /* if -1, will be set right before the Task is input */,
                tense, memory.newStampSerial(), this.memory.narParameters.DURATION);

        final TruthValue truth = parseTruth(truthString, punc);
        final Term content = parseTerm(str.substring(0, last));
        if (content == null) throw new InvalidInputException("Content term missing");
            
        final Sentence sentence = new Sentence(
            content,
            punc,
            truth,
            stamp);

        //if ((content instanceof Conjunction) && Variable.containVarDep(content.getName())) {
        //    sentence.setRevisible(false);
        //}
        final BudgetValue budget = parseBudget(budgetString, punc, truth);
        return new Task(sentence, budget, Task.EnumType.INPUT);
    }

    /* ---------- react values ---------- */
    /**
     * Return the prefix of a task symbol that contains a BudgetValue
     *
     * @param s the addInput in a StringBuilder
     * @return a String containing a BudgetValue
     * @throws InvalidInputException if the addInput cannot be parsed into a BudgetValue
     */
    private static String getBudgetString(final StringBuilder s) throws InvalidInputException {
        if (s.length() == 0 || s.charAt(0) != BUDGET_VALUE_MARK) {
            return null;
        }
        final int i = s.indexOf(valueOf(BUDGET_VALUE_MARK), 1);    // looking for the end
        if (i < 0) {
            throw new InvalidInputException("missing budget closer");
        }
        final String budgetString = s.substring(1, i).trim();
        if (budgetString.length() == 0) {
            throw new InvalidInputException("empty budget");
        }
        s.delete(0, i + 1);
        return budgetString;
    }

    /**
     * Return the postfix of a task symbol that contains a TruthValue
     *
     * @return a String containing a TruthValue
     * @param s the addInput in a StringBuilder
     * @throws InvalidInputException if the addInput cannot be parsed into a TruthValue
     */
    private static String getTruthString(final StringBuilder s) throws InvalidInputException {
        final int last = s.length() - 1;
        if (s.length() == 0 || s.charAt(last) != TRUTH_VALUE_MARK) {       // use default
            return null;
        }
        final int first = s.indexOf(valueOf(TRUTH_VALUE_MARK));    // looking for the beginning
        if (first == last) { // no matching closer
            throw new InvalidInputException("missing truth mark");
        }
        final String truthString = s.substring(first + 1, last).trim();
        if (truthString.length() == 0) {                // empty usage
            throw new InvalidInputException("empty truth");
        }
        s.delete(first, last + 1);                 // remaining addInput to be processed outside
        s.trimToSize();
        return truthString;
    }

    /**
     * react the addInput String into a TruthValue (or DesireValue)
     *
     * @param s addInput String
     * @param type Task type
     * @return the addInput TruthValue
     */
    private TruthValue parseTruth(final String s, final char type) {
        if ((type == QUESTION_MARK) || (type == QUEST_MARK)) {
            return null;
        }
        float frequency = 1.0f;
        float confidence = memory.narParameters.DEFAULT_JUDGMENT_CONFIDENCE;
        if(type==GOAL_MARK) {
            confidence = memory.narParameters.DEFAULT_GOAL_CONFIDENCE;
        }
        if (s != null) {
            final int i = s.indexOf(VALUE_SEPARATOR);
            if (i < 0) {
                frequency = parseFloat(s);
            } else {
                frequency = parseFloat(s.substring(0, i));
                confidence = parseFloat(s.substring(i + 1));
            }
        }
        return new TruthValue(frequency, confidence, memory.narParameters);
    }

    /**
     * react the addInput String into a BudgetValue
     *
     * @param truth the TruthValue of the task
     * @param s addInput String
     * @param punctuation Task punctuation
     * @return the addInput BudgetValue
     * @throws InvalidInputException If the String cannot be parsed into a BudgetValue
     */
    private BudgetValue parseBudget(final String s, final char punctuation, final TruthValue truth) throws InvalidInputException {
        float priority, durability;
        switch (punctuation) {
            case JUDGMENT_MARK:
                priority = memory.narParameters.DEFAULT_JUDGMENT_PRIORITY;
                durability = memory.narParameters.DEFAULT_JUDGMENT_DURABILITY;
                break;
            case QUESTION_MARK:
                priority = memory.narParameters.DEFAULT_QUESTION_PRIORITY;
                durability = memory.narParameters.DEFAULT_QUESTION_DURABILITY;
                break;
            case GOAL_MARK:
                priority = memory.narParameters.DEFAULT_GOAL_PRIORITY;
                durability = memory.narParameters.DEFAULT_GOAL_DURABILITY;
                break;
            case QUEST_MARK:
                priority = memory.narParameters.DEFAULT_QUEST_PRIORITY;
                durability = memory.narParameters.DEFAULT_QUEST_DURABILITY;
                break;                
            default:
                throw new InvalidInputException("unknown punctuation: '" + punctuation + "'");
        }
        if (s != null) { // overrite default
            final int i = s.indexOf(VALUE_SEPARATOR);
            if (i < 0) {        // default durability
                priority = parseFloat(s);
            } else {
                int i2 = s.indexOf(VALUE_SEPARATOR, i+1);
                if (i2 == -1)
                    i2 = s.length();
                priority = parseFloat(s.substring(0, i));
                durability = parseFloat(s.substring(i + 1, i2));
            }
        }
        final float quality = (truth == null) ? 1 : truthToQuality(truth);
        return new BudgetValue(priority, durability, quality, memory.narParameters);
    }

    /**
     * Recognize the tense of an addInput sentence
     * @param s the addInput in a StringBuilder
     * @return a tense value
     */
    public static Tense parseTense(final StringBuilder s) {
        final int i = s.indexOf(Symbols.TENSE_MARK);
        String t = "";
        if (i > 0) {
            t = s.substring(i).trim();
            s.delete(i, s.length());
        }
        return Tense.tense(t);
    }

    
    
    /* ---------- react String into term ---------- */
    /**
     * Top-level method that react a Term in general, which may recursively call
 itself.
     * <p>
 There are 5 valid cases: 1. (Op, A1, ..., An) is a CompoundTerm if Op is
 a built-in getOperator 2. {A1, ..., An} is an SetExt; 3. [A1, ..., An] is an
 SetInt; 4. <T1 Re T2> is a Statement (including higher-order Statement);
     * 5. otherwise it is a simple term.
     *
     * @param s the String to be parsed
     * @return the Term generated from the String
     */
    public Term parseTerm(String s) throws InvalidInputException {
        s = s.trim();
        
        if (s.length() == 0) return null;
        
        final int index = s.length() - 1;
        final char first = s.charAt(0);
        final char last = s.charAt(index);

        final NativeOperator opener = getOpener(first);
        if (opener!=null) {
            switch (opener) {
                case COMPOUND_TERM_OPENER:
                    if (last == COMPOUND_TERM_CLOSER.ch) {
                       return parseCompoundTerm(s.substring(1, index));
                    } else {
                        throw new InvalidInputException("missing CompoundTerm closer");
                    }
                case SET_EXT_OPENER:
                    if (last == SET_EXT_CLOSER.ch) {
                        return SetExt.make(parseArguments(s.substring(1, index) + ARGUMENT_SEPARATOR));
                    } else {
                        throw new InvalidInputException("missing ExtensionSet closer");
                    }                    
                case SET_INT_OPENER:
                    if (last == SET_INT_CLOSER.ch) {
                        return SetInt.make(parseArguments(s.substring(1, index) + ARGUMENT_SEPARATOR));
                    } else {
                        throw new InvalidInputException("missing IntensionSet closer");
                    }   
                case STATEMENT_OPENER:
                    if (last == STATEMENT_CLOSER.ch) {
                        return parseStatement(s.substring(1, index));
                    } else {
                        throw new InvalidInputException("missing Statement closer");
                    }
            }
        }
        else if (MiscFlags.FUNCTIONAL_OPERATIONAL_FORMAT) {
            
            //parse functional operation:
            //  function()
            //  function(a)
            //  function(a,b)
            
            //test for existence of matching parentheses at beginning at index!=0
            final int pOpen = s.indexOf('(');
            final int pClose = s.lastIndexOf(')');
            if ((pOpen!=-1) && (pClose!=-1) && (pClose==s.length()-1)) {
                
                final String operatorString = Operator.addPrefixIfMissing( s.substring(0, pOpen) );
                                
                final Operator operator = memory.getOperator(operatorString);
                
                if (operator == null) {
                    //???
                    throw new InvalidInputException("Unknown operator: " + operatorString);
                }
                
                final String argString = s.substring(pOpen+1, pClose+1);
                
                
                final Term[] a;
                if (argString.length() > 1) {                
                    final List<Term> args = parseArguments(argString);
                    a = args.toArray(new Term[0]);
                }
                else {
                    //void "()" arguments, default to (SELF)
                    a = Operation.SELF_TERM_ARRAY;
                }                                                            
                
                final Operation o = Operation.make(operator, a, true);
                return o;                
            }
        }

        //if no opener, parse the term            
        return parseAtomicTerm(s);

    }

//    private static void showWarning(String message) {
//		new TemporaryFrame( message + "\n( the faulty line has been kept in the addInput window )",
//				40000, TemporaryFrame.WARNING );
//    }
    /**
     * Parse a Term that has no internal structure.
     * <p>
     * The Term can be a constant or a variable.
     *
     * @param s0 the String to be parsed
     * @throws InvalidInputException the String cannot be parsed into a Term
     * @return the Term generated from the String
     */
    private Term parseAtomicTerm(final String s0) throws InvalidInputException {
        final String s = s0.trim();
        if (s.length() == 0) {
            throw new InvalidInputException("missing term");
        }
        
        final Operator op = memory.getOperator(s0);
        if(op != null) {
            return op;
        }
        
        if (s.contains(" ")) { // invalid characters in a name
            throw new InvalidInputException("invalid term: " + s);
        }
        
        final char c = s.charAt(0);
        if (c == Symbols.INTERVAL_PREFIX) {
            return Interval.interval(s);
        }
 
        if (containVar(s) && !s.equals("#")) {
            return new Variable(s);
        } else {
            return Term.get(s);
        }
    }

    /**
     * Parse a String to create a Statement.
     *
     * @return the Statement generated from the String
     * @param s0 The addInput String to be parsed
     * @throws InvalidInputException the String cannot be parsed into a Term
     */
    private Statement parseStatement(final String s0) throws InvalidInputException {
        final String s = s0.trim();
        final int i = topRelation(s);
        if (i < 0) {
            throw new InvalidInputException("invalid statement: topRelation(s) < 0");
        }
        final String relation = s.substring(i, i + 3);
        final Term subject = parseTerm(s.substring(0, i));
        final Term predicate = parseTerm(s.substring(i + 3));
        final Statement t = make(getRelation(relation), subject, predicate, false, 0);
        if (t == null) {
            throw new InvalidInputException("invalid statement: statement unable to create: " + getOperator(relation) + " " + subject + " " + predicate);
        }
        return t;
    }

    /**
     * Parse a String to create a CompoundTerm.
     *
     * @return the Term generated from the String
     * @param s0 The String to be parsed
     * @throws InvalidInputException the String cannot be parsed into a Term
     */
    private Term parseCompoundTerm(final String s0) throws InvalidInputException {
        final String s = s0.trim();
        if (s.isEmpty()) {
            throw new InvalidInputException("Empty compound term: " + s);
        }
        final int firstSeparator = s.indexOf(ARGUMENT_SEPARATOR);
        if (firstSeparator == -1) {
            throw new InvalidInputException("Invalid compound term (missing ARGUMENT_SEPARATOR): " + s);
        }
                
        final String op = (firstSeparator < 0) ? s : s.substring(0, firstSeparator).trim();
        final NativeOperator oNative = getOperator(op);
        final Operator oRegistered = memory.getOperator(op);
        
        if ((oRegistered==null) && (oNative == null)) {
            throw new InvalidInputException("Unknown operator: " + op);
        }

        final List<Term> arg = (firstSeparator < 0) ? new ArrayList<>(0)
                : parseArguments(s.substring(firstSeparator + 1) + ARGUMENT_SEPARATOR);

        final Term[] argA = arg.toArray(new Term[0]);
        
        final Term t;
        
        if (oNative!=null) {
            t = Terms.term(oNative, argA);
        }
        else if (oRegistered!=null) {
            t = make(oRegistered, argA, true);
        }
        else {
            throw new InvalidInputException("Invalid compound term");
        }
        
        return t;
    }

    /**
     * Parse a String into the argument get of a CompoundTerm.
     *
     * @return the arguments in an List
     * @param s0 The String to be parsed
     * @throws InvalidInputException the String cannot be parsed into an argument get
     */
    private List<Term> parseArguments(final String s0) throws InvalidInputException {
        final String s = s0.trim();
        final List<Term> list = new ArrayList<>();
        int start = 0;
        int end = 0;
        Term t;
        while (end < s.length() - 1) {
            end = nextSeparator(s, start);
            if (end == start)
                break;
            t = parseTerm(s.substring(start, end));     // recursive call
            list.add(t);
            start = end + 1;
        }
        if (list.isEmpty()) {
            throw new InvalidInputException("null argument");
        }
        return list;
    }

    /* ---------- locate top-level substring ---------- */
    /**
     * Locate the first top-level separator in a CompoundTerm
     *
     * @return the index of the next seperator in a String
     * @param s The String to be parsed
     * @param first The starting index
     */
    private static int nextSeparator(final String s, final int first) {
        int levelCounter = 0;
        int i = first;
        while (i < s.length() - 1) {
            if (isOpener(s, i)) {
                levelCounter++;
            } else if (isCloser(s, i)) {
                levelCounter--;
            } else if (s.charAt(i) == ARGUMENT_SEPARATOR) {
                if (levelCounter == 0) {
                    break;
                }
            }
            i++;
        }
        return i;
    }

    /**
     * locate the top-level getRelation in a statement
     *
     * @return the index of the top-level getRelation
     * @param s The String to be parsed
     */
    private static int topRelation(final String s) {      // need efficiency improvement
        int levelCounter = 0;
        int i = 0;
        while (i < s.length() - 3) {    // don't need to check the last 3 characters
            if ((levelCounter == 0) && (isRelation(s.substring(i, i + 3)))) {
                return i;
            }
            if (isOpener(s, i)) {
                levelCounter++;
            } else if (isCloser(s, i)) {
                levelCounter--;
            }
            i++;
        }
        return -1;
    }

    /* ---------- recognize symbols ---------- */
    /**
     * Check CompoundTerm opener symbol
     *
     * @return if the given String is an opener symbol
     * @param s The String to be checked
     * @param i The starting index
     */
    private static boolean isOpener(final String s, final int i) {
        final char c = s.charAt(i);
        
        final boolean b = (getOpener(c)!=null);
        if (!b)
            return false;
        
        return i + 3 > s.length() || !isRelation(s.substring(i, i + 3));
    }

    /**
     * Check CompoundTerm closer symbol
     *
     * @return if the given String is a closer symbol
     * @param s The String to be checked
     * @param i The starting index
     */
    private static boolean isCloser(final String s, final int i) {
        final char c = s.charAt(i);

        final boolean b = (getCloser(c)!=null);
        if (!b)
            return false;
        
        return i < 2 || !isRelation(s.substring(i - 2, i + 1));
    }

    public static boolean possiblyNarsese(final String s) {
        return !s.contains("(") && !s.contains(")") && !s.contains("<") && !s.contains(">");
    }
            
    
}
