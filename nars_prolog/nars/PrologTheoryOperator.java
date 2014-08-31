package nars;

import java.util.List;
import nars.entity.Task;
import nars.language.Term;
import nars.prolog.Agent;
import nars.prolog.Prolog;
import nars.core.Memory;


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
public class PrologTheoryOperator extends nars.operator.Operator {
    private final PrologContext context;
    
    static public class ConversionFailedException extends RuntimeException {
        public ConversionFailedException() {
            super("Conversion Failed");
        }
    }
   
    public PrologTheoryOperator(PrologContext context) {
        super("^prologTheory");
        this.context = context;
    }

    @Override
    protected List<Task> execute(nars.operator.Operation operation, Term[] args, Memory memory) {
        if (args.length != 2) {
            return null;
        }
       
        Term key = args[0];        
        String theory = PrologQueryOperator.getStringOfTerm(args[1]);
        
        Agent a = new Agent(theory);
        Prolog p = new Prolog();
        
        context.prologs.put(key, p);
        
        memory.output(Prolog.class, key + "=" + p );

        return null;
    }
   
   
}
