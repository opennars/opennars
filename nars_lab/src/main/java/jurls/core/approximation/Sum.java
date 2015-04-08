/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.approximation;

/**
 *
 * @author thorsten
 */
public class Sum implements DiffableFunctionSource {

    private final DiffableFunctionSource[] xs;

    public Sum(DiffableFunctionSource... xs) {
        this.xs = xs;
    }

    @Override
    public String valueToSource(SourceEnvironment se) {
        String sum = se.allocateVariable();

        se.assign(sum).append("0;").nl();
        for (DiffableFunctionSource x : xs) {
            String v = x.valueToSource(se);
            se.additiveAssign(sum).append(v).append(";").nl();
        }

        return sum;
    }

    @Override
    public String partialDeriveToSource(SourceEnvironment se) {
        String sum = se.allocateVariable();

        se.assign(sum).append("0;").nl();
        for (DiffableFunctionSource x : xs) {
            String v = x.partialDeriveToSource(se);
            se.additiveAssign(sum).append(v).append(";").nl();
        }

        return sum;
    }

}
