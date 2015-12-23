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
package nars.testchamber.operator;

import nars.nal.nal8.Execution;
import nars.nal.nal8.operator.SyncOperator;
import nars.term.Term;
import nars.testchamber.TestChamber;

/**
 *  A class used as a template for Operator definition.
 * TODO: memory.registerOperator(new Goto("^goto"));
 */
public class Deactivate extends SyncOperator {

    TestChamber chamb;
    public Deactivate(TestChamber chamb, String name) {
        super(name);
        this.chamb=chamb;
    }

    @Override
    public void execute(Execution e) {
        Term[] argTerms = e.argArray();

        TestChamber.executed=true;
        System.out.println("Executed: " + this);

        for (int i = 0; i < 1; i++) {
            Term t = argTerms[i];
            System.out.println(" --- " + t);
            TestChamber.operateObj(t.toString(), "deactivate");

        }
        
       // if(nars.grid2d.Grid2DSpace.world_used) {
            //ok lets start pathfinding tool
            //nars.grid2d.Grid2DSpace.pathFindAndGoto(arg);
       // }
    }

}
