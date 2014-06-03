/*
 * Question.java
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
import com.googlecode.opennars.main.Memory;

/**
 * A Question is a sentence without a truth value needs evaluation, and may conain query variables
 */
public class Question extends Sentence {

    public Question(Term term, char punc, Memory memory) {
        content = term;
        punctuation = punc;
        this.memory = memory;
    }
}

