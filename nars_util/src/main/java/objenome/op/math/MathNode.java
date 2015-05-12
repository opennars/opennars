package objenome.op.math;

import objenome.op.Doubliteral;
import objenome.op.Literal;
import objenome.op.Node;

/**
 * Created by me on 5/6/15.
 */
abstract public class MathNode extends Node<Node, Double> {

    public final Literal zero = new Doubliteral(0.0);
    public final Literal one = new Doubliteral(1.0);
    public final Literal two = new Doubliteral(2.0);

    public MathNode(Node... x) {
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

    public double getChildEvaluated(final int childNum) {
        return getChild(childNum).asDouble();
    }

    /** the fast double-only version that should be implemented */
    abstract public double asDouble();

    @Override
    final public Double evaluate() {
        return asDouble();
    }
}
