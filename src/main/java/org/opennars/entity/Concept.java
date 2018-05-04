/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.opennars.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opennars.control.ConceptProcessing;
import org.opennars.io.events.Events.BeliefSelect;
import org.opennars.io.events.Events.TaskLinkAdd;
import org.opennars.io.events.Events.TaskLinkRemove;
import org.opennars.io.events.Events.TermLinkAdd;
import org.opennars.io.events.Events.TermLinkRemove;
import org.opennars.storage.Memory;
import org.opennars.main.Shell;
import org.opennars.main.Parameters;
import org.opennars.control.DerivationContext;
import static org.opennars.inference.BudgetFunctions.distributeAmongLinks;
import static org.opennars.inference.BudgetFunctions.rankBelief;
import org.opennars.inference.LocalRules;
import org.opennars.io.Symbols.NativeOperator;
import org.opennars.language.CompoundTerm;
import org.opennars.language.Term;
import org.opennars.storage.Bag;
import org.opennars.storage.LevelBag;
import static org.opennars.inference.UtilityFunctions.or;

public class Concept extends Item<Term> implements Serializable {

    
    /**
     * The term is the unique ID of the concept
     */
    public final Term term;
    
    //recent events that happened before the operation the
    //concept represents was executed
    public Bag<Task<Term>,Sentence<Term>> seq_before;

    /**
     * Task links for indirect processing
     */
    public final Bag<TaskLink,Task> taskLinks;

    /**
     * Term links between the term and its components and compounds; beliefs
     */
    public final Bag<TermLink,TermLink> termLinks;

    /**
     * Link templates of TermLink, only in concepts with CompoundTerm Templates
     * are used to improve the efficiency of TermLink building
     */
    public List<TermLink> termLinkTemplates;

    /**
     * Pending Question directly asked about the term
     *
     * Note: since this is iterated frequently, an array should be used. To
     * avoid iterator allocation, use .get(n) in a for-loop
     */
    public final List<Task> questions;

    
    /**
     * Pending Quests to be answered by new desire values
     */
    public final List<Task> quests;

    /**
     * Judgments directly made about the term Use ArrayList because of access
     * and insertion in the middle
     */
    public final ArrayList<Task> beliefs;
    public final ArrayList<Task> executable_preconditions;

    /**
     * Desire values on the term, similar to the above one
     */
    public final ArrayList<Task> desires;

    /**
     * Reference to the memory to which the Concept belongs
     */
    public final Memory memory;
    

    //use to create averaging stats of occurring intervals
    //so that revision can decide whether to use the new or old term
    //based on which intervals are closer to the average
    public ArrayList<Float> recent_intervals = new ArrayList<Float>();

    public boolean observable = false;

    /**
     * Constructor, called in Memory.getConcept only
     *
     * @param tm A term corresponding to the concept
     * @param memory A reference to the memory
     */
    public Concept(final BudgetValue b, final Term tm, final Memory memory) {
        super(b);        
        
        this.term = tm;
        this.memory = memory;

        this.questions = new ArrayList<>();
        this.beliefs = new ArrayList<>();
        this.executable_preconditions = new ArrayList<>();
        this.quests = new ArrayList<>();
        this.desires = new ArrayList<>();

        this.taskLinks = new LevelBag<>(Parameters.TASK_LINK_BAG_LEVELS, Parameters.TASK_LINK_BAG_SIZE);
        this.termLinks = new LevelBag<>(Parameters.TERM_LINK_BAG_LEVELS, Parameters.TERM_LINK_BAG_SIZE);
                
        if (tm instanceof CompoundTerm) {
            this.termLinkTemplates = ((CompoundTerm) tm).prepareComponentLinks();
        } else {
            this.termLinkTemplates = null;
        }

    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Concept)) return false;
        return ((Concept)obj).name().equals(name());
    }

    @Override public int hashCode() { return name().hashCode();     }

    
    @Override
    public Term name() {
        return term;
    }



    public void addToTable(final Task task, final boolean rankTruthExpectation, final ArrayList<Task> table, final int max, final Class eventAdd, final Class eventRemove, final Object... extraEventArguments) {
        
        int preSize = table.size();
        Task removedT;
        Sentence removed = null;
        removedT = addToTable(task, table, max, rankTruthExpectation);
        if(removedT != null) {
            removed=removedT.sentence;
        }

        if (removed != null) {
            memory.event.emit(eventRemove, this, removed, task, extraEventArguments);
        }
        if ((preSize != table.size()) || (removed != null)) {
            memory.event.emit(eventAdd, this, task, extraEventArguments);
        }
    }
    
    /**
     * Link to a new task from all relevant concepts for continued processing in
     * the near future for unspecified time.
     * <p>
     * The only method that calls the TaskLink constructor.
     *
     * @param task The task to be linked
     * @param content The content of the task
     */
    public void linkToTask(final Task task, DerivationContext cont) {
        final BudgetValue taskBudget = task.budget;

        insertTaskLink(new TaskLink(task, null, taskBudget,
                Parameters.TERM_LINK_RECORD_LENGTH), cont);  // link type: SELF

        if (!(term instanceof CompoundTerm)) {
            return;
        }
        if (termLinkTemplates.isEmpty()) {
            return;
        }
                
        final BudgetValue subBudget = distributeAmongLinks(taskBudget, termLinkTemplates.size());
        if (subBudget.aboveThreshold()) {

            for (int t = 0; t < termLinkTemplates.size(); t++) {
                TermLink termLink = termLinkTemplates.get(t);

               if(termLink.type == TermLink.TEMPORAL)
                    continue;
                Term componentTerm = termLink.target;

                Concept componentConcept = memory.conceptualize(subBudget, componentTerm);

                if (componentConcept != null) {
                    componentConcept.insertTaskLink(
                        new TaskLink(task, termLink, subBudget, Parameters.TERM_LINK_RECORD_LENGTH), cont
                    );
                }
            }

            buildTermLinks(taskBudget);  // recursively insert TermLink
        }
    }

    /**
     * Add a new belief (or goal) into the table Sort the beliefs/desires by
     * rank, and remove redundant or low rank one
     *
     * @param newSentence The judgment to be processed
     * @param table The table to be revised
     * @param capacity The capacity of the table
     * @return whether table was modified
     */
    public static Task addToTable(final Task newTask, final List<Task> table, final int capacity, boolean rankTruthExpectation) {
        Sentence newSentence = newTask.sentence;
        final float rank1 = rankBelief(newSentence, rankTruthExpectation);    // for the new isBelief
        float rank2;        
        int i;
        for (i = 0; i < table.size(); i++) {
            Sentence judgment2 = table.get(i).sentence;
            rank2 = rankBelief(judgment2, rankTruthExpectation);
            if (rank1 >= rank2) {
                if (newSentence.truth.equals(judgment2.truth) && newSentence.stamp.equals(judgment2.stamp,false,true,true)) {
                    //System.out.println(" ---------- Equivalent Belief: " + newSentence + " == " + judgment2);
                    return null;
                }
                table.add(i, newTask);
                break;
            }            
        }
        
        if (table.size() == capacity) {
            // nothing
        }
        else if (table.size() > capacity) {
            Task removed = table.remove(table.size() - 1);
            return removed;
        }
        else if (i == table.size()) { // branch implies implicit table.size() < capacity
            table.add(newTask);
        }
        
        return null;
    }

    /**
     * Select a belief value or desire value for a given query
     *
     * @param query The query to be processed
     * @param list The list of beliefs or desires to be used
     * @return The best candidate selected
     */
    public Task selectCandidate(final Task query, final List<Task> list) {
 //        if (list == null) {
        //            return null;
        //        }
        float currentBest = 0;
        float beliefQuality;
        Task candidate = null;
        boolean rateByConfidence = true; //table vote, yes/no question / local processing
        synchronized (list) {            
            for (int i = 0; i < list.size(); i++) {
                Task judgT = list.get(i);
                Sentence judg = judgT.sentence;
                beliefQuality = LocalRules.solutionQuality(rateByConfidence, query, judg, memory); //makes revision explicitly search for 
                if (beliefQuality > currentBest /*&& (!forRevision || judgT.sentence.equalsContent(query)) */ /*&& (!forRevision || !Stamp.baseOverlap(query.stamp.evidentialBase, judg.stamp.evidentialBase)) */) {
                    currentBest = beliefQuality;
                    candidate = judgT;
                }
            }
        }
        return candidate;
    }

    public float negConfirmationPriority = 0.0f;
    public Task negConfirmation = null;
    public long negConfirm_abort_mintime = 0;
    public long negConfirm_abort_maxtime = 0;
    
    /* ---------- insert Links for indirect processing ---------- */
    /**
     * Insert a TaskLink into the TaskLink bag
     * <p>
     * called only from Memory.continuedProcess
     *
     * @param taskLink The termLink to be inserted
     */
    protected boolean insertTaskLink(final TaskLink taskLink, DerivationContext nal) {        
        Task target = taskLink.getTarget();
        
        //what question answering, question side:
        Task ques = taskLink.getTarget();
        ConceptProcessing.ProcessWhatQuestion(this, ques, nal);
        
        //what question answering, belief side:
        Task t = taskLink.getTarget();
        ConceptProcessing.ProcessWhatQuestionAnswer(this, t, nal);
        
        //HANDLE MAX PER CONTENT
        //if taskLinks already contain a certain amount of tasks with same content then one has to go
        boolean isEternal = target.sentence.isEternal();
        int nSameContent = 0;
        float lowest_priority = Float.MAX_VALUE;
        TaskLink lowest = null;
        for(TaskLink tl : taskLinks) {
            Sentence s = tl.getTarget().sentence;
            if(s.getTerm().equals(taskLink.getTerm()) && s.isEternal() == isEternal) {
                nSameContent++; //same content and occurrence-type, so count +1
                if(tl.getPriority() < lowest_priority) { //the current one has lower priority so save as lowest
                    lowest_priority = tl.getPriority();
                    lowest = tl;
                }
                if(nSameContent > Parameters.TASKLINK_PER_CONTENT) { //ok we reached the maximum so lets delete the lowest
                    taskLinks.take(lowest);
                    memory.emit(TaskLinkRemove.class, lowest, this);
                    break;
                }
            }
        }
        //END HANDLE MAX PER CONTENT
        
        
        TaskLink removed = taskLinks.putIn(taskLink);
        
        if (removed!=null) {
            if (removed == taskLink) {
                memory.emit(TaskLinkRemove.class, taskLink, this);
                return false;
            }
            else {
                memory.emit(TaskLinkRemove.class, removed, this);
            }
            
            removed.end();
        }
        memory.emit(TaskLinkAdd.class, taskLink, this);
        return true;
    }


    /**
     * Recursively build TermLinks between a compound and its components
     * <p>
     * called only from Memory.continuedProcess
     *
     * @param taskBudget The BudgetValue of the task
     */
    public void buildTermLinks(final BudgetValue taskBudget) {
        if (termLinkTemplates.size() == 0) {
            return;
        }
        
        BudgetValue subBudget = distributeAmongLinks(taskBudget, termLinkTemplates.size());

        if (!subBudget.aboveThreshold()) {
            return;
        }

        for (final TermLink template : termLinkTemplates) {
            if (template.type == TermLink.TRANSFORM) {
                continue;
            }

            Term target = template.target;

            final Concept concept = memory.conceptualize(taskBudget, target);
            if (concept == null) {
                continue;
            }

            // this termLink to that and vice versa
            insertTermLink(new TermLink(target, template, subBudget));
            concept.insertTermLink(new TermLink(term, template, subBudget));

            if (target instanceof CompoundTerm && template.type != TermLink.TEMPORAL) {
                concept.buildTermLinks(subBudget);
            }
        }
    }

    /**
     * Insert a TermLink into the TermLink bag
     * <p>
     * called from buildTermLinks only
     *
     * @param termLink The termLink to be inserted
     */
    public boolean insertTermLink(final TermLink termLink) {
        TermLink removed = termLinks.putIn(termLink);
        if (removed!=null) {
            if (removed == termLink) {
                memory.emit(TermLinkRemove.class, termLink, this);
                return false;
            }
            else {
                memory.emit(TermLinkRemove.class, removed, this);
            }
        }
        memory.emit(TermLinkAdd.class, termLink, this);
        return true;        
    }

    /**
     * Return a string representation of the concept, called in ConceptBag only
     *
     * @return The concept name, with taskBudget in the full version
     */
    @Override
    public String toString() {  // called from concept bag
        //return (super.toStringBrief() + " " + key);
        return super.toStringExternal();
    }

    /**
     * called from {@link Shell}
     */
    @Override
    public String toStringLong() {
        String res = 
                toStringExternal() + " " + term.name()
                + toStringIfNotNull(termLinks.size(), "termLinks")
                + toStringIfNotNull(taskLinks.size(), "taskLinks")
                + toStringIfNotNull(beliefs.size(), "beliefs")
                + toStringIfNotNull(desires.size(), "desires")
                + toStringIfNotNull(questions.size(), "questions")
                + toStringIfNotNull(quests.size(), "quests");
        
                //+ toStringIfNotNull(null, "questions");
        /*for (Task t : questions) {
            res += t.toString();
        }*/
        // TODO other details?
        return res;
    }

    private String toStringIfNotNull(final Object item, final String title) {
        if (item == null) {
            return "";
        }

        final String itemString = item.toString();

        return new StringBuilder(2 + title.length() + itemString.length() + 1).
                append(" ").append(title).append(':').append(itemString).toString();
    }

    /**
     * Recalculate the quality of the concept [to be refined to show
     * extension/intension balance]
     *
     * @return The quality value
     */
    @Override
    public float getQuality() {
        float linkPriority = termLinks.getAveragePriority();
        float termComplexityFactor = 1.0f / term.getComplexity()*Parameters.COMPLEXITY_UNIT;
        float result = or(linkPriority, termComplexityFactor);
        if (result < 0) {
            throw new RuntimeException("Concept.getQuality < 0:  result=" + result + ", linkPriority=" + linkPriority + " ,termComplexityFactor=" + termComplexityFactor + ", termLinks.size=" + termLinks.size());
        }
        return result;

    }

    /**
     * Return the templates for TermLinks, only called in
     * Memory.continuedProcess
     *
     * @return The template get
     */
    public List<TermLink> getTermLinkTemplates() {
        return termLinkTemplates;
    }

    /**
     * Select a isBelief to interact with the given task in inference
     * <p>
     * get the first qualified one
     * <p>
     * only called in RuleTables.reason
     *
     * @param task The selected task
     * @return The selected isBelief
     */
    public Sentence getBelief(final DerivationContext nal, final Task task) {
        final Stamp taskStamp = task.sentence.stamp;
        final long currentTime = memory.time();

        for (final Task beliefT : beliefs) {  
            Sentence belief = beliefT.sentence;
            nal.emit(BeliefSelect.class, belief);
            nal.setTheNewStamp(taskStamp, belief.stamp, currentTime);
            
            Sentence projectedBelief = belief.projection(taskStamp.getOccurrenceTime(), memory.time());
            /*if (projectedBelief.getOccurenceTime() != belief.getOccurenceTime()) {
               nal.singlePremiseTask(projectedBelief, task.budget);
            }*/
            
            return projectedBelief;     // return the first satisfying belief
        }
        return null;
    }

    /**
     * Get the current overall desire value. TODO to be refined
     */
    public TruthValue getDesire() {
        if (desires.isEmpty()) {
            return null;
        }
        TruthValue topValue = desires.get(0).sentence.truth;
        return topValue;
    }



    @Override
    public void end() {
        for (Task t : questions) t.end();
        for (Task t : quests) t.end();
        
        questions.clear();
        quests.clear();                
        desires.clear();
        //evidentalDiscountBases.clear();
        termLinks.clear();
        taskLinks.clear();        
        beliefs.clear();
        termLinkTemplates.clear();
    }
    
    /**
     * Replace default to prevent repeated inference, by checking TaskLink
     *
     * @param taskLink The selected TaskLink
     * @param time The current time
     * @return The selected TermLink
     */
    public TermLink selectTermLink(final TaskLink taskLink, final long time) {
        ConceptProcessing.maintainDisappointedAnticipations(this);

        int toMatch = Parameters.TERM_LINK_MAX_MATCHED; //Math.min(memory.param.termLinkMaxMatched.get(), termLinks.size());
        for (int i = 0; (i < toMatch) && (termLinks.size() > 0); i++) {
            
            final TermLink termLink = termLinks.takeNext();
            if (termLink==null)
                break;
            
            if (taskLink.novel(termLink, time)) {
                //return, will be re-inserted in caller method when finished processing it
                return termLink;
            }
            //just put back since it isn't novel
            returnTermLink(termLink);
        }
        return null;

    }

    public void returnTermLink(TermLink termLink) {
        termLinks.putBack(termLink, memory.cycles(memory.param.termLinkForgetDurations), memory);
    }

    /**
     * Return the questions, called in ComposionalRules in
     * dedConjunctionByQuestion only
     */
    public List<Task> getQuestions() {
        return Collections.unmodifiableList(questions);
    }
    public List<Task> getQuess() {
        return Collections.unmodifiableList(quests);
    }

    public void discountConfidence(final boolean onBeliefs) {
        if (onBeliefs) {
            for (final Task t : beliefs) {
                t.sentence.discountConfidence();
            }
        } else {
            for (final Task t : desires) {
                t.sentence.discountConfidence();
            }
        }
    }

    public NativeOperator operator() {
        return term.operator();
    }

    public Term getTerm() {
        return term;
    }

    /** returns unmodifidable collection wrapping beliefs */
    public List<Task> getBeliefs() {
        return Collections.unmodifiableList(beliefs);
    }
    
    /** returns unmodifidable collection wrapping beliefs */
    public List<Task> getDesires() {
        return Collections.unmodifiableList(desires);
    }
}
