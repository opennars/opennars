package org.zhz.dfargx;

import org.zhz.dfargx.automata.DFA;
import org.zhz.dfargx.automata.NFA;
import org.zhz.dfargx.tree.SyntaxTree;

import java.util.Enumeration;

/**
 * Created on 5/25/15.
 */
public class RegexSearcher implements Enumeration<MatchedText> {
    private int[][] transitionTable;
    private int is;
    private int rs;
    private boolean[] fs;
    private String str;

    private int startPos;
    private MatchedText text;

    public RegexSearcher(String regex) {
        compile(regex);
        str = null;
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

    public void search(String str) {
        startPos = 0;
        text = null;
        this.str = str;
    }

    @Override
    public boolean hasMoreElements() {
        while (startPos < str.length()) {
            int s = is;
            for (int i = startPos; i < str.length(); i++) {
                char ch = str.charAt(i);
                s = transitionTable[s][ch];
                if (s == rs) {
                    break;
                } else if (fs[s]) {
                    text = new MatchedText(str.substring(startPos, i + 1), startPos);
                    startPos = i + 1;
                    return true;
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
