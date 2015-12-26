package org.zhz.dfargx.automata;

import com.gs.collections.impl.map.mutable.primitive.CharObjectHashMap;

import java.util.HashSet;
import java.util.Set;

/**
 * Created on 2015/5/10.
 */
public class NFAState {

    private final Set<NFAState> directTable;
    public final CharObjectHashMap<Set<NFAState>> transitions;
    private final int id;

    public NFAState(int id) {
        directTable = new HashSet<>();
        transitions = new CharObjectHashMap<>();
        this.id = id;
    }

    public void transitionRule(char ch, NFAState state) {
        Set<NFAState> stateSet = transitions.getIfAbsentPut(ch, HashSet::new);
        stateSet.add(state);
    }

    public void directRule(NFAState state) {
        directTable.add(state);
    }

//    public int getId() {
//        return id;
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        //if (o == null || getClass() != o.getClass()) return false;
        NFAState state = (NFAState) o;
        return id == state.id; //Objects.equals(id, state.id);
    }

    @Override
    public int hashCode() {
        return id; //Objects.hash(id);
    }

    public Set<NFAState> getDirectTable() {
        return directTable;
    }

    @Override
    public String toString() {
        return "NFAState{" +
                "id=" + id +
                '}';
    }
}
