/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.becca;

import com.google.common.util.concurrent.AtomicDouble;
import static java.lang.Math.pow;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import jurls.core.approximation.Scalar;
import org.apache.commons.math3.linear.DefaultRealMatrixPreservingVisitor;
import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

abstract public class Ziptie {
    
    /**
     * 
     * @param signal input signal vector
     * @param result may be null, in which case a new array will be allocated
     * @return double[] result
     */
    abstract public double[] in(double[] signal, double[] result);

}
