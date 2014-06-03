/*
 * Sentence.java
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

import com.googlecode.opennars.language.*;
import com.googlecode.opennars.main.*;
import com.googlecode.opennars.parser.Symbols;

/**
 * A Sentence contains a Statement, a TruthValue, and a Base list.
 *<p>
 * It is used as the premises and conclusions of all inference rules.
 */
public abstract class Sentence {    
    protected Term content;
    protected char punctuation;
    protected TruthValue truth = null;
    protected Base base = null;
    protected boolean input = false;            // whether it is an input sentence
    protected Judgement bestSolution = null;       // for Question and Goal
    protected Memory memory;					// to change things
//    protected Object token;						// to track information through inferences
    
    /**
     * Make a Sentence from an input String. Called by StringParser.
     * @param term The content of the sentence
     * @param punc The punctuation (and therefore, type) of the sentence
     * @param truth The truth value of the sentence, if it is a Judgement (or Goal)
     * @param base The base of the truth value (for Judgement or Goal)
     * @return the Sentence generated from the arguments
     */
    public static Sentence make(Term term, char punc, TruthValue truth, Base base, Memory memory) {
        if (term instanceof CompoundTerm)
            ((CompoundTerm) term).renameVariables();
        switch (punc) {
            case Symbols.JUDGMENT_MARK:
                return new Judgement(term, punc, truth, base, memory);
            case Symbols.GOAL_MARK:
                return new Goal(term, punc, truth, base, memory);
            case Symbols.QUESTION_MARK:
                return new Question(term, punc, memory);
            default:
                return null;
        }
    }

    /**
     * Make a derived Sentence. Called by memory 
     * @param term The content of the sentence
     * @param oldS A sample sentence providing the type of the new sentence
     * @param truth The truth value of the sentence, if it is a Judgement (or Goal)
     * @param base The base of the truth value (for Judgement or Goal)
     * @return the Sentence generated from the arguments
     */
    public static Sentence make(Sentence oldS, Term term, TruthValue truth, Base base, Memory memory) {
        if (term instanceof CompoundTerm)
            ((CompoundTerm) term).renameVariables();
        if (oldS instanceof Question)
            return new Question(term, Symbols.QUESTION_MARK, memory);
        if (oldS instanceof Goal)
            return new Goal(term, Symbols.GOAL_MARK, truth, base, memory);
        return new Judgement(term, Symbols.JUDGMENT_MARK, truth, base, memory);
    }
    
    public Term getContent() {
        return content;
    }

    public Term cloneContent() {
        return (Term) content.clone();
    }

    public TruthValue getTruth() {
        return null;
    }

    public Base getBase() {
        return null;
    }
    
    // distinguish Judgement from Goal
    public boolean isJudgment() {
        return (punctuation == Symbols.JUDGMENT_MARK);
    }

    public boolean isInput() {
        return input;
    }
    
    public void setInput() {
        input = true;
    }

    public Judgement getBestSolution() {
        return bestSolution;
    }
    
    public void setBestSolution(Judgement judg) {
        bestSolution = judg;
        if (input)
            memory.report(judg, false);        // report answer to input question
    }
    
    // display a sentence
    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append(content.getName());
        s.append(punctuation + " ");
        if (truth != null) {
            s.append(truth.toString()); 
            s.append(base.toString());
        }
        if (bestSolution != null)
            s.append("BestSolution: " + bestSolution);
        
        return s.toString();
    }
    
    // display a sentence in compact form (2 digits)
    public String toString2() {
        StringBuffer s = new StringBuffer();
        s.append(content.getName());
        s.append(punctuation + " ");
        if (truth != null) {
            s.append(truth.toString2()); 
        }
        return s.toString();
    }

	public boolean isGoal() {
		return (this.punctuation == Symbols.GOAL_MARK);
	}
	
	public boolean isQuestion() {
		return (this.punctuation == Symbols.QUESTION_MARK);
	}
}
