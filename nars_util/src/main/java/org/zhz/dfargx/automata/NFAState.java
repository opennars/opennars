package org.zhz.dfargx.automata;

import java.util.*;

/**
 * Created on 2015/5/10.
 */
public class NFAState {

    private Set<NFAState> directTable;
    private Map<Character, Set<NFAState>> transitionMap;
    private int id;

    public NFAState(int id) {
        directTable = new HashSet<>();
        transitionMap = new HashMap<>();
        this.id = id;
    }

    public void transitionRule(char ch, NFAState state) {
        Set<NFAState> stateSet = transitionMap.get(ch);
        if (stateSet == null) {
            stateSet = new HashSet<>();
            transitionMap.put(ch, stateSet);
        }
        stateSet.add(state);
    }

    public void directRule(NFAState state) {
        directTable.add(state);
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NFAState state = (NFAState) o;
        return Objects.equals(id, state.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Set<NFAState> getDirectTable() {
        return directTable;
    }

    public Map<Character, Set<NFAState>> getTransitionMap() {
        return transitionMap;
    }

    @Override
    public String toString() {
        return "NFAState{" +
                "id=" + id +
                '}';
    }
}
