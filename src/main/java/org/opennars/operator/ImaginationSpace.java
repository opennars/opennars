/* 
 * The MIT License
 *
 * Copyright 2018 The OpenNARS authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.opennars.operator;

import org.opennars.entity.TruthValue;
import org.opennars.language.Conjunction;

/**
 * ImaginationSpace: A group of operations that when executed in a certain sequence 
 * add up to a certain declarative "picture". This "picture" might be different than the feedback of 
 * the last operation in the sequence: for instance in case of eye movements, each movement 
 * feedback corresponds to a sampling result, where each adds its part of information.
 * @author Patrick
 */
public interface ImaginationSpace {
    //
    TruthValue AbductionOrComparisonTo(final ImaginationSpace obj, boolean comparison);
    //attaches an imagination space to the conjunction that is constructed
    //by starting with the leftmost element of the conjunction
    //and then gradually moving to the right
    ImaginationSpace ConstructSpace(Conjunction program);
    //Has to return a new instance, not changing "this"!
    ImaginationSpace ProgressSpace(Operation op, ImaginationSpace B);
    //Check whether the operation is part of the space:
    boolean IsOperationInSpace(Operation oper);
}
