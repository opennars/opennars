/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objenome.solver;

import java.util.Map;
import objenome.Multitainer;
import objenome.goal.Problem;

/**
 *
 * @author me
 */
public interface Solver {

    public void solve(Multitainer g, Map<Problem, Solution> p, Object[] targets);
    
}
