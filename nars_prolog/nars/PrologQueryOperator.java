package nars;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import nars.core.Parameters;
import nars.entity.Task;
import nars.io.Symbols;
import nars.io.Texts;
import nars.language.Inheritance;
import nars.language.Product;
import nars.language.Term;
import nars.operator.Operation;
import nars.operator.Operator;
import nars.prolog.Int;
import nars.prolog.InvalidTheoryException;
import nars.prolog.NoSolutionException;
import nars.prolog.Prolog;
import nars.prolog.SolveInfo;
import nars.prolog.Struct;
import nars.prolog.Theory;
import nars.prolog.Var;
import nars.core.Memory;
import nars.prolog.MalformedGoalException;

/**
 * 
 * 
 * Usage:
 * (^prologQuery, "database(Query,0).", (*, theory0), "Query", #0)
 * 
 * @author me
 */


public class PrologQueryOperator extends Operator {
    private final PrologContext context;

    
    
    public PrologQueryOperator(PrologContext context) {
        super("^prologQuery");
        this.context = context;
    }

    @Override
    protected List<Task> execute(Operation operation, Term[] args, Memory memory) {
        if (args.length < 2) {
            // TODO< error report >
            return null;
        }
        
        if (((args.length - 2) % 2) != 0) {
            // TODO< error report >
            return null;
        }
        
        // check if 1st parameter is a string
        if (!(args[0] instanceof Term)) {
            // TODO< report error >
            return null;
        }
        
        // check if 2nd parameter is a product
        if (!(args[1] instanceof Product)) {
            // TODO< error report >
            return null;
        }
        
        Product theoryProduct = (Product)args[1];
        
        // check if product is not empty
        if (theoryProduct.term.length == 0) {
            // TODO< error report >
            return null;
        }
        
        // TODO< what do we do if we got many theories? >
        ArrayList<String> theories = new ArrayList<>();
        theories.add(context.theories.get(theoryProduct.term[0]));
        
        Term queryTerm = (Term)args[0];
        String query = getStringOfTerm(queryTerm);
        
        
        
        // get all variablenames
        // prolog must resolve the variables and assign values to them
        String[] variableNames = getVariableNamesOfArgs(args);
        
        // execute
        nars.prolog.Term[] resolvedVariableValues = prologParseAndExecuteAndDereferenceInput(theories, query, variableNames);
       
       
       
        // TODO< convert the result from the prolog to strings >
        memory.output(Prolog.class, query + " | TODO");
        //memory.output(Prolog.class, query + " | " + result);
       
        // set result values
        Term[] resultTerms = getResultVariablesFromPrologVariables(resolvedVariableValues, args);
       
        // create evaluation result
        //  compose operation before resultTerms
        int i;
       
        Term[] resultProductTerms = new Term[1 + resultTerms.length];
        resultProductTerms[0] = operation;
        for( i = 0; i < resultTerms.length; i++ ) {
            resultProductTerms[i + 1] = resultTerms[i];
        }
       
        //  create the nars result and return it
        Inheritance resultInheritance = Inheritance.make(
                Product.make(resultProductTerms, memory),
                new Term("prolog_evaluation"), memory);
       
        memory.output(Task.class, resultInheritance);
       
        ArrayList<Task> results = new ArrayList<>(1);
        results.add(memory.newTask(resultInheritance, Symbols.JUDGMENT_MARK, 1f, 0.99f, Parameters.DEFAULT_JUDGMENT_PRIORITY, Parameters.DEFAULT_JUDGMENT_DURABILITY));
               
        return results;
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
    static public String getStringOfTerm(Term term) {
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
    
    
    static private Term[] getResultVariablesFromPrologVariables(nars.prolog.Term[] prologVariables, Term[] args) {
        int numberOfVariables = (args.length - 2) / 2;
        int variableI;
       
        Term[] resultTerms = new Term[numberOfVariables];
       
        for( variableI = 0; variableI < numberOfVariables; variableI++ ) {
            if( prologVariables[variableI] instanceof Int ) {
                Int prologIntegerTerm = (Int)prologVariables[variableI];
               
                resultTerms[variableI] = new Term(String.valueOf(prologIntegerTerm.intValue()));
               
                continue;
            }
            else if( prologVariables[variableI] instanceof nars.prolog.Float ) {
                nars.prolog.Float prologFloatTerm = (nars.prolog.Float)prologVariables[variableI];
               
                resultTerms[variableI] = new Term(String.valueOf(prologFloatTerm.floatValue()));
               
                continue;
            }
            else if( prologVariables[variableI] instanceof Struct ) {
                Struct compoundTerm = (Struct)prologVariables[variableI];
               
                ArrayList<nars.prolog.Term> compundConvertedToArray = convertChainedCompoundTermToList(compoundTerm);
               
                try {
                    String variableAsString = tryToConvertPrologListToString(compundConvertedToArray);
                   
                    resultTerms[variableI] = new Term("\"" + variableAsString + "\"");
               
                    continue; // for debugging
                }
                catch( PrologTheoryOperator.ConversionFailedException conversionFailedException ) {
                    // the alternative is a product of numbers
                    // ASK< this may be not 100% correct, because prolog lists can be in lists etc >
                   
                    // TODO
                   
                    throw new RuntimeException("TODO");
                }
               
                // unreachable
            }
           
            throw new RuntimeException("Unhandled type of result variable");
        }
       
        return resultTerms;
    }
   
   
    private nars.prolog.Term[] prologParseAndExecuteAndDereferenceInput(ArrayList<String> theories, String input, String[] dereferencingVariableNames) {
        Prolog prolog = new Prolog();
        
        try {
            // TODO< many teories >
            
            prolog.addTheory(new Theory(theories.get(0)));
            
        } catch (InvalidTheoryException ex) {
            Logger.getLogger(PrologTheoryOperator.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
        //return solveInfo.getSolution();
        
        return executePrologGoalAndDereferenceVariable(prolog, input, dereferencingVariableNames);
    }
   
    // TODO< return status/ error/sucess >
    private nars.prolog.Term[] executePrologGoalAndDereferenceVariable(Prolog p, String goal, String[] VariableNames) {
        SolveInfo solution;
        
        try {
            solution = p.solve(goal);
        }
        catch (MalformedGoalException exception) {
            // TODO< error handing >
            throw new RuntimeException("MalformedGoalException");
        }
        
        if (solution == null ) {
            return null; // TODO error
        }
        
        if (!solution.isSuccess()) {
            throw new RuntimeException("Query was not successful");
        }
        
        nars.prolog.Term solutionTerm;
        
        try {
            solutionTerm = solution.getSolution();
        }
        catch (NoSolutionException exception) {
            throw new RuntimeException("Query had no solution");
        }
        
        nars.prolog.Term[] resultArray;

        resultArray = new nars.prolog.Term[VariableNames.length];
        

        int variableI;

        for( variableI = 0; variableI < VariableNames.length; variableI++ ) {
            // get variable and dereference
            //  get the variable which has the name
            Var variableTerm = getVariableByNameRecursive(solutionTerm, VariableNames[variableI]);

            if( variableTerm == null )
            {
                return null; // error
            }

            variableTerm.resolveTerm();
            nars.prolog.Term dereferencedTerm = variableTerm.getTerm();

            resultArray[variableI] = dereferencedTerm;
        }
        
        
        return resultArray;
    }
   
    // tries to get a variable from a term by name
    // returns null if it wasn't found
    static private nars.prolog.Var getVariableByNameRecursive(nars.prolog.Term term, String name) {
        if( term instanceof Struct ) {
            Struct s = (Struct)term;
            for (int i = 0; i < s.getArity(); i++) {
                nars.prolog.Term iterationTerm = s.getArg(i);
                nars.prolog.Var result = getVariableByNameRecursive(iterationTerm, name);
               
                if( result != null ) {
                    return result;
                }
            }
           
            return null;
        }
        else if( term instanceof nars.prolog.Var ) {
            nars.prolog.Var termAsVar = (nars.prolog.Var)term;
            String nameOfVar = termAsVar.name().toString();
            
            if( nameOfVar.equals(name) ) {
                return termAsVar;
            }
           
            return null;
        }
        else if( term instanceof Int ) {
            return null;
        }
       
        throw new RuntimeException("Internal Error: Unknown prolog term!");
    }
   
    // converts a chained compound term (which contains oher compound terms) to a list
    static private ArrayList<nars.prolog.Term> convertChainedCompoundTermToList(Struct compoundTerm) {
        ArrayList<nars.prolog.Term> result = new ArrayList<>();
       
        Struct currentCompundTerm = compoundTerm;
       
        for(;;) {
            if( currentCompundTerm.getArity() == 0 ) {
                // end is reached
                break;
            }
            else if( currentCompundTerm.getArity() != 2 ) {
                throw new RuntimeException("Compound must have two or zero arguments!");
            }
           
            result.add(currentCompundTerm.getArg(0));
           
            nars.prolog.Term arg2 = currentCompundTerm.getArg(1);
            
            if ( arg2.isAtom()) {
                Struct atomTerm = (Struct)arg2;
               
                /*if( !atomTerm.value.equals("[]") ) {
                    throw new RuntimeException("[] AtomTerm excepted!");
                }*/
               
                // this is the last element of the list, we are done
                break;
            }
           
            if( !(arg2 instanceof Struct) ) {
                throw new RuntimeException("Second argument of Compound term is expected to be a compound term!");
            }
           
            currentCompundTerm = (Struct)(arg2);
        }
       
        return result;
    }
   
    // tries to convert a list with integer terms to an string
    // checks also if the signs are correct
    // throws an ConversionFailedException if the conversion is not possible
    static private String tryToConvertPrologListToString(ArrayList<nars.prolog.Term> array) {
        String result = "";
       
        for( nars.prolog.Term iterationTerm : array ) {
            if( !(iterationTerm instanceof Int) ) {
                throw new PrologTheoryOperator.ConversionFailedException();
            }
           
            Int integerTerm = (Int)iterationTerm;
            int integer = integerTerm.intValue();
           
            if( integer > 127 || integer < 0 ) {
                throw new PrologTheoryOperator.ConversionFailedException();
            }
           
            result += Character.toString((char)integer);
        }
       
        return result;
    }
   
    
    
    // TODO< move into prolog utils >
    private static String unescape(String text) {
        return text.replace("\\\"", "\"");
    }   
    
    
}
