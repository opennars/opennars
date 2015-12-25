package org.zhz.dfargx.node;

import org.zhz.dfargx.automata.NFA;
import org.zhz.dfargx.stack.OperatingStack;
import org.zhz.dfargx.stack.ShuntingStack;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created on 5/5/15.
 */
public abstract class Node {

    private Node left;
    private Node right;

    public Node() {
        left = right = null;
    }

    public Node right() {
        return right;
    }

    public Node left() {
        return left;
    }

    public boolean hasLeft() {
        return left != null;
    }

    public boolean ifHasLeft(Consumer<Node> ifHas) {
        return ifHas(true, ifHas);
    }

    public boolean ifHasRight(Consumer<Node> ifHas) {
        return ifHas(false, ifHas);
    }

    public boolean ifHas(boolean leftOrRight, Consumer<Node> ifHas) {
        Node n = leftOrRight ? left : right;
        if (n != null) {
            ifHas.accept(n);
            return true;
        }
        return false;
    }

    public boolean ifEmptyLeft(Supplier<Node> fillWith) {
        return ifEmpty(true, fillWith);
    }

    public boolean ifEmptyRight(Supplier<Node> fillWith) {
        return ifEmpty(false, fillWith);
    }
    public boolean ifEmpty(boolean leftOrRight, Supplier<Node> fillWith) {
        Node n = leftOrRight ? left : right;
        if (n == null) {
            n = fillWith.get();
            if (n!=null) {
                if (leftOrRight)
                    this.left = n;
                else
                    this.right = n;
                return true;
            }
        }
        return false;
    }

    public boolean hasRight() {
        return right != null;
    }

    public void setLeft(Node left) {
        this.left = left;
    }

    public void setRight(Node right) {
        this.right = right;
    }

    public abstract void accept(NFA nfa);

    public abstract Node copy();

    public abstract void accept(OperatingStack operatingStack);

    public abstract void accept(ShuntingStack shuntingStack);

    @Override
    public abstract String toString();
}
