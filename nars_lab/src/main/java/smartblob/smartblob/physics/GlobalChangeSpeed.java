/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package smartblob.smartblob.physics;

/** Part of a SmartblobSim physics simulation, like a wall or way of bouncing on eachother.
This may be only for the whole Set of smartblobs at once,
so physics inside individual smartblobs may need to be done separately.
I really thought of this for bouncing on walls.
*/
public interface GlobalChangeSpeed{
	
	public void globalChangeSpeed(SmartblobSim sim, float secondsSinceLastCall);

}
