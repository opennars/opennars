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
 */
public class ChoicePointContext {
    
    ClauseStore compatibleGoals;
    ExecutionContext executionContext;
    ChoicePointContext prevChoicePointContext;
    SubGoalId indexSubGoal;
    OneWayList<List<Var>> varsToDeunify;
    
    
    public String toString(){
        return "     ChoicePointId: "+executionContext.getId()+ ':' +indexSubGoal+ '\n' +
               //"varsToDeunify: "+getVarsToDeunify()+"\n"+
               "     compGoals:     "+compatibleGoals+ '\n';
    }
    
    /*
     * Methods for spyListeners
     */
    
   
    public ClauseStore getCompatibleGoals() {
        return compatibleGoals;
    }
    
    
    public ExecutionContext getExecutionContext() {
        return executionContext;
    }
    
    public SubGoalId getIndexBack() {
        return indexSubGoal;
    }
    
//    public List<List<Var>> getVarsToDeunify() {
//        ArrayList<List<Var>> l = new ArrayList<>();
//        OneWayList<List<Var>> t = varsToDeunify;
//        while (t != null) {
//            l.add(t.getHead());
//            t = t.getTail();
//        }
//        return l;
//    }
}