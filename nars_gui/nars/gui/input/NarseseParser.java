package nars.gui.input;

import java.util.Scanner;
import org.parboiled.BaseParser;
import static org.parboiled.BaseParser.EOI;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.parserunners.RecoveringParseRunner;
import org.parboiled.support.MatcherPath;
import static org.parboiled.support.ParseTreeUtils.printNodeTree;
import org.parboiled.support.ParsingResult;

/**
 * NARese, syntax and language for interacting with a NAR in NARS.
 * @see https://code.google.com/p/open-nars/wiki/InputOutputFormat
 */
public class NarseseParser extends BaseParser<Object> {

    public Rule Input() {
        return sequence(zeroOrMore(Task()), WhiteSpace(), EOI);
    }
    
    public Rule Task() {
        //TODO separate goal into an alternate form "!" because it does not use a tense
        
        return sequence(
                WhiteSpace(),
                optional(Budget()),
                WhiteSpace(),
                Term(),
                SentenceTypeChar(),
                WhiteSpace(),
                //optional(Tense())
                optional(Truth()));
    }

    Rule Budget() {
        return sequence("%", ShortFloat(), optional(sequence(";", ShortFloat(), optional(sequence(";", ShortFloat())))));
    }

    Rule Truth() {
        return sequence("%", ShortFloat(), optional(sequence(";", ShortFloat())));
    }
    
    Rule ShortFloat() {
        //TODO use more specific shortfloat number
        return Number();
    }
    
    Rule SentenceTypeChar() {
        return anyOf(".?!");
    }
    

    Rule OperationExecution() {
        //TODO
        // "(^"<word> {","<term>} ")"         // (an operation to be executed)   */           
        return string("(^)");
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
        //TODO use separate rules for each so a parse can identify them
        return sequence("<", WhiteSpace(), Term(), WhiteSpace(), CopulaOperator(), WhiteSpace(), Term(), WhiteSpace(),">");        
    }
    
    Rule CopulaOperator() {
        return firstOf(string("-->"), string("<->"));
    }
        
    Rule Term() {
        /*
                 <term> ::= <word>                             // an atomic constant term
                        | <variable>                         // an atomic variable term
                        | <compound-term>                    // a term with internal structure
                        | <statement>                        // a statement can serve as a term
        */
        return firstOf(Literal(), QuotedLiteral(), Variable(), CompoundTerm(), Copula(), OperationExecution());
    }
       
    Rule Literal() {
        return oneOrMore(noneOf(" ,.!?<>-=|&()<>[]{}#$\""));
    }
    
    Rule QuotedLiteral() {
        return sequence("\"", AnyString(), "\"");
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
                optional('.', oneOrMore(Digit()))
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
        while (true) {
            String input = sc.nextLine();
            RecoveringParseRunner rpr = new RecoveringParseRunner(p.Input());
            ParsingResult r = rpr.run(input);
            System.out.println("valid? " + (r.matched && (r.parseErrors.isEmpty())) );
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
        }
        
    }
}