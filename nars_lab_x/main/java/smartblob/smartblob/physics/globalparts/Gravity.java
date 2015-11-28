/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package smartblob.smartblob.physics.globalparts;


import smartblob.Smartblob;
import smartblob.blobs.layeredzigzag.CornerData;
import smartblob.blobs.layeredzigzag.LayeredZigzag;
import smartblob.smartblob.physics.GlobalChangeSpeed;
import smartblob.smartblob.physics.SmartblobSim;

/** Subtracts from vertical speed continuously
(actually adds since positive is down in java graphics) of all CornerData */
public class Gravity implements GlobalChangeSpeed {
	
	public float acceleration;
	
	public Gravity(float acceleration){
		this.acceleration = acceleration;
	}
	
	public void globalChangeSpeed(SmartblobSim sim, float secondsSinceLastCall){
		boolean downIsPositive = true; //in java graphics down is positive y
		float amount = secondsSinceLastCall*acceleration;
		float addToSpeed = downIsPositive ? amount : -amount;
		Smartblob blobArray[];
		synchronized(sim.smartblobs){
			blobArray = sim.smartblobs.toArray(new Smartblob[0]);
		}
		for(Smartblob blob : blobArray){
			if(blob instanceof LayeredZigzag){
				addToAllYSpeeds((LayeredZigzag)blob, addToSpeed);
			}else{
				System.out.println(Smartblob.class.getName()+" type unknown: "+blob.getClass().getName());
			}
		}
	}
	
	public static void addToAllYSpeeds(LayeredZigzag z, float addToAllSpeeds){
		for(CornerData layerOfCorners[] : z.corners){
			for(CornerData cd : layerOfCorners){
				cd.speedY += addToAllSpeeds;
			}
		}
	}

}
