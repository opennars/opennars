/*
 * Sample.java
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
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.grid2d.operator;

import nars.Memory;
import nars.grid2d.TestChamber;
import nars.nal.Task;
import nars.nal.nal8.Operation;
import nars.nal.nal8.SynchOperator;
import nars.nal.term.Term;

import java.util.List;

/**
 *  A class used as a template for Operator definition.
 * TODO: memory.registerOperator(new Goto("^goto"));
 */
public class Deactivate extends SynchOperator {

    TestChamber chamb;
    public Deactivate(TestChamber chamb, String name) {
        super(name);
        this.chamb=chamb;
    }

    @Override
    protected List<Task> execute(Operation operation, Memory memory) {
        //Operation content = (Operation) task.getContent();
        //Operator op = content.getOperator();



        TestChamber.executed=true;
        System.out.println("Executed: " + this);
        for (Term t : operation.arg()) {
            System.out.println(" --- " + t);
            TestChamber.operateObj(t.toString(), "deactivate");
            break;
        }
        
        
       // if(nars.grid2d.Grid2DSpace.world_used) {
            //ok lets start pathfinding tool
            //nars.grid2d.Grid2DSpace.pathFindAndGoto(arg);
       // }
        
       
        
        return null;
    }

}
