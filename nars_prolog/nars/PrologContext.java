package nars;

import java.util.Map;
import java.util.WeakHashMap;
import nars.core.NAR;
import nars.language.Term;
import nars.prolog.Prolog;

/**
 *
 * @author me
 */
public class PrologContext {
    
    /** maps a NARS term to the coresponding prolog interpreter istance */
    public final Map<Term, Prolog> prologInterpreters = new WeakHashMap();
    private final NAR nar;
    
    public PrologContext(NAR n) {
        this.nar = n;
        
        nar.memory.addOperator(new PrologTheoryStringOperator(this));
        nar.memory.addOperator(new PrologQueryOperator(this));
        nar.memory.addOperator(new PrologTheoryFileOperator(this));
        
    }
}
