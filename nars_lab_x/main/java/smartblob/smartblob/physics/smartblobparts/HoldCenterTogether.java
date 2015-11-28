/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package smartblob.smartblob.physics.smartblobparts;

import smartblob.blobs.layeredzigzag.CornerData;
import smartblob.blobs.layeredzigzag.LayeredZigzag;

/** Holds layer 0 corners/points to the average position and speed of all of them */
public class HoldCenterTogether extends AbstractChangeSpeedLZ{
	
	public void changeSpeed(LayeredZigzag blob, float secondsSinceLastCall){
		float totalY = 0, totalX = 0, totalYSpeed = 0, totalXSpeed = 0;
		for(int p=0; p<blob.layerSize; p++){
			CornerData cd = blob.corners[0][p];
			totalY += cd.y;
			totalX += cd.x;
			totalYSpeed += cd.speedY;
			totalXSpeed += cd.speedX;
		}
		float aveY = totalY/blob.layerSize;
		float aveX = totalX/blob.layerSize;
		float aveYSpeed = totalYSpeed/blob.layerSize;
		float aveXSpeed = totalXSpeed/blob.layerSize;
		for(int p=0; p<blob.layerSize; p++){
			CornerData cd = blob.corners[0][p];
			cd.y = aveY;
			cd.x = aveX;
			cd.speedY = aveYSpeed;
			cd.speedX = aveXSpeed;
		}
	}

}