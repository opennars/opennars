package nars.op.software.scheme.expressions;


import nars.term.Atom;
import nars.util.utf8.Utf8;

public class SymbolExpression extends Atom implements Expression {


    //public final String value;

    public SymbolExpression(String value) {
        super(value);
    }

    public static SymbolExpression symbol(String s) {
        return new SymbolExpression(s);
    }

    @Override
    public boolean equals(final Object o) {
        return getClass() == o.getClass() &&
                Utf8.equals2(bytes(), ((SymbolExpression) o).bytes());
    }

    /*@Override
    public int hashCode() {
        return value.hashCode();
    }*/

    /*public String toString() {
        return String.format("symbol(%s)", value);
    }*/

    @Override
    public String print() {
        return toString();
    }
}
