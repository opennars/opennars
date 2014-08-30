package nars;

import java.util.List;
import nars.entity.Task;
import nars.io.Texts;
import nars.language.Term;
import nars.prolog.Agent;
import nars.prolog.Prolog;
import nars.storage.Memory;


/**
 * Executes Prolog code
 * Access Prolog from NARS
 * @author irc SquareOfTwo | github PtrMan
 */
public class PrologTheoryOperator extends nars.operator.Operator {
    private final PrologQueryOperator queryOperator;
    
    static public class ConversionFailedException extends RuntimeException {
        public ConversionFailedException() {
            super("Conversion Failed");
        }
    }
   
    public PrologTheoryOperator(PrologQueryOperator q) {
        super("^prologTheory");
        this.queryOperator = q;
    }

    @Override
    protected List<Task> execute(nars.operator.Operation operation, Term[] args, Memory memory) {
        if (args.length < 2) {
            return null;
        }
        if (((args.length - 2) % 2) != 0) {
            return null;
        }
       
        // get all variablenames
        // prolog must resolve the variables and assign values to them
        String[] variableNames = getVariableNamesOfArgs(args);
       
       
        /*
        if (!(args[2] instanceof Variable)){
            //TODO< report error >
            return null;
        }
        */
       
       
       
        String theory = getStringOfTerm(args[1]);
        Agent a = new Agent(theory);
        Prolog p = new Prolog();

        return null;
    }
   
    static private String[] getVariableNamesOfArgs(Term[] args) {
        int numberOfVariables = (args.length - 2) / 2;
        int variableI;
       
        String[] variableNames = new String[numberOfVariables];
       
        for( variableI = 0; variableI < numberOfVariables; variableI++ ) {
            Term termWithVariableName = args[2+2*variableI];
           
            if( !(termWithVariableName instanceof Term) ) {
                throw new RuntimeException("Result Variable Name is not an term!");
            }
           
            variableNames[variableI] = getStringOfTerm(termWithVariableName);
        }
       
        return variableNames;
    }
   
    // tries to convert the Term (which must be a string) to a string with the content
    static private String getStringOfTerm(Term term) {
        // escape sign codes
        String string = term.name().toString();
        string = Texts.unescape(string).toString();
        if (string.charAt(0) != '"') {
            throw new RuntimeException("term is not a string as expected!");
        }
       
        string = string.substring(1, string.length()-1);
       
        // because the text can contain quoted text
        string = unescape(string);
       
        return string;
    }
    
    // TODO< move into prolog utils >
    private static String unescape(String text) {
        return text.replace("\\\"", "\"");
    }   
   
}
