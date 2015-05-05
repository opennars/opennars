/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.operate.meta;

import nars.nal.Statement;
import nars.nal.nal1.Inheritance;
import nars.nal.nal2.Similarity;
import nars.nal.nal4.Product;
import nars.nal.nal8.TermFunction;
import nars.nal.term.Atom;
import nars.nal.term.Compound;
import nars.nal.term.Term;

/**
 * Produces canonical "Reflective-Narsese" representation of a parameter term
 * @author me
 */
public class Reflect extends TermFunction {


    /*
     <(*,<(*,good,property) --> inheritance>,(&&,<(*,human,good) --> product>,<(*,(*,human,good),inheritance) --> inheritance>)) --> conjunction>.
    */
    
    public Reflect() {
        super("^reflect");
    }

    
    @Override
    public Term function(Term[] x) {
        

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
        return Inheritance.make(Product.make(getMetaTerm(s.getSubject()), getMetaTerm(s.getPredicate())), Atom.quoted(operatorName));
    }
    public static Term sop(Statement s, Term predicate) {
        return Inheritance.make(Product.make(getMetaTerm(s.getSubject()),getMetaTerm(s.getPredicate())), predicate);
    }
    public static Term sop(String operatorName, Term... t) {
        Term[] m = new Term[t.length];
        int i = 0;
        for (Term x : t)
            m[i++] = getMetaTerm(x);
        
        return Inheritance.make(Product.make(m), Atom.quoted(operatorName));
    }
    
    public static Term getMetaTerm(Term node) {
        if (!(node instanceof Compound)) {
            return node;
        }
        Compound t = (Compound)node;
        switch (t.operator()) {
            case INHERITANCE: return sop((Inheritance)t, "inheritance");
            case SIMILARITY:  return sop((Similarity)t, "similarity");
            default: return sop(t.operator().toString(), t.term);                
        }
        
    }

}
