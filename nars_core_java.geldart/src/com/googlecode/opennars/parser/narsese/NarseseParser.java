/*
 * StringParser.java
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

package com.googlecode.opennars.parser.narsese;

import java.util.*;

import com.googlecode.opennars.entity.*;
import com.googlecode.opennars.language.*;
import com.googlecode.opennars.main.*;
import com.googlecode.opennars.parser.InvalidInputException;
import com.googlecode.opennars.parser.Parser;


/**
 * Parse input String into Task.
 */
public class NarseseParser extends Parser {
        
    /**
     * return the prefex of a task string that contains a BudgetValue
     * @return a String containing a BudgetValue
     * @param s the input in a StringBuffer
     * @throws com.googlecode.opennars.parser.NarseseParser.InvalidInputException if the input cannot be parsed into a BudgetValue
     */
    static String getBudgetString(StringBuffer s) throws InvalidInputException {
        if (s.charAt(0) != BUDGET_VALUE_MARK) // use default
            return null;  // null values
        int i = s.indexOf(BUDGET_VALUE_MARK + "", 1);    // looking for the end
        if (i < 0) // no matching closer
            throw new InvalidInputException("missing budget closer");
        String budgetString = s.substring(1, i).trim();
        if (budgetString.length() == 0) // empty usage
            throw new InvalidInputException("empty budget");
        s.delete(0, i+1);                 // remaining input to be processed outside
        return budgetString;
    }
    
    /**
     * return the postfex of a task string that contains a TruthValue
     * @return a String containing a TruthValue
     * @param s the input in a StringBuffer
     * @throws com.googlecode.opennars.parser.NarseseParser.InvalidInputException if the input cannot be parsed into a TruthValue
     */
    static String getTruthString(StringBuffer s) throws InvalidInputException {
        int last = s.length()-1;
        if (s.charAt(last) != TRUTH_VALUE_MARK)     // use default
            return null;
        int first = s.indexOf(TRUTH_VALUE_MARK + "");    // looking for the beginning
        if (first == last) // no matching closer
            throw new InvalidInputException("missing truth mark");
        String truthString = s.substring(first+1, last).trim();
        if (truthString.length() == 0) // empty usage
            throw new InvalidInputException("empty truth");
        s.delete(first, last+1);                 // remaining input to be processed outside
        return truthString;
    }
    
    /**
     * parse the input String into a TruthValue (or DesireValue)
     * @param s input String
     * @param type Task type
     * @param memory the memory object being inserted into
     * @throws com.googlecode.opennars.parser.NarseseParser.InvalidInputException If the String cannot be parsed into a TruthValue
     * @return the input TruthValue
     */
    static TruthValue parseTruth(String s, char type, Memory memory) throws InvalidInputException {
        if (type == QUESTION_MARK)
            return null;
        float frequency = 1.0f;
        float confidence = memory.getParameters().DEFAULT_JUDGMENT_CONFIDENCE;
        if (s != null) {
            int i = s.indexOf(VALUE_SEPARATOR);
            if (i < 0)
                frequency = Float.parseFloat(s);
            else {
                frequency = Float.parseFloat(s.substring(0,i));
                confidence = Float.parseFloat(s.substring(i+1));
            }
        }
        return new TruthValue(frequency, confidence);
    }
    
    /**
     * parse the input String into a BudgetValue
     * @param s input String
     * @param punctuation Task punctuation
     * @param truth the TruthValue of the task
     * @param memory the memory object being inserted into
     * 
     * @return the input BudgetValue
     * @throws com.googlecode.opennars.parser.NarseseParser.InvalidInputException If the String cannot be parsed into a BudgetValue
     */
    static BudgetValue parseBudget(String s, char punctuation, TruthValue truth, Memory memory) throws InvalidInputException {
        float priority, durability;
        switch (punctuation) {
            case JUDGMENT_MARK:
                priority = memory.getParameters().DEFAULT_JUDGMENT_PRIORITY;
                durability = memory.getParameters().DEFAULT_JUDGMENT_DURABILITY;
                break;
            case GOAL_MARK:
                priority = memory.getParameters().DEFAULT_GOAL_PRIORITY;
                durability = memory.getParameters().DEFAULT_GOAL_DURABILITY;
                break;
            case QUESTION_MARK:
                priority = memory.getParameters().DEFAULT_QUESTION_PRIORITY;
                durability = memory.getParameters().DEFAULT_QUESTION_DURABILITY;
                break;
            default:
                throw new InvalidInputException("unknown punctuation");
        }
        if (s != null) { // overrite default
            int i = s.indexOf(VALUE_SEPARATOR);
            if (i < 0) {        // default durability
                priority = Float.parseFloat(s);
            } else {
                priority = Float.parseFloat(s.substring(0, i));
                durability = Float.parseFloat(s.substring(i+1));
            }
        }
        float quality = (punctuation == QUESTION_MARK) ? 1 : memory.budgetfunctions.truthToQuality(truth);
        return new BudgetValue(priority, durability, quality, memory);
    }
    
    /* ---------- parse String into term ---------- */
    
    /**
     * Top-level method that parse a Term in general, which may recursively call itself.
     * <p>
     * There are 5 valid cases:
     * 1. (Op, A1, ..., An) is a common CompoundTerm (including CompoundStatement);
     * 2. {A1, ..., An} is an SetExt;
     * 3. [A1, ..., An] is an SetInt;
     * 4. <T1 Re T2> is a Statement (including higher-order Statement);
     * 5. otherwise it is a simple term.
     * @param s0 the String to be parsed
     * @param memory the memory object being inserted into
     * @throws com.googlecode.opennars.parser.NarseseParser.InvalidInputException the String cannot be parsed into a Term
     * @return the Term generated from the String
     */
    static Term parseTerm(String s0, Memory memory) throws InvalidInputException {
        String s = s0.trim();
        if (s.length() == 0)
            throw new InvalidInputException("missing content");
        Term t = memory.nameToListedTerm(s);    // existing constant or operator
        if (t != null)
            return t;                           // existing Term
        int index = s.length()-1;
        char first = s.charAt(0);
        char last = s.charAt(index);
        switch (first) {
            case COMPOUND_TERM_OPENER:
                if (last == COMPOUND_TERM_CLOSER)
                    return parseCompoundTerm(s.substring(1, index), memory);
                else
                    throw new InvalidInputException("missing CompoundTerm closer");
            case SET_EXT_OPENER:
                if (last == SET_EXT_CLOSER)
                    return SetExt.make(parseArguments(s.substring(1, index) + ARGUMENT_SEPARATOR, memory), memory);
                else
                    throw new InvalidInputException("missing ExtensionSet closer");
            case SET_INT_OPENER:
                if (last == SET_INT_CLOSER)
                    return SetInt.make(parseArguments(s.substring(1, index) + ARGUMENT_SEPARATOR, memory), memory);
                else
                    throw new InvalidInputException("missing IntensionSet closer");
            case STATEMENT_OPENER:
                if (last == STATEMENT_CLOSER)
                    return parseStatement(s.substring(1, index), memory);
                else
                    throw new InvalidInputException("missing Statement closer");
            default:
                return parseSimpleTerm(s);
        }
    }
    
    /**
     * Parse a Term that has no internal structure.
     * <p>
     * The Term can be a constant or a variable.
     * @param s0 the String to be parsed
     * @throws com.googlecode.opennars.parser.NarseseParser.InvalidInputException the String cannot be parsed into a Term
     * @return the Term generated from the String
     */
    private static Term parseSimpleTerm(String s0) throws InvalidInputException {
        String s = s0.trim();
        if (s.length() == 0)
            throw new InvalidInputException("missing term");
        if (s.contains(" "))                // invalid characters in a name
            throw new InvalidInputException("invalid term");
        Term term;
        char prefix = s.charAt(0);
        if ((prefix == VARIABLE_TAG) || (prefix == QUERY_VARIABLE_TAG)) {
            term = new Variable(s);         // the only place to directly call this constructor
        } else
            term = new Term(s);             // the only place to directly call this constructor
        return term;
    }
    
    /**
     * Parse a String to create a Statement.
     * @return the Statement generated from the String
     * @param s0 The input String to be parsed
     * @param memory the memory object being inserted into
     * @throws com.googlecode.opennars.parser.NarseseParser.InvalidInputException the String cannot be parsed into a Term
     */
    private static Statement parseStatement(String s0, Memory memory) throws InvalidInputException {
        String s = s0.trim();
        int i = topRelation(s);
        if (i < 0)
            throw new InvalidInputException("invalid statement");
        String relation = s.substring(i, i+3);
        Term subject = parseTerm(s.substring(0, i), memory);
        Term predicate = parseTerm(s.substring(i+3), memory);
        Statement t = Statement.make(relation, subject, predicate, memory);
        if (t == null)
            throw new InvalidInputException("invalid statement");
        return t;
    }
    
    /**
     * Parse a String to create a CompoundTerm.
     * @return the Term generated from the String
     * @param s0 The String to be parsed
     * @param memory the memory object being inserted into
     * @throws com.googlecode.opennars.parser.NarseseParser.InvalidInputException the String cannot be parsed into a Term
     */
    private static Term parseCompoundTerm(String s0, Memory memory) throws InvalidInputException {
        String s = s0.trim();
        int firstSeparator = s.indexOf(ARGUMENT_SEPARATOR);
        String op = s.substring(0, firstSeparator).trim();
        if (!CompoundTerm.isOperator(op, memory))
            throw new InvalidInputException("unknown operator: " + op);
        ArrayList<Term> arg = parseArguments(s.substring(firstSeparator+1) + ARGUMENT_SEPARATOR, memory);
        Term t = CompoundTerm.make(op, arg, memory);
        if (t == null)
            throw new InvalidInputException("invalid compound term");
        return t;
    }
    
    /**
     * Parse a String into the argument list of a CompoundTerm.
     * @return the arguments in an ArrayList
     * @param s0 The String to be parsed
     * @param memory the memory object being inserted into
     * @throws com.googlecode.opennars.parser.NarseseParser.InvalidInputException the String cannot be parsed into an argument list
     */
    private static ArrayList<Term> parseArguments(String s0, Memory memory) throws InvalidInputException {
        String s = s0.trim();
        ArrayList<Term> list = new ArrayList<Term>();
        int start = 0;
        int end = 0;
        Term t;
        while (end < s.length()-1) {
            end = nextSeparator(s, start);
            t = parseTerm(s.substring(start, end), memory);     // recursive call
            list.add(t);
            start = end+1;
        }
        if (list.isEmpty())
            throw new InvalidInputException("null argument");
        return list;
    }
    
    /* ---------- locate top-level substring ---------- */
    
    /**
     * Locate the first top-level separator in a CompoundTerm
     * @return the index of the next seperator in a String
     * @param s The String to be parsed
     * @param first The starting index
     */
    private static int nextSeparator(String s, int first) {
        int levelCounter = 0;
        int i = first;
        while (i < s.length()-1) {
            if (isOpener(s, i))
                levelCounter++;
            else if (isCloser(s, i))
                levelCounter--;
            else if (s.charAt(i) == ARGUMENT_SEPARATOR)
                if (levelCounter == 0)
                    break;
            i++;
        }
        return i;
    }
    
    /**
     * locate the top-level relation in a statement
     * @return the index of the top-level relation
     * @param s The String to be parsed
     */
    private static int topRelation(String s) {      // need efficiency improvement
        int levelCounter = 0;
        int i = 0;
        while (i < s.length()-3) {    // don't need to check the last 3 characters
            if ((levelCounter == 0) && (Statement.isRelation(s.substring(i, i+3))))
                return i;
            if (isOpener(s, i))
                levelCounter++;
            else if (isCloser(s, i))
                levelCounter--;
            i++;
        }
        return -1;
    }
        
    /* ---------- recognize symbols ---------- */
        
    /**
     * check CompoundTerm opener symbol
     * @return if the given String is an opener symbol
     * @param s The String to be checked
     * @param i The starting index
     */
    private static boolean isOpener(String s, int i) {
        char c = s.charAt(i);
        boolean b = (c == COMPOUND_TERM_OPENER) ||
                (c == SET_EXT_OPENER) ||
                (c == SET_INT_OPENER) ||
                (c == STATEMENT_OPENER);
        if (!b)
            return false;
        if (i+3 <= s.length() && Statement.isRelation(s.substring(i, i+3)))
            return false;
        return true;
    }
    
    /**
     * check CompoundTerm closer symbol
     * @return if the given String is a closer symbol
     * @param s The String to be checked
     * @param i The starting index
     */
    private static boolean isCloser(String s, int i) {
        char c = s.charAt(i);
        boolean b = (c == COMPOUND_TERM_CLOSER) ||
                (c == SET_EXT_CLOSER) ||
                (c == SET_INT_CLOSER) ||
                (c == STATEMENT_CLOSER);
        if (!b)
            return false;
        if (i >= 2 && Statement.isRelation(s.substring(i-2, i+1)))
            return false;
        return true;
    }

	public Task parseTask(String input, Memory memory) throws InvalidInputException {
		StringBuffer buffer = new StringBuffer(input);
        String budgetString = getBudgetString(buffer);
        String truthString = getTruthString(buffer);
        String str = buffer.toString().trim();
        int last = str.length() - 1;
        char punc = str.charAt(last);
        TruthValue truth = parseTruth(truthString, punc, memory);
        BudgetValue budget = parseBudget(budgetString, punc, truth, memory);
        Term content = parseTerm(str.substring(0,last), memory);
        Base base = (punc == QUESTION_MARK) ? null : new Base();
        Sentence sentence = Sentence.make(content, punc, truth, base, memory);
        if (sentence == null)
            throw new InvalidInputException("invalid sentence");
        sentence.setInput();
        Task task = new Task(sentence, budget, memory);
        return task;
	}

	@Override
	public List<Task> parseTasks(String input, Memory memory)
			throws InvalidInputException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String serialiseSentence(Sentence task, Memory memory) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String serialiseSentences(List<Sentence> tasks, Memory memory) {
		// TODO Auto-generated method stub
		return null;
	}
}
