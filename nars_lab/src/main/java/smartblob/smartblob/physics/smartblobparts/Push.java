/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package smartblob.smartblob.physics.smartblobparts;


import smartblob.blobs.layeredzigzag.CornerData;
import smartblob.blobs.layeredzigzag.LayeredZigzag;

/** A constant acceleration and direction of a specific CornerData */
public class Push extends AbstractChangeSpeedLZ{
	
	public final CornerData cd;
	
	public float accelerateY, accelerateX;
	
	public Push(CornerData cd, float accelerateY, float accelerateX){
		this.cd = cd;
		this.accelerateY = accelerateY;
		this.accelerateX = accelerateX;
	}
	
	public void changeSpeed(LayeredZigzag blob, float secondsSinceLastCall){
		if(blob != cd.smartblob) return;
		cd.speedY += accelerateY*secondsSinceLastCall;
		cd.speedX += accelerateX*secondsSinceLastCall;
	}

}