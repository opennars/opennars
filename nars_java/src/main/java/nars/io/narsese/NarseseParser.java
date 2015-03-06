package nars.io.narsese;

import nars.build.Default;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.Parameters;
import nars.io.Symbols;
import nars.io.Texts;
import nars.logic.NALOperator;
import nars.logic.entity.*;
import nars.logic.entity.stamp.Stamp;
import nars.logic.nal7.Interval;
import nars.logic.nal7.Tense;
import nars.logic.nal8.Operation;
import org.parboiled.BaseParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.parserunners.ParseRunner;
import org.parboiled.parserunners.RecoveringParseRunner;
import org.parboiled.support.MatcherPath;
import org.parboiled.support.ParsingResult;
import org.parboiled.support.Var;

import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

import static nars.logic.NALOperator.*;
import static org.parboiled.support.ParseTreeUtils.printNodeTree;

/**
 * NARese, syntax and language for interacting with a NAR in NARS.
 * https://code.google.com/p/open-nars/wiki/InputOutputFormat
 */
public class NarseseParser extends BaseParser<Object> {

    private final int level;
    ParseRunner inputParser = new RecoveringParseRunner(Input());
    ParseRunner singleTaskParser = new RecoveringParseRunner(Task());
    ParseRunner singleTermParser = new RecoveringParseRunner(Term());

    public Memory memory;

    protected NarseseParser() {
        this(8);
    }

    protected NarseseParser(int level) {
        this.level = level;
    }

    public boolean nal(int n) { return n >= level; }

    public Rule Input() {
        return sequence(s(), zeroOrMore(sequence(Task(), s())), EOI);
    }
    
    public Rule Task() {
        //TODO separate goal into an alternate form "!" because it does not use a tense
        Var<float[]> budget = new Var();
        Var<Character> punc = new Var();
        Var<Term> term = new Var();
        Var<TruthValue> truth = new Var();

        return sequence(
                s(),

                optional(sequence(Budget(), budget.set((float[]) pop()))),

                s(),

                Term(),
                term.set( (Term)pop() ),

                SentenceTypeChar(),
                punc.set( matchedChar() ),

                s(),

                //optional(Tense())

                optional(sequence(Truth(), truth.set((TruthValue) pop()))),

                push(getTask(budget, term, punc, truth))

        );
    }

    Task getTask(Var<float[]> budget, Var<Term> term, Var<Character> punc, Var<TruthValue> truth) {

        char p = punc.get();

        TruthValue t = truth.get();
        if ((t == null) && ((p == Symbols.JUDGMENT) || (p == Symbols.GOAL)))
            t = new TruthValue(p);

        float[] b = budget.get();
        if (b!=null && ((b.length == 0) || (Float.isNaN(b[0]))))
            b = null;
        BudgetValue B = (b == null) ? new BudgetValue(p, t) :
                b.length == 1 ? new BudgetValue(b[0], p, t) : new BudgetValue(b[0], b[1], t);

        Term content = term.get();

        Tense tense = Tense.Eternal; //TODO support different tense

        return new Task(new Sentence(content, p, t, new Stamp(memory, Stamp.UNPERCEIVED, tense)), B);
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


    Rule Budget() { return firstOf(BudgetPriorityDurability(), BudgetPriority()); }

    Rule BudgetPriority() {
        return sequence(Symbols.BUDGET_VALUE_MARK,
                ShortFloat(),
                optional(Symbols.BUDGET_VALUE_MARK),
                push( new float[] { (float)pop() } ) //intermediate representation
        );
    }

    Rule BudgetPriorityDurability() {
        return sequence(Symbols.BUDGET_VALUE_MARK,
                ShortFloat(), Symbols.VALUE_SEPARATOR, ShortFloat(),
                optional(Symbols.BUDGET_VALUE_MARK),
                swap() && push(new float[]{(float) pop(), (float)pop() } ) //intermediate representation
        );
    }

    Rule Truth() {

        return sequence(
                Symbols.TRUTH_VALUE_MARK, ShortFloat(), Symbols.VALUE_SEPARATOR, ShortFloat(),
                optional(Symbols.TRUTH_VALUE_MARK), //tailing '%' is optional
                swap() && push(new TruthValue( (float)pop(), (float)pop() ))
        );
    }
    
    Rule ShortFloat() {
        //TODO use more specific shortfloat number
        return Number();
    }
    
    Rule SentenceTypeChar() {
        return anyOf(".?!@");
    }

    Rule Copula() {
            /*<copula> ::= "-->"                              // inheritance
                        | "<->"                              // similarity
                        | "{--"                              // instance
                        | "--]"                              // property
                        | "{-]"                              // instance-property
                        | "==>"                              // implication
                        | "=/>"                              // (predictive implication)
                        | "=|>"                              // (concurrent implication)
                        | "=\>"                              // (retrospective implication)
                        | "<=>"                              // equivalence
                        | "</>"                              // (predictive equivalence)
                        | "<|>"                              // (concurrent equivalence)*/

        /**
         *   :- (apply, prolog implication)
         *   -: (reverse apply)
         */
        //TODO use separate rules for each so a parse can identify them
        return sequence(
                '<', s(), Term(), s(), CopulaOperator(), s(), Term(), s(), '>',

                push( getTerm( (Term)pop(), (NALOperator)pop(), (Term)pop() ) )
        );
    }


    
    Rule CopulaOperator() {
        NALOperator[] ops = getCopulas();
        Rule[] copulas = new Rule[ops.length];
        for (int i = 0; i < ops.length; i++) {
            copulas[i] = string(ops[i].symbol);
        }
        return sequence(
                firstOf(copulas),
                push(Symbols.getOperator(match()))
        );
    }

    public NALOperator[] getCopulas() {
        switch (level) {
            case 1: return new NALOperator[] {
                    INHERITANCE
            };
            case 2: return new NALOperator[] {
                    INHERITANCE,
                    SIMILARITY, PROPERTY, INSTANCE, INSTANCE_PROPERTY
            };

            //TODO case 5..6.. without temporal equiv &  impl..

            default: return new NALOperator[] {
                    INHERITANCE,
                    SIMILARITY, PROPERTY, INSTANCE, INSTANCE_PROPERTY,
                    IMPLICATION,
                    EQUIVALENCE,
                    IMPLICATION_AFTER, IMPLICATION_BEFORE, IMPLICATION_WHEN,
                    EQUIVALENCE_AFTER, EQUIVALENCE_WHEN
            };
        }
    }

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
                firstOf(
                        Interval(),

                        CompoundTerm(),
                        Copula(),

                        QuotedLiteral(),
                        Atom(),
                        Variable()
                ),
                push(Term.get(pop()))
        );
    }


    /** an atomic term */
    Rule Atom() {
        return sequence(
                oneOrMore(noneOf(" ,.!?^<>-=*|&()<>[]{}#$\"\t")),
                push(match())
        );
    }
    
    Rule QuotedLiteral() {
        return sequence("\"", AnyString(), "\"", push(Texts.escapeLiteral(match())));
    }
    
    Rule AnyString() {
        //TODO handle \" escape
        return oneOrMore(noneOf("\""));
    }


    Rule Interval() {
        return sequence(Symbols.INTERVAL_PREFIX, sequence(oneOrMore(digit()), push(match()),
                push(Interval.interval(-1 + Integer.valueOf((String)pop())))
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
                push(new Variable((String)pop() + (String) pop() ) )
        );
    }
    
    Rule CompoundTerm() {
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
        return firstOf(

                //TODO move to FunctionalOperationTerm() reason
                //Functional form of an Operation, ex: operator(p1,p2)
                sequence(
                        Atom(),
                        push(NALOperator.OPERATION),
                        VectorTerm(NALOperator.COMPOUND_TERM_OPENER, NALOperator.COMPOUND_TERM_CLOSER)
                ),

                VectorTerm(NALOperator.SET_EXT_OPENER, NALOperator.SET_EXT_CLOSER),
                VectorTerm(NALOperator.SET_INT_OPENER, NALOperator.SET_INT_CLOSER),
                VectorTerm(NALOperator.COMPOUND_TERM_OPENER, NALOperator.COMPOUND_TERM_CLOSER), //NAL4



                MultiArgTerm(),
                InfixCompoundTerm()
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
                        NALOperator.IMAGE_INT.symbol,
                        NALOperator.OPERATION.ch
                ),
                push(Symbols.getOperator(match()))
        );
    }
    /** those compound operators which can take 2 arguments (should be everything except negation) */
    Rule CompoundOperator2() {
        return sequence(
                firstOf(
                        //order the smaller ones last, because they are less specific
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

    /** a list of terms enclosed by an opener/closer pair, which may compose a set or product */
    Rule VectorTerm(NALOperator opener, NALOperator closer) {
        return sequence(
            opener.ch, s(),
            push(opener),
            Term(),
            zeroOrMore(

                    //TODO merge this with the similar construct in MultiArgTerm, maybe use a common Rule
                    sequence(
                        s(),
                        optional(sequence(Symbols.ARGUMENT_SEPARATOR, s())),
                        Term()
                    )

            ),
            s(), closer.ch,
            push( nextTermVector() )
        );
    }

    /** list of terms prefixed by a particular compound term operator */
    Rule MultiArgTerm() {
        return sequence(
                NALOperator.COMPOUND_TERM_OPENER.ch,

                s(),

                //special handling to allow (-- x) , without the comma
                //TODO move the (-- x) case to a separate reason to prevent suggesting invalid completions like (-- x y)
                firstOf(
                    sequence( CompoundOperator(), s(), optional(Symbols.ARGUMENT_SEPARATOR)),
                    sequence(NALOperator.NEGATION.symbol, push(NALOperator.NEGATION)),
                    Term()
                ),

                zeroOrMore(
                        sequence(
                                s(),
                                optional(sequence(Symbols.ARGUMENT_SEPARATOR, s())),
                                Term()
                        )
                ),

                s(),

                NALOperator.COMPOUND_TERM_CLOSER.ch,

                push( nextTermVector() )
        );
    }

    /** two or more terms separated by a compound term operator.
     * if > 2 terms, each instance of the infix'd operator must be equal,
     * ex: does not support different operators (a * b & c) */
    Rule InfixCompoundTerm() {
        return sequence(
                NALOperator.COMPOUND_TERM_OPENER.ch,
                s(),
                Term(),

                oneOrMore(
                        sequence(
                                s(),
                                CompoundOperator2(),
                                s(),
                                Term()
                        )
                ),

                s(),

                NALOperator.COMPOUND_TERM_CLOSER.ch,
                push( nextTermVector() )
        );
    }




    /** produce a term from the terms (& <=1 NALOperator's) on the value stack */
    Term nextTermVector() {

        List<Term> vectorterms = Parameters.newArrayList();

        NALOperator op = null;

        //System.err.println(getContext().getValueStack());
        while ( !getContext().getValueStack().isEmpty() ) {
            Object p = pop();

            if (p instanceof String) {
                if (op == OPERATION) {
                    //TODO somehow avoid adding the non-prefixed version of the term to the symbol table, for efficiency
                }

                p = Term.get(p);
            }

            if (p instanceof Term) {
                Term t = (Term) p;
                vectorterms.add(t);
            }
            else if ((p instanceof NALOperator) && (p!=NALOperator.COMPOUND_TERM_OPENER) /* ignore the compound term opener */) {

                if ((op!=null) && (op!=(/*(NALOperator)*/p)))
                    throw new InvalidInputException("CompoundTerm must use only one type of operator; " + p + " contradicts " + op);
                op = (NALOperator)p;
            }
        }

        if (vectorterms.isEmpty()) return null;

        Collections.reverse(vectorterms);

        if ((op == null) || (op == COMPOUND_TERM_OPENER)) {
            //product without '*,' prefix
            op = PRODUCT;
        }

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

    /** whitespace, optional */
    Rule s() {
        return zeroOrMore(anyOf(" \t\f\n"));
    }

    public static NarseseParser newParser(NAR n) {
        return newParser(n.memory);
    }

    public static NarseseParser newParser(Memory m) {
        NarseseParser np = Parboiled.createParser(NarseseParser.class);
        np.memory = m;
        return np;
    }


    /** parse a series of tasks */
    public void parse(String input, Consumer<Task> c) {
        ParsingResult r = inputParser.run(input);
        //r.getValueStack().iterator().forEachRemaining(x -> System.out.println("  " + x.getClass() + " " + x));
        r.getValueStack().iterator().forEachRemaining(x -> {
           if (x instanceof Task)
               c.accept((Task)x);
            else {
               throw new RuntimeException("Unknown parse result: " + x + " (" + x.getClass() + ')');
           }
        });
    }

    /** parse one task */
    public Task parseTask(String input) throws InvalidInputException {
        ParsingResult r = singleTaskParser.run(input);

        Object x = r.getValueStack().iterator().next();
        if (x instanceof Task)
            return (Task)x;

        throw new InvalidInputException(r.parseErrors.toString());
    }

    /** parse one term */
    public <T extends Term> T parseTerm(String input) throws InvalidInputException {
        ParsingResult r = singleTermParser.run(input);

        Object x = r.getValueStack().iterator().next();
        if (x != null) {
            try {
                return (T) x;
            }
            catch (ClassCastException cce) {
                throw new InvalidInputException("Term type mismatch: " + x.getClass(), cce);
            }
        }

        throw new InvalidInputException(r.parseErrors.isEmpty() ? "No result for: " + input : r.parseErrors.toString());
    }



    /** interactive parse test */
    public static void main(String[] args) {
        NAR n = new NAR(new Default());
        NarseseParser p = NarseseParser.newParser(n);

        Scanner sc = new Scanner(System.in);

        String input = null; //"<a ==> b>. %0.00;0.9%";

        while (true) {
            if (input == null)
                input = sc.nextLine();

            ParseRunner rpr = new RecoveringParseRunner(p.Input());
            //TracingParseRunner rpr = new TracingParseRunner(p.Input());

            ParsingResult r = rpr.run(input);

            System.out.println("valid? " + (r.matched && (r.parseErrors.isEmpty())) );
            r.getValueStack().iterator().forEachRemaining(x -> System.out.println("  " + x.getClass() + ' ' + x));

            for (Object e : r.parseErrors) {
                if (e instanceof InvalidInputError) {
                    InvalidInputError iie = (InvalidInputError) e;
                    System.err.println(e);
                    if (iie.getErrorMessage()!=null)
                        System.err.println(iie.getErrorMessage());
                    for (MatcherPath m : iie.getFailedMatchers()) {                        
                        System.err.println("  ?-> " + m);
                    }
                    System.err.println(" at: " + iie.getStartIndex() + " to " + iie.getEndIndex());
                }
                else {
                    System.err.println(e);
                }
                
            }

            System.out.println(printNodeTree(r));

            input = null;
        }
        
    }


}
