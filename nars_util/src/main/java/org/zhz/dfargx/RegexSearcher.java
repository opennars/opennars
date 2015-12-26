package org.zhz.dfargx;

import org.zhz.dfargx.automata.DFA;
import org.zhz.dfargx.automata.NFA;

import java.util.Arrays;
import java.util.Enumeration;

/**
 * Created on 5/25/15.
 */
public class RegexSearcher extends DFA implements Enumeration<MatchedText> {
    private final int[][] transitionTable;
    private final int is;
    private final int rs;
    private final boolean[] fs;

    private String str;

    private int startPos;
    private MatchedText text;

    @Override
    public String toString() {
        return "RegexSearcher{" +
                "transitionTable=" + Arrays.deepToString(transitionTable) +
                ", is=" + is +
                ", rs=" + rs +
                ", fs=" + Arrays.toString(fs) +
                '}';
    }

    public RegexSearcher(String regex) {
        super(new NFA(new SyntaxTree(regex).getRoot()).getStateList());
        transitionTable = getTransitionTable();
        is = getInitState();
        fs = getFinalStates();
        rs = getRejectedState();
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
