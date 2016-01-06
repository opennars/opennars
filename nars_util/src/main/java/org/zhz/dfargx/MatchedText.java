package org.zhz.dfargx;

/**
 * Created on 5/25/15.
 */
public final class MatchedText {
    private final String text;
    private final int Pos;

    MatchedText(String text, int Pos) {
        this.text = text;
        this.Pos = Pos;
    }

    public String getText() {
        return text;
    }

    public int getPos() {
        return Pos;
    }

    @Override
    public String toString() {
        return "match:(" +
                '\'' + text + '\'' +
                ',' + Pos +
                ')';
    }
}
