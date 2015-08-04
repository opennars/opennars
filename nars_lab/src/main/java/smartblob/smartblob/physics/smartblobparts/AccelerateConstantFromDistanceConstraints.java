/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package smartblob.smartblob.physics.smartblobparts;

import smartblob.blobs.layeredzigzag.CornerData;
import smartblob.blobs.layeredzigzag.LayeredZigzag;
import smartblob.blobs.layeredzigzag.LineData;

/** TODO... physicsmataV1.2,1 demonstrates that moving a constant distance each cycle
is an effective way (while questionably efficient as it needs more cycles)
to sync distance constraints across an object. I'm extending that to apply
to speed instead of position, which is harder.
*/
public class AccelerateConstantFromDistanceConstraints extends AbstractChangeSpeedLZ{
	
	public float acceleration;
	
	public AccelerateConstantFromDistanceConstraints(float acceleration){
		this.acceleration = acceleration;
	}
	
	/** TODO Because of pascalstri and bellcurves in general defining the relation
	between stdDev and variance as squared, the constant amount added to
	speed is scaled by TODO is it the sqrt or squared of secondsSinceLastCall?
	For now I'll just use secondsSinceLastCall directly, but fix that later.
	*/
	public void changeSpeed(LayeredZigzag blob, float secondsSinceLastCall){
		float accNow = acceleration*secondsSinceLastCall;
		for(LineData ld : blob.allLineDatas()){
			CornerData a = ld.adjacentCorners[0], b = ld.adjacentCorners[1];
			float dy = b.y - a.y;
			float dx = b.x - a.x;
			float distance = (float)Math.sqrt(dx*dx+dy*dy);
			if(distance == 0) continue;
			float wantToAddToDistance = ld.targetDistance-distance; //positive or negative
			float addToEachSpeed = 0<wantToAddToDistance ? accNow : -accNow;
			float normDy = dy/distance, normDx = dx/distance;
			float addToSpeedY = normDy*addToEachSpeed;
			float addToSpeedX = normDx*addToEachSpeed;
			b.speedY += addToSpeedY;
			a.speedY -= addToSpeedY;
			b.speedX += addToSpeedX;
			a.speedX -= addToSpeedX;
		}
	}

}