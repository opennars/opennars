/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.approximation;

/**
 * 
 * @author thorsten2
 */
public interface DiffableFunctionSource {

	String valueToSource(SourceEnvironment se);

	String partialDeriveToSource(SourceEnvironment se);
}
