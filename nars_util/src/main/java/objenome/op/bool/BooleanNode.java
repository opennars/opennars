package objenome.op.bool;

import objenome.op.Literal;
import objenome.op.Node;

/**
 * Created by me on 5/6/15.
 */
public abstract class BooleanNode<N extends Node> extends Node<N, Boolean> {

    public static final Literal False = new Literal(false);
    public static final Literal True = new Literal(true);

    @SafeVarargs
    public BooleanNode(N... n) {
        super(n);
    }

    /** returns the constant (literal) boolean value (0 = false, 1 = true) of a child, if it exists.
     * return -1 if the child is not a literal
     */
    public int getChildConstantValue(int childNum) {
        Node c = getChild(childNum);
        if (c instanceof Literal) {
            boolean b = (Boolean)c.evaluate();
            return b ? 1 : 0;
        }
        return -1;
    }
}
