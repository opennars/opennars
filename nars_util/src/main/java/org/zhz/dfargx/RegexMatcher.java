package org.zhz.dfargx;

import org.zhz.dfargx.automata.DFA;
import org.zhz.dfargx.automata.NFA;

/**
 * Created on 2015/5/11.
 */
public class RegexMatcher {

    private final int[][] transitionTable;
    private final int is;
    private final int rs;
    private final boolean[] fs;

    public RegexMatcher(String regex) {
        SyntaxTree syntaxTree = new SyntaxTree(regex);
        NFA nfa = new NFA(syntaxTree.getRoot());
        DFA dfa = new DFA(nfa.getStateList());
        transitionTable = dfa.getTransitionTable();
        is = dfa.getInitState();
        fs = dfa.getFinalStates();
        rs = dfa.getRejectedState();
    }

    public final boolean match(String str) {
        int s = is;
        int[][] t = this.transitionTable;
        final int rejected = rs;
        for (int i = 0, length = str.length()-1; (length--) >= 0; i++) {
            if ((s = t[s][str.charAt(i)]) == rejected) {
                return false; // fast failed using rejected state
            }
        }
        return fs[s];
    }
}
