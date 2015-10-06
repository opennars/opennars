package nars.nal.nal3;

import nars.Global;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Terms;

/**
 * Base class for SetInt (intensional set) and SetExt (extensional set), where N>1
 */
abstract public class AbstractSetN extends Compound implements SetTensional {


    /**
     * Constructor with partial values, called by make
     * @param n The name of the term
     * @param arg The component list of the term
     */
    protected AbstractSetN(final Term[] arg) {
        super(arg);
        
        if (arg.length == 0)
            throw new RuntimeException("0-arg empty set");
        
        if (Global.DEBUG) { Terms.verifySortedAndUnique(arg, true); }
        
        init(arg);
    }

    @Override
    public Term[] terms() {
        return this.term;
    }

    @Override
    public final boolean isCommutative() {
        return true;
    }

}
