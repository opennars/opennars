/*
 * SyllogisticRules.java
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
 * but WITHOUT ANY WARRANTY; without even the abduction warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.inference;

import nars.entity.*;
import nars.language.*;
import nars.io.Symbols;
import nars.storage.Memory;

/**
 * Syllogisms: Inference rules based on the transitivity of the relation.
 */
public final class SyllogisticRules {

    /* --------------- rules used in both first-tense inference and higher-tense inference --------------- */
    /**
     * {<S ==> M>, <M ==> P>} |- {<S ==> P>, <P ==> S>}
     * @param term1 Subject of the first new task
     * @param term2 Predicate of the first new task
     * @param sentence The first premise
     * @param belief The second premise
     * @param memory Reference to the memory
     */
    static void dedExe(Term term1, Term term2, Sentence sentence, Sentence belief, Memory memory) {
        if (Statement.invalidStatement(term1, term2)) {
            return;
        }
        TruthValue value1 = sentence.getTruth();
        TruthValue value2 = belief.getTruth();
        TruthValue truth1 = null;
        TruthValue truth2 = null;
        BudgetValue budget1, budget2;
        if (sentence.isQuestion()) {
            budget1 = BudgetFunctions.backwardWeak(value2, memory);
            budget2 = BudgetFunctions.backwardWeak(value2, memory);
        } else {
            truth1 = TruthFunctions.deduction(value1, value2);
            truth2 = TruthFunctions.exemplification(value1, value2);
            budget1 = BudgetFunctions.forward(truth1, memory);
            budget2 = BudgetFunctions.forward(truth2, memory);
        }
        Statement content = (Statement) sentence.getContent();
        Statement content1 = Statement.make(content, term1, term2, memory);
        Statement content2 = Statement.make(content, term2, term1, memory);
        memory.doublePremiseTask(content1, truth1, budget1);
        memory.doublePremiseTask(content2, truth2, budget2);
    }

    /**
     * {<M ==> S>, <M ==> P>} |- {<S ==> P>, <P ==> S>, <S <=> P>}
     * @param term1 Subject of the first new task
     * @param term2 Predicate of the first new task
     * @param taskSentence The first premise
     * @param belief The second premise
     * @param figure Locations of the shared term in premises
     * @param memory Reference to the memory
     */
    static void abdIndCom(Term term1, Term term2, Sentence taskSentence, Sentence belief, int figure, Memory memory) {
        if (Statement.invalidStatement(term1, term2)) {
            return;
        }
        Statement taskContent = (Statement) taskSentence.getContent();
        TruthValue truth1 = null;
        TruthValue truth2 = null;
        TruthValue truth3 = null;
        BudgetValue budget1, budget2, budget3;
        TruthValue value1 = taskSentence.getTruth();
        TruthValue value2 = belief.getTruth();
        if (taskSentence.isQuestion()) {
            budget1 = BudgetFunctions.backward(value2, memory);
            budget2 = BudgetFunctions.backwardWeak(value2, memory);
            budget3 = BudgetFunctions.backward(value2, memory);
        } else {
            truth1 = TruthFunctions.abduction(value1, value2);
            truth2 = TruthFunctions.abduction(value2, value1);
            truth3 = TruthFunctions.comparison(value1, value2);
            budget1 = BudgetFunctions.forward(truth1, memory);
            budget2 = BudgetFunctions.forward(truth2, memory);
            budget3 = BudgetFunctions.forward(truth3, memory);
        }
        Statement statement1 = Statement.make(taskContent, term1, term2, memory);
        Statement statement2 = Statement.make(taskContent, term2, term1, memory);
        Statement statement3 = Statement.makeSym(taskContent, term1, term2, memory);
        memory.doublePremiseTask(statement1, truth1, budget1);
        memory.doublePremiseTask(statement2, truth2, budget2);
        memory.doublePremiseTask(statement3, truth3, budget3);
    }

    /**
     * {<S ==> P>, <M <=> P>} |- <S ==> P>
     * @param term1 Subject of the new task
     * @param term2 Predicate of the new task
     * @param asym The asymmetric premise
     * @param sym The symmetric premise
     * @param figure Locations of the shared term in premises
     * @param memory Reference to the memory
     */
    static void analogy(Term term1, Term term2, Sentence asym, Sentence sym, int figure, Memory memory) {
        if (Statement.invalidStatement(term1, term2)) {
            return;
        }
        Statement asymSt = (Statement) asym.getContent();
//        Statement symSt = (Statement) sym.getContent();
        TruthValue truth = null;
        BudgetValue budget;
        Sentence sentence = memory.currentTask.getSentence();
        CompoundTerm taskTerm = (CompoundTerm) sentence.getContent();
        if (sentence.isQuestion()) {
            if (taskTerm.isCommutative()) {
                budget = BudgetFunctions.backwardWeak(asym.getTruth(), memory);
            } else {
                budget = BudgetFunctions.backward(sym.getTruth(), memory);
            }
        } else {
            truth = TruthFunctions.analogy(asym.getTruth(), sym.getTruth());
            budget = BudgetFunctions.forward(truth, memory);
        }
        Term content = Statement.make(asymSt, term1, term2, memory);
        memory.doublePremiseTask(content, truth, budget);
    }

    /**
     * {<S <=> M>, <M <=> P>} |- <S <=> P>
     * @param term1 Subject of the new task
     * @param term2 Predicate of the new task
     * @param belief The first premise
     * @param sentence The second premise
     * @param figure Locations of the shared term in premises
     * @param memory Reference to the memory
     */
    static void resemblance(Term term1, Term term2, Sentence belief, Sentence sentence, int figure, Memory memory) {
        if (Statement.invalidStatement(term1, term2)) {
            return;
        }
        Statement st1 = (Statement) belief.getContent();
//        Statement st2 = (Statement) sentence.getContent();
        TruthValue truth = null;
        BudgetValue budget;
        if (sentence.isQuestion()) {
            budget = BudgetFunctions.backward(belief.getTruth(), memory);
        } else {
            truth = TruthFunctions.resemblance(belief.getTruth(), sentence.getTruth());
            budget = BudgetFunctions.forward(truth, memory);
        }
        Term statement = Statement.make(st1, term1, term2, memory);
        memory.doublePremiseTask(statement, truth, budget);
    }

    /* --------------- rules used only in conditional inference --------------- */
    /**
     * {<<M --> S> ==> <M --> P>>, <M --> S>} |- <M --> P>
     * {<<M --> S> ==> <M --> P>>, <M --> P>} |- <M --> S>
     * {<<M --> S> <=> <M --> P>>, <M --> S>} |- <M --> P>
     * {<<M --> S> <=> <M --> P>>, <M --> P>} |- <M --> S>
     * @param mainSentence The implication/equivalence premise
     * @param subSentence The premise on part of s1
     * @param side The location of s2 in s1
     * @param memory Reference to the memory
     */
    static void detachment(Sentence mainSentence, Sentence subSentence, int side, Memory memory) {
        Statement statement = (Statement) mainSentence.getContent();
        if (!(statement instanceof Implication) && !(statement instanceof Equivalence)) {
            return;
        }
        Term subject = statement.getSubject();
        Term predicate = statement.getPredicate();
        Term content;
        if (side == 0) { // term.equals(subject)) {
            content = predicate;
        } else if (side == 1) { //  term.equals(predicate)) {
            content = subject;
        } else {
            return;
        }
        if ((content instanceof Statement) && ((Statement) content).invalid()) {
            return;
        }
        Sentence taskSentence = memory.currentTask.getSentence();
        Sentence beliefSentence = memory.currentBelief;
        TruthValue beliefTruth = beliefSentence.getTruth();
        TruthValue truth1 = mainSentence.getTruth();
        TruthValue truth2 = subSentence.getTruth();
        TruthValue truth = null;
        BudgetValue budget;
        if (taskSentence.isQuestion()) {
            if (statement instanceof Equivalence) {
                budget = BudgetFunctions.backward(beliefTruth, memory);
            } else if (side == 0) {
                budget = BudgetFunctions.backwardWeak(beliefTruth, memory);
            } else {
                budget = BudgetFunctions.backward(beliefTruth, memory);
            }
        } else {
            if (statement instanceof Equivalence) {
                truth = TruthFunctions.analogy(truth2, truth1);
            } else if (side == 0) {
                truth = TruthFunctions.deduction(truth1, truth2);
            } else {
                truth = TruthFunctions.abduction(truth2, truth1);
            }
            budget = BudgetFunctions.forward(truth, memory);
        }
        memory.doublePremiseTask(content, truth, budget);
    }

    /**
     * {<(&&, S1, S2, S3) ==> P>, S1} |- <(&&, S2, S3) ==> P>
     * {<(&&, S2, S3) ==> P>, <S1 ==> S2>} |- <(&&, S1, S3) ==> P>
     * {<(&&, S1, S3) ==> P>, <S1 ==> S2>} |- <(&&, S2, S3) ==> P>
     * @param premise1 The conditional premise
     * @param index The location of the shared term in the condition of premise1
     * @param premise2 The premise which, or part of which, appears in the condition of premise1
     * @param side The location of the shared term in premise2: 0 for subject, 1 for predicate, -1 for the whole term
     * @param memory Reference to the memory
     */
    static void conditionalDedInd(Implication premise1, short index, Term premise2, int side, Memory memory) {
        Task task = memory.currentTask;
        Sentence taskSentence = task.getSentence();
        Sentence belief = memory.currentBelief;
        boolean deduction = (side != 0);
        boolean conditionalTask = Variable.hasSubstitute(Symbols.VAR_INDEPENDENT, premise2, belief.getContent());
        Term commonComponent;
        Term newComponent = null;
        if (side == 0) {
            commonComponent = ((Statement) premise2).getSubject();
            newComponent = ((Statement) premise2).getPredicate();
        } else if (side == 1) {
            commonComponent = ((Statement) premise2).getPredicate();
            newComponent = ((Statement) premise2).getSubject();
        } else {
            commonComponent = premise2;
        }
        Conjunction oldCondition = (Conjunction) premise1.getSubject();
        int index2 = oldCondition.getComponents().indexOf(commonComponent);
        if (index2 >= 0) {
            index = (short) index2;
        } else {
            boolean match = Variable.unify(Symbols.VAR_INDEPENDENT, oldCondition.componentAt(index), commonComponent, premise1, premise2);
            if (!match && (commonComponent.getClass() == oldCondition.getClass())) {
                match = Variable.unify(Symbols.VAR_INDEPENDENT, oldCondition.componentAt(index), ((CompoundTerm) commonComponent).componentAt(index), premise1, premise2);
            }
            if (!match) {
                return;
            }
        }
        Term newCondition;
        if (oldCondition.equals(commonComponent)) {
            newCondition = null;
        } else {
            newCondition = CompoundTerm.setComponent(oldCondition, index, newComponent, memory);
        }
        Term content;
        if (newCondition != null) {
            content = Statement.make(premise1, newCondition, premise1.getPredicate(), memory);
        } else {
            content = premise1.getPredicate();
        }
        if (content == null) {
            return;
        }
        TruthValue truth1 = taskSentence.getTruth();
        TruthValue truth2 = belief.getTruth();
        TruthValue truth = null;
        BudgetValue budget;
        if (taskSentence.isQuestion()) {
            budget = BudgetFunctions.backwardWeak(truth2, memory);
        } else {
            if (deduction) {
                truth = TruthFunctions.deduction(truth1, truth2);
            } else if (conditionalTask) {
                truth = TruthFunctions.induction(truth2, truth1);
            } else {
                truth = TruthFunctions.induction(truth1, truth2);
            }
            budget = BudgetFunctions.forward(truth, memory);
        }
        memory.doublePremiseTask(content, truth, budget);
    }

    /**
     * {<(&&, S1, S2) <=> P>, (&&, S1, S2)} |- P
     * @param premise1 The equivalence premise
     * @param index The location of the shared term in the condition of premise1
     * @param premise2 The premise which, or part of which, appears in the condition of premise1
     * @param side The location of the shared term in premise2: 0 for subject, 1 for predicate, -1 for the whole term
     * @param memory Reference to the memory
     */
    static void conditionalAna(Equivalence premise1, short index, Term premise2, int side, Memory memory) {
        Task task = memory.currentTask;
        Sentence taskSentence = task.getSentence();
        Sentence belief = memory.currentBelief;
        boolean conditionalTask = Variable.hasSubstitute(Symbols.VAR_INDEPENDENT, premise2, belief.getContent());
        Term commonComponent;
        Term newComponent = null;
        if (side == 0) {
            commonComponent = ((Statement) premise2).getSubject();
            newComponent = ((Statement) premise2).getPredicate();
        } else if (side == 1) {
            commonComponent = ((Statement) premise2).getPredicate();
            newComponent = ((Statement) premise2).getSubject();
        } else {
            commonComponent = premise2;
        }
        Conjunction oldCondition = (Conjunction) premise1.getSubject();
        boolean match = Variable.unify(Symbols.VAR_DEPENDENT, oldCondition.componentAt(index), commonComponent, premise1, premise2);
        if (!match && (commonComponent.getClass() == oldCondition.getClass())) {
            match = Variable.unify(Symbols.VAR_DEPENDENT, oldCondition.componentAt(index), ((CompoundTerm) commonComponent).componentAt(index), premise1, premise2);
        }
        if (!match) {
            return;
        }
        Term newCondition;
        if (oldCondition.equals(commonComponent)) {
            newCondition = null;
        } else {
            newCondition = CompoundTerm.setComponent(oldCondition, index, newComponent, memory);
        }
        Term content;
        if (newCondition != null) {
            content = Statement.make(premise1, newCondition, premise1.getPredicate(), memory);
        } else {
            content = premise1.getPredicate();
        }
        if (content == null) {
            return;
        }
        TruthValue truth1 = taskSentence.getTruth();
        TruthValue truth2 = belief.getTruth();
        TruthValue truth = null;
        BudgetValue budget;
        if (taskSentence.isQuestion()) {
            budget = BudgetFunctions.backwardWeak(truth2, memory);
        } else {
            if (conditionalTask) {
                truth = TruthFunctions.comparison(truth1, truth2);
            } else {
                truth = TruthFunctions.analogy(truth1, truth2);
            }
            budget = BudgetFunctions.forward(truth, memory);
        }
        memory.doublePremiseTask(content, truth, budget);
    }

    /**
     * {<(&&, S2, S3) ==> P>, <(&&, S1, S3) ==> P>} |- <S1 ==> S2>
     * @param cond1 The condition of the first premise
     * @param cond2 The condition of the second premise
     * @param taskContent The first premise
     * @param st2 The second premise
     * @param memory Reference to the memory
     * @return Whether there are derived tasks
     */
    static boolean conditionalAbd(Term cond1, Term cond2, Statement st1, Statement st2, Memory memory) {
        if (!(st1 instanceof Implication) || !(st2 instanceof Implication)) {
            return false;
        }
        if (!(cond1 instanceof Conjunction) && !(cond2 instanceof Conjunction)) {
            return false;
        }
        Term term1 = null;
        Term term2 = null;
        if (cond1 instanceof Conjunction) {
            term1 = CompoundTerm.reduceComponents((Conjunction) cond1, cond2, memory);
        }
        if (cond2 instanceof Conjunction) {
            term2 = CompoundTerm.reduceComponents((Conjunction) cond2, cond1, memory);
        }
        if ((term1 == null) && (term2 == null)) {
            return false;
        }
        Task task = memory.currentTask;
        Sentence sentence = task.getSentence();
        Sentence belief = memory.currentBelief;
        TruthValue value1 = sentence.getTruth();
        TruthValue value2 = belief.getTruth();
        Term content;
        TruthValue truth = null;
        BudgetValue budget;
        if (term1 != null) {
            if (term2 != null) {
                content = Statement.make(st2, term2, term1, memory);
            } else {
                content = term1;
            }
            if (sentence.isQuestion()) {
                budget = BudgetFunctions.backwardWeak(value2, memory);
            } else {
                truth = TruthFunctions.abduction(value2, value1);
                budget = BudgetFunctions.forward(truth, memory);
            }
            memory.doublePremiseTask(content, truth, budget);
        }
        if (term2 != null) {
            if (term1 != null) {
                content = Statement.make(st1, term1, term2, memory);
            } else {
                content = term2;
            }
            if (sentence.isQuestion()) {
                budget = BudgetFunctions.backwardWeak(value2, memory);
            } else {
                truth = TruthFunctions.abduction(value1, value2);
                budget = BudgetFunctions.forward(truth, memory);
            }
            memory.doublePremiseTask(content, truth, budget);
        }
        return true;
    }

    /**
     * {(&&, <#x() --> S>, <#x() --> P>>, <M --> P>} |- <M --> S>
     * @param compound The compound term to be decomposed
     * @param component The part of the compound to be removed
     * @param compoundTask Whether the compound comes from the task
     * @param memory Reference to the memory
     */
    static void elimiVarDep(CompoundTerm compound, Term component, boolean compoundTask, Memory memory) {
        Term content = CompoundTerm.reduceComponents(compound, component, memory);
        Task task = memory.currentTask;
        Sentence sentence = task.getSentence();
        Sentence belief = memory.currentBelief;
        TruthValue v1 = sentence.getTruth();
        TruthValue v2 = belief.getTruth();
        TruthValue truth = null;
        BudgetValue budget;
        if (sentence.isQuestion()) {
            budget = (compoundTask ? BudgetFunctions.backward(v2, memory) : BudgetFunctions.backwardWeak(v2, memory));
        } else {
            truth = (compoundTask ? TruthFunctions.anonymousAnalogy(v1, v2) : TruthFunctions.anonymousAnalogy(v2, v1));
            budget = BudgetFunctions.compoundForward(truth, content, memory);
        }
        memory.doublePremiseTask(content, truth, budget);
    }
}
