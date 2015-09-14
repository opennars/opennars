/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open of template in of editor.
 */
package objenome;

import objenome.MultitainerTest.*;
import objenome.solution.SetImplementationClass;
import objenome.solution.SetIntegerValue;
import objenome.solution.dependency.Builder;
import objenome.solution.dependency.ClassBuilder;
import objenome.solution.dependency.DecideImplementationClass;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static objenome.solution.dependency.Builder.of;
import static org.junit.Assert.*;

/**
 *
 * @author me
 */
public class PhenotainerTest {

    
    @Test public void testIntegerParameterObjosome() {
        Multitainer g = new Multitainer();

        Builder builder = g.any(Part.class, of(PartN.class));
        assertTrue("an any expression of one element is reduced to an autowired 'use()'", (builder instanceof ClassBuilder) && (!(builder instanceof DecideImplementationClass)));
                        
        Objenome o = g.random(Part.class);

        SetIntegerValue partSelect = (SetIntegerValue)o.getSolutions().get(0);
        
        Part m = o.get(Part.class);
        assertNotNull(m);
        
        int expectedResult = partSelect.getValue();
        
        assertEquals(expectedResult, m.function());        
        
    }
    
    
    @Test public void testSimpleObjosome() {
        Multitainer g = new Multitainer();

        g.any(Part.class, of(Part0.class, Part1.class));
                        
        Objenome o = g.random(Machine.class);

        assertEquals(1, o.getSolutionSize());
        
        SetImplementationClass partSelect = (SetImplementationClass)o.getSolutions().get(0);
        
        Machine m = o.get(Machine.class);
        
        int expectedResult = partSelect.getValue() == Part0.class ? 0 : 1;
        
        assertEquals(expectedResult, m.function());        
        
        Machine possiblyDifferent = o.mutate().get(Machine.class);
        assertTrue(possiblyDifferent!=null);
        
        
    }

    @Test public void testMultiGene() {
        Multitainer g = new Multitainer();

        g.any(Part.class, of(Part0.class, Part1.class, PartN.class));
                        
        Objenome o = g.random(Machine.class);
        
        assertEquals(2, o.getSolutionSize());

        
                
        Machine m = o.get(Machine.class);
        assertNotNull(m);
        

        
        Machine n = o.get(Machine.class);                
        assertNotNull(n);
        assertTrue(n.function() > -1);
        
    }

    @Test public void testRecursiveAuto() { testMultiGeneRecursive(false);     }
    @Test public void testRecursiveManual() { testMultiGeneRecursive(true);    }
            
    protected void testMultiGeneRecursive(boolean includeUnnecessaryTargetWhichCouldBeDiscoveredAutomagically) {
        
        Multitainer g = new Multitainer();

        g.any(PartWithSubPart.class, of(SubPart0.class, SubPart1.class));
        g.any(Part.class, of(Part0.class, Part1.class, PartN.class));

        //find Part dependency of Machine recursively without being specified
        
        Objenome o = 
                includeUnnecessaryTargetWhichCouldBeDiscoveredAutomagically ?
                    g.random(Machine.class, Part.class) :
                    g.random(Machine.class);
        
        System.out.println(o.getSolutions());
        
        assertEquals("one solution for subpart impl choice, one for part impl choice, and 3rd for PartN parameter", 3, o.getSolutionSize());

        Container c = o.container();
        
        for (Builder b : c.getBuilders().values()) {
            assertTrue(!(b instanceof DecideImplementationClass));                
        }
        
        Machine m = o.get(Machine.class);
                
        assertTrue(m.function() > -1);
    }
    
    @Test public void testReuse() {
        Multitainer g = new Multitainer();

        g.any(Part.class, of(Part0.class, Part1.class, PartN.class));
                        
        Objenome o = g.random(Machine.class);

        Set<Class<?>> uniqueClasses = new HashSet<>();
        for (int i = 0; i < 55; i++) {  
            
            Container c = o.container();
            Machine m = c.get(Machine.class);
            
            
            assertEquals("iteration " + i,  ((SetImplementationClass)o.getSolutions().get(0)).getValue(), m.part.getClass() );
            uniqueClasses.add(m.part.getClass());
            
            o.mutate();            
        }
        
        assertEquals(3, uniqueClasses.size());
        
        
        
    }    
}
