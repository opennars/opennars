package com.googlecode.opennars.parser;

import java.util.List;

import com.googlecode.opennars.entity.Sentence;
import com.googlecode.opennars.entity.Task;
import com.googlecode.opennars.main.Memory;

public abstract class Parser extends Symbols {

	/**
	 * Parses a given string into a (single) task.
	 * @param buffer the single-line input String
	 * @param memory the memory object doing the parsing
	 * @return an input Task, or null if the input line cannot be parsed into a Task
	 * @throws InvalidInputException 
	 */
	public abstract Task parseTask(String input, Memory memory) throws InvalidInputException;
	
	/**
	 * Parses a given string into a (list of) tasks.
	 * @param input
	 * @param memory
	 * @return a list of input Tasks, or an empty list if the input line cannot be parsed into a Task
	 * @throws InvalidInputException
	 */
	public abstract List<Task> parseTasks(String input, Memory memory) throws InvalidInputException;
	
	/**
	 * Serialises the sentence to this parser's format
	 * @param task
	 * @param memory
	 * @return a string containing the serialised task
	 */
	public abstract String serialiseSentence(Sentence task, Memory memory);
	
	public abstract String serialiseSentences(List<Sentence> tasks, Memory memory);
}