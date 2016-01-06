//package nars.prolog;
//
//import nars.nal.term.Term;
//import nars.tuprolog.InvalidLibraryException;
//import nars.tuprolog.Prolog;
//import nars.tuprolog.lib.BasicLibrary;
//
///**
// *
// *
// */
//public class PrologTheoryUtility {
//    static public class LibraryLoadingFailedException extends RuntimeException {
//        public LibraryLoadingFailedException() {
//            super("Libary Loading Failed");
//        }
//    }
//
//    // throws LibraryLoadingFailedException
//    static public Prolog getOrCreatePrologContext(Term prologInterpreterKey, PrologContext context) {
//        Prolog prologInterpreter;
//
//        boolean prologInterpreterKnown = context.prologs.containsKey(prologInterpreterKey);
//        if (prologInterpreterKnown) {
//            prologInterpreter = context.prologs.get(prologInterpreterKey);
//        }
//        else {
//            prologInterpreter = new Prolog();
//
//            try {
//                prologInterpreter.loadLibrary(new BasicLibrary());
//            }
//            catch (InvalidLibraryException exception) {
//                throw new LibraryLoadingFailedException();
//            }
//
//            context.prologs.put(prologInterpreterKey, prologInterpreter);
//        }
//
//        return prologInterpreter;
//    }
// }
