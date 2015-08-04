/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package smartblob.realtimeschedulerTodoThreadpool;

/** Similar to TimedEvent and used as a parameter in Eventable.event(Object)
for what was done using Eventable type DigitalTimeTask,
any task that runs in discrete steps instead of based on time passed between steps.
<br><br>
Should Long be used instead of CountEvent? It has the same data.-------
<br><br>
TODO Should CountEvent have a string name for what is being counted? Probably not.
<br><br>
TODO instead of SequenceEvent, should TimedEvent and CountEvent extend Number?
*/
public class CountEvent implements SequenceEvent{
	
	public final long count;
	
	/** Uses current time from DatastructUtil.time() */
	public CountEvent(long count){
		this.count = count;
	}
	
	public Number sequenceNumber(){ return count; }

}