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

import nars.nal.term.Term;

/**
 * @author Alex Benini
 *
 */
public class StageGoalSelection extends Stage {
    
    
    
    public StageGoalSelection(Engine c) {
        this.c = c;
        stateName = "Call";
    }
    
    
    /* (non-Javadoc)
     * @see alice.tuprolog.AbstractRunState#doJob()
     */
    void run(Engine.State e) {
        PTerm curGoal = null;
        while (curGoal == null) {
            curGoal = e.currentContext.goalsToEval.fetch();
            if (curGoal==null){
                // demo termination
                if (e.currentContext.fatherCtx == null) {
                    //verify ChoicePoint
                    e.nextState = (e.choicePointSelector.existChoicePoint())? c.END_TRUE_CP : c.END_TRUE;
                    return;
                }
                // Caso di rimozione di un contesto di esecuzione
                e.currentContext = e.currentContext.fatherCtx;
            } else {
                // Caso di individuazione curGoal
                Term goal_app = curGoal.getTerm();
                if (!(goal_app instanceof Struct)) {
                    e.nextState = c.END_FALSE;
                    return;
                }
                
                // Code inserted to allow evaluation of meta-clause
                // such as p(X) :- X. When evaluating directly terms,
                // they are converted to execution of a call/1 predicate.
                // This enables the dynamic linking of built-ins for
                // terms coming from outside the demonstration context.
                if (curGoal != goal_app)
                    curGoal = new Struct("call", goal_app);
                
                e.currentContext.currentGoal = (Struct) curGoal;
                e.nextState = c.GOAL_EVALUATION;
                return;
            }            
        }//while
    }
    
}