/*
 * CompositionalRules.java
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

import java.util.*;

import nars.entity.*;
import nars.io.Symbols;
import nars.language.*;
import nars.storage.Memory;


public final class CompositionalRules {

public static void EliminateVariableOfConditionAbductive(int figure,Sentence sentence,Sentence belief,Memory memory) {
        Term T1=(Term) sentence.getContent().clone();
        Term T2=(Term) belief.getContent().clone();
        Term S1=((Statement)T2).getSubject();
        Term P1=((Statement)T2).getPredicate();
        Term S2=((Statement)T1).getSubject();
        Term P2=((Statement)T1).getPredicate();
        
        if(figure==21) {
            HashMap<Term,Term> res1=new HashMap<>();
            HashMap<Term,Term> res2=new HashMap<>();
            Variable.findSubstitute(Symbols.VAR_INDEPENDENT, P1, S2, res1, res2); //this part is 
            ((CompoundTerm) T1).applySubstitute(res2); //independent, the rule works if it unifies
            ((CompoundTerm) T2).applySubstitute(res1);
            if(S1 instanceof Conjunction) {
                //try to unify P2 with a component
                for(Term s1 : ((CompoundTerm)S1).cloneComponents()) {
                    HashMap<Term,Term> res3=new HashMap<>();
                    HashMap<Term,Term> res4=new HashMap<>(); //here the dependent part matters, see example of Issue40
                    if(Variable.findSubstitute(Symbols.VAR_DEPENDENT, s1, P2, res3, res4)) { 
                        for(Term s2 : ((CompoundTerm)S1).cloneComponents()) {
                            ((CompoundTerm) s2).applySubstitute(res3);
                            if(!s2.equals(s1)) {
                                TruthValue truth = TruthFunctions.abduction(sentence.truth, belief.truth);
                                BudgetValue budget = BudgetFunctions.compoundForward(truth, s2, memory);
                                memory.doublePremiseTask(s2, truth, budget);
                            }
                        }
                    }
                }
            }
            if(P2 instanceof Conjunction) {
                //try to unify S1 with a component
                for(Term s1 : ((CompoundTerm)P2).cloneComponents()) {
                    HashMap<Term,Term> res3=new HashMap<>();
                    HashMap<Term,Term> res4=new HashMap<>(); //here the dependent part matters, see example of Issue40
                    if(Variable.findSubstitute(Symbols.VAR_DEPENDENT, s1, S1, res3, res4)) { 
                        for(Term s2 : ((CompoundTerm)P2).cloneComponents()) {
                            ((CompoundTerm) s2).applySubstitute(res3);
                            if(!s2.equals(s1)) {
                                TruthValue truth = TruthFunctions.abduction(sentence.truth, belief.truth);
                                BudgetValue budget = BudgetFunctions.compoundForward(truth, s2, memory);
                                memory.doublePremiseTask(s2, truth, budget);
                            }
                        }
                    }
                }
            }
        }
        
        if(figure==12) {
            HashMap<Term,Term> res1=new HashMap<>();
            HashMap<Term,Term> res2=new HashMap<>();
            Variable.findSubstitute(Symbols.VAR_INDEPENDENT, S1, P2, res1, res2); //this part is 
            ((CompoundTerm) T1).applySubstitute(res2); //independent, the rule works if it unifies
            ((CompoundTerm) T2).applySubstitute(res1);
            if(S2 instanceof Conjunction) {
                //try to unify P1 with a component
                for(Term s1 : ((CompoundTerm)S2).cloneComponents()) {
                    HashMap<Term,Term> res3=new HashMap<>();
                    HashMap<Term,Term> res4=new HashMap<>(); //here the dependent part matters, see example of Issue40
                    if(Variable.findSubstitute(Symbols.VAR_DEPENDENT, s1, P1, res3, res4)) { 
                        for(Term s2 : ((CompoundTerm)S2).cloneComponents()) {
                            ((CompoundTerm) s2).applySubstitute(res3);
                            if(!s2.equals(s1)) {
                                TruthValue truth = TruthFunctions.abduction(sentence.truth, belief.truth);
                                BudgetValue budget = BudgetFunctions.compoundForward(truth, s2, memory);
                                memory.doublePremiseTask(s2, truth, budget);
                            }
                        }
                    }
                }
            }
            if(P1 instanceof Conjunction) {
                //try to unify S2 with a component
                for(Term s1 : ((CompoundTerm)P1).cloneComponents()) {
                    HashMap<Term,Term> res3=new HashMap<>();
                    HashMap<Term,Term> res4=new HashMap<>(); //here the dependent part matters, see example of Issue40
                    if(Variable.findSubstitute(Symbols.VAR_DEPENDENT, s1, S2, res3, res4)) { 
                        for(Term s2 : ((CompoundTerm)P1).cloneComponents()) {
                            ((CompoundTerm) s2).applySubstitute(res3);
                            if(!s2.equals(s1)) {
                                TruthValue truth = TruthFunctions.abduction(sentence.truth, belief.truth);
                                BudgetValue budget = BudgetFunctions.compoundForward(truth, s2, memory);
                                memory.doublePremiseTask(s2, truth, budget);
                            }
                        }
                    }
                }
            }
        }
        
        if(figure==11) {
            HashMap<Term,Term> res1=new HashMap<>();
            HashMap<Term,Term> res2=new HashMap<>();
            Variable.findSubstitute(Symbols.VAR_INDEPENDENT, S1, S2, res1, res2); //this part is 
            ((CompoundTerm) T1).applySubstitute(res2); //independent, the rule works if it unifies
            ((CompoundTerm) T2).applySubstitute(res1);
            if(P1 instanceof Conjunction) {
                //try to unify P2 with a component
                for(Term s1 : ((CompoundTerm)P1).cloneComponents()) {
                    HashMap<Term,Term> res3=new HashMap<>();
                    HashMap<Term,Term> res4=new HashMap<>(); //here the dependent part matters, see example of Issue40
                    if(Variable.findSubstitute(Symbols.VAR_DEPENDENT, s1, P2, res3, res4)) { 
                        for(Term s2 : ((CompoundTerm)P1).cloneComponents()) {
                            ((CompoundTerm) s2).applySubstitute(res3);
                            if(!s2.equals(s1)) {
                                TruthValue truth = TruthFunctions.abduction(sentence.truth, belief.truth);
                                BudgetValue budget = BudgetFunctions.compoundForward(truth, s2, memory);
                                memory.doublePremiseTask(s2, truth, budget);
                            }
                        }
                    }
                }
            }
            if(P2 instanceof Conjunction) {
                //try to unify P1 with a component
                for(Term s1 : ((CompoundTerm)P2).cloneComponents()) {
                    HashMap<Term,Term> res3=new HashMap<>();
                    HashMap<Term,Term> res4=new HashMap<>(); //here the dependent part matters, see example of Issue40
                    if(Variable.findSubstitute(Symbols.VAR_DEPENDENT, s1, P1, res3, res4)) { 
                        for(Term s2 : ((CompoundTerm)P2).cloneComponents()) {
                            ((CompoundTerm) s2).applySubstitute(res3);
                            if(!s2.equals(s1)) {
                                TruthValue truth = TruthFunctions.abduction(sentence.truth, belief.truth);
                                BudgetValue budget = BudgetFunctions.compoundForward(truth, s2, memory);
                                memory.doublePremiseTask(s2, truth, budget);
                            }
                        }
                    }
                }
            }
        }
        
        if(figure==22) {
            HashMap<Term,Term> res1=new HashMap<>();
            HashMap<Term,Term> res2=new HashMap<>();
            Variable.findSubstitute(Symbols.VAR_INDEPENDENT, P1, P2, res1, res2); //this part is 
            ((CompoundTerm) T1).applySubstitute(res2); //independent, the rule works if it unifies
            ((CompoundTerm) T2).applySubstitute(res1);
            if(S1 instanceof Conjunction) {
                //try to unify S2 with a component
                for(Term s1 : ((CompoundTerm)S1).cloneComponents()) {
                    HashMap<Term,Term> res3=new HashMap<>();
                    HashMap<Term,Term> res4=new HashMap<>(); //here the dependent part matters, see example of Issue40
                    if(Variable.findSubstitute(Symbols.VAR_DEPENDENT, s1, S2, res3, res4)) { 
                        for(Term s2 : ((CompoundTerm)S1).cloneComponents()) {
                            ((CompoundTerm) s2).applySubstitute(res3);
                            if(!s2.equals(s1)) {
                                TruthValue truth = TruthFunctions.abduction(sentence.truth, belief.truth);
                                BudgetValue budget = BudgetFunctions.compoundForward(truth, s2, memory);
                                memory.doublePremiseTask(s2, truth, budget);
                            }
                        }
                    }
                }
            }
            if(S2 instanceof Conjunction) {
                //try to unify S1 with a component
                for(Term s1 : ((CompoundTerm)S2).cloneComponents()) {
                    HashMap<Term,Term> res3=new HashMap<>();
                    HashMap<Term,Term> res4=new HashMap<>(); //here the dependent part matters, see example of Issue40
                    if(Variable.findSubstitute(Symbols.VAR_DEPENDENT, s1, S1, res3, res4)) { 
                        for(Term s2 : ((CompoundTerm)S2).cloneComponents()) {
                            ((CompoundTerm) s2).applySubstitute(res3);
                            if(!s2.equals(s1)) {
                                TruthValue truth = TruthFunctions.abduction(sentence.truth, belief.truth);
                                BudgetValue budget = BudgetFunctions.compoundForward(truth, s2, memory);
                                memory.doublePremiseTask(s2, truth, budget);
                            }
                        }
                    }
                }
            }
        }
    }
    
  static void IntroVarSameSubjectOrPredicate(Sentence originalMainSentence, Sentence subSentence, Term component, Term content, int index,Memory memory) {
        Sentence cloned=(Sentence) originalMainSentence.clone();
        Term T1=cloned.getContent();
        if(!(T1 instanceof CompoundTerm) || !(content instanceof CompoundTerm)) {
            return;
        }
        CompoundTerm T=(CompoundTerm) T1;
        CompoundTerm T2=(CompoundTerm) content.clone();
        if((component instanceof Inheritance && content instanceof Inheritance) ||
           (component instanceof Similarity && content instanceof Similarity)) {
            CompoundTerm result=T;
            if(component.equals(content)) {
                return; //wouldnt make sense to create a conjunction here, would contain a statement twice
            }
            if(((Statement)component).getPredicate().equals(((Statement)content).getPredicate()) && !(((Statement)component).getPredicate() instanceof Variable)) {
                Variable V=new Variable("#depIndVar1");
                CompoundTerm zw=(CompoundTerm) T.getComponents().get(index).clone();
                zw=(CompoundTerm) CompoundTerm.setComponent(zw,1,V,memory);
                T2=(CompoundTerm) CompoundTerm.setComponent(T2,1,V,memory);
                Conjunction res=(Conjunction) Conjunction.make(zw, T2, memory);
                T=(CompoundTerm) CompoundTerm.setComponent(T, index, res, memory);
            }
            else 
            if(((Statement)component).getSubject().equals(((Statement)content).getSubject()) && !(((Statement)component).getSubject() instanceof Variable)) {
                Variable V=new Variable("#depIndVar2");
                CompoundTerm zw=(CompoundTerm) T.getComponents().get(index).clone();
                zw=(CompoundTerm) CompoundTerm.setComponent(zw,0,V,memory);
                T2=(CompoundTerm) CompoundTerm.setComponent(T2,0,V,memory);
                Conjunction res=(Conjunction) Conjunction.make(zw, T2, memory);
                T=(CompoundTerm) CompoundTerm.setComponent(T, index, res, memory);
            }
            TruthValue truth = TruthFunctions.induction(originalMainSentence.truth, subSentence.truth);
            BudgetValue budget = BudgetFunctions.compoundForward(truth, T, memory);
            memory.doublePremiseTask(T, truth, budget);
        }
    }
    
    static boolean dedSecondLayerVariableUnification(Task task, Memory memory)
    {
        Sentence taskSentence=task.getSentence();
        if(taskSentence==null || taskSentence.isQuestion()) {
            return false;
        }
        Term taskterm=taskSentence.getContent();
        if(taskterm instanceof CompoundTerm && (taskterm instanceof Disjunction || taskterm instanceof Conjunction || taskterm instanceof Equivalence || taskterm instanceof Implication)) { //lets just allow conjunctions, implication and equivalence for now
            if(!Variable.containVar(taskterm.toString())) {
                return false;
            }           
            Concept second=memory.concepts.processNext();
            if(second==null) {
                return false;
            }
            if (memory.getRecorder().isActive()) {
                memory.getRecorder().append(" * Selected Concept (For Second Layer Unification): " + second.term);
            }            
            
            Term secterm=second.term;
            if(second.beliefs==null || second.beliefs.size()==0) {
                return false;
            }
           
            Sentence second_belief=second.beliefs.get(Memory.randomNumber.nextInt(second.beliefs.size()));
            TruthValue truthSecond=second_belief.truth;
            //we have to select a random belief
            ArrayList<CompoundTerm> terms_dependent=new ArrayList<CompoundTerm>();
            ArrayList<CompoundTerm> terms_independent=new ArrayList<CompoundTerm>();
            
            //ok, we have selected a second concept, we know the truth value of a belief of it, lets now go through taskterms components
            //for two levels, and remember the terms which unify with second
            List<Term> components_level1=((CompoundTerm)taskterm).getComponents();            
            Term secterm_unwrap=(Term) CompoundTerm.unwrapNegation(secterm).clone();
            for(Term T1 : components_level1) {
                Term T1_unwrap=CompoundTerm.unwrapNegation(T1);
                HashMap<Term, Term> Values = new HashMap<Term, Term>(); //we are only interested in first variables
                if(Variable.findSubstitute(Symbols.VAR_DEPENDENT, T1_unwrap, secterm_unwrap,Values,new HashMap<Term, Term>())) {
                    CompoundTerm taskterm_subs=((CompoundTerm)taskterm.clone());
                    taskterm_subs.applySubstitute(Values);
                    taskterm_subs=CompoundTerm.ReduceTillLayer2(taskterm_subs,secterm,memory);
                    if(taskterm_subs!=null) {
                        terms_dependent.add(taskterm_subs);
                    }
                }
                HashMap<Term, Term> Values2 = new HashMap<Term, Term>(); //we are only interested in first variables
                if(Variable.findSubstitute(Symbols.VAR_INDEPENDENT, T1_unwrap, secterm_unwrap,Values2,new HashMap<Term, Term>())) {
                    CompoundTerm taskterm_subs=((CompoundTerm)taskterm.clone());
                    taskterm_subs.applySubstitute(Values2);
                    taskterm_subs=CompoundTerm.ReduceTillLayer2(taskterm_subs,secterm,memory);
                    if(taskterm_subs!=null) {
                        terms_independent.add(taskterm_subs);
                    }
                }
                if(!((T1_unwrap instanceof Implication) || (T1_unwrap instanceof Equivalence) || (T1_unwrap instanceof Conjunction) || (T1_unwrap instanceof Disjunction))) {
                    continue;
                }
                if(T1_unwrap instanceof CompoundTerm) {
                    List<Term> components_level2 = ((CompoundTerm)T1_unwrap).getComponents();
                    for(Term T2 : components_level2) {
                        Term T2_unwrap=(Term) CompoundTerm.unwrapNegation(T2).clone(); 
                        HashMap<Term, Term> Values3 = new HashMap<Term, Term>(); //we are only interested in first variables
                        if(Variable.findSubstitute(Symbols.VAR_DEPENDENT, T2_unwrap, secterm_unwrap,Values3,new HashMap<Term, Term>())) {
                            //terms_dependent_compound_terms.put(Values3, (CompoundTerm)T1_unwrap);
                            CompoundTerm taskterm_subs=((CompoundTerm)taskterm.clone());
                            taskterm_subs.applySubstitute(Values3);
                            taskterm_subs=CompoundTerm.ReduceTillLayer2(taskterm_subs,secterm,memory);
                            if(taskterm_subs!=null) {
                                terms_dependent.add(taskterm_subs);
                            }
                        }
                        HashMap<Term, Term> Values4 = new HashMap<Term, Term>(); //we are only interested in first variables
                        if(Variable.findSubstitute(Symbols.VAR_INDEPENDENT, T2_unwrap, secterm_unwrap,Values4,new HashMap<Term, Term>())) {
                            //terms_independent_compound_terms.put(Values4, (CompoundTerm)T1_unwrap);
                            CompoundTerm taskterm_subs=((CompoundTerm)taskterm.clone());
                            taskterm_subs.applySubstitute(Values4);
                            taskterm_subs=CompoundTerm.ReduceTillLayer2(taskterm_subs,secterm,memory);
                            if(taskterm_subs!=null) {
                                terms_independent.add(taskterm_subs);
                            }
                        }
                    }
                }
            }
            
            Stamp ss = new Stamp(taskSentence.stamp, second_belief.stamp,memory.getTime());
            dedSecondLayerVariableUnificationTerms(memory, task, second_belief, ss, terms_dependent, TruthFunctions.anonymousAnalogy(taskSentence.truth, truthSecond));
            dedSecondLayerVariableUnificationTerms(memory, task, second_belief, ss, terms_independent, TruthFunctions.deduction(taskSentence.truth, truthSecond));
            
            Term result;
            TruthValue truth;
            
            for(int i=0;i<terms_independent.size();i++) {
                result=terms_independent.get(i);
                truth=TruthFunctions.deduction(taskSentence.truth, truthSecond);
               
                Stamp useEvidentalBase=new Stamp(taskSentence.stamp, second_belief.stamp,memory.getTime());
                Sentence newSentence = new Sentence(result, Symbols.JUDGMENT_MARK, truth, 
                        new Stamp(taskSentence.stamp, memory.getTime(), useEvidentalBase) );                
                
                BudgetValue budget = BudgetFunctions.compoundForward(truth, newSentence.getContent(), memory);
                Task newTask = new Task(newSentence, budget, task, null);
                Task dummy = new Task(second_belief, budget, task, null);
                memory.setCurrentBelief(taskSentence);
                memory.setCurrentTask(dummy);
                memory.derivedTask(newTask, false, false);
            }
            return true;
        }
        return true;
    }
        
    /* -------------------- questions which contain answers which are of no value for NARS but need to be answered -------------------- */
    /**
     * {(&&,A,B,...)?, A,B} |- {(&&,A,B)}
     * {(&&,A,_components_1_)?, (&&,_part_of_components_1_),A} |- {(&&,A,_part_of_components_1_,B)}
     * and also the case where both are conjunctions, all components need to be subterm of the question-conjunction
     * in order for the subterms of both conjunctions to be collected together.
     *
     * @param sentence The first premise
     * @param belief The second premise
     * @param memory Reference to the memory
     */
    static void dedConjunctionByQuestion(final Sentence sentence, final Sentence belief, final Memory memory) {
        if(sentence==null || belief==null || sentence.isQuestion() || belief.isQuestion()) {
            return;
        }
        
        Term term1 = sentence.getContent();
        Term term2 = belief.getContent();
        Deque<Concept>[] bag = memory.concepts.itemTable;        
        
        for (final Deque<Concept> baglevel : bag) {
            
            if (baglevel == null)
                continue;
            
            for (final Concept concept : baglevel) {
                
                final List<Task> questions = concept.getQuestions();                
                for (int i = 0; i < questions.size(); i++) {
                    final Task question = questions.get(i);                                    
                    
                    if(question==null) {
                        continue;
                    }
                    
                    Sentence qu=question.getSentence();
                    
                    if(qu==null) {
                        continue;
                    }
                    
                    Term pcontent = qu.getContent();
                    final CompoundTerm ctpcontent = (CompoundTerm)pcontent;
                    if(pcontent==null || !(pcontent instanceof Conjunction) || ctpcontent.containVar()) {
                        continue;
                    }
                    if(!(term1 instanceof Conjunction) && !(term2 instanceof Conjunction)) {
                        if(!(ctpcontent).containComponent(term1) || !(ctpcontent).containComponent(term2)) {
                            continue;
                        }
                    }
                    
                    if(term1 instanceof Conjunction) {
                        if(!(term2 instanceof Conjunction) && !(ctpcontent).containComponent(term2))
                            continue;
                        if(((CompoundTerm)term1).containVar())
                            continue;                        
                        if (!((CompoundTerm)term1).containAllComponents(ctpcontent))
                            continue;
                    }
                    
                    if(term2 instanceof Conjunction) {
                        if(!(term1 instanceof Conjunction) && !(ctpcontent).containComponent(term1))
                            continue;
                        if(((CompoundTerm)term2).containVar())
                            continue;
                        if (!((CompoundTerm)term2).containAllComponents(ctpcontent))
                            continue;
                    }
                    
                    Term conj = Conjunction.make(term1, term2, memory);
                    
                    if (Variable.containDepOrIndepVar(conj.toString()))
                        continue;
                    
                    TruthValue truthT = memory.getCurrentTask().getSentence().truth;
                    TruthValue truthB = memory.getCurrentBelief().truth;
                    if(truthT==null || truthB==null) {
                        return;
                    }
                    TruthValue truthAnd = TruthFunctions.intersection(truthT, truthB);
                    BudgetValue budget = BudgetFunctions.compoundForward(truthAnd, conj, memory);
                    memory.doublePremiseTask(conj, truthAnd, budget);
                    return;
                }
            }
        }

    }
    
    /* -------------------- intersections and differences -------------------- */
    /**
     * {<S ==> M>, <P ==> M>} |- {<(S|P) ==> M>, <(S&P) ==> M>, <(S-P) ==> M>,
     * <(P-S) ==> M>}
     *
     * @param taskSentence The first premise
     * @param belief The second premise
     * @param index The location of the shared term
     * @param memory Reference to the memory
     */
    static void composeCompound(final Statement taskContent, final Statement beliefContent, final int index, final Memory memory) {
        if ((!memory.getCurrentTask().getSentence().isJudgment()) || (taskContent.getClass() != beliefContent.getClass())) {
            return;
        }
        final Term componentT = taskContent.componentAt(1 - index);
        final Term componentB = beliefContent.componentAt(1 - index);
        final Term componentCommon = taskContent.componentAt(index);
        int order1 = taskContent.getTemporalOrder();
        int order2 = beliefContent.getTemporalOrder();
        int order = TemporalRules.composeOrder(order1, order2);
        if (order == TemporalRules.ORDER_INVALID) {
            return;
        }
        if ((componentT instanceof CompoundTerm) && ((CompoundTerm) componentT).containAllComponents(componentB)) {
            decomposeCompound((CompoundTerm) componentT, componentB, componentCommon, index, true, order, memory);
            return;
        } else if ((componentB instanceof CompoundTerm) && ((CompoundTerm) componentB).containAllComponents(componentT)) {
            decomposeCompound((CompoundTerm) componentB, componentT, componentCommon, index, false, order, memory);
            return;
        }
        final TruthValue truthT = memory.getCurrentTask().getSentence().truth;
        final TruthValue truthB = memory.getCurrentBelief().truth;
        final TruthValue truthOr = TruthFunctions.union(truthT, truthB);
        final TruthValue truthAnd = TruthFunctions.intersection(truthT, truthB);
        TruthValue truthDif = null;
        Term termOr = null;
        Term termAnd = null;
        Term termDif = null;
        if (index == 0) {
            if (taskContent instanceof Inheritance) {
                termOr = IntersectionInt.make(componentT, componentB, memory);
                termAnd = IntersectionExt.make(componentT, componentB, memory);
                if (truthB.isNegative()) {
                    if (!truthT.isNegative()) {
                        termDif = DifferenceExt.make(componentT, componentB, memory);
                        truthDif = TruthFunctions.intersection(truthT, TruthFunctions.negation(truthB));
                    }
                } else if (truthT.isNegative()) {
                    termDif = DifferenceExt.make(componentB, componentT, memory);
                    truthDif = TruthFunctions.intersection(truthB, TruthFunctions.negation(truthT));
                }
            } else if (taskContent instanceof Implication) {
                termOr = Disjunction.make(componentT, componentB, memory);
                termAnd = Conjunction.make(componentT, componentB, memory);
            }
            processComposed(taskContent, (Term) componentCommon.clone(), termOr, order, truthOr, memory);
            processComposed(taskContent, (Term) componentCommon.clone(), termAnd, order, truthAnd, memory);
            processComposed(taskContent, (Term) componentCommon.clone(), termDif, order, truthDif, memory);
        } else {    // index == 1
            if (taskContent instanceof Inheritance) {
                termOr = IntersectionExt.make(componentT, componentB, memory);
                termAnd = IntersectionInt.make(componentT, componentB, memory);
                if (truthB.isNegative()) {
                    if (!truthT.isNegative()) {
                        termDif = DifferenceInt.make(componentT, componentB, memory);
                        truthDif = TruthFunctions.intersection(truthT, TruthFunctions.negation(truthB));
                    }
                } else if (truthT.isNegative()) {
                    termDif = DifferenceInt.make(componentB, componentT, memory);
                    truthDif = TruthFunctions.intersection(truthB, TruthFunctions.negation(truthT));
                }
            } else if (taskContent instanceof Implication) {
                termOr = Conjunction.make(componentT, componentB, memory);
                termAnd = Disjunction.make(componentT, componentB, memory);
            }
            processComposed(taskContent, termOr, (Term) componentCommon.clone(), order, truthOr, memory);
            processComposed(taskContent, termAnd, (Term) componentCommon.clone(), order, truthAnd, memory);
            processComposed(taskContent, termDif, (Term) componentCommon.clone(), order, truthDif, memory);
        }
        if (taskContent instanceof Inheritance) {
            introVarOuter(taskContent, beliefContent, index, memory);//            introVarImage(taskContent, beliefContent, index, memory);
        }
    }

    /**
     * Finish composing implication term
     *
     * @param premise1 Type of the contentInd
     * @param subject Subject of contentInd
     * @param predicate Predicate of contentInd
     * @param truth TruthValue of the contentInd
     * @param memory Reference to the memory
     */
    private static void processComposed(Statement statement, Term subject, Term predicate, int order, TruthValue truth, Memory memory) {
        if ((subject == null) || (predicate == null)) {
            return;
        }
        Term content = Statement.make(statement, subject, predicate, order, memory);
        if ((content == null) || content.equals(statement) || content.equals(memory.getCurrentBelief().getContent())) {
            return;
        }
        BudgetValue budget = BudgetFunctions.compoundForward(truth, content, memory);
        memory.doublePremiseTask(content, truth, budget);
    }

    /**
     * {<(S|P) ==> M>, <P ==> M>} |- <S ==> M>
     *
     * @param implication The implication term to be decomposed
     * @param componentCommon The part of the implication to be removed
     * @param term1 The other term in the contentInd
     * @param index The location of the shared term: 0 for subject, 1 for
     * predicate
     * @param compoundTask Whether the implication comes from the task
     * @param memory Reference to the memory
     */
    private static void decomposeCompound(CompoundTerm compound, Term component, Term term1, int index, boolean compoundTask, int order, Memory memory) {
        
        if ((compound instanceof Statement) || (compound instanceof ImageExt) || (compound instanceof ImageInt)) {
            return;
        }
        Term term2 = CompoundTerm.reduceComponents(compound, component, memory);
        if (term2 == null) {
            return;
        }
        Task task = memory.getCurrentTask();
        Sentence sentence = task.getSentence();
        Sentence belief = memory.getCurrentBelief();
        Statement oldContent = (Statement) task.getContent();
        TruthValue v1,
                v2;
        if (compoundTask) {
            v1 = sentence.truth;
            v2 = belief.truth;
        } else {
            v1 = belief.truth;
            v2 = sentence.truth;
        }
        TruthValue truth = null;
        Term content;
        if (index == 0) {
            content = Statement.make(oldContent, term1, term2, order, memory);
            if (content == null) {
                return;
            }
            if (oldContent instanceof Inheritance) {
                if (compound instanceof IntersectionExt) {
                    truth = TruthFunctions.reduceConjunction(v1, v2);
                } else if (compound instanceof IntersectionInt) {
                    truth = TruthFunctions.reduceDisjunction(v1, v2);
                } else if ((compound instanceof SetInt) && (component instanceof SetInt)) {
                    truth = TruthFunctions.reduceConjunction(v1, v2);
                } else if ((compound instanceof SetExt) && (component instanceof SetExt)) {
                    truth = TruthFunctions.reduceDisjunction(v1, v2);
                } else if (compound instanceof DifferenceExt) {
                    if (compound.componentAt(0).equals(component)) {
                        truth = TruthFunctions.reduceDisjunction(v2, v1);
                    } else {
                        truth = TruthFunctions.reduceConjunctionNeg(v1, v2);
                    }
                }
            } else if (oldContent instanceof Implication) {
                if (compound instanceof Conjunction) {
                    truth = TruthFunctions.reduceConjunction(v1, v2);
                } else if (compound instanceof Disjunction) {
                    truth = TruthFunctions.reduceDisjunction(v1, v2);
                }
            }
        } else {
            content = Statement.make(oldContent, term2, term1, order, memory);
            if (content == null) {
                return;
            }
            if (oldContent instanceof Inheritance) {
                if (compound instanceof IntersectionInt) {
                    truth = TruthFunctions.reduceConjunction(v1, v2);
                } else if (compound instanceof IntersectionExt) {
                    truth = TruthFunctions.reduceDisjunction(v1, v2);
                } else if ((compound instanceof SetExt) && (component instanceof SetExt)) {
                    truth = TruthFunctions.reduceConjunction(v1, v2);
                } else if ((compound instanceof SetInt) && (component instanceof SetInt)) {
                    truth = TruthFunctions.reduceDisjunction(v1, v2);
                } else if (compound instanceof DifferenceInt) {
                    if (compound.componentAt(1).equals(component)) {
                        truth = TruthFunctions.reduceDisjunction(v2, v1);
                    } else {
                        truth = TruthFunctions.reduceConjunctionNeg(v1, v2);
                    }
                }
            } else if (oldContent instanceof Implication) {
                if (compound instanceof Disjunction) {
                    truth = TruthFunctions.reduceConjunction(v1, v2);
                } else if (compound instanceof Conjunction) {
                    truth = TruthFunctions.reduceDisjunction(v1, v2);
                }
            }
        }
        if (truth != null) {
            BudgetValue budget = BudgetFunctions.compoundForward(truth, content, memory);
            memory.doublePremiseTask(content, truth, budget);
        }
    }

    /**
     * {(||, S, P), P} |- S {(&&, S, P), P} |- S
     *
     * @param implication The implication term to be decomposed
     * @param componentCommon The part of the implication to be removed
     * @param compoundTask Whether the implication comes from the task
     * @param memory Reference to the memory
     */
    static void decomposeStatement(CompoundTerm compound, Term component, boolean compoundTask, Memory memory) {
        Task task = memory.getCurrentTask();
        Sentence sentence = task.getSentence();

        Sentence belief = memory.getCurrentBelief();
        Term content = CompoundTerm.reduceComponents(compound, component, memory);
        if (content == null) {
            return;
        }
        TruthValue truth = null;
        BudgetValue budget;
        if (sentence.isQuestion()) {
            budget = BudgetFunctions.compoundBackward(content, memory);
            memory.doublePremiseTask(content, truth, budget);
            // special inference to answer conjunctive questions with query variables
            if (Variable.containVarQuery(sentence.getContent().getName())) {
                Concept contentConcept = memory.termToConcept(content);
                if (contentConcept == null) {
                    return;
                }
                Sentence contentBelief = contentConcept.getBelief(task);
                if (contentBelief == null) {
                    return;
                }
                Task contentTask = new Task(contentBelief, task.budget);
                memory.setCurrentTask(contentTask);
                Term conj = Conjunction.make(component, content, memory);
                truth = TruthFunctions.intersection(contentBelief.truth, belief.truth);
                budget = BudgetFunctions.compoundForward(truth, conj, memory);
                memory.doublePremiseTask(conj, truth, budget);
            }        
        } else {
            TruthValue v1, v2;
            if (compoundTask) {
                v1 = sentence.truth;
                v2 = belief.truth;
            } else {
                v1 = belief.truth;
                v2 = sentence.truth;
            }
            if (compound instanceof Conjunction) {
                if (sentence instanceof Sentence) {
                    truth = TruthFunctions.reduceConjunction(v1, v2);
                }
                } else if (compound instanceof Disjunction) {
                    if (sentence instanceof Sentence) {
                        truth = TruthFunctions.reduceDisjunction(v1, v2);
                    }
                } else {
                return;
            }
            budget = BudgetFunctions.compoundForward(truth, content, memory);
            memory.doublePremiseTask(content, truth, budget);
        }

    }

    /* --------------- rules used for variable introduction --------------- */
    /**
     * Introduce a dependent variable in an outer-layer conjunction
     *
     * @param taskContent The first premise <M --> S>
     * @param beliefContent The second premise <M --> P>
     * @param index The location of the shared term: 0 for subject, 1 for
     * predicate
     * @param memory Reference to the memory
     */
    private static void introVarOuter(Statement taskContent, Statement beliefContent, int index, Memory memory) {
        TruthValue truthT = memory.getCurrentTask().getSentence().truth;
        TruthValue truthB = memory.getCurrentBelief().truth;
        Variable varInd = new Variable("$varInd1");
        Variable varInd2 = new Variable("$varInd2");
        Term term11, term12, term21, term22, commonTerm;
        HashMap<Term, Term> subs = new HashMap<>();
        if (index == 0) {
            term11 = varInd;
            term21 = varInd;
            term12 = taskContent.getPredicate();
            term22 = beliefContent.getPredicate();
            if ((term12 instanceof ImageExt) && (term22 instanceof ImageExt)) {
                commonTerm = ((ImageExt) term12).getTheOtherComponent();
                if ((commonTerm == null) || !((ImageExt) term22).containTerm(commonTerm)) {
                    commonTerm = ((ImageExt) term22).getTheOtherComponent();
                    if ((commonTerm == null) || !((ImageExt) term12).containTerm(commonTerm)) {
                        commonTerm = null;
                    }
                }
                if (commonTerm != null) {
                    subs.put(commonTerm, varInd2);
                    ((ImageExt) term12).applySubstitute(subs);
                    ((ImageExt) term22).applySubstitute(subs);
                }
            }
        } else {
            term11 = taskContent.getSubject();
            term21 = beliefContent.getSubject();
            term12 = varInd;
            term22 = varInd;
            if ((term11 instanceof ImageInt) && (term21 instanceof ImageInt)) {
                commonTerm = ((ImageInt) term11).getTheOtherComponent();
                if ((commonTerm == null) || !((ImageInt) term21).containTerm(commonTerm)) {
                    commonTerm = ((ImageInt) term21).getTheOtherComponent();
                    if ((commonTerm == null) || !((ImageInt) term11).containTerm(commonTerm)) {
                        commonTerm = null;
                    }
                }
                if (commonTerm != null) {
                    subs.put(commonTerm, varInd2);
                    ((ImageInt) term11).applySubstitute(subs);
                    ((ImageInt) term21).applySubstitute(subs);
                }
            }
        }
        Statement state1 = Inheritance.make(term11, term12, memory);
        Statement state2 = Inheritance.make(term21, term22, memory);
        Term content = Implication.make(state1, state2, memory);
        if (content == null) {
            return;
        }
        
        TruthValue truth = TruthFunctions.induction(truthT, truthB);
        BudgetValue budget = BudgetFunctions.compoundForward(truth, content, memory);
        memory.doublePremiseTask(content, truth, budget);
        content = Implication.make(state2, state1, memory);
        truth = TruthFunctions.induction(truthB, truthT);
        budget = BudgetFunctions.compoundForward(truth, content, memory);
        memory.doublePremiseTask(content, truth, budget);
        content = Equivalence.make(state1, state2, memory);
        truth = TruthFunctions.comparison(truthT, truthB);
        budget = BudgetFunctions.compoundForward(truth, content, memory);
        memory.doublePremiseTask(content, truth, budget);
        Variable varDep = new Variable("#varDep");
        if (index == 0) {
            state1 = Inheritance.make(varDep, taskContent.getPredicate(), memory);
            state2 = Inheritance.make(varDep, beliefContent.getPredicate(), memory);
        } else {
            state1 = Inheritance.make(taskContent.getSubject(), varDep, memory);
            state2 = Inheritance.make(beliefContent.getSubject(), varDep, memory);
        }
        content = Conjunction.make(state1, state2, memory);
        truth = TruthFunctions.intersection(truthT, truthB);
        budget = BudgetFunctions.compoundForward(truth, content, memory);
        memory.doublePremiseTask(content, truth, budget);
    }

    /**
     * {<M --> S>, <C ==> <M --> P>>} |- <(&&, <#x --> S>, C) ==> <#x --> P>>
     * {<M --> S>, (&&, C, <M --> P>)} |- (&&, C, <<#x --> S> ==> <#x --> P>>)
     *
     * @param taskContent The first premise directly used in internal induction,
     * <M --> S>
     * @param beliefContent The componentCommon to be used as a premise in
     * internal induction, <M --> P>
     * @param oldCompound The whole contentInd of the first premise, Implication
     * or Conjunction
     * @param memory Reference to the memory
     */
    static void introVarInner(Statement premise1, Statement premise2, CompoundTerm oldCompound, Memory memory) {
        Task task = memory.getCurrentTask();
        Sentence taskSentence = task.getSentence();
        if (!taskSentence.isJudgment() || (premise1.getClass() != premise2.getClass()) || oldCompound.containComponent(premise1)) {
            return;
        }
        Term subject1 = premise1.getSubject();
        Term subject2 = premise2.getSubject();
        Term predicate1 = premise1.getPredicate();
        Term predicate2 = premise2.getPredicate();
        Term commonTerm1, commonTerm2;
        if (subject1.equals(subject2)) {
            commonTerm1 = subject1;
            commonTerm2 = secondCommonTerm(predicate1, predicate2, 0);
        } else if (predicate1.equals(predicate2)) {
            commonTerm1 = predicate1;
            commonTerm2 = secondCommonTerm(subject1, subject2, 0);
        } else {
            return;
        }
        Sentence belief = memory.getCurrentBelief();
        HashMap<Term, Term> substitute = new HashMap<>();
        substitute.put(commonTerm1, new Variable("#varDep2"));
        CompoundTerm content = (CompoundTerm) Conjunction.make(premise1, oldCompound, memory);
        content.applySubstitute(substitute);
        TruthValue truth = TruthFunctions.intersection(taskSentence.truth, belief.truth);
        BudgetValue budget = BudgetFunctions.forward(truth, memory);
        memory.doublePremiseTask(content, truth, budget);
        substitute.clear();
        substitute.put(commonTerm1, new Variable("$varInd1"));
        if (commonTerm2 != null) {
            substitute.put(commonTerm2, new Variable("$varInd2"));
        }
        content = Implication.make(premise1, oldCompound, memory);
        if (content == null)
            return;
        content.applySubstitute(substitute);
        if (premise1.equals(taskSentence.getContent())) {
            truth = TruthFunctions.induction(belief.truth, taskSentence.truth);
        } else {
            truth = TruthFunctions.induction(taskSentence.truth, belief.truth);
        }
        budget = BudgetFunctions.forward(truth, memory);
        memory.doublePremiseTask(content, truth, budget);
    }

    /**
     * Introduce a second independent variable into two terms with a common
     * component
     *
     * @param term1 The first term
     * @param term2 The second term
     * @param index The index of the terms in their statement
     */
    private static Term secondCommonTerm(Term term1, Term term2, int index) {
        Term commonTerm = null;
        if (index == 0) {
            if ((term1 instanceof ImageExt) && (term2 instanceof ImageExt)) {
                commonTerm = ((ImageExt) term1).getTheOtherComponent();
                if ((commonTerm == null) || !((ImageExt) term2).containTerm(commonTerm)) {
                    commonTerm = ((ImageExt) term2).getTheOtherComponent();
                    if ((commonTerm == null) || !((ImageExt) term1).containTerm(commonTerm)) {
                        commonTerm = null;
                    }
                }
            }
        } else {
            if ((term1 instanceof ImageInt) && (term2 instanceof ImageInt)) {
                commonTerm = ((ImageInt) term1).getTheOtherComponent();
                if ((commonTerm == null) || !((ImageInt) term2).containTerm(commonTerm)) {
                    commonTerm = ((ImageInt) term2).getTheOtherComponent();
                    if ((commonTerm == null) || !((ImageExt) term1).containTerm(commonTerm)) {
                        commonTerm = null;
                    }
                }
            }
        }
        return commonTerm;
    }

    private static void dedSecondLayerVariableUnificationTerms(Memory memory, Task task, Sentence second_belief, Stamp s, ArrayList<CompoundTerm> terms_dependent, TruthValue truth) {
        
            Sentence taskSentence = task.getSentence();
            
            Stamp sx = new Stamp(taskSentence.stamp, memory.getTime(), s);
            
            for(int i=0;i<terms_dependent.size();i++) {
                final CompoundTerm result = terms_dependent.get(i);
               
                Sentence newSentence = new Sentence(result, Symbols.JUDGMENT_MARK, truth, sx);
                                                
                BudgetValue budget = BudgetFunctions.compoundForward(truth, newSentence.getContent(), memory);
                
                Task newTask = new Task(newSentence, budget, task, null);
                Task dummy = new Task(second_belief, budget, task, null);
                
                memory.setCurrentBelief(taskSentence);
                memory.setCurrentTask(dummy);
                
                memory.derivedTask(newTask, false, false);
            }    
    }
}
