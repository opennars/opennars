package nars;

import nars.language.Term;
import nars.prolog.InvalidLibraryException;
import nars.prolog.Prolog;
import nars.prolog.lib.BasicLibrary;

/**
 *
 * 
 */
public class PrologTheoryUtility {
    static public class LibraryLoadingFailedException extends RuntimeException {
        public LibraryLoadingFailedException() {
            super("Libary Loading Failed");
        }
    }
    
    // throws LibraryLoadingFailedException
    static public Prolog getOrCreatePrologContext(Term prologInterpreterKey, PrologContext context) {
        Prolog prologInterpreter;
        
        boolean prologInterpreterKnown = context.prologInterpreters.containsKey(prologInterpreterKey);
        if (prologInterpreterKnown) {
            prologInterpreter = context.prologInterpreters.get(prologInterpreterKey);
        }
        else {
            prologInterpreter = new Prolog();
            
            try {
                prologInterpreter.loadLibrary(new BasicLibrary());
            }
            catch (InvalidLibraryException exception) {
                throw new LibraryLoadingFailedException();
            }
            
            context.prologInterpreters.put(prologInterpreterKey, prologInterpreter);
        }
        
        return prologInterpreter;
    }
}
