package nars.op.software.scheme.expressions;


import nars.term.atom.Atom;

public class SymbolExpression extends Atom implements Expression {



    public SymbolExpression(String value) {
        super(value);
    }

    public static SymbolExpression symbol(String s) {
        return new SymbolExpression(s);
    }


    @Override
    public String print() {
        return toString();
    }

}
