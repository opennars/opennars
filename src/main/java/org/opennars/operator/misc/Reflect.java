/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package org.opennars.operator.misc;

import org.opennars.language.*;
import org.opennars.operator.FunctionOperator;
import org.opennars.storage.Memory;

/**
 * Produces canonical "Reflective-Narsese" representation of a parameter term
 * @author me
 */
public class Reflect extends FunctionOperator {


    /*
     <(*,<(*,good,property) --> inheritance>,(&&,<(*,human,good) --> product>,<(*,(*,human,good),inheritance) --> inheritance>)) --> conjunction>.
    */
    
    public Reflect() {
        super("^reflect");
    } 
    
    @Override
    protected Term function(final Memory memory, final Term[] x) {
        
        if (x.length!=1) {
            throw new IllegalStateException("Requires 1 Term argument");
        }

        final Term content = x[0];


        return getMetaTerm(content);
    }


    /**
     * <(*,subject,object) --> predicate>
     */
    public static Term sop(final Term subject, final Term object, final Term predicate) {
        return Inheritance.make(Product.make(getMetaTerm(subject),getMetaTerm(object)), predicate);
    }
    public static Term sop(final Statement s, final String operatorName) {
        return Inheritance.make(Product.make(getMetaTerm(s.getSubject()),getMetaTerm(s.getPredicate())), Term.get(operatorName));
    }
    public static Term sop(final Statement s, final Term predicate) {
        return Inheritance.make(Product.make(getMetaTerm(s.getSubject()),getMetaTerm(s.getPredicate())), predicate);
    }
    public static Term sop(final String operatorName, final Term... t) {
        final Term[] m = new Term[t.length];
        int i = 0;
        for (final Term x : t)
            m[i++] = getMetaTerm(x);
        
        return Inheritance.make(Product.make(m), Term.get(operatorName));
    }
    
    public static Term getMetaTerm(final Term node) {
        if (!(node instanceof CompoundTerm)) {
            return node;
        }
        final CompoundTerm t = (CompoundTerm)node;
        switch (t.operator()) {
            case INHERITANCE: return sop((Inheritance)t, "inheritance");
            case SIMILARITY:  return sop((Similarity)t, "similarity");
            default: return sop(t.operator().toString(), t.term);                
        }
        
    }

    @Override
    protected Term getRange() {
        return Term.get("reflect");
    }
    
    
}
