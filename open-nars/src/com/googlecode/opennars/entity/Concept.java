/*
 * Concept.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.googlecode.opennars.entity;

import java.util.*;
import java.io.*;

import com.googlecode.opennars.inference.*;
import com.googlecode.opennars.language.*;
import com.googlecode.opennars.main.*;
import com.googlecode.opennars.operation.*;
import com.googlecode.opennars.parser.*;
import com.googlecode.opennars.storage.*;


/**
 * A concept contains information directly related to a term, including directly and indirectly related tasks and beliefs.
 * <p>
 * To make sure the space will be released, no other will refer to a concept, except in a concept bag.
 */
public final class Concept extends Item {
    /* Constant term as the unique ID of the concept */
    private Term term;
    /* Direct Task lists (they may never be used for a Term that is not used as a Statement) */
    private ArrayList<Judgement> directBeliefs;      // Judgments with the same content
    private ArrayList<Goal> directGoals;            // Goals that can be directly achieved
    private Question directQuestion;                // Question that can be directly answered
    private boolean revisible = true;               // truth value of judgments can be revised
    /* Link bags for indirect processing (always used) */
    private ArrayList<TermLink> linkTemplates;  // templates of TermLink, only in Concepts for CompoundTerms
    private TaskLinkBag taskLinks;
    private TermLinkBag termLinks;
    
    /* ---------- constructor ---------- */
    
    /**
     * Constructor
     * @param tm A constant term corresponding to the concept
     */
    public Concept(Term tm, Memory memory) {
        super(memory);
        term = tm;
        key = tm.toString();
        directBeliefs = new ArrayList<Judgement>();
        directGoals = new ArrayList<Goal>();
        directQuestion = null;
        taskLinks = new TaskLinkBag();
        termLinks = new TermLinkBag();
        if (tm instanceof CompoundTerm) {
            linkTemplates = ((CompoundTerm) tm).prepareComponentLinks(getMemory());
            checkRevisibility();
        }
    }
    
    private void checkRevisibility() {
        revisible = !(term instanceof Tense);                                   // no tense
        if (revisible)
            revisible = ((key.indexOf("()") < 0) && (key.indexOf("(#")) < 0);   // no dependent variable
    }
    
    /* ---------- add direct information as Tasks ---------- */
    
    /**
     * New task to be directly processed in a constant time, called from Memory only
     * @param task The task to be processed
     */
    public void directProcess(Task task) {
        getMemory().currentTask = task;
        Sentence sentence = task.getSentence();
        if (sentence instanceof Question)
            processQuestion(task);
        else if (sentence instanceof Goal)
            processGoal(task);
        else
            processJudgment(task);
        getMemory().activateConcept(this, task.getBudget());
    }
    
    /**
     * New question to be directly answered by existing beliefs
     * @param task The task to be processed
     */
    private void processQuestion(Task task) {
        if (directQuestion == null)
            directQuestion = (Question) task.getSentence();         // remember it
        for (int i = 0; i < directBeliefs.size(); i++) {
            Judgement judg = directBeliefs.get(i);
            getMemory().getRuletables().getMatchingRules().trySolution(directQuestion, judg, task);    // look for better answer
        }
    }
    
    /**
     * New judgment
     * @param task The task to be processed
     */
    private void processJudgment(Task task) {
        Judgement judg = (Judgement) task.getSentence();
        if (revisible)
            reviseTable(task, directBeliefs);
        else
            updateTable(task);
        if (task.getPriority() > 0) {               // if still valuable --- necessary???
            if (directQuestion != null)
                getMemory().getRuletables().getMatchingRules().trySolution(directQuestion, judg, task);
            for (int i = 0; i < directGoals.size(); i++) {
                Goal goal = directGoals.get(i);
                getMemory().getRuletables().getMatchingRules().trySolution(goal, judg, task);
            }
            addToTable(judg, directBeliefs, getMemory().getParameters().MAXMUM_BELIEF_LENGTH);
        }
    }
    
    /**
     * New goal
     * @param task The task to be processed
     */
    private void processGoal(Task task) {
        Goal goal = (Goal) task.getSentence();
        if (revisible)
            reviseTable(task, directGoals);
        else
            updateTable(task);
        for (int i = 0; i < directBeliefs.size(); i++) {
            Judgement judg = directBeliefs.get(i);
            getMemory().getRuletables().getMatchingRules().trySolution(goal, judg, task);
        }
        if (task.getPriority() > 0) {              // if still valuable
            addToTable(goal, directGoals, getMemory().getParameters().MAXMUM_GOALS_LENGTH);         // with the feedbacks
        }
        decisionMaking(task);
    }
    
    private void decisionMaking(Task task) {    // add plausibility
        Goal goal = (Goal) task.getSentence();
        float desire = 2 * goal.getTruth().getExpectation() - 1;
        float quality = (desire < 0) ? 0 : desire;
        task.setQuality(quality);
    }
    
    // revise previous beliefs or goals
    private void reviseTable(Task task, ArrayList table) {
        Judgement belief;
        for (int i = 0; i < table.size(); i++) {    // call select()
            belief = (Judgement) table.get(i);
            if (belief.noOverlapping((Judgement) task.getSentence()))
                getMemory().getRuletables().getMatchingRules().revision(task, belief, false);
        }
    }
    
    // to be rewritten
    private void updateTable(Task task) {
//        Judgement belief;
//        for (int i = 0; i < directBeliefs.size(); i++) {    // call select()
//            belief = directBeliefs.get(i);
//            if (((Judgement) task.getSentence()).getBase().latest() > belief.getBase().latest())
//                getMemory().getRuletables().getMatchingRules().update(task, belief);
//        }
    }
    
    // add the Task as a new direct Belief or Goal, remove redundant ones
    // table sorted by rank
    private void addToTable(Judgement newJudgement, ArrayList table, int capacity) {
        float rank1 = this.budgetfunctions.rankBelief(newJudgement);    // for the new belief
        Base base1 = newJudgement.getBase();
        Judgement judgement2;
        float rank2;
        int i;
        for (i = 0; i < table.size(); i++) {        // go through everyone
            judgement2 = (Judgement) table.get(i);
            rank2 = this.budgetfunctions.rankBelief(judgement2); // previous belief
            if (rank1 >= rank2) {
                if (newJudgement.equivalentTo(judgement2))
                    return;
                table.add(i, newJudgement);
                break;
            }
        }
        if (table.size() == capacity)
            table.remove(capacity - 1);
        else if (i == table.size())
            table.add(newJudgement);
    }

    // return a piece of Belief to be used with the task
    // get the first qualified one
    public Judgement getBelief(Task task) {
        Sentence sentence = task.getSentence();
        Judgement belief;
        for (int i = 0; i < directBeliefs.size(); i++) {
            belief = directBeliefs.get(i);
            if ((sentence instanceof Question) || belief.noOverlapping((Judgement) sentence)) {
                return belief;
            }
        }
        return null;
    }
    
    /* ---------- insert relational information as Links ---------- */
    
    public ArrayList<TermLink> getTermLinks() {
        return linkTemplates;
    }
    
    // insert TaskLink into the task base, called from Memory only
    public void insertTaskLink(TaskLink taskLink) {
        BudgetValue budget = taskLink.getBudget();
        taskLinks.putIn(taskLink);
        getMemory().activateConcept(this, budget);       // activate the concept
        if (term instanceof CompoundTerm)
            buildTermLinks(budget);
    }
    
    private void buildTermLinks(BudgetValue budget) {
        Term t;
        Concept c;
        TermLink cLink1, cLink2;
        BudgetValue subBudget = this.budgetfunctions.distributeAmongLinks(budget, linkTemplates.size());
        if (!subBudget.aboveThreshold())
            return;
        for(TermLink link : linkTemplates) {
            t = link.getTarget();
            c = getMemory().getConcept(t);
            cLink1 = new TermLink(t, link, subBudget, getMemory());
            insertTermLink(cLink1);   // this link to that
            cLink2 = new TermLink(term, link, subBudget, getMemory());
            c.insertTermLink(cLink2);   // that link to this
            if (t instanceof CompoundTerm)
                c.buildTermLinks(subBudget);
        }
    }
    
    // insert TermLink into the Belief base, called from Memory only
    public void insertTermLink(TermLink cLink) {
        termLinks.putIn(cLink);
        getMemory().activateConcept(this, cLink.getBudget());
    }
    
    /* ---------- main loop ---------- */
    
    // a single step of syllogism within a concept
    public void fire() {
        TaskLink tLink = (TaskLink) taskLinks.takeOut();
        if (tLink == null)
            return;
        getMemory().currentTaskLink = tLink;
        getMemory().currentBeliefLink = null;
        Task task = tLink.getTargetTask();
        getMemory().currentTask = task;
        if ((tLink.getType() == TermLink.TRANSFORM) && !task.isStructual()) {
            getMemory().getRuletables().transformTask(task, tLink);      // inference from a TaskLink and the Task --- for Product and Image
            return; // cannot be used otherwise
        }
        TermLink bLink = (TermLink) termLinks.takeOut(tLink);  // to avoid repeated syllogism
        if (bLink != null) {
            getMemory().currentBeliefLink = bLink;
            getMemory().getRuletables().reason(tLink, bLink);
            termLinks.putBack(bLink);
        }
        taskLinks.putBack(tLink);
    }
    
    /* ---------- utility ---------- */
    
    public Term getTerm() {     // called from Memory only
        return term;
    }
    
    public String toString() {  // called from concept bag
    	return (super.toString2() + " " + key);
    }
    
    public float getQuality() {         // re-calculate-and-set? consider syntactic complexity?
        return UtilityFunctions.and(taskLinks.averagePriority(), termLinks.averagePriority());
    }
    
    /* ---------- reporting ---------- */
    
    // display direct belief, questions, and goals
    public String displayContent() {
        StringBuffer buffer = new StringBuffer();
        if (directBeliefs.size() > 0) {
            buffer.append("  Beliefs:\n");
            for (int i = 0; i < directBeliefs.size(); i++)
                buffer.append(directBeliefs.get(i) + "\n");
        }
        if (directGoals.size() > 0) {
            buffer.append("\n  Goals:\n");
            for (int i = 0; i < directGoals.size(); i++)
                buffer.append(directGoals.get(i) + "\n");
        }
        if (directQuestion != null)
            buffer.append("\n  Question:\n" + directQuestion + "\n");
        return buffer.toString();
    }
}

