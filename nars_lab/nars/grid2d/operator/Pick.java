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
 
import java.util.List;
import nars.core.Memory;
import nars.entity.Task;
import nars.grid2d.TestChamber;
import nars.language.Term;
import nars.operator.Operation;
import nars.operator.Operator;

/**
 *  A class used as a template for Operator definition.
 * TODO: memory.registerOperator(new Goto("^goto"));
 */
public class Pick extends Operator {

    TestChamber chamb;
    public Pick(TestChamber chamb, String name) {
        super(name);
        this.chamb=chamb;
    }

    @Override
    protected List<Task> execute(Operation operation, Term[] args, Memory memory) {
        //Operation content = (Operation) task.getContent();
        //Operator op = content.getOperator();
         
        TestChamber.executed=true;
        System.out.println("Executed: " + this);
        for (Term t : args) {
            System.out.print(" --- " + t);
            chamb.operateObj(t.toString(),"pick");
        }
        
        
       // if(nars.grid2d.Grid2DSpace.world_used) {
            //ok lets start pathfinding tool
            //nars.grid2d.Grid2DSpace.pathFindAndGoto(arg);
       // }
        
       
        
        return null;
    }

}
