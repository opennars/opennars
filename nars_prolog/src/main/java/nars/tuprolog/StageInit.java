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

/**
 * @author Alex Benini
 *
 * Initial state of demostration
 */
public class StageInit extends Stage {
    
    
    public StageInit(Engine c) {
        this.c = c;
        stateName = "Goal";
    }
    
    
    /* (non-Javadoc)
     * @see alice.tuprolog.AbstractRunState#doJob()
     */
    void run(Engine.State e) {
        e.prepareGoal();
        
        /* Initialize first executionContext */
        ExecutionContext eCtx = new ExecutionContext(0);
        eCtx.goalsToEval = new SubGoalStore();
        eCtx.goalsToEval.load(Clause.extractBody(e.startGoal));
        eCtx.clause = (Struct)e.query;
        eCtx.depth = 0;
        eCtx.fatherCtx = null;
       	eCtx.haveAlternatives = false;
        
        /* Initialize VM environment */
        e.initialize(eCtx);
        
        
        /* Set the future state */
        e.nextState = c.GOAL_SELECTION;
    }
    
}
