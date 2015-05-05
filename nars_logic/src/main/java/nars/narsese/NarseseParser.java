package nars.narsese;

import com.github.fge.grappa.Grappa;
import com.github.fge.grappa.parsers.BaseParser;
import com.github.fge.grappa.rules.Rule;
import com.github.fge.grappa.run.ListeningParseRunner;
import com.github.fge.grappa.run.ParseRunner;
import com.github.fge.grappa.run.ParsingResult;
import com.github.fge.grappa.stack.ValueStack;
import com.github.fge.grappa.support.Var;
import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.budget.Budget;
import nars.Symbols;
import nars.nal.NALOperator;
import nars.nal.Sentence;
import nars.nal.Task;
import nars.nal.Truth;
import nars.nal.nal1.Inheritance;
import nars.nal.nal1.Negation;
import nars.nal.nal7.Interval;
import nars.nal.nal7.Tense;
import nars.nal.nal8.Operation;
import nars.nal.nal8.Operator;
import nars.nal.stamp.Stamp;
import nars.nal.term.Atom;
import nars.nal.term.Compound;
import nars.nal.term.Term;
import nars.nal.term.Variable;
import nars.model.impl.Default;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

import static nars.Symbols.IMAGE_PLACE_HOLDER;
import static nars.nal.NALOperator.*;

/**
 * NARese, syntax and language for interacting with a NAR in NARS.
 * https://code.google.com/p/open-nars/wiki/InputOutputFormat
 */
public class NarseseParser extends BaseParser<Object> {

    private final int level;

    //These should be set to something like RecoveringParseRunner for performance
    public final ParseRunner inputParser = new ListeningParseRunner(Input());
    public final ParseRunner singleTaskParser = new ListeningParseRunner(Task(true));

    //use a parameter or something to avoid this extra instance
    @Deprecated final ParseRunner singleTaskParserNonNewStamp = new ListeningParseRunner(Task(false));

    public final ParseRunner singleTermParser = new ListeningParseRunner(Term()); //new ErrorReportingParseRunner(Term(), 0);

    public Memory memory;

    protected NarseseParser() {
        this(8);
    }

    protected NarseseParser(int minNALLevel) {
        this.level = minNALLevel;
    }

    public boolean nal(int n) {
        return n >= level;
    }

    public Rule Input() {
        return sequence(s(), zeroOrMore(sequence(firstOf(Comment(), Task(true)), s())), EOI);
    }

    public Rule Comment() {
        return sequence("//", zeroOrMore(noneOf("\n")), push(match()));
    }

    public Rule Task(boolean newStamp) {
        //TODO separate goal into an alternate form "!" because it does not use a tense
        Var<float[]> budget = new Var();
        Var<Character> punc = new Var();
        Var<Term> term = new Var();
        Var<Truth> truth = new Var();
        Var<Tense> tense = new Var(Tense.Eternal);

        return sequence(
                s(),

                optional(sequence(Budget(), budget.set((float[]) pop()))),



                Term(),

                term.set((Term) pop()),



                SentenceTypeChar(),
                punc.set(matchedChar()),


                s(),

                optional(
                        sequence(Tense(), s(), tense.set((Tense)pop()))
                ),

                optional(sequence(
                        firstOf(Truth(), TruthFreqOnly()),
                        truth.set((Truth) pop())
                        )
                ),

                push(getTask(budget, term, punc, truth, tense, newStamp))

        );
    }

    Task getTask(Var<float[]> budget, Var<Term> term, Var<Character> punc, Var<Truth> truth, Var<Tense> tense, boolean newStamp) {

        char p = punc.get();

        Truth t = truth.get();
        if ((t == null) && ((p == Symbols.JUDGMENT) || (p == Symbols.GOAL)))
            t = new Truth(p);

        float[] b = budget.get();
        if (b != null && ((b.length == 0) || (Float.isNaN(b[0]))))
            b = null;
        Budget B = (b == null) ? new Budget(p, t) :
                b.length == 1 ? new Budget(b[0], p, t) :
                        b.length == 2 ? new Budget(b[0], b[1], t) :
                                new Budget(b[0], b[1], b[2]);

        Term content = term.get();
        if (!(content instanceof Compound)) {
            return null;
        }

        content = Sentence.termOrNull(content);
        if (content==null) return null;

        Tense te = tense.get();

        return new Task(new Sentence((Compound)content, p, t,
                getNewStamp(memory, newStamp, Stamp.UNPERCEIVED, te),
                false), B );

    }



//    Rule Operation() {
//        //TODO
//        // "(^"<word> {","<term>} ")"         // (an operation to be executed)   */
//        return sequence(
//                NALOperator.COMPOUND_TERM_OPENER.ch,
//                s(),
//                NALOperator.OPERATION,
//                s(),
//                Literal(),
//                )
//    }


    Rule Budget() {
        return firstOf(BudgetPriorityDurabilityQuality(), BudgetPriorityDurability(), BudgetPriority());
    }

    Rule BudgetPriority() {
        return sequence(Symbols.BUDGET_VALUE_MARK,
                ShortFloat(),
                optional(Symbols.BUDGET_VALUE_MARK),
                push(new float[]{(float) pop()}) //intermediate representation
        );
    }

    Rule BudgetPriorityDurability() {
        return sequence(Symbols.BUDGET_VALUE_MARK,
                ShortFloat(), Symbols.VALUE_SEPARATOR, ShortFloat(),
                optional(Symbols.BUDGET_VALUE_MARK),
                swap() && push(new float[]{(float) pop(), (float) pop()}) //intermediate representation
        );
    }

    Rule BudgetPriorityDurabilityQuality() {
        return sequence(Symbols.BUDGET_VALUE_MARK,
                ShortFloat(), Symbols.VALUE_SEPARATOR, ShortFloat(), Symbols.VALUE_SEPARATOR, ShortFloat(),
                optional(Symbols.BUDGET_VALUE_MARK),
                swap() && push(new float[]{(float) pop(), (float) pop(), (float) pop()}) //intermediate representation
        );
    }

    Rule Tense() {
        return firstOf(
            sequence(Symbols.TENSE_PRESENT, push(Tense.Present)),
            sequence(Symbols.TENSE_PAST, push(Tense.Past)),
            sequence(Symbols.TENSE_FUTURE, push(Tense.Future))
        );
    }

    Rule Truth() {

        return sequence(
                Symbols.TRUTH_VALUE_MARK, ShortFloat(), Symbols.VALUE_SEPARATOR, ShortFloat(),
                optional(Symbols.TRUTH_VALUE_MARK), //tailing '%' is optional
                swap() && push(new Truth((float) pop(), (float) pop()))
        );
    }
    Rule TruthFreqOnly() {

        return sequence(
                Symbols.TRUTH_VALUE_MARK, ShortFloat(),
                optional(Symbols.TRUTH_VALUE_MARK), //tailing '%' is optional
                push(new Truth((float) pop(), Global.DEFAULT_JUDGMENT_CONFIDENCE))
        );
    }

    Rule ShortFloat() {
        //TODO use more specific shortfloat number
        return Number();
    }

    Rule SentenceTypeChar() {
        return anyOf(".?!@");
    }

//    /**
//     * copula, statement, relation
//     */
//    Rule Copula() {
//            /*<copula> ::= "-->"                              // inheritance
//                        | "<->"                              // similarity
//                        | "{--"                              // instance
//                        | "--]"                              // property
//                        | "{-]"                              // instance-property
//                        | "==>"                              // implication
//                        | "=/>"                              // (predictive implication)
//                        | "=|>"                              // (concurrent implication)
//                        | "=\>"                              // (retrospective implication)
//                        | "<=>"                              // equivalence
//                        | "</>"                              // (predictive equivalence)
//                        | "<|>"                              // (concurrent equivalence)*/
//
//        /**
//         * ??
//         *   :- (apply, prolog implication)
//         *   -: (reverse apply)
//         */
//        //TODO use separate rules for each so a parse can identify them
//        return sequence(String.valueOf(NALOperator.STATEMENT_OPENER), StatementContent(), String.valueOf(NALOperator.STATEMENT_CLOSER));
//    }


//    Rule StatementContent() {
//        return sequence(sequence(s(), Term(), s(), CopulaOperator(), s(), Term(), s()),
//                push(getTerm((Term) pop(), (NALOperator) pop(), (Term) pop()))
//                //push(nextTermVector()) //((Term) pop(), (NALOperator) pop(), (Term) pop()))
//        );
//    }

//    Rule CopulaOperator() {
//        NALOperator[] ops = getCopulas();
//        Rule[] copulas = new Rule[ops.length];
//        for (int i = 0; i < ops.length; i++) {
//            copulas[i] = string(ops[i].symbol);
//        }
//        return sequence(
//                firstOf(copulas),
//                push(Symbols.getOperator(match()))
//        );
//    }

//    public NALOperator[] getCopulas() {
//        switch (level) {
//            case 1:
//                return new NALOperator[]{
//                        INHERITANCE
//                };
//            case 2:
//                return new NALOperator[]{
//                        INHERITANCE,
//                        SIMILARITY, PROPERTY, INSTANCE, INSTANCE_PROPERTY
//                };
//
//            //TODO case 5..6.. without temporal equiv &  impl..
//
//            default:
//                return new NALOperator[]{
//                        INHERITANCE,
//                        SIMILARITY, PROPERTY, INSTANCE, INSTANCE_PROPERTY,
//                        IMPLICATION,
//                        EQUIVALENCE,
//                        IMPLICATION_AFTER, IMPLICATION_BEFORE, IMPLICATION_WHEN,
//                        EQUIVALENCE_AFTER, EQUIVALENCE_WHEN
//                };
//        }
//    }

    static Term getTerm(Term predicate, NALOperator op, Term subject) {
        return Memory.term(op, subject, predicate);
    }

    Rule Term() {
        /*
                 <term> ::= <word>                             // an atomic constant term
                        | <variable>                         // an atomic variable term
                        | <compound-term>                    // a term with internal structure
                        | <statement>                        // a statement can serve as a term
        */

        return sequence(
                s(),
                firstOf(
                        Interval(),
                        Variable(),
                        QuotedLiteral(),
                        ImageIndex(),

                        //Copula(),

                        //negation shorthand
                        sequence(NALOperator.NEGATION.symbol, s(), Term(), push(Negation.make(term(pop())))),


                        MultiArgTerm(NALOperator.SET_EXT_OPENER, NALOperator.SET_EXT_CLOSER, false, false, false),
                        MultiArgTerm(NALOperator.SET_INT_OPENER, NALOperator.SET_INT_CLOSER, false, false, false),

                        sequence(
                                Atom(),
                                EmptyOperationParens()
                        ),

                        //Functional form of an Operation, ex: operate(p1,p2), TODO move to FunctionalOperationTerm() rule
                        sequence(
                                Atom(),
                                MultiArgTerm(NALOperator.OPERATION, NALOperator.COMPOUND_TERM_OPENER, NALOperator.COMPOUND_TERM_CLOSER, false, false, false, true)
                        ),





                        MultiArgTerm(null, NALOperator.COMPOUND_TERM_OPENER, NALOperator.COMPOUND_TERM_CLOSER, true, false, false, false),


                        //default to product if no operator specified in ( )
                        MultiArgTerm(NALOperator.PRODUCT, NALOperator.COMPOUND_TERM_OPENER, NALOperator.COMPOUND_TERM_CLOSER, false, false, false, false),

                        MultiArgTerm(null, NALOperator.STATEMENT_OPENER, NALOperator.STATEMENT_CLOSER, false, true, true, false),
                        MultiArgTerm(null, NALOperator.COMPOUND_TERM_OPENER, NALOperator.COMPOUND_TERM_CLOSER, false, true, true, false),

                        NamespacedAtom(),
                        Atom()

                ),

                push(term(pop())),

                s()
        );
    }

//    Term[] nextTerms() {
//        //pop a list of terms, fail if not all are terms
//        List<Term> vectorterms = Global.newArrayList();
//
//        while (!getContext().getValueStack().isEmpty()) {
//            Object o = pop();
//            if (!(o instanceof Term)) throw new RuntimeException(o + " (" + o.getClass().getSimpleName() + ") is not a Term for in nextTerms()");
//            vectorterms.add((Term) o);
//        }
//        return vectorterms.toArray(new Term[vectorterms.size()]);
//    }

//    Rule InnerCompound() {
//        //special handling to allow (-- x) , without the comma
//        //TODO move the (-- x) case to a separate rule to prevent suggesting invalid completions like (-- x y)
//        return firstOf(
//                CompoundOperator(),
//                push(NALOperator.PRODUCT) //DEFAULT
//                //Term()
//        );
//    }


    /**
     * an atomic term, returns a String because the result may be used as a Variable name
     */
    Rule Atom() {
        return sequence(
                oneOrMore(noneOf(" ,.!?" + Symbols.INTERVAL_PREFIX + "<>-=*|&()<>[]{}%#$@\'\"\t\n")),
                push(match())
        );
    }

    /**
     * MACRO: namespace.x    becomes    <x --> namespace>
     */
    Rule NamespacedAtom() {
        return sequence(Atom(), '.', Atom(), push(Inheritance.make(Atom.get(pop()), Atom.get(pop()))));
    }

    public static Stamp getNewStamp(Memory memory, boolean newStamp, long creationTime, Tense tense) {
        return new Stamp(
                newStamp ? new long[] { memory.newStampSerial() } : new long[] { /* blank */ },
                memory, creationTime, tense);
    }

    /** creates a parser that is not associated with a memory; it will not parse any operator terms (which are registered with a Memory instance) */
    public static NarseseParser newParser() {
        return newParser((Memory)null);
    }


    final static Atom imageIndexTerm = Atom.get(String.valueOf(IMAGE_PLACE_HOLDER));

    Rule ImageIndex() {
        return sequence("_", push(imageIndexTerm));
    }

    Rule QuotedLiteral() {
        return sequence("\"", AnyString(),
                   push("\"" + match() + "\""), "\"");
    }

    Rule AnyString() {
        //TODO handle \" escape
        return oneOrMore(noneOf("\""));
    }


    Rule Interval() {
        return sequence(Symbols.INTERVAL_PREFIX, sequence(oneOrMore(digit()), push(match()),
                push(Interval.interval(-1 + Integer.valueOf((String) pop())))
        ));
    }

    Rule Variable() {
        /*
           <variable> ::= "$"<word>                          // independent variable
                        | "#"[<word>]                        // dependent variable
                        | "?"[<word>]                        // query variable in question
        */
        return sequence(
                firstOf(Symbols.VAR_INDEPENDENT, Symbols.VAR_DEPENDENT, Symbols.VAR_QUERY),
                push(match()), Atom(), swap(),
                push(new Variable((String) pop() + (String) pop()))
        );
    }

    //Rule CompoundTerm() {
        /*
         <compound-term> ::= "{" <term> {","<term>} "}"         // extensional set
                        | "[" <term> {","<term>} "]"         // intensional set
                        | "(&," <term> {","<term>} ")"       // extensional intersection
                        | "(|," <term> {","<term>} ")"       // intensional intersection
                        | "(*," <term> {","<term>} ")"       // product
                        | "(/," <term> {","<term>} ")"       // extensional image
                        | "(\," <term> {","<term>} ")"       // intensional image
                        | "(||," <term> {","<term>} ")"      // disjunction
                        | "(&&," <term> {","<term>} ")"      // conjunction
                        | "(&/," <term> {","<term>} ")"      // (sequential events)
                        | "(&|," <term> {","<term>} ")"      // (parallel events)
                        | "(--," <term> ")"                  // negation
                        | "(-," <term> "," <term> ")"        // extensional difference
                        | "(~," <term> "," <term> ")"        // intensional difference
        
        */

    //}

    Rule AnyOperator() {
        return sequence(firstOf(


                        INHERITANCE.symbol,


                        SIMILARITY.symbol,

                        PROPERTY.symbol,
                        INSTANCE.symbol,
                        INSTANCE_PROPERTY.symbol,

                        NEGATION.symbol,

                        IMPLICATION.symbol,
                        EQUIVALENCE.symbol,
                        IMPLICATION_AFTER.symbol, IMPLICATION_BEFORE.symbol, IMPLICATION_WHEN.symbol,
                        EQUIVALENCE_AFTER.symbol, EQUIVALENCE_WHEN.symbol,
                        DISJUNCTION.symbol,
                        CONJUNCTION.symbol,
                        SEQUENCE.symbol,
                        PARALLEL.symbol,

                        anyOf(
                                INTERSECTION_EXT.symbol +
                                INTERSECTION_INT.symbol +
                                DIFFERENCE_EXT.symbol +
                                DIFFERENCE_INT.symbol +  PRODUCT.symbol + IMAGE_EXT.symbol + IMAGE_INT.symbol
                        )


                        //OPERATION.ch
                ),
                push(Symbols.getOperator(match()))
        );
    }

    Rule CompoundOperator() {
        return sequence(
                firstOf(
                        NALOperator.NEGATION.symbol,
                        NALOperator.DISJUNCTION.symbol,
                        NALOperator.CONJUNCTION.symbol,
                        NALOperator.SEQUENCE.symbol,
                        NALOperator.PARALLEL.symbol,
                        NALOperator.DIFFERENCE_EXT.symbol,
                        NALOperator.DIFFERENCE_INT.symbol,
                        NALOperator.INTERSECTION_EXT.symbol,
                        NALOperator.INTERSECTION_INT.symbol,
                        NALOperator.PRODUCT.symbol,
                        NALOperator.IMAGE_EXT.symbol,
                        NALOperator.IMAGE_INT.symbol
                        //NALOperator.OPERATION.ch
                ),
                push(Symbols.getOperator(match()))
        );
    }

    /**
     * those compound operators which can take 2 arguments (should be everything except negation)
     */
    Rule CompoundOperator2() {
        return sequence(
                firstOf(
                        NALOperator.DISJUNCTION.symbol,
                        NALOperator.CONJUNCTION.symbol,
                        NALOperator.SEQUENCE.symbol,
                        NALOperator.PARALLEL.symbol,
                        NALOperator.DIFFERENCE_EXT.symbol,
                        NALOperator.DIFFERENCE_INT.symbol,
                        NALOperator.INTERSECTION_EXT.symbol,
                        NALOperator.INTERSECTION_INT.symbol,
                        NALOperator.PRODUCT.symbol,
                        NALOperator.IMAGE_EXT.symbol,
                        NALOperator.IMAGE_INT.symbol
                ),
                push(Symbols.getOperator(match()))
        );
    }


    Rule ArgSep() {
        return sequence(s(), String.valueOf(Symbols.ARGUMENT_SEPARATOR), s());

        /*
        return firstOf(
                //check the ' , ' comma separated first, it is more complex
                sequence(s(), String.valueOf(Symbols.ARGUMENT_SEPARATOR), s()),


                //then allow plain whitespace to function as a term separator?
                s()
        );*/
    }

    Rule MultiArgTerm(NALOperator open, NALOperator close, boolean allowInitialOp, boolean allowInternalOp, boolean allowSpaceToSeparate) {
        return MultiArgTerm(open, open, close, allowInitialOp, allowInternalOp, allowSpaceToSeparate, false);
    }

    boolean OperationPrefixTerm() {
        return push( new Object[] { term("^" + pop()), (Compound.class) } );
    }

    /**
     * list of terms prefixed by a particular compound term operate
     */
    Rule MultiArgTerm(NALOperator defaultOp, NALOperator open, NALOperator close, boolean initialOp, boolean allowInternalOp, boolean spaceSeparates, boolean operatorPrecedes) {


        return sequence(

                operatorPrecedes ?  OperationPrefixTerm() : push(Compound.class),

                open != null ? sequence(open.ch, s()) : s(),

                initialOp ? AnyOperator() : Term(),

                spaceSeparates ?

                        sequence( s(), AnyOperator(), s(), Term() )

                        :

                        zeroOrMore(sequence(
                            spaceSeparates ? s() : ArgSep(),
                            allowInternalOp ? AnyOperatorOrTerm() : Term()
                        )),

                close != null ? sequence(s(), close.ch) : s(),

                push(nextTermVector(defaultOp, allowInternalOp))
        );
    }

    /**
     * operation()
     */
    Rule EmptyOperationParens() {
        return sequence(

                OperationPrefixTerm(),

                s(), NALOperator.COMPOUND_TERM_OPENER.ch, s(), NALOperator.COMPOUND_TERM_CLOSER.ch,

                push(nextTermVector(NALOperator.OPERATION, false))
        );
    }

    Rule AnyOperatorOrTerm() {
        return firstOf(AnyOperator(), Term());
    }

//    /** term without outer parenthes, only applicable when top-level in a task. */
//    Rule ExposedTerm() {
//        return MultiArgTerm(null, null, InnerCompound());
//    }

//    /** two or more terms separated by a compound term operate.
//     * if > 2 terms, each instance of the infix'd operate must be equal,
//     * ex: does not support different operators (a * b & c) */
//    Rule InfixCompoundTerm() {
//        return sequence(
//                NALOperator.COMPOUND_TERM_OPENER.ch,
//                s(),
//                Term(),
//
//                oneOrMore(
//                        sequence(
//                                s(),
//                                CompoundOperator2(),
//                                s(),
//                                Term()
//                        )
//                ),
//
//                s(),
//
//                NALOperator.COMPOUND_TERM_CLOSER.ch,
//                push( nextTermVector() )
//        );
//    }


    Term term(Object o) {
        if (o instanceof Term) return ((Term)o);
        if (o instanceof String) {
            String s= (String)o;
            if (s.charAt(0) == NALOperator.OPERATION.ch) {
                if (memory!=null)
                    return memory.operator(s);
                return null;
            } else {
                return Atom.get(s);
            }
        }
        throw new RuntimeException(o + " is not a term");
    }


    /**
     * produce a term from the terms (& <=1 NALOperator's) on the value stack
     */
    Term nextTermVector(NALOperator op /*default */, boolean allowInternalOp) {

        List<Term> vectorterms = Global.newArrayList();


        //System.err.println(getContext().getValueStack());

        ValueStack<Object> stack = getContext().getValueStack();
        while (!stack.isEmpty()) {
            Object p = pop();

            if (p instanceof Object[]) {
                //it's an array so unpack by pushing everything back onto the stack except the last item which will be used as normal below
                Object[] pp = (Object[])p;
                if (pp.length > 1) {
                    for (int i = pp.length-1; i >= 1; i--) {
                    //for (int i = 0; i < pp.length-1; i++) {
                        stack.push(pp[i]);
                    }
                }

                p = pp[0];
            }

            if (p == Compound.class) break; //beginning of stack frame for this term




            if (p instanceof Term) {
                Term t = (Term) p;
                vectorterms.add(t);
            } else if (p instanceof NALOperator) {

                if (op != null) {
                    if ((!allowInternalOp) && (!p.equals(op)))
                        throw new RuntimeException("Internal operator " + p + " not allowed here; default op=" + op);

                    throw new InvalidInputException("Too many operators involved: " + op + "," + p + " in " + stack + ":" + vectorterms);
                }

                op = (NALOperator)p;
            }
        }


        if (vectorterms.isEmpty()) return null;

//        if ((vectorterms.size() == 1) && (op == null))
//            return vectorterms.get(0);

        int v = vectorterms.size();


        //System.err.println("  " + (negated ? "--" : "") + op + vectorterms);

        Collections.reverse(vectorterms);

        //System.out.println(vectorterms);

        if ((op == null || op == PRODUCT) && (vectorterms.get(0) instanceof Operator)) {
            op = NALOperator.OPERATION;
        }

        if (op == null) op = NALOperator.PRODUCT;

        Term[] va = vectorterms.toArray(new Term[vectorterms.size()]);

        if (op == OPERATION)
            return Operation.make(memory, va);

        return Memory.term(op, va);
    }

    Rule Number() {
        return sequence(
                sequence(
                        optional('-'),
                        oneOrMore(digit()),
                        optional('.', oneOrMore(digit()))
                ),
                push(Float.parseFloat(matchOrDefault("NaN")))
        );
    }

    /**
     * whitespace, optional
     */
    Rule s() {
        return zeroOrMore(anyOf(" \t\f\n"));
    }

    public static NarseseParser newParser(NAR n) {
        return newParser(n.memory);
    }

    public static NarseseParser newParser(Memory m) {
        NarseseParser np = Grappa.createParser(NarseseParser.class);
        np.memory = m;
        return np;
    }


    /**
     * parse a series of tasks
     */
    public synchronized void parse(String input, Consumer<Task> c) {
        ParsingResult r = inputParser.run(input);
        //r.getValueStack().iterator().forEachRemaining(x -> System.out.println("  " + x.getClass() + " " + x));
        r.getValueStack().iterator().forEachRemaining(x -> {
            if (x instanceof Task)
                c.accept((Task) x);
            else {
                throw new RuntimeException("Unknown parse result: " + x + " (" + x.getClass() + ')');
            }
        });
    }

    /**
     * parse one task
     */
    public synchronized Task parseTask(String input, boolean newStamp) throws InvalidInputException {
        ParsingResult r = null;
        try {
            input = input.trim();
            if (newStamp)
                r = singleTaskParser.run(input);
            else
                r = singleTaskParserNonNewStamp.run(input);
        }
        catch (Throwable ge) {
            throw new InvalidInputException(ge.toString() + " " + ge.getCause() + ": parsing: " + input);
        }

        if (r == null)
            throw new InvalidInputException("null parse: " + input);

        Iterator ir = r.getValueStack().iterator();
        if (ir.hasNext()) {
            Object x = ir.next();
            if (x instanceof Task)
                return (Task) x;
        }

        throw newParseException(input, r);
    }

    /**
     * parse one term
     */
    public synchronized <T extends Term> T parseTerm(String input) throws InvalidInputException {
        ParsingResult r = singleTermParser.run(input);


        if (!r.getValueStack().isEmpty()) {

            Object x = r.getValueStack().iterator().next();
            if (x != null) {
                try {
                    return (T) x;
                } catch (ClassCastException cce) {
                    throw new InvalidInputException("Term type mismatch: " + x.getClass(), cce);
                }
            }
        }

        throw newParseException(input, r);
    }

    public static InvalidInputException newParseException(String input, ParsingResult r) {

        //if (!r.isSuccess()) {
            return new InvalidInputException("input: " + input + " (" + r.toString() + ')');
        //}
//        if (r.parseErrors.isEmpty())
//            return new InvalidInputException("No parse result for: " + input);
//
//        String all = "\n";
//        for (Object o : r.getParseErrors()) {
//            ParseError pe = (ParseError)o;
//            all += pe.getClass().getSimpleName() + ": " + pe.getErrorMessage() + " @ " + pe.getStartIndex() + "\n";
//        }
//        return new InvalidInputException(all + " for input: " + input);
    }


    /**
     * interactive parse test
     */
    public static void main(String[] args) {
        NAR n = new NAR(new Default());
        NarseseParser p = NarseseParser.newParser(n);

        Scanner sc = new Scanner(System.in);

        String input = null; //"<a ==> b>. %0.00;0.9%";

        while (true) {
            if (input == null)
                input = sc.nextLine();

            ParseRunner rpr = new ListeningParseRunner<>(p.Input());
            //TracingParseRunner rpr = new TracingParseRunner(p.Input());

            ParsingResult r = rpr.run(input);

            //p.printDebugResultInfo(r);
            input = null;
        }

    }

//    public void printDebugResultInfo(ParsingResult r) {
//
//        System.out.println("valid? " + (r.isSuccess() && (r.getParseErrors().isEmpty())));
//        r.getValueStack().iterator().forEachRemaining(x -> System.out.println("  " + x.getClass() + ' ' + x));
//
//        for (Object e : r.getParseErrors()) {
//            if (e instanceof InvalidInputError) {
//                InvalidInputError iie = (InvalidInputError) e;
//                System.err.println(e);
//                if (iie.getErrorMessage() != null)
//                    System.err.println(iie.getErrorMessage());
//                for (MatcherPath m : iie.getFailedMatchers()) {
//                    System.err.println("  ?-> " + m);
//                }
//                System.err.println(" at: " + iie.getStartIndex() + " to " + iie.getEndIndex());
//            } else {
//                System.err.println(e);
//            }
//
//        }
//
//        System.out.println(printNodeTree(r));
//
//    }


}
