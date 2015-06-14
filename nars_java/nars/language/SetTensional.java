package nars.language;

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
    protected SetTensional(final CharSequence name, final Term[] arg) {
        super(name, arg);
    }

    /**
     * constructor with full values, called by clone
     * @param n The name of the term
     * @param cs Component list
     * @param open Open variable list
     * @param i Syntactic complexity of the compound
     */
    protected SetTensional(final CharSequence n, final Term[] cs, final boolean con, final short i) {
        super(n, cs, con, i);
    }    
    
    /**
     * make the oldName of an ExtensionSet or IntensionSet
     *
     * @param opener the set opener
     * @param closer the set closer
     * @param arg the list of term
     * @return the oldName of the term
     */
    protected static String makeSetName(final char opener, final Term[] arg, final char closer) {
        final int sizeEstimate = 12 * arg.length + 2;
        
        StringBuilder name = new StringBuilder(sizeEstimate)
            .append(opener);

        if (arg.length == 0) { 
            //is empty arg valid?            
            //throw new RuntimeException("Empty arg list for makeSetName");            
        }
        else {
        
            name.append(arg[0].name());

            for (int i = 1; i < arg.length; i++) {
                name.append(Symbols.ARGUMENT_SEPARATOR).append(arg[i].name());
            }
        }
        
        name.append(closer);
        
        return name.toString();
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
