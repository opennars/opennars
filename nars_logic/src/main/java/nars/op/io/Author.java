//package nars.operate.io;
//
//
//import nars.Memory;
//import nars.io.narsese.Narsese;
//import nars.nal.Task;
//import nars.nal.term.Term;
//
//import nars.nal.nal8.Operator;
//
//import java.util.List;
//
///** sets the input's current self; useful for switching between authors / provenance */
//public class Author extends Operator {
//
//    private final Narsese parser;
//
//    public Author(Narsese n) {
//        super("^author");
//        this.parser = n;
//    }
//
//    @Override
//    protected List<Task> execute(Operation operation, Term[] args, Memory memory) {
//        parser.setSelf(args[0]);
//        return null;
//    }
//
//    @Override
//    public boolean isImmediate() {
//        return true;
//    }
//}
