/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package smartblob.smartblob.physics;

import smartblob.Smartblob;
import smartblob.SmartblobUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Simulation of smartblob physics including collisions with eachother andOr walls
and maybe 1d heightmap from floor andOr other walls.
*/
public class SmartblobSim{
	
	public final List<Smartblob> smartblobs = new CopyOnWriteArrayList();
	
	public final List<GlobalChangeSpeed> physicsParts;
	
	public SmartblobSim(GlobalChangeSpeed... physicsParts){
		this.physicsParts = new ArrayList(Arrays.asList(physicsParts));
	}
	
	public void nextState(float secondsThisTime){
		Smartblob blobArray[];
		synchronized(smartblobs){
			blobArray = smartblobs.toArray(new Smartblob[0]);
		}
		for(Smartblob blob : blobArray){
			blob.onStartUpdateSpeeds();
		}
		for(GlobalChangeSpeed p : physicsParts){
			p.globalChangeSpeed(this, secondsThisTime);
		}
		for(Smartblob blob : blobArray){
			blob.onEndUpdateSpeeds();
		}
		SmartblobUtil.moveAll(this, secondsThisTime); //calls onStart*  and onEnd* *UpdatePositions
		for(Smartblob blob : blobArray){
			blob.onStartUpdateSpeeds();
		}
		for(Smartblob blob : blobArray){
			//blob.nextState(secondsSinceLastCall); //does all SmartblobPhysicsPart and updateShape and maybe more
			//TODO threads

			ArrayList<ChangeSpeed> mp = blob.mutablePhysics();
			for (int i = 0; i < mp.size(); i++) {
				ChangeSpeed c = mp.get(i);
				c.changeSpeed(blob, secondsThisTime);
			}

			blob.onEndUpdateSpeeds();
		}
		SmartblobUtil.moveAll(this, secondsThisTime); //calls onStart*  and onEnd* *UpdatePositions
	}

}
