/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package smartblob;


import smartblob.datastruct.Statsys;
import smartblob.smartblob.physics.ChangeSpeed;

import java.util.ArrayList;

/** A smartblob is a changing polygon which has a statsys observing and moving it
while the smartblob bounces on and is grabbed by other smartblobs.
The polygon can only change to another polygon with the same number of points.
<br><br>
Positions and speeds are never updated together as enforced by 4 funcs
which mark the start and end of updating positions and speeds.
*/
public interface Smartblob{
	
	public Statsys brain();
	
	/** This is an approximation of the shape which can actually be scalars.
	<br><br>
	Make sure to use 1 pixel bigger in width and height for bounding rectangle
	because scalar positions get rounded down. Or are they rounded either way?
	I'm creating boundingRectangle() for that. Use that instead of this Polygons rect.
	<br><br>
	OLD BUT PARTIALLY RELEVANT:
	For compatibility with shapes that have int positions,
	a shape may have scalar positions but they can never get close enough
	to eachother that any 2 points occupy the same pixel at int positions,
	except for the innermost layer which are all held to equal x and y.
	Violating this may result in errors where Polygon objects say
	you have not enclosed a well defined shape since it crosses itself.
	*/
	public Shape shape();
	
	/** 1 pixel bigger in all directions than the Polygon's rectangle since its based on ints
	and I'm undecided what kind of rounding I'll end up using.
	*/
	public Rectangle boundingRectangle();
	
	/** Mutable list of physics ops that act on this smartblob. Add them to the list. */
	public ArrayList<ChangeSpeed> mutablePhysics();
	
	/** Between onStartUpdatePositions() and onEndUpdatePositions() */
	public boolean isUpdatingPositions();
	
	/** Between onStartUpdateSpeeds() and onEndUpdateSpeeds() */
	public boolean isUpdatingSpeeds();
	
	/** Tells this Smartblob that positions are being updated (based on speeds),
	maybe by external code.
	*/
	public void onStartUpdatePositions();
	
	/** This func includes updating boundingRectangle() and shape()
	or onStartUpdatePosition may mark the need for new Rectangle and Shape
	in each call of those funcs. It can only be cached after end and before start.
	*/
	public void onEndUpdatePositions();
	
	/** Tells this Smartblob that speeds are being updated, maybe by external code */
	public void onStartUpdateSpeeds();
	
	public void onEndUpdateSpeeds();

}