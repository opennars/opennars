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
import nars.core.NAR;
import nars.io.TextPerception;
import nars.io.TextPerception.InvalidInputException;
import nars.language.Inheritance;
import nars.language.Term;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author me
 */
public class TermTest {

    
    @Test
    public void testConjunctionTreeSet() {
        NAR n = new NAR();
        
        try {           
            
            String term1String ="<#1 --> (&,boy,(/,taller_than,{Tom},_))>";
            Term term1 = TextPerception.parseTerm(term1String, n.memory);

            // <#1 --> (|,boy,(/,taller_than,{Tom},_))>
            Term term2 = TextPerception.parseTerm("<#1 --> (|,boy,(/,taller_than,{Tom},_))>", n.memory);
   
            assertTrue(term1.toString().equals(term1String));
            assertTrue(term1.getComplexity() > 1);
            assertTrue(term1.getComplexity() == term2.getComplexity());
                        
            assertTrue(term1.getClass().equals(Inheritance.class));
            assertTrue(term1.getClass().equals(Inheritance.class));
            
            
            //System.out.println("t1: " + term1 + ", complexity=" + term1.getComplexity());
            //System.out.println("t2: " + term2 + ", complexity=" + term2.getComplexity());
            
            
            assertTrue(term1.equals(term1.clone()));
            assertTrue(term1.compareTo((Term)term1.clone())==0);            
            assertTrue(term2.equals(term2.clone()));
            assertTrue(term2.compareTo((Term)term2.clone())==0);
            
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
            
            TreeSet<Term> set = new TreeSet<Term>();
            boolean added1 = set.add((Term) term1.clone());
            boolean added2 = set.add((Term) term2.clone());
            assertTrue("term 1 added to set", added1);
            assertTrue("term 2 added to set", added2);
            
            assertTrue(set.size() == 2);
        }
        catch (InvalidInputException e) {
            assertTrue(e.toString(), false);
        }
    }
}
