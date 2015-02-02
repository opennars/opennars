package nars.io.narsese;

import nars.core.Memory;
import nars.io.Symbols;
import nars.io.Texts;
import nars.logic.entity.*;
import org.parboiled.*;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.parserunners.RecoveringParseRunner;
import org.parboiled.parserunners.TracingParseRunner;
import org.parboiled.support.MatcherPath;
import org.parboiled.support.ParsingResult;
import org.parboiled.support.Var;

import java.util.Scanner;

import static nars.io.Symbols.NativeOperator;
import static nars.io.Symbols.NativeOperator.*;
import static org.parboiled.support.ParseTreeUtils.printNodeTree;

/**
 * NARese, syntax and language for interacting with a NAR in NARS.
 * https://code.google.com/p/open-nars/wiki/InputOutputFormat
 */
public class NarseseParser extends BaseParser<Object> {

    private final int level;

    public NarseseParser() {
        this(8);
    }

    public NarseseParser(int level) {
        this.level = level;
    }

    public boolean nal(int n) { return n >= level; }

    public Rule Input() {
        return sequence(zeroOrMore(Task()), WhiteSpace(), EOI);
    }
    
    public Rule Task() {
        //TODO separate goal into an alternate form "!" because it does not use a tense
        Var<float[]> budget = new Var();
        Var<Character> punc = new Var();
        Var<Term> term = new Var();
        Var<TruthValue> truth = new Var();

        return sequence(
                WhiteSpace(),

                optional(sequence(Budget(), budget.set((float[]) pop()))),

                WhiteSpace(),

                Term(),
                term.set( (Term)pop() ),

                SentenceTypeChar(),
                punc.set( matchedChar() ),

                WhiteSpace(),

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

//    Rule Budget() {
//        return sequence('$',
//                ShortFloat(),
//                optional(sequence(";", ShortFloat(),
//                        optional(sequence(";", ShortFloat())))),
//                optional('$')
//        );
//    }

    Rule Budget() { return firstOf(BudgetPriorityDurability(), BudgetPriority()); }

    Rule BudgetPriority() {
        return sequence('$',
                ShortFloat(),
                push( new float[] { (float)pop() } ), //intermediate representation
                optional('$')
        );
    }

    Rule BudgetPriorityDurability() {
        return sequence('$',
                ShortFloat(), ';', ShortFloat(),
                swap(),
                push(new float[]{(float) pop(), (float)pop() } ), //intermediate representation
                optional('$')
        );
    }

    Rule Truth() {

        return sequence(
                '%', ShortFloat(), ';', ShortFloat(), //optional(sequence(";", ShortFloat())),
                swap(),
                push(new TruthValue( (float)pop(), (float)pop() )),
                optional('%') //tailing '%' is optional
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
                "<", WhiteSpace(), Term(), WhiteSpace(), CopulaOperator(), WhiteSpace(), Term(), WhiteSpace(),">",

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
        return sequence("{", Term(), zeroOrMore(sequence(",",Term())),"}");
        //TODO add the remaining ones
    }
    

    Rule Number() {
        return sequence(
                optional('-'),
                oneOrMore(Digit()),
                push( match() ),
                optional('.', oneOrMore(Digit())),
                push(matchOrDefault(".0")),
                swap(),
                push( Float.parseFloat( (String)pop() + (String)pop()))
        );
    }

    Rule Digit() {
        return charRange('0', '9');
    }

    Rule WhiteSpace() {
        return zeroOrMore(anyOf(" \t\f\n"));
    }

    public static NarseseParser newParser() {
        return Parboiled.createParser(NarseseParser.class);
    }
    
    public static void main(String[] args) {
        NarseseParser p = NarseseParser.newParser();

        Scanner sc = new Scanner(System.in);

        String input = "<a ==> b>. %0.00;0.9%";

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
