/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objenome.solver;


/** thrown by an empty static method to supply training examples to a learning method
 *  that can eventually implement the method by dynamic compilation.
 *  an 'intelligent' alternative to UnsupportedOperationException.
 */
public abstract class Behaviors extends RuntimeException {

    //TODO expression evaluators that can be returned as Behavior inputs and outputs
    
    public static class Behavior<X,Y> {

        public static final double DO = 1.0;
        public static final double DONT = 0.0;
        
        public final X[] input;
        public final Y output;
        
        /** from 0=do not to 1.0=always */
        public final double desirability;

        public Behavior(X[] input, Y output, double desirability) {
            this.input = input;
            this.output = output;
            this.desirability = desirability;            
        }
        
        public Behavior(X[] input, Y output, boolean desired) {
            this(input, output, desired ? 1.0 : 0.0);
        }
    }

    /** recommended training method, or null for automatic */
    public abstract Class[] training();

    public abstract Behavior[] behavior();
    
}
