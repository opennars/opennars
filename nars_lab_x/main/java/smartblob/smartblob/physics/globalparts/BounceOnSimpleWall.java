/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package smartblob.smartblob.physics.globalparts;


import smartblob.Smartblob;
import smartblob.blobs.layeredzigzag.CornerData;
import smartblob.blobs.layeredzigzag.LayeredZigzag;
import smartblob.smartblob.physics.GlobalChangeSpeed;
import smartblob.smartblob.physics.SmartblobSim;

public class BounceOnSimpleWall implements GlobalChangeSpeed {
	
	//TODO optimize collisions by checking boundingRectangle
	
	public float position;
	
	public final boolean verticalInsteadOfHorizontal;
	
	public final boolean maxInsteadOfMin;
	
	public BounceOnSimpleWall(float position, boolean verticalInsteadOfHorizontal, boolean maxInsteadOfMin){
		this.position = position;
		this.verticalInsteadOfHorizontal = verticalInsteadOfHorizontal;
		this.maxInsteadOfMin = maxInsteadOfMin;
	}
	
	public void globalChangeSpeed(SmartblobSim sim, float secondsSinceLastCall){
		Smartblob blobArray[];
		synchronized(sim.smartblobs){
			blobArray = sim.smartblobs.toArray(new Smartblob[0]);
		}
		for(Smartblob blob : blobArray){
			Rectangle r = blob.boundingRectangle();
			if(anyPartIsPastThisWall(r)){
				if(blob instanceof LayeredZigzag){
					bounceSomePartsOnWall((LayeredZigzag)blob);
				}else{
					System.out.println(Smartblob.class.getName()+" type unknown: "+blob.getClass().getName());
				}
			}
		}
	}
	
	public boolean anyPartIsPastThisWall(Rectangle r){
		if(verticalInsteadOfHorizontal){
			if(maxInsteadOfMin){ //max vertical
				return r.y+r.height <= position;
			}else{ //min vertical
				return position <= r.y+r.height;
			}
		}else{
			if(maxInsteadOfMin){ //max horizontal
				return r.x+r.width <= position;
			}else{ //min horizontal
				return position <= r.x+r.width;
			}
		}
	}
	
	public void bounceSomePartsOnWall(LayeredZigzag z){
		final float position = this.position;
		for(CornerData layerOfCorners[] : z.corners){
			for(CornerData cd : layerOfCorners){
				if(verticalInsteadOfHorizontal){
					if(maxInsteadOfMin){ //max vertical
						if(position < cd.y){
							cd.speedY = -Math.abs(cd.speedY);
							//cd.y = position;
							//TODO float past = cd.y-position;
							//TODO cd.addToY -= 2*position;
						}
					}else{ //min vertical
						if(cd.y < position){
							cd.speedY = Math.abs(cd.speedY);
							//cd.y = position;
							//TODO float past = position-cd.y;
							//TODO cd.addToY += 2*position;
						}
					}
				}else{
					if(maxInsteadOfMin){ //max horizontal
						if(position < cd.x){
							cd.speedX = -Math.abs(cd.speedX);
							//cd.x = position;
							//TODO float past = cd.x-position;
							//TODO cd.addToX -= 2*position;
						}
					}else{ //min horizontal
						if(cd.x < position){
							cd.speedX = Math.abs(cd.speedX);
							//cd.x = position;
							//TODO float past = position-cd.x;
							//TODO cd.addToX += 2*position;
						}
					}
				}
			}
		}
	}

}
