package nars.op.software.scheme.expressions;

public class NumberExpression implements Expression {
    public final long value;

    public NumberExpression(long value) {
        this.value = value;
    }

    public static NumberExpression number(long n) {
        return new NumberExpression(n);
    }

    @Override
    public boolean equals(Object o) {
        return getClass() == o.getClass() && value == ((NumberExpression) o).value;
    }

    @Override
    public int hashCode() {
        return (int) (value ^ (value >>> 32));
    }

    public String toString() {
        return String.format("number(%s)", value);
    }

    @Override
    public String print() {
        return Long.toString(value);
    }
}
