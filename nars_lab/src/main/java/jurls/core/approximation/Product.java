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
public class Product implements DiffableFunctionSource {

    private final DiffableFunctionSource a;
    private final DiffableFunctionSource b;

    public Product(DiffableFunctionSource a, DiffableFunctionSource b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public String valueToSource(SourceEnvironment se) {
        String av = a.valueToSource(se);
        String bv = b.valueToSource(se);
        String y = se.allocateVariable();

        se.assign(y).append(av).append(" * ").append(bv).append(";").nl();

        return y;
    }

    @Override
    public String partialDeriveToSource(SourceEnvironment se) {
        String av = a.valueToSource(se);
        String adv = a.partialDeriveToSource(se);
        String bv = b.valueToSource(se);
        String bdv = b.partialDeriveToSource(se);
        String y = se.allocateVariable();

        se.assign(y).append(av).append(" * ").append(bdv).
                append(" + ").append(adv).append(" * ").append(bv).
                append(";").nl();

        return y;
    }
}
