package smartblob.realtimeschedulerTodoThreadpool;

/** This is a more efficient and general replacement for Task.
Instead of having to run on an interval andOr having a separate system for
separate keyboard/mouse/gameController events, all that will be done
in 1 func that takes 1 param. It can be called on a timer andOr
any kind of events. Its a simple expandable paradigm.
<br><br>
An object implementing Eventable should not just run 1 more cycle
of their calculation as a Task would do, because event(Object) may
be called many times in the same millisecond or less often
like would happen in a Task, and its probably not on event intervals,
so the object has to consider what time it is
(DatastructUtil.time() has microsecond precision)
before deciding how much of the next calculation to do
and considering the new Object coming in representing an event.
<br><br>
This is needed to avoid the slowness and display problems of Dynarect
when repainted when events dont happen, or on the other side of that
when they have a long interval they dont get repainted when events happen.
<br><br>
TODO Should Eventable be merged with Dynarect.in and Dynarect.out funcs?
x and y params could be part of context object
*/
public interface Eventable{

	public void event();

}