package nars.io.narsese;

import nars.core.Memory;
import nars.io.Symbols;
import nars.io.Texts;
import nars.logic.entity.*;
import nars.logic.nal3.SetExt;
import nars.logic.nal3.SetInt;
import nars.logic.nal4.Product;
import org.parboiled.*;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.parserunners.RecoveringParseRunner;
import org.parboiled.support.MatcherPath;
import org.parboiled.support.ParsingResult;
import org.parboiled.support.Var;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

import static nars.io.Symbols.NativeOperator;
import static nars.io.Symbols.NativeOperator.*;
import static org.parboiled.support.ParseTreeUtils.printNodeTree;

/**
 * NARese, syntax and language for interacting with a NAR in NARS.
 * https://code.google.com/p/open-nars/wiki/InputOutputFormat
 */
public class NarseseParser extends BaseParser<Object> {

    private final int level;
    RecoveringParseRunner rpr = new RecoveringParseRunner(Input());

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

    static Task getTask(Var<float[]> budget, Var<Term> term, Var<Character> punc, Var<TruthValue> truth) {

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

        return new Task(new Sentence(content, p, t, null), B);
    }


    Rule OperationExecution() {
        //TODO
        // "(^"<word> {","<term>} ")"         // (an operation to be executed)   */
        return string("(^)");
    }


    Rule Budget() { return firstOf(BudgetPriorityDurability(), BudgetPriority()); }

    Rule BudgetPriority() {
        return sequence('$',
                ShortFloat(),
                optional('$'),
                push( new float[] { (float)pop() } ) //intermediate representation
        );
    }

    Rule BudgetPriorityDurability() {
        return sequence("$",
                ShortFloat(), ";", ShortFloat(),
                optional("$"),
                swap() && push(new float[]{(float) pop(), (float)pop() } ) //intermediate representation
        );
    }

    Rule Truth() {

        return sequence(
                '%', ShortFloat(), ';', ShortFloat(),
                optional('%'), //tailing '%' is optional
                swap() && push(new TruthValue( (float)pop(), (float)pop() ))
        );
    }
    
    Rule ShortFloat() {
        //TODO use more specific shortfloat number
        return Number();
    }
    
    Rule SentenceTypeChar() {
        return anyOf(".?!");
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
                "<", s(), Term(), s(), CopulaOperator(), s(), Term(), s(),">",

                push( getTerm( (Term)pop(), (NativeOperator)pop(), (Term)pop() ) )
        );
    }


    
    Rule CopulaOperator() {
        NativeOperator[] ops = getCopulas();
        Rule[] copulas = new Rule[ops.length];
        for (int i = 0; i < ops.length; i++) {
            copulas[i] = string(ops[i].symbol);
        }
        return sequence(
                firstOf(copulas),
                push(Symbols.getOperator(match()))
        );
    }

    public NativeOperator[] getCopulas() {
        switch (level) {
            case 1: return new NativeOperator[] {
                    INHERITANCE
            };
            case 2: return new NativeOperator[] {
                    INHERITANCE,
                    SIMILARITY, PROPERTY, INSTANCE, INSTANCE_PROPERTY
            };

            //TODO case 5..6.. without temporal equiv &  impl..

            default: return new NativeOperator[] {
                    INHERITANCE,
                    SIMILARITY, PROPERTY, INSTANCE, INSTANCE_PROPERTY,
                    IMPLICATION,
                    EQUIVALENCE,
                    IMPLICATION_AFTER, IMPLICATION_BEFORE, IMPLICATION_WHEN,
                    EQUIVALENCE_AFTER, EQUIVALENCE_WHEN
            };
        }
    }

    static Term getTerm(Term predicate, NativeOperator op, Term subject) {
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
                        QuotedLiteral(),
                        Literal(),
                        Variable(),
                        CompoundTerm(),
                        Copula(),
                        nTimes(nal(8) ? 1 : 0, OperationExecution()
                        )
                ),
                push(Term.get(pop()))
        );
    }

       
    Rule Literal() {
        return sequence(
                oneOrMore(noneOf(" ,.!?<>-=|&()<>[]{}#$\"")),
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
            
    
    Rule Variable() {
        /*
           <variable> ::= "$"<word>                          // independent variable
                        | "#"[<word>]                        // dependent variable
                        | "?"[<word>]                        // query variable in question
        */
        return firstOf(IndependentVariable(), DependentVariable(), QueryVariable());
    }
    
    Rule IndependentVariable() { return sequence("$", Literal());     }
    Rule DependentVariable() { return sequence("#", optional(Literal())); }
    Rule QueryVariable() { return sequence("?", optional(Literal())); }
    
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
        //return sequence("{", Term(), zeroOrMore(sequence(",",Term())),"}");
        return firstOf(
                VectorTerm(NativeOperator.SET_EXT_OPENER, NativeOperator.SET_EXT_CLOSER),
                VectorTerm(NativeOperator.SET_INT_OPENER, NativeOperator.SET_INT_CLOSER),
                VectorTerm(NativeOperator.COMPOUND_TERM_CLOSER, NativeOperator.COMPOUND_TERM_CLOSER), //NAL4
                Infix2Term(),
                MultiArgTerm()
        );
    }

    Rule CompoundOperator() {
        return sequence(
                firstOf(
                        NativeOperator.INTERSECTION_EXT.symbol,
                        NativeOperator.INTERSECTION_INT.symbol,
                        NativeOperator.PRODUCT.symbol,
                        NativeOperator.IMAGE_EXT.symbol,
                        NativeOperator.IMAGE_INT.symbol,
                        NativeOperator.DISJUNCTION.symbol,
                        NativeOperator.CONJUNCTION.symbol,
                        NativeOperator.SEQUENCE.symbol,
                        NativeOperator.PARALLEL.symbol,
                        NativeOperator.NEGATION.symbol,
                        NativeOperator.DIFFERENCE_EXT.symbol,
                        NativeOperator.DIFFERENCE_INT.symbol
                ),
                push(Symbols.getOperator(match()))
        );
    }
    /** those compound operators which can take 2 arguments */
    Rule CompoundOperator2() {
        return sequence(
                firstOf(
                        NativeOperator.INTERSECTION_EXT.symbol,
                        NativeOperator.INTERSECTION_INT.symbol,
                        NativeOperator.PRODUCT.symbol,
                        NativeOperator.IMAGE_EXT.symbol,
                        NativeOperator.IMAGE_INT.symbol,
                        NativeOperator.DISJUNCTION.symbol,
                        NativeOperator.CONJUNCTION.symbol,
                        NativeOperator.SEQUENCE.symbol,
                        NativeOperator.PARALLEL.symbol,
                        NativeOperator.DIFFERENCE_EXT.symbol,
                        NativeOperator.DIFFERENCE_INT.symbol
                ),
                push(Symbols.getOperator(match()))
        );
    }

    /** a list of terms enclosed by an opener/closer pair, which may compose a set or product */
    Rule VectorTerm(NativeOperator opener, NativeOperator closer) {
        return sequence(
            opener.ch, s(),
            push(opener),
            Term(), zeroOrMore(sequence(Symbols.ARGUMENT_SEPARATOR, s(), Term())),
            s(), closer.ch,
            push( nextTermVector() )
        );
    }

    /** list of terms prefixed by a particular compound term operator */
    Rule MultiArgTerm() {
        return sequence(
                NativeOperator.COMPOUND_TERM_OPENER.ch,

                s(),

                //special handling to allow (-- x) , without the comma
                //TODO move the (-- x) case to a separate rule to prevent suggesting invalid completions like (-- x y)
                firstOf(
                    sequence(CompoundOperator(), s(), Symbols.ARGUMENT_SEPARATOR),
                    sequence( NativeOperator.NEGATION.symbol, push(NativeOperator.NEGATION))
                ),

                s(),

                Term(), zeroOrMore(sequence(Symbols.ARGUMENT_SEPARATOR,Term())),

                s(),

                NativeOperator.COMPOUND_TERM_CLOSER.ch,
                push( nextTermVector() )
        );
    }

    /** two terms separated by a compound term operator which supports exactly 2 arguments */
    Rule Infix2Term() {
        return sequence(
                NativeOperator.COMPOUND_TERM_OPENER.ch,
                s(),
                Term(),
                s(),
                CompoundOperator2(),
                s(),
                Term(),
                s(),
                NativeOperator.COMPOUND_TERM_CLOSER.ch,
                swap(2),
                push( nextTermVector() )
        );
    }

    List<Term> vectorterms = new ArrayList();

    Term nextTermVector() {
        Object nextPop;
        vectorterms.clear();
        while ( (nextPop = pop()) instanceof Term ) {
            Term t = (Term)nextPop;
            vectorterms.add(t);
        }
        NativeOperator n = (NativeOperator)nextPop;
        switch (n) {
            case COMPOUND_TERM_OPENER:
                return new Product(vectorterms); //product, without leading '*'
            case SET_EXT_OPENER:
                return SetExt.make(vectorterms);
            case SET_INT_OPENER:
                return SetInt.make(vectorterms);
            default:
                throw new RuntimeException("Invalid operator for nextTermVector: " + n);
        }
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

    public static NarseseParser newParser() {
        return Parboiled.createParser(NarseseParser.class);
    }


    /** parse a series of tasks */
    public void parse(String input, Consumer<Task> c) {
        ParsingResult r = rpr.run(input);
        //r.getValueStack().iterator().forEachRemaining(x -> System.out.println("  " + x.getClass() + " " + x));
        r.getValueStack().iterator().forEachRemaining(x -> {
           if (x instanceof Task)
               c.accept((Task)x);
            else {
               throw new RuntimeException("Unknown parse result: " + x + " (" + x.getClass() + ")");
           }
        });
    }

    /** parse one task */
    public Task parseTask(String input) {
        ParsingResult r = rpr.run(input);
        //r.getValueStack().iterator().forEachRemaining(x -> System.out.println("  " + x.getClass() + " " + x));
        Var<Task> theTask = new Var();
        r.getValueStack().iterator().forEachRemaining(x -> {
            if (x instanceof Task) {
                if (theTask.get() == null)
                    theTask.set((Task)x);
                else {
                    throw new RuntimeException(input + " contained more than 1 Task: " + x + " (" + x.getClass() + ")");
                }
            }
            else {
                throw new RuntimeException(input + " produced unknown parse result: " + x + " (" + x.getClass() + ")");
            }
        });
        return theTask.get();
    }

    /** interactive parse test */
    public static void main(String[] args) {
        NarseseParser p = NarseseParser.newParser();

        Scanner sc = new Scanner(System.in);

        String input = null; //"<a ==> b>. %0.00;0.9%";

        while (true) {
            if (input == null)
                input = sc.nextLine();

            RecoveringParseRunner rpr = new RecoveringParseRunner(p.Input());
            //TracingParseRunner rpr = new TracingParseRunner(p.Input());

            ParsingResult r = rpr.run(input);

            System.out.println("valid? " + (r.matched && (r.parseErrors.isEmpty())) );
            r.getValueStack().iterator().forEachRemaining(x -> System.out.println("  " + x.getClass() + " " + x));

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
