package org.zhz.dfargx;

import org.zhz.dfargx.automata.DFA;
import org.zhz.dfargx.automata.NFA;

import java.util.Arrays;

/**
 * Created on 2015/5/11.
 */
public class RegexMatcher extends DFA {

    private final int[][] transitionTable;
    private final int is;
    private final int rs;
    private final boolean[] fs;

    @Override
    public String toString() {
        return "RegexMatcher{" +
                "transitionTable=" + Arrays.toString(transitionTable) +
                ", is=" + is +
                ", rs=" + rs +
                ", fs=" + Arrays.toString(fs) +
                '}';
    }

    public RegexMatcher(String regex) {
        super(new NFA(new SyntaxTree(regex).getRoot()).getStateList());

        transitionTable = getTransitionTable();
        is = getInitState();
        fs = getFinalStates();
        rs = getRejectedState();
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
