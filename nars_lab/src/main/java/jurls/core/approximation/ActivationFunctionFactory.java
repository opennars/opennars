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
public interface ActivationFunctionFactory {

    DiffableFunctionSource newInstance(GeneratorContext gc, List<DiffableFunctionSource> xs);

    double getDelta();
}
