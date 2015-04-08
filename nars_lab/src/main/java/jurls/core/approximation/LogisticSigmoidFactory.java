/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.approximation;

import java.util.List;

/**
 *
 * @author thorsten2
 */
public class LogisticSigmoidFactory implements ActivationFunctionFactory {

    @Override
    public DiffableFunctionSource newInstance(GeneratorContext gc, List<DiffableFunctionSource> xs) {
        return new LogisticSigmoid(new Sum(xs.toArray(new DiffableFunctionSource[0])));
    }

    @Override
    public double getDelta() {
        return -10;
    }

}
