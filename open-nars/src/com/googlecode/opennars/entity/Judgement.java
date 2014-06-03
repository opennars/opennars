/*
 * Judgement.java
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

import com.googlecode.opennars.language.Term;
import com.googlecode.opennars.main.*;

/**
 * A Judgement is an piece of new knowledge to be absorbed.
 */
public class Judgement extends Sentence {
    
    public Judgement(Term term, char punc, TruthValue t, Base b, Memory memory) {
        content = term;
        punctuation = punc;
        truth = t;
        base = b;
        this.memory = memory;
    }
  
    public TruthValue getTruth() {
        return truth;
    }

    public float getFrequency() {
        return truth.getFrequency();
    }

    public float getConfidence() {
        return truth.getConfidence();
    }

    public Base getBase() {
        return base;
    }

    boolean equivalentTo(Judgement judgement2) {
        return (truth.equals(judgement2.getTruth()) && base.equals(judgement2.getBase())); // may have different key
    }

    public float getExpectationDifference(Judgement that) {
        return getTruth().getExpDifAbs(that.getTruth());
    }
    
    public float solutionQuality(Sentence sentence) {
        Term problem = sentence.getContent(); 
        if (sentence instanceof Goal) 
            return truth.getExpectation();
        else if (problem.isConstant())          // "yes/no" question
            return truth.getConfidence();                                 // by confidence
        else                                                            // "what" question or goal
            return truth.getExpectation() / content.getComplexity();      // by likelihood/simplicity, to be refined
    }
    
    public boolean noOverlapping(Judgement judgement) {
        Base b = Base.make(base, judgement.getBase());
        if (b == null)
            return false;
        memory.currentBase = b;
        return true;
    }
}

