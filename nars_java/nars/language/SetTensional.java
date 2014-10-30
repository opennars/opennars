package nars.language;

import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.TreeSet;
import nars.core.Parameters;
import nars.io.Symbols;
import static nars.io.Symbols.NativeOperator.COMPOUND_TERM_CLOSER;
import static nars.io.Symbols.NativeOperator.COMPOUND_TERM_OPENER;

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
        
        if (Parameters.DEBUG) {
            verifySortedAndUnique(arg, true);
        }
    }
    
    public static Term[] verifySortedAndUnique(final Term[] arg, boolean allowSingleton) {        
        if (arg.length == 0) {
            throw new RuntimeException("Needs >0 components");
        }
        if (!allowSingleton && (arg.length == 1)) {
            throw new RuntimeException("Needs >1 components: " + Arrays.toString(arg));
        }
        TreeSet<Term> s = Term.toSortedSet(arg);
        if (arg.length!=s.size()) {
            throw new RuntimeException("Contains duplicates: " + Arrays.toString(arg));
        }
        int j = 0;
        for (Term t : s) {
            if (!t.equals(arg[j++]))
                throw new RuntimeException("Un-ordered: " + Arrays.toString(arg) + " , correct order=" + s);
        }        
        return s.toArray(new Term[s.size()]);
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
               
        
        return n.compact();
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
