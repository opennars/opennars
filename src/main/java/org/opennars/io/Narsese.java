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

import java.io.Serializable;
import static java.lang.Float.parseFloat;
import java.util.ArrayList;
import org.opennars.storage.Memory;
import org.opennars.main.NAR;
import org.opennars.main.Parameters;
import org.opennars.entity.BudgetValue;
import org.opennars.entity.Sentence;
import org.opennars.entity.Stamp;
import org.opennars.entity.Task;
import org.opennars.entity.TruthValue;
import static org.opennars.inference.BudgetFunctions.truthToQuality;
import static org.opennars.io.Symbols.ARGUMENT_SEPARATOR;
import static org.opennars.io.Symbols.BUDGET_VALUE_MARK;
import static org.opennars.io.Symbols.GOAL_MARK;
import static org.opennars.io.Symbols.INPUT_LINE_PREFIX;
import static org.opennars.io.Symbols.JUDGMENT_MARK;
import org.opennars.io.Symbols.NativeOperator;
import static org.opennars.io.Symbols.NativeOperator.COMPOUND_TERM_CLOSER;
import static org.opennars.io.Symbols.NativeOperator.SET_EXT_CLOSER;
import static org.opennars.io.Symbols.NativeOperator.SET_INT_CLOSER;
import static org.opennars.io.Symbols.NativeOperator.STATEMENT_CLOSER;
import static org.opennars.io.Symbols.OUTPUT_LINE_PREFIX;
import static org.opennars.io.Symbols.PREFIX_MARK;
import static org.opennars.io.Symbols.QUESTION_MARK;
import static org.opennars.io.Symbols.QUEST_MARK;
import static org.opennars.io.Symbols.STAMP_CLOSER;
import static org.opennars.io.Symbols.STAMP_OPENER;
import static org.opennars.io.Symbols.TRUTH_VALUE_MARK;
import static org.opennars.io.Symbols.VALUE_SEPARATOR;
import static org.opennars.io.Symbols.getCloser;
import static org.opennars.io.Symbols.getOpener;
import static org.opennars.io.Symbols.getRelation;
import static org.opennars.io.Symbols.isRelation;
import org.opennars.language.Interval;
import org.opennars.language.SetExt;
import org.opennars.language.SetInt;
import org.opennars.language.Statement;
import org.opennars.language.Tense;
import org.opennars.language.Term;
import org.opennars.language.Terms;
import org.opennars.language.Variable;
import org.opennars.operator.Operation;
import static org.opennars.operator.Operation.make;
import org.opennars.operator.Operator;
import static java.lang.String.valueOf;
import static org.opennars.io.Symbols.getOperator;
import static org.opennars.language.Variables.containVar;
import static org.opennars.language.Statement.make;

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
        InvalidInputException(String s) {
            super(s);
        }
    }    
    
    public Narsese(Memory memory) {        
        this.memory = memory;
    }

    public Narsese(NAR n) {
        this(n.memory);
    }
    

    /**
     * Parse a line of addInput experience
     * <p>
     * called from ExperienceIO.loadLine
     *
     * @param buffer The line to be parsed
     * @param memory Reference to the memory
     * @param time The current time
     * @return An experienced task
     */
    public Task parseNarsese(StringBuilder buffer) throws InvalidInputException {
        
        int i = buffer.indexOf(valueOf(PREFIX_MARK));
        if (i > 0) {
            String prefix = buffer.substring(0, i).trim();
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
            int j = buffer.lastIndexOf(valueOf(STAMP_OPENER));
            buffer.delete(j - 1, buffer.length());
        }
        c = buffer.charAt(buffer.length() - 1);
        if (c == ']') {
            int j = buffer.lastIndexOf(valueOf('['));
            buffer.delete(j-1, buffer.length());
        }
        return parseTask(buffer.toString().trim());
    }
    
    /**
     * Enter a new Task in String into the memory, called from InputWindow or
     * locally.
     *
     * @param s the single-line addInput String
     * @param memory Reference to the memory
     * @param time The current time
     * @return An experienced task
     */    
    public Task parseTask(String s) throws InvalidInputException {
        StringBuilder buffer = new StringBuilder(Texts.escape(s));
        
        String budgetString = getBudgetString(buffer);
        String truthString = getTruthString(buffer);
        Tense tense = parseTense(buffer);
        String str = buffer.toString().trim();
        int last = str.length() - 1;
        char punc = str.charAt(last);
        
        Stamp stamp = new Stamp(-1 /* if -1, will be set right before the Task is input */, 
                tense, memory.newStampSerial(), Parameters.DURATION);

        TruthValue truth = parseTruth(truthString, punc);
        Term content = parseTerm(str.substring(0, last));
        if (content == null) throw new InvalidInputException("Content term missing");
            
        Sentence sentence = new Sentence(
            content,
            punc,
            truth,
            stamp);

        //if ((content instanceof Conjunction) && Variable.containVarDep(content.getName())) {
        //    sentence.setRevisible(false);
        //}
        BudgetValue budget = parseBudget(budgetString, punc, truth);
        Task task = new Task(sentence, budget, true);
        return task;

    }

    /* ---------- react values ---------- */
    /**
     * Return the prefix of a task symbol that contains a BudgetValue
     *
     * @param s the addInput in a StringBuilder
     * @return a String containing a BudgetValue
     * @throws org.opennars.io.StringParser.InvalidInputException if the addInput cannot be
 parsed into a BudgetValue
     */
    private static String getBudgetString(StringBuilder s) throws InvalidInputException {
        if (s.charAt(0) != BUDGET_VALUE_MARK) {
            return null;
        }
        int i = s.indexOf(valueOf(BUDGET_VALUE_MARK), 1);    // looking for the end
        if (i < 0) {
            throw new InvalidInputException("missing budget closer");
        }
        String budgetString = s.substring(1, i).trim();
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
     * @throws org.opennars.io.StringParser.InvalidInputException if the addInput cannot be
 parsed into a TruthValue
     */
    private static String getTruthString(final StringBuilder s) throws InvalidInputException {
        final int last = s.length() - 1;
        if (s.charAt(last) != TRUTH_VALUE_MARK) {       // use default
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
    private static TruthValue parseTruth(String s, char type) {
        if ((type == QUESTION_MARK) || (type == QUEST_MARK)) {
            return null;
        }
        float frequency = 1.0f;
        float confidence = Parameters.DEFAULT_JUDGMENT_CONFIDENCE;
        if(type==GOAL_MARK) {
            confidence = Parameters.DEFAULT_GOAL_CONFIDENCE;
        }
        if (s != null) {
            int i = s.indexOf(VALUE_SEPARATOR);
            if (i < 0) {
                frequency = parseFloat(s);
            } else {
                frequency = parseFloat(s.substring(0, i));
                confidence = parseFloat(s.substring(i + 1));
            }
        }
        return new TruthValue(frequency, confidence);
    }

    /**
     * react the addInput String into a BudgetValue
     *
     * @param truth the TruthValue of the task
     * @param s addInput String
     * @param punctuation Task punctuation
     * @return the addInput BudgetValue
     * @throws org.opennars.io.StringParser.InvalidInputException If the String cannot
     * be parsed into a BudgetValue
     */
    private static BudgetValue parseBudget(String s, char punctuation, TruthValue truth) throws InvalidInputException {
        float priority, durability;
        switch (punctuation) {
            case JUDGMENT_MARK:
                priority = Parameters.DEFAULT_JUDGMENT_PRIORITY;
                durability = Parameters.DEFAULT_JUDGMENT_DURABILITY;
                break;
            case QUESTION_MARK:
                priority = Parameters.DEFAULT_QUESTION_PRIORITY;
                durability = Parameters.DEFAULT_QUESTION_DURABILITY;
                break;
            case GOAL_MARK:
                priority = Parameters.DEFAULT_GOAL_PRIORITY;
                durability = Parameters.DEFAULT_GOAL_DURABILITY;
                break;
            case QUEST_MARK:
                priority = Parameters.DEFAULT_QUEST_PRIORITY;
                durability = Parameters.DEFAULT_QUEST_DURABILITY;
                break;                
            default:
                throw new InvalidInputException("unknown punctuation: '" + punctuation + "'");
        }
        if (s != null) { // overrite default
            int i = s.indexOf(VALUE_SEPARATOR);
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
        float quality = (truth == null) ? 1 : truthToQuality(truth);
        return new BudgetValue(priority, durability, quality);
    }

    /**
     * Recognize the tense of an addInput sentence
     * @param s the addInput in a StringBuilder
     * @return a tense value
     */
    public static Tense parseTense(StringBuilder s) {
        int i = s.indexOf(Symbols.TENSE_MARK);
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
     * @param s0 the String to be parsed
     * @param memory Reference to the memory
     * @return the Term generated from the String
     */
    public Term parseTerm(String s) throws InvalidInputException {
        s = s.trim();
        
        if (s.length() == 0) return null;
        
        int index = s.length() - 1;
        char first = s.charAt(0);
        char last = s.charAt(index);

        NativeOperator opener = getOpener(first);
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
        else if (Parameters.FUNCTIONAL_OPERATIONAL_FORMAT) {
            
            //parse functional operation:
            //  function()
            //  function(a)
            //  function(a,b)
            
            //test for existence of matching parentheses at beginning at index!=0
            int pOpen = s.indexOf('(');
            int pClose = s.lastIndexOf(')');
            if ((pOpen!=-1) && (pClose!=-1) && (pClose==s.length()-1)) {
                
                String operatorString = Operator.addPrefixIfMissing( s.substring(0, pOpen) );
                                
                Operator operator = memory.getOperator(operatorString);
                
                if (operator == null) {
                    //???
                    throw new InvalidInputException("Unknown operator: " + operatorString);
                }
                
                String argString = s.substring(pOpen+1, pClose+1);               
                
                
                Term[] a;                
                if (argString.length() > 1) {                
                    ArrayList<Term> args = parseArguments(argString);                                
                    a = args.toArray(new Term[args.size()]);
                }
                else {
                    //void "()" arguments, default to (SELF)
                    a = Operation.SELF_TERM_ARRAY;
                }                                                            
                
                Operation o = Operation.make(operator, a, true);
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
     * @throws org.opennars.io.StringParser.InvalidInputException the String cannot be
     * parsed into a Term
     * @return the Term generated from the String
     */
    private Term parseAtomicTerm(String s0) throws InvalidInputException {
        String s = s0.trim();
        if (s.length() == 0) {
            throw new InvalidInputException("missing term");
        }
        
        Operator op = memory.getOperator(s0);
        if(op != null) {
            return op;
        }
        
        if (s.contains(" ")) { // invalid characters in a name
            throw new InvalidInputException("invalid term");
        }
        
        char c = s.charAt(0);
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
     * @throws org.opennars.io.StringParser.InvalidInputException the String cannot be
     * parsed into a Term
     */
    private Statement parseStatement(String s0) throws InvalidInputException {
        String s = s0.trim();
        int i = topRelation(s);
        if (i < 0) {
            throw new InvalidInputException("invalid statement: topRelation(s) < 0");
        }
        String relation = s.substring(i, i + 3);
        Term subject = parseTerm(s.substring(0, i));
        Term predicate = parseTerm(s.substring(i + 3));
        Statement t = make(getRelation(relation), subject, predicate, false, 0);
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
     * @throws org.opennars.io.StringParser.InvalidInputException the String cannot be
     * parsed into a Term
     */
    private Term parseCompoundTerm(final String s0) throws InvalidInputException {
        String s = s0.trim();
        if (s.isEmpty()) {
            throw new InvalidInputException("Empty compound term: " + s);
        }
        int firstSeparator = s.indexOf(ARGUMENT_SEPARATOR);
        if (firstSeparator == -1) {
            throw new InvalidInputException("Invalid compound term (missing ARGUMENT_SEPARATOR): " + s);
        }
                
        String op = (firstSeparator < 0) ? s : s.substring(0, firstSeparator).trim();
        NativeOperator oNative = getOperator(op);
        Operator oRegistered = memory.getOperator(op);
        
        if ((oRegistered==null) && (oNative == null)) {
            throw new InvalidInputException("Unknown operator: " + op);
        }

        ArrayList<Term> arg = (firstSeparator < 0) ? new ArrayList<>(0)
                : parseArguments(s.substring(firstSeparator + 1) + ARGUMENT_SEPARATOR);

        Term[] argA = arg.toArray(new Term[arg.size()]);
        
        Term t;
        
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
     * @return the arguments in an ArrayList
     * @param s0 The String to be parsed
     * @throws org.opennars.io.StringParser.InvalidInputException the String cannot be
     * parsed into an argument get
     */
    private ArrayList<Term> parseArguments(String s0) throws InvalidInputException {
        String s = s0.trim();
        ArrayList<Term> list = new ArrayList<>();
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
    private static int nextSeparator(String s, int first) {
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
        char c = s.charAt(i);
        
        boolean b = (getOpener(c)!=null);
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
    private static boolean isCloser(String s, int i) {
        char c = s.charAt(i);

        boolean b = (getCloser(c)!=null);
        if (!b)
            return false;
        
        return i < 2 || !isRelation(s.substring(i - 2, i + 1));
    }

    public static boolean possiblyNarsese(String s) {
        if(!s.contains("(") && !s.contains(")") && !s.contains("<") && !s.contains(">")) {
            return true;
        }
        return false;
    }
            
    
}
