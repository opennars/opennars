/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.op.meta;

import nars.nal.nal1.Inheritance;
import nars.nal.nal2.Similarity;
import nars.nal.nal4.Product;
import nars.nal.nal8.Operation;
import nars.nal.nal8.operator.TermFunction;
import nars.term.Statement;
import nars.term.Term;
import nars.term.atom.Atom;
import nars.term.compound.Compound;

/**
 * Produces canonical "Reflective-Narsese" representation of a parameter term
 * @author me
 */
public class reflect extends TermFunction {


    /*
     <(*,<(*,good,property) --> inheritance>,(&&,<(*,human,good) --> product>,<(*,(*,human,good),inheritance) --> inheritance>)) --> conjunction>.
    */

    
    @Override
    public Term function(Operation x) {

        Term content = x.arg(0);

        return getMetaTerm(content);
    }


    /**
     * <(*,subject,object) --> predicate>
     */
    public static Term sop(Term subject, Term object, Term predicate) {
        return Inheritance.make(Product.make(getMetaTerm(subject),getMetaTerm(object)), predicate);
    }
    public static Term sop(Statement s, String operatorName) {
        return Inheritance.make(Product.make(getMetaTerm(s.getSubject()), getMetaTerm(s.getPredicate())), Atom.quote(operatorName));
    }
    public static Term sop(Statement s, Term predicate) {
        return Inheritance.make(Product.make(getMetaTerm(s.getSubject()),getMetaTerm(s.getPredicate())), predicate);
    }
    public static Term sop(String operatorName, Compound c) {
        Term[] m = new Term[c.size()];
        for (int i = 0; i < c.size(); i++)
            m[i] = getMetaTerm(c.term(i));

        return Inheritance.make(Product.make(m), Atom.quote(operatorName));
    }
    
    public static Term getMetaTerm(Term node) {
        if (!(node instanceof Compound)) {
            return node;
        }
        Compound t = (Compound)node;
        switch (t.op()) {
            case INHERITANCE: return sop((Inheritance)t, "inheritance");
            case SIMILARITY:  return sop((Similarity)t, "similarity");
            default: return sop(t.op().toString(), t);
        }
        
    }

}
