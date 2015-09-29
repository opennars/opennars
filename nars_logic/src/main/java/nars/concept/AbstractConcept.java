package nars.concept;

import nars.Memory;
import nars.budget.Budget;
import nars.budget.Item;
import nars.term.Term;
import nars.term.Variable;

import java.util.Map;

/**
 * Created by me on 7/29/15.
 */
public abstract class AbstractConcept extends Item<Term> implements Concept {

    protected final Term term;

    final long creationTime;
    Map<Object, Object> meta = null;
    boolean constant = false;

    @Deprecated protected transient final Memory memory;

    @Deprecated final static Variable how = new Variable("?how");

    public AbstractConcept(final Term term, Budget budget, final Memory memory) {
        super(budget);
        this.memory = memory;
        this.term = term;
        this.creationTime = memory.time();
    }

    /**
     * metadata table where processes can store and retrieve concept-specific data by a key. lazily allocated
     */
    @Override
    final public Map<Object, Object> getMeta() {
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
    final public Memory getMemory() {
        return memory;
    }

    /**
     * The term is the unique ID of the concept
     */
    @Override
    final public Term getTerm() {
        return term;
    }

    @Override
    final public long getCreationTime() {
        return creationTime;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Concept)) return false;
        return ((Concept) obj).getTerm().equals(getTerm());
    }

    @Override
    final public int hashCode() {
        return getTerm().hashCode();
    }

    @Override
    final public Term name() {
        return getTerm();
    }

    /**
     * Return a string representation of the concept, called in ConceptBag only
     *
     * @return The concept name, with taskBudget in the full version
     */
    @Override
    final public String toString() {  // called from concept bag
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

        zero();

        super.delete();

        if (getMeta() != null) {
            getMeta().clear();
            setMeta(null);
        }

    }


    @Override
    final public boolean isConstant() {
        return constant;
    }

    @Override
    final public boolean setConstant(boolean b) {
        this.constant = b;
        return constant;
    }
}
