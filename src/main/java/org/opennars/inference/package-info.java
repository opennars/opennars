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
/**
 * The inference rules and control functions
 *
 * <p>
 * The entry point of the package is <tt>RuleTables</tt>, which dispatch the premises
 * (a task, and maybe also a belief) to various rules, according to their type combination.
 * </p>
 *
 * <p>
 * There are four major groups of inference rules:
 * </p>
 *
 * <ol>
 * <li><tt>LocalRules</tt>, where the task and belief contains the same pair
 * of terms, and the rules provide direct solutions to problems, revise beliefs,
 * and derive some conclusions;</li>
 * <li><tt>SyllogisticRules</tt>, where the task and belief share one common term,
 * and the rules derive conclusions between the other two terms;</li>
 * <li><tt>CompositionalRules</tt>, where the rules derive conclusions by compose or
 * decompose the terms in premises, so as to form new terms that are not in the two premises;</li>
 * <li><tt>StructuralRules</tt>, where the task derives conclusions all by itself,
 * while the other "premise" serves by indicating a certain syntactic structure in a compound term.</li>
 * </ol>
 *
 * <p>
 * In the system, forward inference (the task is a Judgment) and backward inference
 * (the task is a Question) are mostly isomorphic to each other, so that the
 * inference rules produce clonclusions with the same content for different types of tasks.
 * However, there are exceptions. For example, backward inference does not generate compound terms.
 * </p>
 *
 * <p>
 * There are three files containing numerical functions:
 * </p>
 *
 * <ol>
 * <li><tt>TruthFunctions</tt>: the functions that calculate the truth value
 * of the derived judgments and the desire value (a variant of truth value) of the
 * derived goals;</li>
 * <li><tt>BudgetFunctions</tt>: the functions that calculate the budget value of
 * the derived tasks, as well as adjust the budget value of the involved items
 * (concept, task, and links);</li>
 * <li><tt>UtilityFunctions</tt>: the common basic functions used by the others.</li>
 * </ol>
 *
 * <p>
 * In each case, there may be multiple applicable rules, which will be applied in parallel.
 * For each rule, each conclusion is formed in three stages, to determine (1) the content
 * (as a Term), (2) the truth-value, and (3) the budget-value, roughly in that order.
 * </p>
 */
package org.opennars.inference;

