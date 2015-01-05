/*
  Part of the G4P library for Processing 
  	http://www.lagers.org.uk/g4p/index.html
	http://sourceforge.net/projects/g4p/files/?source=navbar
	
  Copyright (c) 2012 Peter Lager

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General
  Public License along with this library; if not, write to the
  Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  Boston, MA  02111-1307  USA
 */

package automenta.vivisect.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;

import javax.swing.Timer;

import processing.core.PApplet;

/**
 * This class is used to trigger events at user defined intervals. The event will
 * call a user defined method/function. The only restriction is that the method
 * used has a single parameter of type GTimer and returns void eg <br> 
 * <pre>
 * void fireBall(GTimer timer){ ... }
 * </pre><br>
 *
 * Each timer object must have its own handler
 * 
 * It has no visible GUI representation so will not appear in the GUI.
 * 
 * @author Peter Lager
 *
 */
public class GTimer implements GConstantsInternal {

	/* This must be set by the constructor */
	protected PApplet app;

	/* The object to handle the event */
	protected Object eventHandlerObject = null;
	/* The method in eventHandlerObject to execute */
	protected Method eventHandlerMethod = null;
	/* the name of the method to handle the event */ 
	protected String eventHandlerMethodName;

	// The number of repeats i.e. events to be fired.
	protected int nrepeats = -1;
	
	protected Timer timer = null;

	/**
	 * Create the GTimer object with this ctor.
	 * 
	 * 'methodName' is the method/function to be called every 'interval' 
	 * milliseconds. 'obj' is the object that contains the method/function
	 * 'methodName'
	 * 
	 * For most users 'methodName' will be in their main sketch so this
	 * parameter has the same value as 'theApplet'
	 * 
	 * @param theApplet a reference to the PApplet object (invariably <b>this</b>)
	 * @param obj the object that has the method to be executed (likely to be <b>this</b>)
	 * @param methodName the name of the method to be called by the timer
	 * @param delay the initial delay and the time (in millisecs) between function calls
	 */
	public GTimer(PApplet theApplet, Object obj, String methodName, int delay){
		this(theApplet, obj, methodName, delay, delay);
	}

	/**
	 * Create the GTimer object with this ctor.
	 * 
	 * 'methodName' is the method/function to be called every 'interval' 
	 * milliseconds. 'obj' is the object that contains the method/function
	 * 'methodName'
	 * 
	 * For most users 'methodName' will be in their main sketch so this
	 * parameter has the same value as 'theApplet'
	 * 
	 * @param theApplet a reference to the PApplet object (invariably <b>this</b>)
	 * @param obj the object that has the method to be executed (likely to be <b>this</b>)
	 * @param methodName the name of the method to be called by the timer
	 * @param delay the time (in millisecs) between function calls
	 * @param initDelay the initial delay (in millisecs)
	 */
	public GTimer(PApplet theApplet, Object obj, String methodName, int delay, int initDelay){
		app = theApplet;
		createEventHandler(obj, methodName);
		// If we have something to handle the event then create the Timer
		if(eventHandlerObject != null){
			timer = new Timer(delay, new ActionListener(){

				public void actionPerformed(ActionEvent e) {
					fireEvent();
				}

			});	
			timer.setInitialDelay(initDelay);
			timer.setDelay(delay);
			timer.stop();
		}
	}

	/**
	 * See if 'obj' has a method called 'methodName' that has a single parameter of type
	 * GTimer and  if so keep a reference to it.
	 * 
	 * @param obj
	 * @param methodName
	 */
	protected void createEventHandler(Object handlerObj, String methodName){
		try{
			eventHandlerMethod = handlerObj.getClass().getMethod(methodName, new Class<?>[] { GTimer.class } );
			eventHandlerObject = handlerObj;
			eventHandlerMethodName = methodName;			
		} catch (Exception e) {
			GMessenger.message(NONEXISTANT, new Object[] {this, methodName, new Class<?>[] { GTimer.class }});
			eventHandlerObject = null;
		}
	}

	/**
	 * Attempt to fire an event for this timer. This will call the 
	 * method/function defined in the ctor.
	 */
	protected void fireEvent(){
		if(eventHandlerMethod != null){
			try {
				eventHandlerMethod.invoke(eventHandlerObject, this);
				if(--nrepeats == 0)
					stop();
			} catch (Exception e) {
				GMessenger.message(EXCP_IN_HANDLER,  
						new Object[] {eventHandlerObject, eventHandlerMethodName, e } );
				System.out.println("Disabling " + eventHandlerMethod.getName() + " due to an unknown error");
				eventHandlerMethod = null;
				eventHandlerObject = null;
			}
		}
	}

	/**
	 * Start the timer (call the method forever)
	 */
	public void start(){
		this.nrepeats = -1;
		if(timer != null)
			timer.start();
	}
	
	/**
	 * Start the timer and call the method for the number of
	 * times indicated by nrepeats
	 * If nrepeats is <=0 then repeat forever
	 * 
	 * @param nrepeats
	 */
	public void start(int nrepeats){
		this.nrepeats = nrepeats;
		if(timer != null)
			timer.start();
	}

	/**
	 * Stop the timer (can be restarted with start() method)
	 */
	public void stop(){
		if(timer != null)
			timer.stop();
	}

	/**
	 * Is the timer running?
	 * @return true if running
	 */
	public boolean isRunning(){
		if(timer != null)
			return timer.isRunning();
		else
			return false;
	}
	
	/**
	 * Set the interval between events
	 * @param interval delay between events in milliseconds
	 */
	public void setInterval(int interval){
		if(timer != null)
			timer.setDelay(interval);
	}
	
	/**
	 * Set the delay before the first event is triggered
	 * @param initDelay initial delay in milliseconds
	 */
	public void setInitialDelay(int initDelay){
		if(timer != null)
			timer.setInitialDelay(initDelay);
	}
	
	/**
	 * Sets the initial delay and the interval between events. <br>
	 * This is equivalent to calling both - <br>
	 * <pre>
	 * setInterval(delay);
	 * setInitialDelay(delay);
	 * </pre><br>
	 * @param delay initial delay and interval in milliseconds
	 */
	public void setDelay(int delay){
		if(timer != null){
			timer.setInitialDelay(delay);
			timer.setDelay(delay);
		}
	}
	
	/**
	 * Get the interval time (milliseconds) between 
	 * events.
	 * @return interval in millsecs or -1 if the timer failed to
	 * be created.
	 */
	public int getInterval(){
		if(timer != null)
			return timer.getDelay();
		else
			return -1;		
	}
	
	/**
	 * Get the initial delay time (milliseconds).
	 * @return initial delay in millsecs or -1 if the timer failed to
	 * be created.
	 */
	public int getInitialDelay(){
		if(timer != null)
			return timer.getInitialDelay();
		else
			return -1;		
	}
	
	/**
	 * See if the GTimer object has been created successfully
	 * @return true if successful
	 */
	public boolean isValid(){
		return (eventHandlerObject != null && timer != null);
	}

}
