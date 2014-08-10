package nars.test.util;

import nars.util.rope.impl.PrePostCharRope;
import static java.lang.String.valueOf;
import nars.core.DefaultNARBuilder;
import nars.core.NAR;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.entity.TruthValue;
import nars.io.Symbols;
import static nars.io.Symbols.NativeOperator.COMPOUND_TERM_CLOSER;
import static nars.io.Symbols.NativeOperator.COMPOUND_TERM_OPENER;
import static nars.io.Symbols.NativeOperator.STATEMENT_CLOSER;
import static nars.io.Symbols.NativeOperator.STATEMENT_OPENER;
import nars.language.CompoundTerm;
import nars.language.Statement;
import nars.language.Term;
import nars.util.rope.Rope;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 *
 * @author me
 */


public class TestRope {
 


    
    public static CharSequence toString(Term term) {
        if (term instanceof Statement) {
            Statement s = (Statement)term;
            /*
            Rope r = cat(
                valueOf(STATEMENT_OPENER.ch),
                toString(s.getSubject()),
                valueOf(' '),
                s.operator().toString(),
                valueOf(' '),
                toString(s.getPredicate()),
                valueOf(STATEMENT_CLOSER.ch));
            */
            
            return new PrePostCharRope(STATEMENT_OPENER.ch, STATEMENT_CLOSER.ch, Rope.cat(
                toString(s.getSubject()),
                valueOf(' '),
                s.operator().toString(),
                valueOf(' '),
                toString(s.getPredicate())
            ));
        }
        else if (term instanceof CompoundTerm) {
            CompoundTerm ct = (CompoundTerm)term;
            
            Rope[] tt = new Rope[ct.term.length];
            int i = 0;
            for (final Term t : ct.term) {
                tt[i++] = Rope.cat(String.valueOf(Symbols.ARGUMENT_SEPARATOR), toString(t));
            }
            
            Rope ttt = Rope.cat(tt);
            return Rope.cat(String.valueOf(COMPOUND_TERM_OPENER.ch), ct.operator().toString(), ttt, String.valueOf(COMPOUND_TERM_CLOSER.ch));

        }
        else
            return term.toString();
    }
    
    @Test
    public void testRope() {
        NAR n = new DefaultNARBuilder().build();
        
        String term1String ="<#1 --> (&,boy,(/,taller_than,{Tom},_))>";
        Term term1 = n.term(term1String);

        Rope tr = (Rope)toString(term1);
        
        //visualize(tr, System.out);
        
        Sentence s = new Sentence(term1, '.', new TruthValue(1,1), new Stamp(n.memory));
        
    }
    
    @Test
    public void testRopeStringEqual() {
        String s = "x";
        Rope r = Rope.build("x");
        
        assertTrue(!s.equals(r));
        
        assertTrue(!r.equals(s));
        
        //careful: these are not commutive because String only equals other String
        //so do not mix String and FastCharSequenceRope in the same collection
        Rope rF = Rope.buildFast("x");
        assertTrue(s.equals(rF));
        assertTrue(rF.equals(s));
    }
    

    
}
