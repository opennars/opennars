/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.approximation;

import java.util.List;
import jurls.core.approximation.Scalar.ArrayIndexScalar;

/**
 *
 * @author thorsten
 */
public interface DiffableFunctionGenerator {

    public DiffableFunction generate(
            Scalar[] inputs,
            double[] array,
            List<ArrayIndexScalar> parameterList,
            int numFeatures
    );
}
