/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.operator.meta;

import nars.core.Memory;
import nars.logic.entity.CompoundTerm;
import nars.logic.entity.Statement;
import nars.logic.entity.Term;
import nars.logic.nal1.Inheritance;
import nars.logic.nal2.Similarity;
import nars.logic.nal4.Product;
import nars.logic.nal8.TermFunction;

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
    public Term function(Memory memory, Term[] x) {
        

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
        return Inheritance.make(Product.make(getMetaTerm(s.getSubject()), getMetaTerm(s.getPredicate())), Term.text(operatorName));
    }
    public static Term sop(Statement s, Term predicate) {
        return Inheritance.make(Product.make(getMetaTerm(s.getSubject()),getMetaTerm(s.getPredicate())), predicate);
    }
    public static Term sop(String operatorName, Term... t) {
        Term[] m = new Term[t.length];
        int i = 0;
        for (Term x : t)
            m[i++] = getMetaTerm(x);
        
        return Inheritance.make(Product.make(m), Term.text(operatorName));
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

}
