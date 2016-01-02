/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objenome.goal.numeric;

import objenome.Objenome;
import objenome.solution.SetNumericValue;
import objenome.solver.NumericSolver;
import org.apache.commons.math3.analysis.solvers.BisectionSolver;

import java.util.List;
import java.util.function.Function;

/**
 * Find zeros of a scalar function within a range
 * Currently, the search ranges can be overriden by overriding getMin and getMax methods
 */
public class FindZeros<C> extends NumericSolver<C> {
    
    public FindZeros(Class<? extends C> model, Function<C, Double> function) {
        super(model, function);
    }
    
    @Override
    public void solve(Objenome o, List<SetNumericValue> variables) {
        if (variables.size() == 1) {
            BisectionSolver solver = new BisectionSolver();
            //bind variables values to objenome
            
            SetNumericValue var = variables.get(0);

            double best = solver.solve(1000, d -> {
                var.setValue(d);
                return eval(o);
            }, getMin(var, var), getMax(var, var)); //var.getMin().doubleValue(), var.getMax().doubleValue());
            
            var.setValue(best);
        } else {
            throw new RuntimeException("Unknown how to solve objenome " + o);
        }
        
    }


}
