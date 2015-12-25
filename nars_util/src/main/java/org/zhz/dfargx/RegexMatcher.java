package org.zhz.dfargx;

import org.zhz.dfargx.automata.DFA;
import org.zhz.dfargx.automata.NFA;
import org.zhz.dfargx.tree.SyntaxTree;

/**
 * Created on 2015/5/11.
 */
public class RegexMatcher {
    private int[][] transitionTable;
    private int is;
    private int rs;
    private boolean[] fs;

    public RegexMatcher(String regex) {
        compile(regex);
    }

    private void compile(String regex) {
        SyntaxTree syntaxTree = new SyntaxTree(regex);
        NFA nfa = new NFA(syntaxTree.getRoot());
        DFA dfa = new DFA(nfa.getStateList());
        transitionTable = dfa.getTransitionTable();
        is = dfa.getInitState();
        fs = dfa.getFinalStates();
        rs = dfa.getRejectedState();
    }

    public boolean match(String str) {
        int s = is;
        for (int i = 0, length = str.length(); i < length; i++) {
            char ch = str.charAt(i);
            s = transitionTable[s][ch];
            if (s == rs) {
                return false; // fast failed using rejected state
            }
        }
        return fs[s];
    }
}
