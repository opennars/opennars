/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objenome.solver;

import objenome.Multitainer;
import objenome.goal.DecideNumericValue;
import objenome.goal.DevelopMethod;
import objenome.problem.Problem;
import objenome.solution.*;
import objenome.solution.dependency.DecideImplementationClass;

import java.util.Map;

/**
 *
 * @author me
 */
public class RandomSolver implements Solver {

    private SetMethodsGPEvolved gpEvolveMethods;

    @Override
    public void solve(Multitainer g, Map<Problem, Solution> p, Object[] targets) {
        for (Map.Entry<Problem, Solution> e : p.entrySet()) {
            Solution existingSolution = e.getValue();
            if (existingSolution == null) {
                e.setValue(getSolution(e.getKey()));
            }
        }
    }

    public Solution getSolution(Problem p) {
        if (p instanceof DecideNumericValue) {
            if (p instanceof DecideNumericValue.DecideBooleanValue) {
                return new SetBooleanValue((DecideNumericValue.DecideBooleanValue) p, Math.random() < 0.5);
            } else if (p instanceof DecideNumericValue.DecideIntegerValue) {
                return new SetIntegerValue((DecideNumericValue.DecideIntegerValue) p, Math.random());
            } else if (p instanceof DecideNumericValue.DecideDoubleValue) {
                return new SetDoubleValue((DecideNumericValue.DecideDoubleValue) p, Math.random());
            }
        } else if (p instanceof DecideImplementationClass) {
            return new SetImplementationClass((DecideImplementationClass) p, Math.random());
        } else if (p instanceof DevelopMethod) {
            if (gpEvolveMethods == null) {
                gpEvolveMethods = new SetMethodsGPEvolved();
            }
            gpEvolveMethods.addMethodToDevelop((DevelopMethod) p);
            return gpEvolveMethods;
        }
        return null;
    }
    
    
    
}
