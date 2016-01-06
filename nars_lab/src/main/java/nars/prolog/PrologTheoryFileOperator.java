//package nars.prolog;
//
//import nars.Utilities;
//import nars.nal.Task;
//import nars.nal.term.Term;
//
//import nars.nal.nal8.Operator;
//import nars.tuprolog.InvalidTheoryException;
//import nars.tuprolog.Prolog;
//import nars.tuprolog.Theory;
//
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.util.List;
//import java.util.concurrent.atomic.AtomicBoolean;
//
///**
// * Prolog operate which loads a theory from a file
// *
// * Usage:
// * (^prologTheoryFile **prolog interpreter key as identifier/term** **theory name as identifier/term or string** **string of the path to the file**)
// *
// */
//public class PrologTheoryFileOperator extends Operator {
//    private final PrologContext context;
//
//    static public class ConversionFailedException extends RuntimeException {
//        public ConversionFailedException() {
//            super("Conversion Failed");
//        }
//    }
//
//    public PrologTheoryFileOperator(PrologContext context) {
//        super("^prologTheoryFile");
//        this.context = context;
//    }
//
//    @Override
//    protected List<Task> execute(Operation operation, Term[] args) {
//        if (args.length != 3) {
//            return null;
//        }
//
//        Term prologInterpreterKey = args[0];
//        String theoryName = args[1].name().toString(); // ASK< correct? >
//        String theoryPath = PrologQueryOperator.getStringOfTerm(args[2]);
//
//        // NOTE< throws exception, we just don't catch it and let nars handle it >
//        Prolog prologInterpreter = PrologTheoryUtility.getOrCreatePrologContext(prologInterpreterKey, context);
//
//        // assignment because of java happyness
//        AtomicBoolean theoryInCache = new AtomicBoolean(false);
//
//        // try to find the theory in the cache
//        // if it was not found we try to load it from the file and store it in the cache
//        CachedTheory foundCachedTheory = context.getCachedTheoryIfCached(theoryName, theoryInCache);
//
//        String theoryContent;
//
//        if (theoryInCache.get()) {
//            theoryContent = foundCachedTheory.content;
//        }
//        else {
//            FileInputStream theoryFile;
//            try {
//                theoryFile = new FileInputStream(theoryPath);
//            }
//            catch (FileNotFoundException exception) {
//                // TODO< report error >
//                return null;
//            }
//
//            try {
//                theoryContent = Utilities.readStringFromInputStream(theoryFile);
//            }
//            catch (IOException exception) {
//                // TODO< report error >
//                return null;
//            }
//
//            // store the theory in the cache
//            context.theoryCache.put(theoryName, new CachedTheory(theoryContent));
//        }
//
//
//        // TODO< map somehow the theory name to the theory itself and reload if overwritten >
//        // NOTE< theoryName is not used >
//        try {
//            prologInterpreter.addTheory(new Theory(theoryContent));
//        }
//        catch (InvalidTheoryException exception) {
//            // TODO< report error >
//            return null;
//        }
//
//
//        nar.memory.emit(Prolog.class, prologInterpreterKey + "=" + theoryPath );
//
//        return null;
//    }
// }