package nars.language;

import java.nio.CharBuffer;
import nars.core.Parameters;
import nars.io.Symbols;

/**
 * Base class for SetInt (intensional set) and SetExt (extensional set)
 */
abstract public class SetTensional extends CompoundTerm {
    /**
     * Constructor with partial values, called by make
     * @param n The name of the term
     * @param arg The component list of the term
     */
    protected SetTensional(final Term[] arg) {
        super(arg);
        
        if (Parameters.DEBUG) { Terms.verifySortedAndUnique(arg, true); }
        
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
        
        
        final CharBuffer n = CharBuffer.allocate(size);
        
        n.append(opener);                    
        for (int i = 0; i < arg.length; i++) {
            if (i!=0) n.append(Symbols.ARGUMENT_SEPARATOR);
            n.append(arg[i].name());
        }        
        n.append(closer);
               
        
        return n.compact().toString();
    }

    
    @Override
    public int getMinimumRequiredComponents() {
        return 1;
    }
    

    /**
     * Check if the compound is communitative.
     * @return true for communitative
     */
    @Override
    public boolean isCommutative() {
        return true;
    }    
}
