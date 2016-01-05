/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objenome.op;

/**
 * 
 * @author thorsten
 */
public interface DiffableFunction {
	double value();
	double partialDerive(Scalar parameter);
}
