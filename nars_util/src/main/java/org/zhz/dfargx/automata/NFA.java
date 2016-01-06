package org.zhz.dfargx.automata;

import nars.util.data.list.FasterList;
import org.zhz.dfargx.node.*;

import java.util.List;
import java.util.Stack;

import static org.zhz.dfargx.automata.NFAStateFactory.create;

/**
 * Created on 2015/5/10.
 */
public class NFA     { // only able to accept wfs accessing order, this class construct a NFA with iterate the syntax tree recursively from the root node.

    private final Stack<NFAState> stateStack;

    final List<NFAState> stateList;

    public NFA(Node root) {
        super();

        stateList = new FasterList();
        NFAState initState = newState();
        NFAState finalState = newState();
        stateStack = new Stack<>();
        stateStack.push(finalState);
        stateStack.push(initState);
        dfs(root);
    }

    private NFAState newState() {
        NFAState nfaState = create();
        stateList.add(nfaState);
        return nfaState;
    }

    private void dfs(Node node) {
        node.accept(this);
        if (node.hasLeft()) {
            dfs(node.left());
            dfs(node.right());
        }
    }

    public List<NFAState> getStateList() {
        return stateList;
    }

    public void visit(LChar lChar) {
        Stack<NFAState> ss = this.stateStack;
        NFAState i = ss.pop();
        NFAState f = ss.pop();
        i.transitionRule(lChar.c, f);
    }

    public void visit(LNull lNull) {
        // do nothing
    }

    public void visit(BOr bOr) {
        Stack<NFAState> ss = this.stateStack;
        NFAState i = ss.pop();
        NFAState f = ss.pop();
        ss.push(f);
        ss.push(i);
        ss.push(f);
        ss.push(i);
    }

    public void visit(BConcat bConcat) {
        Stack<NFAState> ss = this.stateStack;
        NFAState i = ss.pop();
        NFAState f = ss.pop();
        NFAState n = newState();
        ss.push(f);
        ss.push(n);
        ss.push(n);
        ss.push(i);
    }

    public void visit(BMany bMany) {
        Stack<NFAState> ss = this.stateStack;
        NFAState i = ss.pop();
        NFAState f = ss.pop();
        NFAState n = newState();
        i.directRule(n);
        n.directRule(f);
        ss.push(n);
        ss.push(n);
    }

    public void visit(LClosure lClosure) {
        Stack<NFAState> ss = this.stateStack;
        NFAState i = ss.pop();
        NFAState f = ss.pop();
        i.directRule(f);
    }
}
