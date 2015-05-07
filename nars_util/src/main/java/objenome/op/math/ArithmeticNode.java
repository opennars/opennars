package objenome.op.math;

import objenome.op.Literal;
import objenome.op.Node;

/**
 * Created by me on 5/6/15.
 */
abstract public class ArithmeticNode<X extends Node, Y extends Number> extends Node<X, Y> {

    public final Literal zero = new Literal(0.0);
    public final Literal one = new Literal(1.0);
    public final Literal two = new Literal(2.0);

    public ArithmeticNode(X... x) {
        super(x);
    }

    /** returns the constant (literal) double value
     * return Double.NaN if the child is not a literal
     */
    public double getChildConstantValue(final int childNum) {
        Node c = getChild(childNum);
        if (c instanceof Literal) {
            Object o = c.evaluate();
            if (o instanceof Number) {
                return ((Number)o).doubleValue();
            }
        }
        return Double.NaN;
    }



}
