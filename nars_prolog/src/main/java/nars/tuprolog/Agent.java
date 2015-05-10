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

import nars.tuprolog.event.OutputEvent;
import nars.tuprolog.event.OutputListener;
import nars.tuprolog.util.Tools;

import java.io.InputStream;

/**
 * Provides a prolog virtual machine embedded in a separate thread.
 * It needs a theory and optionally a goal.
 * It parses the theory, solves the goal and stops.
 *
 * @see alice.tuprolog.Prolog
 *
 */
public class Agent {
    
    private Prolog core;
    private String theoryText;
    private InputStream theoryInputStream;
    private String goalText;
    
  
    private OutputListener defaultOutputListener = new OutputListener() {
        public void onOutput(OutputEvent ev) {
            System.out.print(ev.getMsg());
        }
    };
    
    
    /**
     * Builds a prolog agent providing it a theory
     *
     * @param theory the text representing the theory
     */
    public Agent(String theory) throws InvalidLibraryException {
        theoryText=theory;
        core=new DefaultProlog();
        core.addOutputListener(defaultOutputListener);
    }
    
    /**
     * Builds a prolog agent providing it a theory and a goal
     */
    public Agent(String theory,String goal) throws InvalidLibraryException {
        theoryText=theory;
        goalText=goal;
        core=new DefaultProlog();
        core.addOutputListener(defaultOutputListener);
    }
    
    /**
     * Constructs the Agent with a theory provided
     * by an input stream
     */
    public Agent(InputStream is) throws InvalidLibraryException {
        theoryInputStream=is;
        core=new DefaultProlog();
        core.addOutputListener(defaultOutputListener);
    }
    
    /**
     * Constructs the Agent with a theory provided
     * by an input stream and a goal
     */
    public Agent(InputStream is,String goal) throws InvalidLibraryException {
        theoryInputStream=is;
        goalText=goal;
        core=new DefaultProlog();
        core.addOutputListener(defaultOutputListener);
    }
    
    /**
     * Starts agent execution
     */
    final  public void spawn(){
        new Agent.AgentThread(this).start();
    }
    
    /**
     * Adds a listener to ouput events
     *
     * @param l the listener
     */
    public synchronized void addOutputListener(OutputListener l) {
        core.addOutputListener(l);
    }
    
    /**
     * Removes a listener to ouput events
     *
     * @param l the listener
     */
    public synchronized void removeOutputListener(OutputListener l) {
        core.removeOutputListener(l);
    }
    
    /**
     * Removes all output event listeners
     */
    public void removeAllOutputListener(){
        core.removeAllOutputListeners();
    }
    
    
    private void body(){
        try {
            if (theoryText==null){
                core.setTheory(new Theory(theoryInputStream));
            } else {
                core.setTheory(new Theory(theoryText));
            }
            if (goalText!=null){
                core.solve(goalText);
            }
        } catch (Exception ex){
            System.err.println("invalid theory or goal.");
            ex.printStackTrace();
        }
    }
    
    
    final class AgentThread extends Thread {
        Agent agent;
        AgentThread(Agent agent){
            this.agent=agent;
        }
        final public void run(){
            agent.body();
        }
    }
    
    
    public static void main(String args[]){
        if (args.length==1 || args.length==2){
            
            //FileReader fr;
            try {
                String text = Tools.loadText(args[0]);
                if (args.length==1){
                    new Agent(text).spawn();
                } else {
                    new Agent(text,args[1]).spawn();
                }
            } catch (Exception ex){
                System.err.println("invalid theory.");
            }
        } else {
            System.err.println("args: <theory file> { goal }");
            System.exit(-1);
        }
    }
    
    
}