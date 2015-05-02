package nars.nal.nal3;

import nars.Global;
import nars.io.Symbols;
import nars.nal.NALOperator;
import nars.nal.Terms;
import nars.nal.term.Compound1;
import nars.nal.term.DefaultCompound;
import nars.nal.term.Term;

import static nars.nal.NALOperator.SET_INT_CLOSER;
import static nars.nal.NALOperator.SET_INT_OPENER;
import static nars.nal.NALOperator.SET_EXT_CLOSER;
import static nars.nal.NALOperator.SET_EXT_OPENER;

/**
 * Base class for SetInt (intensional set) and SetExt (extensional set), where N>1
 */
abstract public class AbstractSetN extends DefaultCompound implements SetTensional {


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
    
    



}
