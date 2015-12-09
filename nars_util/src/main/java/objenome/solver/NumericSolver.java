/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objenome.solver;

import objenome.Multitainer;
import objenome.Objenome;
import objenome.goal.DecideNumericValue;
import objenome.problem.Problem;
import objenome.solution.SetNumericValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 *
 * @author me
 */
public abstract class NumericSolver<C> implements Solver  {
    
    public final Function<C, Double> function;
    public final Class<? extends C> model;

    public NumericSolver(Class<? extends C> model, Function<C, Double> function) {
        this.model = model;
        this.function = function;
        
    }
    
    public abstract void solve(Objenome o, List<SetNumericValue> variables);

    @Override
    public void solve(Multitainer g, Map<Problem, Solution> p, Object[] targets) {
        List<SetNumericValue> variables = new ArrayList();
        
        //store backup in case it needs restored
        Map originalProblems = new HashMap(p);
        
        for (Map.Entry<Problem, Solution> e : p.entrySet()) {
            if (e.getValue() == null) {
                if (!(e.getKey() instanceof DecideNumericValue)) {
                    //wont be able to solve with non-numerical ambiguities
                    return;
                }
            }
       }

        p.entrySet().stream().filter(e -> e.getValue() == null).filter(e -> e.getKey() instanceof DecideNumericValue).forEach(e -> {
            SetNumericValue v = ((DecideNumericValue) e.getKey()).newDefaultSetValue();
            variables.add(v);
            e.setValue(v);
        });
        
        if (variables.isEmpty()) {
            return;
        }


        try {
            Objenome o = g.solve(targets, p);
            solve(o, variables);
            
        } catch (IncompleteSolutionException ex) {
            p.clear();
            p.putAll(originalProblems);
        }
        
    }

    
    protected double eval(Objenome o) {
        o.commit();        
        return function.apply(o.get(model));
    }
    
    
    /** var = gene, from different casts */
    public static double getMin(SetNumericValue var, Solution gene) {
        return var.getMin().doubleValue();
    }
    
    /** var = gene, from different casts */
    public static double getMax(SetNumericValue var, Solution gene) {
        return var.getMax().doubleValue();
    }    
}
