/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objenome.solution;

import objenome.solver.Solution;

/**
 * Indicates the gene produces a Number result (Ex: Integer, Double)
 */
public interface SetNumericValue extends Solution {

	Number getMin();
	Number getMax();

	Number getNumber();

	void setValue(double d);

}
