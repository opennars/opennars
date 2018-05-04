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

import org.opennars.storage.Memory;
import org.opennars.language.CompoundTerm;
import org.opennars.language.Inheritance;
import org.opennars.language.Product;
import org.opennars.language.Similarity;
import org.opennars.language.Statement;
import org.opennars.language.Term;
import org.opennars.operator.FunctionOperator;

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

    final static String requireMessage = "Requires 1 Term argument";    
    
    
    @Override
    protected Term function(Memory memory, Term[] x) {
        
        if (x.length!=1) {
            throw new RuntimeException(requireMessage);
        }

        Term content = x[0];


        return getMetaTerm(content);
    }


    /**
     * <(*,subject,object) --> predicate>
     */
    public static Term sop(Term subject, Term object, Term predicate) {
        return Inheritance.make(Product.make(getMetaTerm(subject),getMetaTerm(object)), predicate);
    }
    public static Term sop(Statement s, String operatorName) {
        return Inheritance.make(Product.make(getMetaTerm(s.getSubject()),getMetaTerm(s.getPredicate())), Term.get(operatorName));
    }
    public static Term sop(Statement s, Term predicate) {
        return Inheritance.make(Product.make(getMetaTerm(s.getSubject()),getMetaTerm(s.getPredicate())), predicate);
    }
    public static Term sop(String operatorName, Term... t) {
        Term[] m = new Term[t.length];
        int i = 0;
        for (Term x : t)
            m[i++] = getMetaTerm(x);
        
        return Inheritance.make(Product.make(m), Term.get(operatorName));
    }
    
    public static Term getMetaTerm(Term node) {
        if (!(node instanceof CompoundTerm)) {
            return node;
        }
        CompoundTerm t = (CompoundTerm)node;
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
