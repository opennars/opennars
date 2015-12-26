package org.zhz.dfargx.automata;

/**
 * Created on 2015/5/10.
 */
public class NFAStateFactory {
    private static int nextID = 0;

    public NFAStateFactory() {

    }

    public static NFAState create() {
        return new NFAState(nextID++);
    }
}
