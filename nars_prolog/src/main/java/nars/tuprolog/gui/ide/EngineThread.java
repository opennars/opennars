/*
 * tuProlog - Copyright (C) 2001-2004  aliCE team at deis.unibo.it
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
package nars.tuprolog.gui.ide;

//import javax.swing.JOptionPane;

import nars.tuprolog.MalformedGoalException;
import nars.tuprolog.Prolog;

/**
 * 
 *
 * @author aricci
 *
 */
public class EngineThread extends Thread {
 
    private String goal;
    private Prolog engine;
    private ConsoleManager console;
    private int actionToDo;
    
    public EngineThread(Prolog engine, String goal, ConsoleManager c){
        this.engine = engine;
        console = c;
        this.goal = goal;
        actionToDo = 1;
    }

    public EngineThread(Prolog engine){
        this.engine = engine;
        actionToDo = 2;
    }
    
    public void run() {
        if (actionToDo==1){
            try {
                engine.solve(goal);
            } catch (MalformedGoalException ex){
                console.setStatusMessage("Syntax Error: malformed goal.");
                
                /**rows added by Castellani Juri to enable toolbars and stop bottons
                 * when there is a malformed goal
                 */
                console.enableTheoryCommands(true);
                console.enableStopButton(false);
                
            }
        } else if (actionToDo==2){
            try {
                engine.solveNext();
            } catch (Exception ex){
                    ex.printStackTrace();
            }
        }
    }
}
