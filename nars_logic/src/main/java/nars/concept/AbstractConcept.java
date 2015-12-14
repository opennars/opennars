package nars.concept;

import nars.Memory;
import nars.budget.Budget;
import nars.nal.nal7.Tense;
import nars.term.Term;

import java.util.Map;

/**
 * Created by me on 7/29/15.
 */
public abstract class AbstractConcept implements Concept {

    private final Term term;
    private final Budget budget;

    long creationTime = Tense.TIMELESS;
    protected Map meta = null;
    protected boolean constant = false;

    @Deprecated protected transient Memory memory;

    //@Deprecated final static Variable how = new Variable("?how");

    public AbstractConcept(Budget b, Term term) {
        this.budget = b;
        this.term = term;
    }

    @Override
    public Budget getBudget() {
        return budget;
    }

    @Override
    public float getPriority() {
        return budget.getPriority();
    }

    @Override
    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * metadata table where processes can store and retrieve concept-specific data by a key. lazily allocated
     */
    @Override
    public final Map<Object, Object> getMeta() {
        return meta;
    }

    @Override
    @Deprecated public void setMeta(Map<Object, Object> meta) {
        this.meta = meta;
    }

    /**
     * Reference to the memory to which the Concept belongs
     */
    @Override
    public final Memory getMemory() {
        return memory;
    }

    @Override
    public void setMemory(Memory memory) {
        this.memory = memory;
        if (memory!=null) {
            if (creationTime == Tense.TIMELESS) {
                creationTime = memory.time();
            }
        }
    }

    /**
     * The term is the unique ID of the concept
     */
    @Override
    public final Term getTerm() {
        return term;
    }

    @Override
    public final long getCreationTime() {
        return creationTime;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Concept)) return false;
        return ((Concept) obj).getTerm().equals(getTerm());
    }

    @Override
    public final int hashCode() {
        return getTerm().hashCode();
    }

    @Override
    public final Term name() {
        return getTerm();
    }

    /**
     * Return a string representation of the concept, called in ConceptBag only
     *
     * @return The concept name, with taskBudget in the full version
     */
    @Override
    public final String toString() {  // called from concept bag
        //return (super.toStringBrief() + " " + key);
        //return super.toStringExternal();
        return getTerm().toString();
    }

//    /**
//     * called from {@link NARRun}
//     */
//    @Override
//    public String toStringLong() {
//        String res =
//                toStringWithBudget() + " " + getTerm().toString()
//                        + toStringIfNotNull(getTermLinks().size(), "termLinks")
//                        + toStringIfNotNull(getTaskLinks().size(), "taskLinks")
//                        + toStringIfNotNull(getBeliefs().size(), "beliefs")
//                        + toStringIfNotNull(getGoals().size(), "goals")
//                        + toStringIfNotNull(getQuestions().size(), "questions")
//                        + toStringIfNotNull(getQuests().size(), "quests");
//
//        //+ toStringIfNotNull(null, "questions");
//        /*for (Task t : questions) {
//            res += t.toString();
//        }*/
//        // TODO other details?
//        return res;
//    }

//    private String toStringIfNotNull(final Object item, final String title) {
//        if (item == null) {
//            return "";
//        }
//
//        final String itemString = item.toString();
//
//        return new StringBuilder(2 + title.length() + itemString.length() + 1).
//                append(' ').append(title).append(':').append(itemString).toString();
//    }

    /** called by memory, dont call self or otherwise */
    @Override public void delete() {
        /*if (getMemory().inCycle())
            throw new RuntimeException("concept " + this + " attempt to delete() during an active cycle; must be done between cycles");
        */
        if (budget.isDeleted())
            return; //already deleted

        budget.delete();

        if (getMeta() != null) {
            getMeta().clear();
            setMeta(null);
        }
    }


    @Override
    public final boolean isConstant() {
        return constant;
    }

    @Override
    public final boolean setConstant(boolean b) {
        constant = b;
        return constant;
    }
}
