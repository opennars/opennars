/*
 * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package nars.tuprolog;

import nars.tuprolog.util.OneWayList;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alex Benini
 *
 */
public class StageRuleSelection extends Stage {
    
    
    
    public StageRuleSelection(Engine c) {
        this.c = c;
        stateName = "Init";
    }
    
    /* (non-Javadoc)
     * @see alice.tuprolog.AbstractRunState#doJob()
     */
    void run(Engine.State e) {
        /*----------------------------------------------------
         * Individuo compatibleGoals e
         * stabilisco se derivo da Backtracking.
         */
        Struct goal = e.currentContext.currentGoal;
        boolean fromBacktracking = true;
        ChoicePointContext alternative = e.currentAlternative;
        ClauseStore clauseStore;
        e.currentAlternative = null;
        if (alternative == null) {
            /* from normal evaluation */
            fromBacktracking = false;
            //List varsList = new LinkedList();
            List<Var> varsList = new ArrayList<>();
            e.currentContext.trailingVars = new OneWayList<>(varsList,e.currentContext.trailingVars);
            clauseStore = ClauseStore.build(goal, varsList, c.find(goal));
            if (clauseStore == null){
                e.nextState = c.BACKTRACK;
                return;
            }
        } else
            clauseStore = alternative.compatibleGoals;
        
        /*-----------------------------------------------------
         * Scelgo una regola fra quelle potenzialmente compatibili.
         */
        Clause clause = clauseStore.fetch();
        
        /*-----------------------------------------------------
         * Build ExecutionContext and ChoicePointContext
         */
        ExecutionContext ec = new ExecutionContext(e.nDemoSteps++);
        ExecutionContext curCtx = e.currentContext;
        ec.clause = clause.getClause();
        //head and body with refresh variables (clause copied)
        clause.performCopy(ec.getId());
        ec.headClause = clause.getHeadCopy();
        ec.goalsToEval = new SubGoalStore();
        ec.goalsToEval.load( clause.getBodyCopy() );
        // The following block encodes cut functionalities, and hardcodes the
        // special treatment that ISO Standard reserves for goal disjunction:
        // section 7.8.6.1 prescribes that ;/2 must be transparent to cut.
        ec.choicePointAfterCut = e.choicePointSelector.getPointer();
        if (alternative != null) {
            ChoicePointContext choicePoint = alternative;
            int depth = alternative.executionContext.depth;
            ec.choicePointAfterCut = choicePoint.prevChoicePointContext;
            Struct currentGoal = choicePoint.executionContext.currentGoal;
            while (currentGoal.getName().equals(";") && currentGoal.size() == 2) {
                if (choicePoint.prevChoicePointContext != null) {
                    int distance = depth - choicePoint.prevChoicePointContext.executionContext.depth;
                    while (distance == 0 && choicePoint.prevChoicePointContext != null) {
                        ec.choicePointAfterCut = choicePoint.prevChoicePointContext.prevChoicePointContext;
                        choicePoint = choicePoint.prevChoicePointContext;
                    }
                    if (distance == 1 && choicePoint.prevChoicePointContext != null) {
                        ec.choicePointAfterCut = choicePoint.prevChoicePointContext.prevChoicePointContext;
                        currentGoal = choicePoint.prevChoicePointContext.executionContext.currentGoal;
                        choicePoint = choicePoint.prevChoicePointContext;
                    } else
                        break;
                } else
                    break;
            }
        }
            
        Struct curGoal = curCtx.currentGoal;
        List<Var> unifiedVars = e.currentContext.trailingVars.getHead();
        curGoal.unify(unifiedVars,unifiedVars,ec.headClause);
        
        ec.haveAlternatives = clauseStore.haveAlternatives();
        
        //creazione cpc
        if (ec.haveAlternatives && !fromBacktracking) {
            ChoicePointContext cpc = new ChoicePointContext();
            cpc.compatibleGoals = clauseStore;
//            c.saveLastTheoryStatus();
            cpc.executionContext = curCtx;
            cpc.indexSubGoal = curCtx.goalsToEval.getCurrentGoalId();
            cpc.varsToDeunify = e.currentContext.trailingVars;
            e.choicePointSelector.add(cpc);
        }
        //distruzione cpc
        if (!ec.haveAlternatives && fromBacktracking) {            
                    e.choicePointSelector.removeUnusedChoicePoints();
                }
        
        ec.performTailRecursionOptimization(e);
        
        ec.saveParentState();
        e.currentContext = ec;
        e.nextState = c.GOAL_SELECTION;
    }
    
}