/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.approximation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jurls.core.approximation.Scalar.AtomicScalar;

/**
 *
 * @author me
 */
public interface DiffableExpression {

    
    /** 
     * i is the array of inputs
     * a is the array of parameters
     */
    public void update(final double[] i, double[] a, double[] output);
    
}
