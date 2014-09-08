package nars;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import nars.core.Memory;
import nars.entity.Task;
import nars.language.Term;
import nars.prolog.InvalidTheoryException;
import nars.prolog.Prolog;
import nars.prolog.Theory;
import nars.prolog.lib.BasicLibrary;

/**
 * Prolog operator which loads a theory from a file
 * 
 * Usage:
 * (^prologTheoryFile **prolog interpreter key as identifier/term** **theory name as identifier/term or string** **string of the path to the file**)
 * 
 */
public class PrologTheoryFileOperator extends nars.operator.Operator {
    private final PrologContext context;
    
    static public class ConversionFailedException extends RuntimeException {
        public ConversionFailedException() {
            super("Conversion Failed");
        }
    }
    
    public PrologTheoryFileOperator(PrologContext context) {
        super("^prologTheoryFile");
        this.context = context;
    }

    @Override
    protected List<Task> execute(nars.operator.Operation operation, Term[] args, Memory memory) {
        if (args.length != 3) {
            return null;
        }
       
        Term prologInterpreterKey = args[0];
        String theoryName = args[1].name().toString(); // ASK< correct? >
        String theoryPath = PrologQueryOperator.getStringOfTerm(args[2]);
        
        // NOTE< throws exception, we just don't catch it and let nars handle it >
        Prolog prologInterpreter = PrologTheoryUtility.getOrCreatePrologContext(prologInterpreterKey, context);
        
        FileInputStream theoryFile;
        try {
            theoryFile = new FileInputStream(theoryPath);
        }
        catch (FileNotFoundException exception) {
            // TODO< report error >
            return null;
        }
        
        
        // TODO< map somehow the theory name to the theory itself and reload if overwritten >
        // NOTE< theoryName is not used >
        try {
            prologInterpreter.addTheory(new Theory(theoryFile));
        }
        catch (InvalidTheoryException exception) {
            // TODO< report error >
            return null;
        }
        catch (IOException exception) {
            // TODO< report error >
            return null;
        }
        
        memory.output(Prolog.class, prologInterpreterKey + "=" + theoryPath );

        return null;
    }
}