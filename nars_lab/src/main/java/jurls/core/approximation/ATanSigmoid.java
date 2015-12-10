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
public class ATanSigmoid implements DiffableFunctionSource {

    private final DiffableFunctionSource x;

    public ATanSigmoid(DiffableFunctionSource x) {
        this.x = x;
    }

    @SuppressWarnings("HardcodedFileSeparator")
    @Override
    public String partialDeriveToSource(SourceEnvironment se) {
        String xv = x.valueToSource(se);
        String xdv = x.partialDeriveToSource(se);
        String y = se.allocateVariable();
        se.assign(y).append(xdv).
                append(" * 1.0 / (1.0 + ").append(xv).append(" * ").
                append(xv).append(");").nl();

        return y;
    }

    @Override
    public String valueToSource(SourceEnvironment se) {
        String xv = x.valueToSource(se);
        String y = se.allocateVariable();
        se.assign(y).append("Math.atan(").append(xv).append(");").nl();
        return y;
    }

}
