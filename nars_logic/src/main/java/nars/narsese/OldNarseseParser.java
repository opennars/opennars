package nars.narsese;

import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.budget.Budget;
import nars.Symbols;
import nars.nal.*;
import nars.nal.nal3.SetExt;
import nars.nal.nal3.SetInt;
import nars.nal.nal7.Interval;
import nars.nal.nal7.Tense;
import nars.nal.nal8.Operation;
import nars.nal.nal8.Operator;
import nars.nal.stamp.Stamp;
import nars.nal.term.*;

import java.util.ArrayList;
import java.util.Arrays;

import static java.lang.Float.parseFloat;
import static java.lang.String.valueOf;
import static nars.Symbols.*;
import static nars.budget.BudgetFunctions.truthToQuality;
import static nars.nal.NALOperator.*;
import static nars.nal.Variables.containVar;
import static nars.nal.nal8.Operation.make;

/**
 * Utility methods for working and reacting to Narsese input.
 * This will eventually be integrated with NarseseParser for systematic
 * parsing and prediction of input.
 *
 * TODO move this inside NarseseParserTest which should be the only place it can be used
 */
@Deprecated public class OldNarseseParser {
    
    public final Memory memory;
    private final NarseseParser newParser;
    private Term self;




    public OldNarseseParser(NAR n, NarseseParser newParser) {

        this.memory = n.memory;
        this.newParser = newParser;
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


//    public static Sentence parseOutput(String s) {
//        Term content = null;
//        char punc = 0;
//        TruthValue truth = null;
//
//        try {
//            StringBuilder buffer = new StringBuilder(s);
//            //String budgetString = getBudgetString(buffer);
//            String truthString = getTruthString(buffer);
//            String str = buffer.toString().trim();
//            int last = str.length() - 1;
//            punc = str.charAt(last);
//            //Stamp stamp = new Stamp(time);
//            truth = parseTruth(truthString, punc);
//
//
//            /*Term content = parseTerm(str.substring(0, last));
//            if (content == null) throw new InvalidInputException("Content term missing");*/
//        }
//        catch (InvalidInputException e) {
//            System.err.println("TextInput.parseOutput: " + s + " : " + e.toString());
//        }
//        return new Sentence(content, punc, truth, null);
//    }


    public Task parseTask(String s) throws InvalidInputException {
        return parseTask(s, true);
    }

    public Task parseTask(String s, boolean newStamp) throws InvalidInputException {
        return newParser.parseTask(s, newStamp);
    }

    public Task parseTaskIfEqualToOldParser(String s) throws InvalidInputException {

        Task u = null, t = null;

        InvalidInputException uError = null;
        try {
            u = parseTaskOld(s, true);
        }
        catch (InvalidInputException tt) {
            uError = tt;
        }


        try {
            t = parseTask(s, true);
            if (t.equals(u))
                return t;
        }
        catch (Throwable e) {
            if (Global.DEBUG)
                System.err.println("Task parse error: " + t + " isnt " + u + ": " + Arrays.toString(e.getStackTrace()));
        }

        if ((u == null) && (t!=null)) return t;
        else {
            if (uError!=null)
                throw uError;
        }

        return u;

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
    public Task parseTaskOld(String s, boolean newStamp) throws InvalidInputException {
        StringBuilder buffer = new StringBuilder(s);

        String budgetString = getBudgetString(buffer);


        Sentence sentence = parseSentenceOld(buffer, newStamp, Stamp.UNPERCEIVED);
        if (sentence == null) return null;

        Budget budget = parseBudget(budgetString, sentence.punctuation, sentence.truth);
        Task task = new Task(sentence, budget);
        return task;

    }

//    public Sentence parseSentence(StringBuilder buffer) {
//        return parseSentence(buffer, true);
//    }
//
//    public Sentence parseSentence(StringBuilder buffer, boolean newStamp) {
//        return parseSentence(buffer, newStamp, Stamp.UNPERCEIVED);
//    }
//


    public Sentence parseSentenceOld(StringBuilder buffer, boolean newStamp, long creationTime) {
        String truthString = getTruthString(buffer);
        Tense tense = parseTense(buffer);
        String str = buffer.toString().trim();
        if (str.isEmpty()) return null;
        int last = str.length() - 1;
        char punc = str.charAt(last);

        /* if -1, will be set right before the Task is input */
        Stamp stamp = NarseseParser.getNewStamp(memory, newStamp, creationTime, tense);

        Truth truth = parseTruth(truthString, punc);
        Term content = parseTerm(str.substring(0, last));
        if (content == null) throw new InvalidInputException("Content term missing");
        if (!(content instanceof Compound)) throw new InvalidInputException("Content term is not compound");

        content = ((Compound) content).cloneNormalized();
        if (content == null) return null;

        return new Sentence((Compound)content, punc, truth, stamp);
        //if ((content instanceof Conjunction) && Variable.containVarDep(content.getName())) {
        //    sentence.setRevisible(false);
        //}

    }

    /* ---------- react values ---------- */
    /**
     * Return the prefix of a task symbol that contains a BudgetValue
     *
     * @param s the addInput in a StringBuilder
     * @return a String containing a BudgetValue
     * @throws nars.io.StringParser.InvalidInputException if the addInput cannot be
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
     * @throws nars.io.StringParser.InvalidInputException if the addInput cannot be
 parsed into a TruthValue
     */
    private static String getTruthString(final StringBuilder s) throws InvalidInputException {
        final int last = s.length() - 1;
        if (last==-1 || s.charAt(last) != TRUTH_VALUE_MARK) {       // use default
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
    private static Truth parseTruth(String s, char type) {
        if ((type == QUESTION) || (type == QUEST)) {
            return null;
        }
        float frequency = 1.0f;
        float confidence = Global.DEFAULT_JUDGMENT_CONFIDENCE;
        if (s != null) {
            int i = s.indexOf(VALUE_SEPARATOR);
            if (i < 0) {
                frequency = parseFloat(s);
            } else {
                frequency = parseFloat(s.substring(0, i));
                confidence = parseFloat(s.substring(i + 1));
            }
        }
        return new Truth(frequency, confidence);
    }

    /**
     * react the addInput String into a BudgetValue
     *
     * @param truth the TruthValue of the task
     * @param s addInput String
     * @param punctuation Task punctuation
     * @return the addInput BudgetValue
     * @throws nars.io.StringParser.InvalidInputException If the String cannot
     * be parsed into a BudgetValue
     */
    private static Budget parseBudget(String s, char punctuation, Truth truth) throws InvalidInputException {
        float priority, durability;
        switch (punctuation) {
            case JUDGMENT:
                priority = Global.DEFAULT_JUDGMENT_PRIORITY;
                durability = Global.DEFAULT_JUDGMENT_DURABILITY;
                break;
            case QUESTION:
                priority = Global.DEFAULT_QUESTION_PRIORITY;
                durability = Global.DEFAULT_QUESTION_DURABILITY;
                break;
            case GOAL:
                priority = Global.DEFAULT_GOAL_PRIORITY;
                durability = Global.DEFAULT_GOAL_DURABILITY;
                break;
            case QUEST:
                priority = Global.DEFAULT_QUEST_PRIORITY;
                durability = Global.DEFAULT_QUEST_DURABILITY;
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
        return new Budget(priority, durability, quality);
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


    public Term parseTerm(String s) throws InvalidInputException {
        return newParser.parseTerm(s);
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
    public Term parseTermOld(String s) throws InvalidInputException {
        s = s.trim();
        
        if (s.length() == 0) return null;
        
        int index = s.length() - 1;
        char first = s.charAt(0);
        char last = s.charAt(index);

        NALOperator opener = getOpener(first);
        if (opener!=null) {
            switch (opener) {
                case COMPOUND_TERM_OPENER:
                    if (last == COMPOUND_TERM_CLOSER.ch) {
                       return parsePossibleCompoundTermTerm(s.substring(1, index));
                    } else {
                        throw new InvalidInputException("missing CompoundTerm closer: " + s);
                    }
                case SET_EXT_OPENER:
                    if (last == SET_EXT_CLOSER.ch) {
                        return SetExt.make(parseArguments(s.substring(1, index) + ARGUMENT_SEPARATOR));
                    } else {
                        throw new InvalidInputException("missing ExtensionSet closer: " + s);
                    }                    
                case SET_INT_OPENER:
                    if (last == SET_INT_CLOSER.ch) {
                        return SetInt.make(parseArguments(s.substring(1, index) + ARGUMENT_SEPARATOR));
                    } else {
                        throw new InvalidInputException("missing IntensionSet closer: " + s);
                    }   
                case STATEMENT_OPENER:
                    if (last == STATEMENT_CLOSER.ch) {
                        return parseStatement(s.substring(1, index));
                    } else {
                        throw new InvalidInputException("missing Statement closer: " + s);
                    }
            }
        }
        else if (Global.FUNCTIONAL_OPERATIONAL_FORMAT) {
            
            //parse functional operation:
            //  function()
            //  function(a)
            //  function(a,b)
            
            //test for existence of matching parentheses at beginning at index!=0
            int pOpen = s.indexOf('(');
            int pClose = s.lastIndexOf(')');
            if ((pOpen!=-1) && (pClose!=-1) && (pClose==s.length()-1)) {
                
                String operatorString = Operator.addPrefixIfMissing( s.substring(0, pOpen) );
                                
                Operator operator = memory.operator(operatorString);
                
                if (operator == null) {
                    //???
                    throw new InvalidInputException("Unknown operate: " + operatorString);
                }
                
                String argString = s.substring(pOpen+1, pClose+1);               
                
                
                Term[] a;                
                if (argString.length() > 1) {                
                    ArrayList<Term> args = parseArguments(argString);                                
                    a = args.toArray(new Term[args.size()]);
                }
                else {
                    //void "()" arguments, default to (SELF)
                    a = Terms.EmptyTermArray;
                }                                                            
                
                Operation o = Operation.make(operator, a, self);
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
     * @throws nars.io.StringParser.InvalidInputException the String cannot be
     * parsed into a Term
     * @return the Term generated from the String
     */
    private Term parseAtomicTerm(String s0) throws InvalidInputException {
        String s = s0.trim();
        if (s.isEmpty()) {
            throw new InvalidInputException("missing term");
        }
        
        
        
        if (s.contains(" ")) { // invalid characters in a name
            throw new InvalidInputException("invalid term");
        }
        
        char c = s.charAt(0);
        if (c == Symbols.INTERVAL_PREFIX) {
            return Interval.interval(s);
        }
        else if (c == '^') {
            return memory.operator(s);
        }
 
        if (containVar(s)) {
            return new Variable(s);
        } else {
            return Atom.get(s);
        }
    }

    /**
     * Parse a String to create a Statement.
     *
     * @return the Statement generated from the String
     * @param s0 The addInput String to be parsed
     * @throws nars.io.StringParser.InvalidInputException the String cannot be
     * parsed into a Term
     */
    private Statement parseStatement(String s0) throws InvalidInputException {
        String s = s0.trim();
        int i = topRelation(s);
        if (i < 0) {
            throw new InvalidInputException("invalid statement: topRelation(s) < 0: " + s0);
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


    public Compound parseCompoundTerm(String s) throws InvalidInputException {
        Term t = parseTerm(s);
        if (t instanceof Compound) return ((Compound)t);
        throw new InvalidInputException(s + " is not a CompoundTerm");
    }

    /**
     * Parse a String to create a CompoundTerm.
     *
     * @return the Term generated from the String
     * @param s0 The String to be parsed
     * @throws nars.io.StringParser.InvalidInputException the String cannot be
     * parsed into a Term
     */
    public Term parsePossibleCompoundTermTerm(final String s0) throws InvalidInputException {
        String s = s0.trim();
        if (s.isEmpty()) {
            throw new InvalidInputException("Empty compound term: " + s);
        }
        int firstSeparator = s.indexOf(ARGUMENT_SEPARATOR);
        if (firstSeparator == -1) {
            throw new InvalidInputException("Invalid compound term (missing ARGUMENT_SEPARATOR): " + s);
        }
                
        String op = (firstSeparator < 0) ? s : s.substring(0, firstSeparator).trim();
        NALOperator oNative = getOperator(op);
        Operator oRegistered = memory.operator(op);
        
        if ((oRegistered==null) && (oNative == null)) {
            throw new InvalidInputException("Unknown operate: " + op);
        }

        ArrayList<Term> arg = (firstSeparator < 0) ? new ArrayList<>(0)
                : parseArguments(s.substring(firstSeparator + 1) + ARGUMENT_SEPARATOR);

        Term[] argA = arg.toArray(new Term[arg.size()]);
        
        Term t;
        
        if (oNative!=null) {
            t = Memory.term(oNative, argA);
        }
        else if (oRegistered!=null) {
            t = Operation.make(oRegistered, argA, self);
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
     * @throws nars.io.StringParser.InvalidInputException the String cannot be
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
        return !s.contains("(") && !s.contains(")") && !s.contains("<") && !s.contains(">");
    }


    public void setSelf(Term arg) {
        this.self = arg;
    }
}
