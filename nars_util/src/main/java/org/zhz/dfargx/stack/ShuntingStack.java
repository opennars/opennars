package org.zhz.dfargx.stack;

import org.zhz.dfargx.node.BranchNode;
import org.zhz.dfargx.node.LeafNode;
import org.zhz.dfargx.node.Node;
import org.zhz.dfargx.node.bracket.LeftBracket;
import org.zhz.dfargx.node.bracket.RightBracket;
import org.zhz.dfargx.util.InvalidSyntaxException;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EmptyStackException;
import java.util.List;

/**
 * Created on 2015/5/9.
 */
public class ShuntingStack { // powered by Shunting Yard Algorithm.
    private final ArrayDeque<Node> finalStack;
    private final ArrayDeque<BranchNode> branchStack;

    public ShuntingStack() {
        finalStack = new ArrayDeque<>();
        branchStack = new ArrayDeque<>();
    }

    public void visit(LeftBracket leftBracket) {
        branchStack.push(leftBracket);
    }

    public void visit(RightBracket rightBracket) {
        try {
            for (Node node = branchStack.pop(); !(node instanceof LeftBracket); node = branchStack.pop()) {
                finalStack.push(node);
            }
        } catch (EmptyStackException e) {
            throw new InvalidSyntaxException(e);
        }
    }

    public void visit(LeafNode leafNode) {
        finalStack.push(leafNode);
    }

    public void visit(BranchNode branchNode) {
        Deque<BranchNode> bs = this.branchStack;
        Deque<Node> fs = this.finalStack;

        int p;
        while (!bs.isEmpty() &&
                (p = branchNode.getPri()) != -1 &&
                (p <= bs.peek().getPri()) ) {
            fs.push(bs.pop());
        }
        bs.push(branchNode);
    }

    public void finish(List<Node> result) {

        ArrayDeque<BranchNode> bs = this.branchStack;
        ArrayDeque<Node> fs = this.finalStack;

        bs.forEach(fs::push);
        bs.clear();

        result.addAll(fs);
        //Lists.reverse(result);
        fs.clear();
    }

    @Override
    public String toString() {
        return "ShuntingStack{" +
                "finalStack=" + finalStack +
                '}';
    }
}
