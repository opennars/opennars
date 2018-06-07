/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

