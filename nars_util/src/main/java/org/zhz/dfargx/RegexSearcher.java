package org.zhz.dfargx;

import org.zhz.dfargx.automata.DFA;
import org.zhz.dfargx.automata.NFA;

import java.util.Enumeration;

/**
 * Created on 5/25/15.
 */
public class RegexSearcher implements Enumeration<MatchedText> {
    private final int[][] transitionTable;
    private final int is;
    private final int rs;
    private final boolean[] fs;

    private String str;

    private int startPos;
    private MatchedText text;

    public RegexSearcher(String regex) {
        SyntaxTree syntaxTree = new SyntaxTree(regex);
        NFA nfa = new NFA(syntaxTree.getRoot());
        DFA dfa = new DFA(nfa.getStateList());
        transitionTable = dfa.getTransitionTable();
        is = dfa.getInitState();
        fs = dfa.getFinalStates();
        rs = dfa.getRejectedState();
        str = null;
    }

    public void search(String str) {
        startPos = 0;
        text = null;
        this.str = str;
    }

    @Override
    public boolean hasMoreElements() {
        int[][] t = this.transitionTable;

        int len = str.length();
        String str = this.str;
        int rs = this.rs;
        boolean[] fs = this.fs;
        while (startPos < len) {
            int s = is;
            for (int i = startPos; i < len; i++) {

                s = t[s][str.charAt(i)];

                if (s == rs) {
                    break;
                } else {

                    if (fs[s]) {
                        text = new MatchedText(str.substring(startPos, i + 1), startPos);
                        startPos = i + 1;
                        return true;
                    }
                }
            }
            startPos++;
        }
        return false;
    }

    @Override
    public MatchedText nextElement() {
        return text;
    }
}
