package nars.tuprolog;

import java.util.Iterator;

/**
 *
 * @author me
 */


public interface PrologTermIterator  {
    public Iterator<? extends Term> iterator(Prolog engine);        
}
