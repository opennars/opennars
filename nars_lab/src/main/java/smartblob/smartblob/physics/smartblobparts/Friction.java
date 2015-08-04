/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package smartblob.smartblob.physics.smartblobparts;

import smartblob.blobs.layeredzigzag.CornerData;
import smartblob.blobs.layeredzigzag.LayeredZigzag;

public class Friction extends AbstractChangeSpeedLZ{
	
	public float friction;
	
	public Friction(float friction){
		this.friction = friction;
	}
	
	public void changeSpeed(LayeredZigzag blob, float secondsSinceLastCall){
		float subtractFromSpeed = friction*secondsSinceLastCall;
		for(int layer=0; layer<blob.layers; layer++){
			for(int p=0; p<blob.layerSize; p++){
				CornerData cd = blob.corners[layer][p];
				float speed = (float)Math.sqrt(cd.speedY*cd.speedY + cd.speedX*cd.speedX);
				float newSpeed = speed-subtractFromSpeed;
				if(newSpeed <= 0){
					cd.speedY = cd.speedX = 0;
				}else{
					float mult = newSpeed/speed;
					cd.speedY *= mult;
					cd.speedX *= mult;
				}
			}
		}
	}

}