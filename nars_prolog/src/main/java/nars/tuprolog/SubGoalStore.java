package nars.tuprolog;


import nars.nal.AbstractSubGoalTree;

public class SubGoalStore {

    private SubGoalTree goals;
    private SubGoalTree commaStruct;
    private int index;
    private DefaultSubGoalId curSGId;
    private boolean fetched=false;

    public SubGoalStore() {
        commaStruct = goals = new SubGoalTree();
        index = 0;
        curSGId = null;
    }

    /**
     *
     */
    public boolean load(SubGoalTree subGoals) {
        commaStruct = subGoals;
        goals=commaStruct.copy();
        return true;

    }

    /**
     * Ripristina ClauseStore allo stato i-esimo
     */
    public PTerm backTo(SubGoalId identifier) {
        popSubGoal((DefaultSubGoalId) identifier);
        index--;
        return fetch();
    }

    public void pushSubGoal(SubGoalTree subGoals) {
        curSGId = new DefaultSubGoalId(curSGId, commaStruct, index);
        commaStruct = subGoals;
        goals = commaStruct.copy();
        index = 0;
    }

    private void popSubGoal(DefaultSubGoalId id) {
        commaStruct = id.getRoot();
        goals = commaStruct.copy();
        index = id.getIndex();
        curSGId = id.getParent();
    }

    /**
     * Restituisce la clausola da caricare
     */
    public PTerm fetch() {
        while (true) {
            fetched = true;
            if (index >= commaStruct.size()) {
                if (curSGId == null) {
                    return null;
                } else {
                    popSubGoal(curSGId);

                }
            } else {

                AbstractSubGoalTree s = commaStruct.getChild(index);
                index++;
                if (s instanceof SubGoalTree) {
                    pushSubGoal((SubGoalTree) s);

                } else {
                    return ((SubGoalElement) s).getValue();
                }

            }
        }
    }

    /**
     * Indice del correntemente in esecuzione
     */
    public SubGoalId getCurrentGoalId() {
        return new DefaultSubGoalId(curSGId, commaStruct, index);
    }

    public boolean haveSubGoals() {
        return (index < goals.size());
    }

    public String toString() {
        return "goals: " + goals + ' '
                + "index: " + index;
    }

    /*
     * Methods for spyListeners
     */
    public SubGoalTree getSubGoals() {
        return goals;
    }

    public int getIndexNextSubGoal() {
        return index;
    }
    public boolean getFetched(){
        return fetched;
    }
    public DefaultSubGoalId getCurSGId() {
        return curSGId;
    }
        
}
