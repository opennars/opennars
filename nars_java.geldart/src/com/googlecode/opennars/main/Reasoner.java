/**
 * 
 */
package com.googlecode.opennars.main;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import com.googlecode.opennars.entity.BudgetValue;
import com.googlecode.opennars.entity.Concept;
import com.googlecode.opennars.entity.Sentence;
import com.googlecode.opennars.entity.Task;
import com.googlecode.opennars.operation.Operator;
import com.googlecode.opennars.parser.InvalidInputException;
import com.googlecode.opennars.parser.Parser;
import com.googlecode.opennars.parser.narsese.NarseseParser;

/**
 * The Reasoner class implements a threaded NARS reasoner.
 * @author jgeldart
 *
 */
public class Reasoner extends Observable implements Observer, Runnable {
	
	private Thread thread;
	private Memory memory;
	private Parser parser;
	private ArrayList<Task> inputQueue;
	
	/**
	 * Create a new reasoner using default settings
	 */
	public Reasoner() {
		parser = new NarseseParser();
		memory = new Memory();
		memory.addObserver(this);
		inputQueue = new ArrayList<Task>();
	}
	
	/**
	 * Creates a new reasoner with the given string parser
	 * @param parser
	 */
	public Reasoner(Parser parser) {
		this();
		this.parser = parser;
	}
	
	/**
	 * Start the reasoner thread
	 */
	public void start() {
		if(thread == null) {
			thread = new Thread(this);
			thread.start();
		}
	}
	
	/**
	 * Stop the reasoner thread
	 */
	public void stop() {
		thread = null;
	}
	
	/**
	 * Perform a single reasoning step
	 */
	public void step() {
		try {
			Task task = this.getTask();
			if(task != null)
				memory.inputTask(task);
			memory.cycle();
		} catch (Exception e) {
			if(e instanceof InvalidInputException) {
				this.hasChanged();
				this.notifyObservers(e);
			}
			else {
				//System.err.println(e.toString());
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Reset the reasoner back to 'factory defaults'
	 */
	public void reset() {
		memory.reset();
	}
	
	/* ----- communicating ----- */
	
	/**
	 * Tell the reasoner a sentence string which is parsed using the chosen parser
	 * @param sent the sentence
	 */
	public synchronized void tellSentenceString(String sent) {
		try {
			Task task = parser.parseTask(sent, memory);
			inputQueue.add(task);
		}
		catch (InvalidInputException e) {
			this.setChanged();
			this.notifyObservers(e);
		}
	}
	
	/**
	 * Tell the reasoner a task
	 * @param task the task
	 */
	public synchronized void tellTask(Task task) {
		inputQueue.add(task);
	}
	
	/**
	 * Tell the reasoner a sentence with the given budget for working with it
	 * @param sent the sentence
	 * @param budget the budget
	 */
	public synchronized void tellSentence(Sentence sent, BudgetValue budget) {
		Task task = new Task(sent, budget, memory);
		inputQueue.add(task);
	}
	
	/**
	 * Tell the reasoner a sentence with the given priority, durability and quality needed
	 * to allocate resources to the task
	 * @param sent the sentence
	 * @param priority the priority
	 * @param durability the durability
	 * @param quality the quality
	 */
	public synchronized void tellSentence(Sentence sent, float priority, float durability, float quality) {
		Task task = new Task(sent, new BudgetValue(priority, durability, quality, memory), memory);
		inputQueue.add(task);
	}

	private synchronized Task getTask() {
		try {
			return inputQueue.remove(0);
		}
		catch (IndexOutOfBoundsException e) {
			return null;
		}
	}
	
	/* ----- setters and getters ----- */
	
	/**
	 * Get the memory
	 */
	public Memory getMemory() {
		return memory;
	}
	
	/**
	 * Returns the parser used to parse sentence strings
	 * @return the parser
	 */
	public Parser getParser() {
		return parser;
	}
	
	/**
	 * Set the parser used to parse sentence strings
	 * @param parser the parser
	 */
	public void setParser(Parser parser) {
		this.parser = parser;
	}
	
	/**
	 * Get the reasoner's parameters
	 * @return the parameters
	 */
	public Parameters getParameters() {
		return memory.getParameters();
	}
	
	/**
	 * Get all the concepts this reasoner currently knows about
	 * @return the concepts
	 */
	public ArrayList<Concept> getConcepts() {
		return memory.getAllConcepts();
	}
	
	/**
	 * Get all the tasks this reasoner currently knows about
	 * @return the tasks
	 */
	public ArrayList<Task> getTasks() {
		return memory.getAllTasks();
	}
	
	/**
	 * Get the current reasoning task
	 * @return the task
	 */
	public Task getCurrentTask() {
		return memory.getCurrentTask();
	}
	
	/**
	 * Fetch the operator with the given name
	 * @param name the operator's name
	 * @return the operator
	 */
	public Operator getOperatorWithName(String name) {
		return memory.nameToOperator(name);
	}
	
	/**
	 * Add the operator with the given name
	 * @param op the operator
	 * @param name the name
	 */
	public void addOperatorWithName(Operator op, String name) {
		memory.addOperatorWithName(op, name);
	}
	
	/* ------ internals ------ */

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable o, Object arg) {
		this.setChanged();
		this.notifyObservers(arg);
	}

	public void run() {
		Thread thisThread = Thread.currentThread();
		while(thread == thisThread) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				
			}
			step();
		}
	}
	
}
