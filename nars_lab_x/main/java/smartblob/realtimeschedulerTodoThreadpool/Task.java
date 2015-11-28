/** Ben F Rayfield offers this software under GNU GPL 2+ open source license(s) */
package smartblob.realtimeschedulerTodoThreadpool;

/** event(Object) function is called approx on preferredInterval with TimedEvent param */
public interface Task extends Eventable{
	
	/** The preferred interval in seconds, normally something around .01 or .05.
	This must be called again after every event(TimedEvent) by thread scheduler code.
	<br><br>
	To turn off this Task after next event(Object),
	preferredInterval() will be called next and returns Double.MAX_VALUE;
	*/
	public double getTargetFPS();
	
	/** Returns true if it should continue. Parameter is how many seconds
	passed since last time. Seconds must range 0 to .5 regardless of actual
	number of seconds passed since it is a realtime interface,
	and larger values may cause errors in decaying vars.
	*
	public boolean nextState(double secondsSinceLastCall);
	*/

}