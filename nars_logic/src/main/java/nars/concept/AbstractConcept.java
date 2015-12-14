package nars.concept;

import nars.NAR;
import nars.task.Task;
import nars.term.Term;

import java.util.Map;

/**
 * Created by me on 7/29/15.
 */
public abstract class AbstractConcept implements Concept {

    private final Term term;

    protected Map meta = null;
    protected boolean constant = false;

    public AbstractConcept(Term term) {
        this.term = term;
    }


    @Override
    public final Term term() {
        return term;
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



    @Override public boolean process(final Task task, NAR nar) {
        throw new RuntimeException("concept " + this + " unimplemented: process " + task + " in " + nar);
    }
    @Override public boolean link(Task task, float scale, NAR nar) {
        throw new RuntimeException("concept " + this + " unimplemented: link " + task + " in " + nar);
    }


        /**
         * The term is the unique ID of the concept
         */
    @Override
    public final Term get() {
        return term;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Concept)) return false;
        return ((Concept) obj).get().equals(term);
    }

    @Override
    public final int hashCode() {
        return term.hashCode();
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
        return term.toString();
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
    public void delete() {
        /*if (getMemory().inCycle())
            throw new RuntimeException("concept " + this + " attempt to delete() during an active cycle; must be done between cycles");
        */

        if (getMeta() != null) {
            getMeta().clear();
            setMeta(null);
        }
        //TODO clear bags
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
