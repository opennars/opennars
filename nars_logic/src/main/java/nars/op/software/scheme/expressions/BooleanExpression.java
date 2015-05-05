package nars.op.software.scheme.expressions;

public class BooleanExpression implements Expression {
    public final boolean value;

    public BooleanExpression(boolean value) {
        this.value = value;
    }

    public static BooleanExpression bool(boolean value) {
        return new BooleanExpression(value);
    }

    @Override
    public boolean equals(Object o) {
        return getClass() == o.getClass() && value == ((BooleanExpression) o).value;
    }

    @Override
    public int hashCode() {
        return (value ? 1 : 0);
    }

    @Override
    public String toString() {
        return String.format("bool(%s)", value);
    }

    @Override
    public String print() {
        return value ? "#t" : "#f";
    }
}
