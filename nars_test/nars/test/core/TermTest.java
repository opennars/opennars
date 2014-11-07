/*
 * Copyright (C) 2014 me
 *
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
package nars.test.core;

import java.util.TreeSet;
import nars.core.NAR;
import nars.core.build.Default;
import nars.entity.Concept;
import nars.io.Texts;
import nars.io.narsese.Narsese;
import nars.language.CompoundTerm;
import nars.language.Inheritance;
import nars.language.Term;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author me
 */
public class TermTest {
    
    NAR n = new Default().build();
    Narsese np = new Narsese(n);
    
    protected void assertEquivalent(String term1String, String term2String) {
        try {
            NAR n = new Default().build();

            Term term1 = np.parseTerm(term1String);
            Term term2 = np.parseTerm(term2String);

            assertTrue(term1 instanceof CompoundTerm);
            assertTrue(term2 instanceof CompoundTerm);
            assert(!term1String.equals(term2String));

            assert(term1.hashCode() == term2.hashCode());
            assert(term1.equals(term2));
            assert(term1.compareTo(term2)==0);        
        }
        catch (Exception e) { assertTrue(e.toString(), false); }
    }
    
    @Test
    public void testCommutativeCompoundTerm() throws Exception {
        NAR n = new Default().build();

        assertEquivalent("(&&,a,b)", "(&&,b,a)");
        assertEquivalent("(&&,(||,b,c),a)", "(&&,a,(||,b,c))");
        assertEquivalent("(&&,(||,c,b),a)", "(&&,a,(||,b,c))");
        
    }
    
    @Test
    public void testConjunctionTreeSet() throws Narsese.InvalidInputException {
        NAR n = new Default().build();
        
        
            
        //these 2 representations are equal, after natural ordering
        String term1String =    "<#1 --> (&,boy,(/,taller_than,{Tom},_))>";
        Term term1 = np.parseTerm(term1String);        
        String term1Alternate = "<#1 --> (&,(/,taller_than,{Tom},_),boy)>";
        Term term1a = np.parseTerm(term1Alternate);
        

        // <#1 --> (|,boy,(/,taller_than,{Tom},_))>
        Term term2 = np.parseTerm("<#1 --> (|,boy,(/,taller_than,{Tom},_))>");

        assertTrue(term1.toString().equals( term1a.toString() ));
        assertTrue(term1.getComplexity() > 1);
        assertTrue(term1.getComplexity() == term2.getComplexity());

        assertTrue(term1.getClass().equals(Inheritance.class));
        assertTrue(term1.getClass().equals(Inheritance.class));


        //System.out.println("t1: " + term1 + ", complexity=" + term1.getComplexity());
        //System.out.println("t2: " + term2 + ", complexity=" + term2.getComplexity());


        assertTrue(term1.equals(term1.clone()));
        assertTrue(term1.compareTo(term1.clone())==0);            
        assertTrue(term2.equals(term2.clone()));
        assertTrue(term2.compareTo(term2.clone())==0);

        boolean t1e2 = term1.equals(term2);
        int t1c2 = term1.compareTo(term2);
        int t2c1 = term2.compareTo(term1);

        assertTrue(!t1e2);
        assertTrue("term1 and term2 inequal, so t1.compareTo(t2) should not = 0", t1c2!=0);
        assertTrue("term1 and term2 inequal, so t2.compareTo(t1) should not = 0", t2c1!=0);

        /*
        System.out.println("t1 equals t2 " + t1e2);
        System.out.println("t1 compareTo t2 " + t1c2);
        System.out.println("t2 compareTo t1 " + t2c1);
        */

        TreeSet<Term> set = new TreeSet<>();
        boolean added1 = set.add((Term) term1.clone());
        boolean added2 = set.add((Term) term2.clone());
        assertTrue("term 1 added to set", added1);
        assertTrue("term 2 added to set", added2);

        assertTrue(set.size() == 2);
        
    }
    
    @Test
    public void testUnconceptualizedTermInstancing() throws Narsese.InvalidInputException {
       NAR n = new Default().build();
        
       String term1String ="<a --> b>";
       Term term1 = np.parseTerm(term1String);
       Term term2 = np.parseTerm(term1String);
       
       assertTrue(term1.equals(term2));
       assertTrue(term1.hashCode() == term2.hashCode());
       
       CompoundTerm cterm1 = ((CompoundTerm)term1);
       CompoundTerm cterm2 = ((CompoundTerm)term2);

       //test subterms
       assertTrue(cterm1.term[0].equals(cterm2.term[0])); //'a'

    }
    
    @Test
    public void testConceptInstancing() throws Narsese.InvalidInputException {
       NAR n = new Default().build();
        
       String statement1 = "<a --> b>.";
       
       Term a = np.parseTerm("a");
       assertTrue(a!=null);
       Term a1 = np.parseTerm("a");
       assertTrue(a.equals(a1));
       
       n.addInput(statement1);       
       n.step(4);
              
       n.addInput(" <a  --> b>.  ");
       n.step(1);
       n.addInput(" <a--> b>.  ");
       n.step(1);
       
       String statement2 = "<a --> c>.";
       n.addInput(statement2);
       n.step(4);
       
       Term a2 = np.parseTerm("a");
       assertTrue(a2!=null);
                     
       Concept ca = n.memory.concept(a2);
       assertTrue(ca!=null);
       
       assertEquals(true, n.memory.concepts.iterator().hasNext());

    }    
    
    @Test
    public void testEscaping() {        
        bidiEscape("c d", "x$# x", "\\\"sdkf sdfjk", "_ _");
        
//        NAR n = new Default().build();
//        n.addInput("<a --> \"b c\">.");
//        n.step(1);
//        n.finish(1);
//        
//        Term t = new Term("\\\"b_c\\\"");
//        System.out.println(t);
//        System.out.println(n.memory.getConcepts());
//        System.out.println(n.memory.conceptProcessor.getConcepts());
//        
//        
//        assertTrue(n.memory.concept(new Term("a"))!=null);
//        assertTrue(n.memory.concept(t)!=null);

    }
    
    protected void bidiEscape(String... tests) {
        for (String s : tests) {
            s = '"' + s + '"';
            String escaped = Texts.escape(s).toString();
            String unescaped = Texts.unescape(escaped).toString();
            //System.out.println(s + " " + escaped + " " + unescaped);
            assertEquals(s, unescaped);
        }
    }

}
