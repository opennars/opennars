package nars.tuprolog;

import nars.nal.AbstractSubGoalTree;
import nars.nal.term.Term;

import java.util.ArrayList;
import java.util.Iterator;


public class SubGoalTree implements AbstractSubGoalTree, Iterable<AbstractSubGoalTree> {
    
    private final ArrayList<AbstractSubGoalTree> terms;
        //private LinkedList terms;
    
    public SubGoalTree() {
        terms = new ArrayList<>();
                //terms = new LinkedList();
    }

        public SubGoalTree(ArrayList<AbstractSubGoalTree> terms) {
        this.terms=terms;
    }
    
    public void addChild(Term term) {
        terms.add(term);
    }
    
    public SubGoalTree addChild() {
        SubGoalTree r = new SubGoalTree();
        terms.add(r);
        return r;
    }
    
    public AbstractSubGoalTree getChild(int i) {
        return terms.get(i);
    }
    
    public Iterator<AbstractSubGoalTree> iterator() {
        return terms.iterator();
    }
    
    public int size() {
        return terms.size();
    }
    
    public boolean isLeaf() { return false; }
    public boolean isRoot() { return true; }
    
    public String toString() {
        String result = " [ ";
        Iterator<AbstractSubGoalTree> i = terms.iterator();
        if (i.hasNext())
            result += i.next().toString();
        while (i.hasNext()) {
            result += " , " + i.next().toString();
        }
        return result + " ] ";
    }

    public boolean removeChild(int i) {
        try {
            terms.remove(i);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public SubGoalTree copy(){
        return new SubGoalTree(terms);
    }
}
