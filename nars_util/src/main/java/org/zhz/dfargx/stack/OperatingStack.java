package org.zhz.dfargx.stack;

import org.zhz.dfargx.node.BranchNode;
import org.zhz.dfargx.node.LeafNode;
import org.zhz.dfargx.node.Node;

import java.util.ArrayDeque;

/**
 * Created on 2015/5/9.
 */
public class OperatingStack {

    private final ArrayDeque<Node> stack;

    public OperatingStack() {
        this.stack = new ArrayDeque<>();
    }

    public void visit(LeafNode leafNode) {
        stack.push(leafNode);
    }

    public void visit(BranchNode branchNode) {
        ArrayDeque<Node> st = this.stack;
        Node right = st.pop();
        Node left = st.pop();
        branchNode.operate(left, right);
        st.push(branchNode);
    }

    public Node pop() {
        return stack.pop();
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    @Override
    public String toString() {
        return "OperatingStack{" +
                "stack=" + stack +
                '}';
    }
}
