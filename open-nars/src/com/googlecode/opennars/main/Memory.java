/*
 * Memory.java
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

package com.googlecode.opennars.main;

import java.util.*;

import com.googlecode.opennars.entity.*;
import com.googlecode.opennars.inference.*;
import com.googlecode.opennars.language.*;
import com.googlecode.opennars.operation.Operator;
import com.googlecode.opennars.parser.*;
import com.googlecode.opennars.parser.narsese.NarseseParser;
import com.googlecode.opennars.storage.*;

/**
 * The memory of the system.
 */
public class Memory extends Observable {
	
	private Parameters parameters;
	private RuleTables ruletables;
	public BudgetFunctions budgetfunctions;
    
    /* ---------- all members have limited space ---------- */
    
    /**
     * Concept bag. Containing all Concepts of the system.
     */
    private ConceptBag concepts;
    
    /**
     * Operators (built-in terms) table. Accessed by name.
     */
    private HashMap<String, Operator> operators;
    
    // There is no global Term table, which may ask for unlimited space.
    
    /**
     * List of inference newTasks, to be processed in the next working cycle.
     */
    private ArrayList<Task> newTasks;
    
    /**
     * New tasks to be processed in the near future.
     */
    private TaskBag taskBuffer;
    
    /* ---------- global variables used to reduce method arguments ---------- */
    
    /**
     * Shortcut to the selected Term.
     */
    public Term currentTerm;
    
    /**
     * Shortcut to the selected TaskLink.
     */
    public TaskLink currentTaskLink;
    
    /**
     * Shortcut to the selected Task.
     */
    public Task currentTask;
    
    /**
     * Shortcut to the selected TermLink.
     */
    public TermLink currentBeliefLink;
    
    /**
     * Shortcut to the selected belief (Sentence).
     */
    public Judgement currentBelief;
    
    public Base currentBase;
    
    /* ---------- initialization ---------- */
    
    /**
     * Initialize a new memory by creating all members.
     * <p>
     * Called in Center.reset only
     */
    public Memory() {
        init();
        ruletables = new RuleTables(this);
        budgetfunctions = new BudgetFunctions(this);
    }
    
    public void reset() {
    	init();
    }
    
    private void init() {
    	concepts = new ConceptBag();         // initially empty
        newTasks = new ArrayList<Task>();     // initially empty
        taskBuffer = new TaskBag();       // initially empty
        operators = Operator.setOperators(); // with operators created
        parameters = new Parameters();
    }
    
    /* ---------- access utilities ---------- */
    
    /**
     * Get a Term for a given name of a Concept or Operator, called in StringParser and the make methods of compound terms.
     * @param name the name of a concept or operator
     * @return a Term or null (if no Concept/Operator has this name)
     */
    public Term nameToListedTerm(String name) {
        Concept concept = concepts.get(name);
        if (concept != null)
            return concept.getTerm();           // Concept associated Term
        return operators.get(name);
    }
    
    /**
     * Check if a string is an operator name, called in StringParser only.
     * @param name the name of a possible operator
     * @return the corresponding operator or null
     */
    public Operator nameToOperator(String name) {
        return operators.get(name);
    }
    
    /**
     * Add a new operator with the given name
     * @param op the operator
     * @param name the operator's name. Should begin with ^.
     */
    public void addOperatorWithName(Operator op, String name) {
    	operators.put(name, op);
    }
    
    /**
     * Remove the operator with the given name
     * @param name the operator's name
     */
    public void removeOperatorWithName(String name) {
    	operators.remove(operators.get(name));
    }
    
    /**
     * Return the parameters of this reasoner
     * @return the parameters
     */
    public Parameters getParameters() {
    	return parameters;
    }

    /**
     * Return an ArrayList of the Concepts this reasoner knows about
     * @return the concepts
     */
    public ArrayList<Concept> getAllConcepts() {
    	return this.concepts.getAllContents();
    }
    
    public HashMap<String,Concept> getConceptMap() {
    	return this.concepts.getNameTable();
    }
    
    public Task getCurrentTask() {
    	return this.currentTask;
    }
    
    /**
     * Return an ArrayList of the Tasks this reasoner is working on
     * @return the tasks
     */
    public ArrayList<Task> getAllTasks() {
    	return this.taskBuffer.getAllContents();
    }

	/**
	 * @return the ruletables
	 */
	public RuleTables getRuletables() {
		return ruletables;
	}

	/**
     * Get an existing Concept for a given name, called from Term and ConceptWindow.
     * @param name the name of a concept
     * @return a Concept or null
     */
    public Concept nameToConcept(String name) {
        return concepts.get(name);
    }
    
    /**
     * Get an existing Concept for a given Term.
     * @param term The Term naming a concept
     * @return a Concept or null
     */
    public Concept termToConcept(Term term) {
        return nameToConcept(term.getName());
    }
    
    /**
     * Get the Concept associated to a Term, or creat it.
     * @param term indicating the concept
     * @return an existing Concept, or a new one
     */
    public Concept getConcept(Term term) {
        String n = term.getName();
        Concept concept = concepts.get(n);
        if (concept == null)
            concept = new Concept(term, this);        // the only place to make a new Concept
        return concept;
    }
    
    /**
     * Adjust the activation level of a Concept or Operator, called in Concept only.
     * @param c the concept to be adusted
     * @param b the new BudgetValue
     */
    public void activateConcept(Concept c, BudgetValue b) {
        BudgetValue budget;
        if (concepts.contains(c)) {     // listed Concept
            concepts.pickOut(c.getKey());
            this.budgetfunctions.activate(c, b);
            concepts.putBack(c);
        } else {                        // new Concept
            this.budgetfunctions.activate(c, b);
            concepts.putIn(c);
        }
    }
    
    /* ---------- new task entries ---------- */
    
    // There are three types of new tasks: (1) input, (2) derived, (3) activated
    // They are all added into the newTasks list, to be processed in the next cycle.
    // Some of them are reported and/or logged.
    
    /**
     * Input task comes from the InputWindow.
     * @param str the input line
     */
    public void inputStringWithParser(String str, Parser parser) throws InvalidInputException {
        Task task = parser.parseTask(str, this);       // the only place to call StringParser
        if (task != null) {
            if (task.aboveThreshold()) {                       // set a threshold?
                report(task.getSentence(), true);             // report input
                newTasks.add(task);       // wait to be processed in the next cycle
            }
        }
    }
    
    /**
     * Input task from some source
     * @param task the task to add
     */
    public void inputTask(Task task) {
    	if (task != null) {
            if (task.aboveThreshold()) {                       // set a threshold?
                report(task.getSentence(), true);             // report input
                newTasks.add(task);       // wait to be processed in the next cycle
            }
        }
    }
    
    /**
     * Derived task comes from the inference rules.
     * @param task the derived task
     */
    private void derivedTask(Task task) {
        if (task.aboveThreshold()) {
            float budget = task.getBudget().singleValue();
            float minSilent = parameters.SILENT_LEVEL / 100.0f;
            if (budget > minSilent)
                report(task.getSentence(), false);        // report significient derived Tasks
            newTasks.add(task);
        }
    }
    
    /**
     * Reporting executed task, called from Concept.directOperation.
     * @param task the executed task
     */
    public void executedTask(Task task) {   // called by the inference rules
        float budget = task.getBudget().singleValue();
        float minSilent = parameters.SILENT_LEVEL / 100.0f;
        if (budget > minSilent)
            report(task.getSentence(), false);
    }
    
    /**
     * Activated task comes from MatchingRules.
     * @param budget The budget value of the new Task
     * @param sentence The content of the new Task
     * @param isInput Whether the question is input
     */
    public void activatedTask(BudgetValue budget, Sentence sentence, boolean isInput) {
        Task task = new Task(sentence, budget, this);
        newTasks.add(task);
    }
    
    /* --------------- new task building --------------- */
    
    /**
     * Shared final operations by all double-premise rules, called from the rules except StructuralRules
     * @param budget The budget value of the new task
     * @param content The content of the new task
     * @param truth The truth value of the new task
     */
    public void doublePremiseTask(BudgetValue budget, Term content, TruthValue truth) {
        Sentence newSentence = Sentence.make(currentTask.getSentence(), content, truth, this.currentBase, this);
        Task newTask = new Task(newSentence, budget, this);
        derivedTask(newTask);
    }
    
    /**
     * Shared final operations by all single-premise rules, called in StructuralRules
     * @param budget The budget value of the new task
     * @param content The content of the new task
     * @param truth The truth value of the new task
     */
    public void singlePremiseTask(BudgetValue budget, Term content, TruthValue truth) {
        Sentence sentence = currentTask.getSentence();
        Sentence newSentence = Sentence.make(sentence, content, truth, sentence.getBase(), this);
        Task newTask = new Task(newSentence, budget, this);
        newTask.setStructual();
        derivedTask(newTask);
    }

    /**
     * Shared final operations by all single-premise rules, called in MatchingRules
     * @param budget The budget value of the new task
     * @param truth The truth value of the new task
     */
    public void singlePremiseTask(TruthValue truth, BudgetValue budget) {
        Term content = this.currentTask.getContent();
        Base base = this.currentBelief.getBase();
        Sentence newJudgment = Sentence.make(content, Symbols.JUDGMENT_MARK, truth, base, this);
        Task newTask = new Task(newJudgment, budget, this);
        newTask.setStructual();
        derivedTask(newTask);
    }
    
    /* ---------- system working cycle ---------- */
    
    /**
     * An atomic working cycle of the system. Called from Center only.
     */
    public void cycle() {
        processTask();      // tune relative frequency?
        processConcept();   // use this order to check the new result
    }
    
    /**
     * Process the newTasks accumulated in the previous cycle, accept input ones
     * and those that corresponding to existing concepts, plus one from the buffer.
     */
    private void processTask() {
        Task task;
        int counter = newTasks.size();              // don't include new tasks produced in the current cycle
        while (counter-- > 0) {                     // process the newTasks of the previous cycle
            task = (Task) newTasks.remove(0);
            if (task.getSentence().isInput() || (termToConcept(task.getContent()) != null))  // new input or existing concept
                immediateProcess(task);                   // immediate process
            else
                taskBuffer.putIn(task);             // postponed process
        }
        task = (Task) taskBuffer.takeOut();         // select a task from taskBuffer
        if (task != null)
            immediateProcess(task);                       // immediate process
    }
    
    /**
     * Select a concept to fire.
     */
    private void processConcept() {
        Concept currentConcept = (Concept) concepts.takeOut();
        if (currentConcept != null) {
            currentTerm = currentConcept.getTerm();
            concepts.putBack(currentConcept);   // current Concept remains in the bag all the time
            currentConcept.fire();              // a working cycle
        }
    }
    
    /* ---------- task / belief insertion ---------- */
    
    /**
     * Imediate processing of a new task
     * @param task the task to be accepted
     */
    private void immediateProcess(Task task) {
        Term content = task.getContent();
        if (content.isConstant()) {                        // does not creat concept for Query?
            Concept c = getConcept(content);
            c.directProcess(task);
        }
        if (task.aboveThreshold())
            continuedProcess(task, content);
    }

    /**
     * Link to a new task from all relevant concepts for distributed processing.
     * @param task The task to be linked
     * @param content The content of the task
     */
    private void continuedProcess(Task task, Term content) {
        TaskLink tLink;
        Concept c1 = null;                      // local Concept
        BudgetValue budget = task.getBudget();
        if (content.isConstant()) {
            c1 = getConcept(content);
            tLink = new TaskLink(task, null, budget, this);   // link type SELF
            c1.insertTaskLink(tLink);
        }
        if (content instanceof CompoundTerm) {
            Term component;                     // component term
            Concept c2;                         // component concept
            TermLink cLink1, cLink2;     // a pair of compound/component links
            ArrayList<TermLink> cLinks;  // link list
            cLinks = (c1 != null) ? c1.getTermLinks() : ((CompoundTerm) content).prepareComponentLinks(this);  // use saved
            short[] indices;
            BudgetValue subBudget = this.budgetfunctions.distributeAmongLinks(budget, cLinks.size());
            if (!subBudget.aboveThreshold())
                return;
            for (TermLink cLink0 : cLinks) {
                component = cLink0.getTarget();
                c2 = getConcept(component);
                if (!(task.isStructual() && (cLink0.getType() == TermLink.TRANSFORM))) {
                    tLink = new TaskLink(task, cLink0, subBudget, this);
                    c2.insertTaskLink(tLink);               // component link to task
                }
            }
        }
    }
    
    /* ---------- report ---------- */
    
    /**
     * Display selected task.
     * @param sentence the sentence to be displayed
     * @param input whether the task is input
     */
    public void report(Sentence sentence, boolean input) {
    	this.setChanged();
        notifyObservers(sentence);
    }
}
