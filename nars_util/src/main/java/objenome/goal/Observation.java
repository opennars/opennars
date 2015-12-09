/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objenome.goal;

/**
 *
 * @author me
 */
public class Observation<A,B> {
    
    public final double weight;
    public final A input;
    public final B output;

    public Observation(A input, B output) {
        this(input, output, 1.0);
    }
    
    public Observation(A input, B output, double weight) {
        this.input = input;
        this.output = output;
        this.weight = weight;
    }
    
    
}
