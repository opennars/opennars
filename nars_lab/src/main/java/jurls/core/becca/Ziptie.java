/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.becca;

abstract public class Ziptie {
    
    /**
     * 
     * @param signal input signal vector
     * @param result may be null, in which case a new array will be allocated
     * @return double[] result
     */
    abstract public double[] in(double[] signal, double[] result);

}
