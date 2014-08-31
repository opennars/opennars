package nars;

import java.util.List;
import nars.entity.Task;
import nars.language.Term;
import nars.prolog.Agent;
import nars.prolog.Prolog;
import nars.core.Memory;
import nars.prolog.InvalidTheoryException;
import nars.prolog.Theory;


/**
 * Executes Prolog code
 * Access Prolog from NARS
 * @author irc SquareOfTwo | github PtrMan
 * 
 * Example:
        < (&/, (^prolog, "exec", "database(Key, Result).", "Result", $ra, "Key", $keyA) )    =/> <(*, $keyA, $ra) --> executed > >.
        <(*, 0, #a) --> executed >!

   Should return: 
        <(*, 0, "test") --> executed>
 */
public class PrologTheoryStringOperator extends nars.operator.Operator {
    private final PrologContext context;
    
    static public class ConversionFailedException extends RuntimeException {
        public ConversionFailedException() {
            super("Conversion Failed");
        }
    }
   
    public PrologTheoryStringOperator(PrologContext context) {
        super("^prologTheoryString");
        this.context = context;
    }

    @Override
    protected List<Task> execute(nars.operator.Operation operation, Term[] args, Memory memory) {
        if (args.length != 3) {
            return null;
        }
       
        Term prologInterpreterKey = args[0];
        String theoryName = args[1].name().toString(); // ASK< correct? >
        String theoryContent = PrologQueryOperator.getStringOfTerm(args[2]);
        
        Prolog prologInterpreter;
        
        boolean prologInterpreterKnown = context.prologInterpreters.containsKey(prologInterpreterKey);
        if (prologInterpreterKnown) {
            prologInterpreter = context.prologInterpreters.get(prologInterpreterKey);
        }
        else {
            prologInterpreter = new Prolog();
            context.prologInterpreters.put(prologInterpreterKey, prologInterpreter);
        }
        
        // TODO< map somehow the theory name to the theory itself and reload if overwritten >
        // NOTE< theoryName is not used >
        try {
            prologInterpreter.addTheory(new Theory(theoryContent));
        }
        catch (InvalidTheoryException exception) {
            // TODO< report error >
            return null;
        }
        
        memory.output(Prolog.class, prologInterpreterKey + "=" + theoryContent );

        return null;
    }
   
   
}
