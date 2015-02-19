package nars;

import nars.core.Memory;
import nars.io.Symbols;
import nars.logic.entity.Sentence;
import nars.logic.entity.Task;
import nars.logic.entity.Term;
import nars.logic.nal8.Operation;
import nars.logic.nal8.Operator;
import nars.prolog.InvalidTheoryException;
import nars.prolog.Prolog;
import nars.prolog.Struct;

import java.util.List;

/**
* Created by me on 2/19/15.
*/
public class PrologFact extends Operator {


    private final PrologContext context;

    protected PrologFact(PrologContext p) {
        super("^fact");
        this.context = p;
    }

    @Override
    protected List<Task> execute(Operation operation, Term[] args, Memory memory) {

        Prolog p = context.getProlog(null); //default

        Sentence s = operation.getTask().sentence;
        if (s.punctuation == Symbols.GOAL) {
            nars.prolog.Term factTerm = NARPrologMirror.pterm(args[0]);
            if (factTerm instanceof Struct)
                try {
                    p.addTheory((Struct)factTerm);
                } catch (InvalidTheoryException e) {
                    System.out.println(e);
                    throw new RuntimeException(e);
                }
            else {
                throw new RuntimeException("Could not assert non-struct: " + factTerm);
            }
        }

        return null;
    }
}
