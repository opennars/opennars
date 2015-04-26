package nars.nal.nal3;

import nars.Global;
import nars.io.Symbols;
import nars.nal.Terms;
import nars.nal.term.DefaultCompound;
import nars.nal.term.Term;

/**
 * Base class for SetInt (intensional set) and SetExt (extensional set)
 */
abstract public class SetTensional extends DefaultCompound {
    /**
     * Constructor with partial values, called by make
     * @param n The name of the term
     * @param arg The component list of the term
     */
    protected SetTensional(final Term[] arg) {
        super(arg);
        
        if (arg.length == 0)
            throw new RuntimeException("0-arg empty set");
        
        if (Global.DEBUG) { Terms.verifySortedAndUnique(arg, true); }
        
        init(arg);
    }
    
    
    /**
     * make the oldName of an ExtensionSet or IntensionSet
     *
     * @param opener the set opener
     * @param closer the set closer
     * @param arg the list of term
     * @return the oldName of the term
     */
    protected static CharSequence makeSetName(final char opener, final Term[] arg, final char closer) {
        int size = 1 + 1 - 1; //opener + closer - 1 [no preceding separator for first element]
        
        for (final Term t : arg) 
            size += 1 + t.name().length();
        
        
        final StringBuilder n = new StringBuilder(size);
        
        n.append(opener);                    
        for (int i = 0; i < arg.length; i++) {
            if (i!=0) n.append(Symbols.ARGUMENT_SEPARATOR);
            n.append(arg[i].name());
        }        
        n.append(closer);
               
        
        return n.toString();
    }


    /**
     * Check if the compound is communitative.
     * @return true for communitative
     */
    @Override
    public final boolean isCommutative() {
        return true;
    }    
}
