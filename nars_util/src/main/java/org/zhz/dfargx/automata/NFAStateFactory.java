package org.zhz.dfargx.automata;

/**
 * Created on 2015/5/10.
 */
public enum NFAStateFactory {
    ;
    private static int nextID = 0;

    public static NFAState create() {
        return new NFAState(nextID++);
    }
}
