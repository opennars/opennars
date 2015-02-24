/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.approximation;

import java.util.HashMap;
import java.util.Map;
import jurls.core.approximation.Scalar.AtomicScalar;

/**
 *
 * @author me
 */
public class DiffableSymbols {
    
    final Map<DiffableFunction, Integer> symbols = new HashMap();
    
    public final double[] input; //backing array in which indexed inputs cn find themselves

    public final double[] array; //backing array in which indexed param values cn find themselves

    public DiffableSymbols(final double[] input, final double[] param) {
        this.input = input;
        this.array = param;
    }
    
    
    /*final Map<Scalar, Integer> arrayed = new HashMap();

    public void arrayed(Scalar s, int index) {
        arrayed.put(s, index);
    }*/
    
    public int bind(DiffableFunction f) {
        Integer i = symbols.get(f);
        if (i != null) {
            return i;
        }
        int index = symbols.size();
        symbols.put(f, index);
        return index;
    }

    @Override
    public String toString() {
        return symbols.toString();
    }
    
    
    public AtomicScalar[] scalars() {
        AtomicScalar[] x = new AtomicScalar[symbols.size()]; 
        for (Map.Entry<DiffableFunction, Integer> e : symbols.entrySet()) {
            if (e.getKey() instanceof AtomicScalar) {
                x[e.getValue()] = (AtomicScalar)e.getKey();
            }
            else {
                throw new RuntimeException("Unsupported symbol type: " + e.getKey() + " (" + e.getKey().getClass() + ")" );
            }
        }
        return x;
    }
    
    
    
}
