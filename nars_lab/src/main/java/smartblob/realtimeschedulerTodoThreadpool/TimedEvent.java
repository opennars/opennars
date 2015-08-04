package smartblob.realtimeschedulerTodoThreadpool;


import smartblob.common.CoreUtil;

/** Normally used as parameter of Eventable.event(Object) when Task runs again.
The main other kind of parameter is MultiTouch, which I'm changing to extend this.
<br><br>
TODO instead of SequenceEvent, should TimedEvent and CountEvent extend Number?
*/
public class TimedEvent implements SequenceEvent{
	
	//TODO when using Eventable inside a Task wrapper, receiving TimedEvents, how will PrilistsPanel.setObjectView turn off the Task that it starts, after next call to start some other?
	
	/** Seconds since year 1970. See DatastructUtil.time() which has microsecond precision. */
	public final double time;
	
	/** Uses current time from DatastructUtil.time() */
	public TimedEvent(){
		this(CoreUtil.time());
	}
	
	public TimedEvent(double time){
		this.time = time;
	}
	
	public Number sequenceNumber(){ return time; }
	
	public String toString(){
		return "[TimedEvent "+time+"]";
	}

}