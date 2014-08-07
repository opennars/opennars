package nars.language;

import java.util.Objects;
import nars.core.Parameters;


public class Atom extends Term {
    
    public final String name;
    
    transient private final int hash;

    public Atom(final String literal) {
        super();
        
        if (literal.length() <= Parameters.INTERNED_TERM_NAME_MAXLEN)
            this.name = literal.intern();
        else
            this.name = literal;
        
        this.hash = Objects.hash(getClass().getSimpleName(), name);
    }
    
    
    
    /**
     * Produce a hash code for the term
     *
     * @return An integer hash code
     */
    @Override
    public int hashCode() {
        return hash;
    }
    
    public boolean isConstant() {
        return true;
    }
    
    public short getComplexity() {
        return 1;
    }

    public boolean containsVar() {
        return false;
    }
    
     
    @Override
    public Atom clone() {
        return new Atom(name);
    }


    @Override
    public boolean equals(final Object that) {
        if (that instanceof Atom)
            return name.equals( ((Atom)that).name );
        return false;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(final Term o) {
        
        if (o instanceof Atom) {
            return name.compareTo(((Atom)o).name);
        }
        else {
            //compare simplenames, more efficient because the full class name will likely share a prefix that must be iterated
            return getClass().getSimpleName().compareTo(o.getClass().getSimpleName());
        }
        
    }
}
