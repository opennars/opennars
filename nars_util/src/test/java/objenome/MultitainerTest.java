/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open of template in of editor.
 */
package objenome;

import objenome.goal.Between;
import objenome.solution.SetImplementationClass;
import objenome.solution.SetIntegerValue;
import objenome.solution.SetNumericValue;
import objenome.solver.Solution;
import org.junit.Test;

import java.util.List;

import static objenome.solution.dependency.Builder.of;
import static objenome.solution.dependency.Builder.the;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author me
 */
public class MultitainerTest {

    public interface Part {
        int function();
    }
    public interface SubPart { int function();    }

    public static class SubPart0 implements SubPart {
        @Override public int function() { return 0; }
    }
    public static class SubPart1 implements SubPart {
        @Override public int function() {  return 1;}         
    }
    
    public static class Part0 implements Part {
        @Override public int function() { return 0;  }
        @Override public String toString() { return getClass().getSimpleName(); }
    }
    public static class Part1 implements Part {
        @Override public int function() { return 1; }
        @Override public String toString() { return getClass().getSimpleName(); }
    }
    public static class PartN implements Part {
        private final int value;

        public PartN( @Between(min=1, max=3) int arg0) {
            value = arg0;
        }
        
        @Override public int function() { return value; }
        @Override public String toString() { return getClass().getSimpleName(); }
    }
    public static class PartWithSubPart implements Part {
        private final SubPart subcomp;
        public PartWithSubPart(SubPart subcomponent) {
            subcomp = subcomponent;
        }        
        @Override public int function() { return subcomp.function();  }
    }

    public static class Machine {
        public final Part part;
        
        public Machine(Part p) {
            part = p;
        }
        public int function() {
            return part.function();
        }

        @Override
        public String toString() {
            return part.toString();
        }
        
    }
    
//    public static class ParametricMachine {
//        public final Part part;
//        
//        public Machine(Part p, double value) {
//            this.part = p;
//        }
//        public int function() {
//            return part.function();
//        }
//    }
    
    

    public static class MachineWithParametricPart {
        public final PartWithSubPart part;
        
        public MachineWithParametricPart(PartWithSubPart p) {
            part = p;
        }
        public int function() {
            return part.function();
        }
    }
    
    
    /** one gene to select between two interfaces with non-parametric constructors */
    @Test public void testSimpleSolutionGeneration() {
        Multitainer c = new Multitainer();
        c.any(Part.class, of(Part0.class, Part1.class));
                        
        Objenome o = c.random(Machine.class);
        assertEquals("obgenome contains one gene to select betwen the implementations of interface Part", 1, o.getSolutionSize());
        
        List<Solution> genes = o.getSolutions();
        
        assertEquals(SetImplementationClass.class, genes.get(0).getClass());
        
    }
    
    
    @Test public void testSimpleSolutionGeneration1() {
        Multitainer c = new Multitainer();
        c.any(Part.class, of(Part0.class, Part1.class));
                        
        Objenome o = c.random(Part.class);
        
        assertEquals(1, o.getSolutionSize());        
        
        List<Solution> genes = o.getSolutions();
        
        assertEquals(SetImplementationClass.class, genes.get(0).getClass());        
    }
    
    //18553247676
    @Test public void testSimpleSolutionGeneration11() {
        Multitainer c = new Multitainer();
        c.any(Part.class, the(PartN.class));
                        
        Objenome o = c.random(Part.class);
        List<Solution> genes = o.getSolutions();
        
        assertEquals(1, o.getSolutionSize());        
        assertEquals(SetIntegerValue.class, genes.get(0).getClass());
                
    }
    
    
    /** one gene to select between two interfaces with parametric constructor in one of of dependencies */
    @Test public void testSimpleSolutionGeneration2() {
        
        Multitainer c = new Multitainer();
        c.any(Part.class, of(Part0.class, Part1.class, PartN.class));
        Objenome o = c.random(Machine.class);
        
        assertEquals("obgenome contains 2 genes: a) to select betwen the implementations of interface Part, and b) to set the int parameter for PartN if that needs instantiated", 2, o.getSolutionSize());
        
        List<Solution> genes = o.getSolutions();
        
        assertEquals(SetImplementationClass.class, genes.get(0).getClass());
                         
        assertEquals(SetIntegerValue.class, genes.get(1).getClass());        
                        
        //assertEquals("[ClassBuilder[class objenome.GenetainerTest$Machine], objenome.GenetainerTest$Part arg0 (part), ClassBuilder[class objenome.GenetainerTest$PartN], int arg0]", ((SetConstantValue)genes.get(1)).getPath().toString());
    }
    
    @Test public void testRecurse2LevelsGeneration() {
        
        Multitainer c = new Multitainer();
        c.usable(Part.class, PartWithSubPart.class);
        c.any(SubPart.class, of(SubPart0.class, SubPart1.class));
        Objenome o = c.random(Machine.class);
        
        List<Solution> genes = o.getSolutions();
        
        assertEquals("obgenome contains 1 gene: to select between subcomponents of the part component", 1, o.getSolutionSize());
        assertEquals(SetImplementationClass.class, genes.get(0).getClass());
    }
    
    @Test public void testMultitypeRecurse() {
        
        Multitainer c = new Multitainer();
        c.any(Part.class, 
                    of(Part0.class, Part1.class, PartN.class, PartWithSubPart.class));
        c.any(SubPart.class, 
                    of(SubPart0.class, SubPart1.class));
        Objenome o = c.random(Machine.class);
                       
        List<Solution> genes = o.getSolutions();
        
        System.out.println(genes);
        assertEquals(3, o.getSolutionSize());
        assertEquals(SetImplementationClass.class, genes.get(0).getClass());
        assertEquals(SetImplementationClass.class, genes.get(1).getClass());
        assertEquals(SetIntegerValue.class, genes.get(2).getClass());
        assertEquals(3, ((SetNumericValue)genes.get(2)).getMax().intValue());
        assertEquals(1, ((SetNumericValue)genes.get(2)).getMin().intValue());
    }
    

    


}
