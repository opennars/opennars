/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.approximation;

/**
 *
 * @author me
 */
public interface Functional<F extends Functional> {

    default public Object[] getDependencies() {
        return new Object[] { };
    }

    default public void replace(int index, F r) {
        throw new RuntimeException(this + ": Replace not supported: " + r + " in index" + index);
    }
}
