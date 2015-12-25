package org.zhz.dfargx.automata;

/**
 * Created on 2015/5/10.
 */
public class NFAStateFactory {
    private static int nextID;

    public NFAStateFactory() {
        nextID = 0;
    }

    public NFAState create() {
        return new NFAState(nextID++);
    }
}
