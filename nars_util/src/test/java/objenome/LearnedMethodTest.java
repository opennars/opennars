/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objenome;

import objenome.solver.Behaviors;
import org.apache.commons.math3.genetics.GeneticAlgorithm;

import static objenome.solver.Behaviors.Behavior.DO;
import static objenome.solver.Behaviors.Behavior.DONT;

/**
 *
 * @author me
 */
public class LearnedMethodTest {
 
    public static enum BooleanMath {
        ;


        public static int xor(int x, int y) {
            
            throw new Behaviors() {

                @Override public Class[] training() {
                    return new Class[] { 
                        GeneticAlgorithm.class 
                        /* FeedForwardNeuralNetwork.class, */ 
                        /* ApproximatePolynomial.class, */
                        //etc..
                    };
                }

                @Override
                public Behavior[] behavior() {
                    return new Behavior[] {
                        new Behavior(new Object[] { 0, 0 }, 0, DO),
                        new Behavior(new Object[] { 0, 1 }, 1, DO),
                        new Behavior(new Object[] { 1, 0 }, 1, DO),
                        new Behavior(new Object[] { 1, 1 }, 0, DO),
                        
                        new Behavior(new Object[] { 0, 0 }, 1, DONT),
                        new Behavior(new Object[] { 0, 1 }, 0, DONT),
                        new Behavior(new Object[] { 1, 0 }, 0, DONT),
                        new Behavior(new Object[] { 1, 1 }, 1, DONT),                        
                    };
                }
                
            };
            
        }        
        
    }
    
}
