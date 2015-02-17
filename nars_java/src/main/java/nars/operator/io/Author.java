package nars.operator.io;


import nars.core.Memory;
import nars.io.narsese.Narsese;
import nars.logic.entity.Task;
import nars.logic.entity.Term;
import nars.logic.nal8.Operation;
import nars.logic.nal8.Operator;

import java.util.List;

/** sets the input's current self; useful for switching between authors / provenance */
public class Author extends Operator {

    private final Narsese parser;

    public Author(Narsese n) {
        super("^author");
        this.parser = n;
    }

    @Override
    protected List<Task> execute(Operation operation, Term[] args, Memory memory) {
        parser.setSelf(args[0]);
        return null;
    }

    @Override
    public boolean isImmediate() {
        return true;
    }
}
