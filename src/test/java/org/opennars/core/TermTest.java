/*
 * The MIT License
 *
 * Copyright 2018 The OpenNARS authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.opennars.core;

import org.junit.Test;
import org.opennars.entity.Concept;
import org.opennars.io.Narsese;
import org.opennars.io.Symbols.NativeOperator;
import org.opennars.io.Texts;
import org.opennars.language.CompoundTerm;
import org.opennars.language.Inheritance;
import org.opennars.language.Statement;
import org.opennars.language.Term;
import org.opennars.main.Nar;
import org.opennars.main.MiscFlags;
import org.opennars.operator.Operation;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.NavigableSet;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 *
 */
public class TermTest {
    
    final Nar n = new Nar();
    final Narsese np = new Narsese(n);

    public TermTest() throws IOException, InstantiationException, InvocationTargetException, NoSuchMethodException, ParserConfigurationException, IllegalAccessException, SAXException, ClassNotFoundException, ParseException {
    }

    protected void assertEquivalent(final String term1String, final String term2String) throws IOException, InstantiationException, InvocationTargetException, NoSuchMethodException, ParserConfigurationException, IllegalAccessException, SAXException, ClassNotFoundException, ParseException {
        final Nar n = new Nar();

        try {
            final Term term1 = np.parseTerm(term1String);
            final Term term2 = np.parseTerm(term2String);

            assertTrue(term1 instanceof CompoundTerm);
            assertTrue(term2 instanceof CompoundTerm);
            assertTrue(!term1String.equals(term2String));

            assertTrue(term1.hashCode() == term2.hashCode());
            assertTrue(term1.equals(term2));
            assertTrue(term1.compareTo(term2)==0);
        } catch (final Narsese.InvalidInputException e) {
            throw new IllegalStateException("Invalid test string.", e);
        }
    }
    
    @Test
    public void testCommutativeCompoundTerm() throws IOException, InstantiationException, InvocationTargetException, NoSuchMethodException, ParserConfigurationException, IllegalAccessException, SAXException, ClassNotFoundException, ParseException {
        final Nar n = new Nar();

        assertEquivalent("(&&,a,b)", "(&&,b,a)");
        assertEquivalent("(&&,(||,b,c),a)", "(&&,a,(||,b,c))");
        assertEquivalent("(&&,(||,c,b),a)", "(&&,a,(||,b,c))");
        
    }
    
    @Test
    public void testTermSort() throws Exception {
        final Nar n = new Nar();
        
        final Narsese m = new Narsese(n);
        final Term a = m.parseTerm("a");
        final Term b = m.parseTerm("b");
        final Term c = m.parseTerm("c");

        assertEquals(3, Term.toSortedSetArray(a, b, c).length);
        assertEquals(2, Term.toSortedSetArray(a, b, b).length);
        assertEquals(1, Term.toSortedSetArray(a, a).length);
        assertEquals(1, Term.toSortedSetArray(a).length);
        assertEquals("correct natural ordering", a, Term.toSortedSetArray(a, b)[0]);
    }    
    
    @Test
    public void testConjunctionTreeSet() throws Narsese.InvalidInputException, IOException, InstantiationException, InvocationTargetException, NoSuchMethodException, ParserConfigurationException, IllegalAccessException, SAXException, ClassNotFoundException, ParseException {
        final Nar n = new Nar();
        
        
            
        //these 2 representations are equal, after natural ordering
        final String term1String =    "<#1 --> (&,boy,(/,taller_than,{Tom},_))>";
        final Term term1 = np.parseTerm(term1String);
        final String term1Alternate = "<#1 --> (&,(/,taller_than,{Tom},_),boy)>";
        final Term term1a = np.parseTerm(term1Alternate);
        

        // <#1 --> (|,boy,(/,taller_than,{Tom},_))>
        final Term term2 = np.parseTerm("<#1 --> (|,boy,(/,taller_than,{Tom},_))>");

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

        final boolean t1e2 = term1.equals(term2);
        final int t1c2 = term1.compareTo(term2);
        final int t2c1 = term2.compareTo(term1);

        assertTrue(!t1e2);
        assertTrue("term1 and term2 inequal, so t1.compareTo(t2) should not = 0", t1c2!=0);
        assertTrue("term1 and term2 inequal, so t2.compareTo(t1) should not = 0", t2c1!=0);

        /*
        System.out.println("t1 equals t2 " + t1e2);
        System.out.println("t1 compareTo t2 " + t1c2);
        System.out.println("t2 compareTo t1 " + t2c1);
        */

        final NavigableSet<Term> set = new TreeSet<>();
        final boolean added1 = set.add(term1.clone());
        final boolean added2 = set.add(term2.clone());
        assertTrue("term 1 added to set", added1);
        assertTrue("term 2 added to set", added2);

        assertTrue(set.size() == 2);
        
    }
    
    @Test
    public void testUnconceptualizedTermInstancing() throws Narsese.InvalidInputException, IOException, InstantiationException, InvocationTargetException, NoSuchMethodException, ParserConfigurationException, IllegalAccessException, SAXException, ClassNotFoundException, ParseException {
       final Nar n = new Nar();
        
       final String term1String ="<a --> b>";
       final Term term1 = np.parseTerm(term1String);
       final Term term2 = np.parseTerm(term1String);
       
       assertTrue(term1.equals(term2));
       assertTrue(term1.hashCode() == term2.hashCode());
       
       final CompoundTerm cterm1 = ((CompoundTerm)term1);
       final CompoundTerm cterm2 = ((CompoundTerm)term2);

       //test subterms
       assertTrue(cterm1.term[0].equals(cterm2.term[0])); //'a'

    }
    
    @Test
    public void testConceptInstancing() throws Narsese.InvalidInputException, IOException, InstantiationException, InvocationTargetException, NoSuchMethodException, ParserConfigurationException, IllegalAccessException, SAXException, ClassNotFoundException, ParseException {
       final Nar n = new Nar();
        
       final String statement1 = "<a --> b>.";
       
       final Term a = np.parseTerm("a");
       assertTrue(a!=null);
       final Term a1 = np.parseTerm("a");
       assertTrue(a.equals(a1));
       
       n.addInput(statement1);       
       n.cycles(4);
              
       n.addInput(" <a  --> b>.  ");
       n.cycles(1);
       n.addInput(" <a--> b>.  ");
       n.cycles(1);
       
       final String statement2 = "<a --> c>.";
       n.addInput(statement2);
       n.cycles(4);
       
       final Term a2 = np.parseTerm("a");
       assertTrue(a2!=null);
                     
       final Concept ca = n.memory.concept(a2);
       assertTrue(ca!=null);
       
       assertEquals(true, n.memory.concepts.iterator().hasNext());

    }    

    @Test
    public void invalidTermIndep() throws Narsese.InvalidInputException, IOException, InstantiationException, InvocationTargetException, NoSuchMethodException, ParserConfigurationException, IllegalAccessException, SAXException, ClassNotFoundException, ParseException {
        
        final String t = "<$1 --> (~,{place4},$1)>";
        final Nar n = new Nar();
        final Narsese p = new Narsese(n);
        
        Term subj = null, pred = null;

        subj = p.parseTerm("$1");
        pred = p.parseTerm("(~,{place4},$1)");
        
            
        final Statement s = Statement.make(NativeOperator.INHERITANCE, subj, pred, false, 0);
        assertEquals(null, s);

        final Inheritance i = Inheritance.make(subj, pred);
        assertEquals(null, i);

        final CompoundTerm forced = (CompoundTerm) p.parseTerm("<a --> b>");
        assertTrue(true);

        forced.term[0] = subj;
        forced.term[1] = pred;
        forced.invalidateName();

        assertEquals(t, forced.toString());

        final CompoundTerm cloned = forced.clone();
        assertEquals(null, cloned);
    }
    
    
    @Test public void testParseOperationInFunctionalForm() throws Narsese.InvalidInputException, IOException, InstantiationException, InvocationTargetException, NoSuchMethodException, ParserConfigurationException, IllegalAccessException, SAXException, ClassNotFoundException, ParseException {
        
        final Nar n = new Nar();
        final Narsese p = new Narsese(n);

        final Term x = p.parseTerm("wonder(a,b)");
        assertEquals(Operation.class, x.getClass());
        assertEquals("(^wonder,a,b)", x.toString());
    }
}
