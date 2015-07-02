/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objenome.solver;

import objenome.Multitainer;
import objenome.problem.Problem;

import java.util.Arrays;

/**
 *
 * @author me
 */
public class IncompleteSolutionException extends Exception {

    public IncompleteSolutionException(Iterable<Problem> p, Object[] keys, Multitainer g) {
        super("Missing solution(s) for " + p + " to build " + Arrays.toString(keys) + " in " + g + ": " + g);
    }
    
}
