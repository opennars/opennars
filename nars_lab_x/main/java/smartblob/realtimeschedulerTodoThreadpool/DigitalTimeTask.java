/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package smartblob.realtimeschedulerTodoThreadpool;

/** A task which does the same thing regardless of how much time
has passed between cycles, like Conways Game Of Life goes to the next cycle.
The task may choose to wait until a certain amount of time has passed
as multiple calls to Task.nextState(double) before calling nextState()
from inside it, or it may call every time, depending on the task.
*/
@Deprecated //Since Task changed to extend Eventable,
//this would be better done by event(new CountEvent(long)),
//which I'm commentingOut nextState() to do that here, but dont need this interface.
public interface DigitalTimeTask extends Task{
	
	/** For DigitalTimeTask that really want to just be a Task,
	use this default interval for nextState to call nextState(double) with,
	or any other interval constant or variable if you have a reason to use a different one.
	*/
	public static final double defaultNextStateInterval = .05;
	
	//public boolean nextState();

}