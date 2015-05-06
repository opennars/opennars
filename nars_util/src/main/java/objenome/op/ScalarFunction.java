/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objenome.op;

/**
 *
 */
abstract public class ScalarFunction<X extends Node> extends Node<X,Double> implements DiffableFunction {
    
 
    
    @Override
    public String getIdentifier() {
        return getClass().getSimpleName().toUpperCase();
    }

    @Override
    public Double evaluate() {
        return value();
    }
    
    
    
}
