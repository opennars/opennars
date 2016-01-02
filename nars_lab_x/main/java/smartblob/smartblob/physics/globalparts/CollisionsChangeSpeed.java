/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package smartblob.smartblob.physics.globalparts;

import smartblob.Smartblob;
import smartblob.SmartblobUtil;
import smartblob.blobs.layeredzigzag.CornerData;
import smartblob.blobs.layeredzigzag.LayeredZigzag;
import smartblob.blobs.layeredzigzag.TriData;
import smartblob.smartblob.physics.GlobalChangeSpeed;
import smartblob.smartblob.physics.SmartblobSim;

/** First all pairs of Smartblob.boundingRectangle() are checked for possible collision.
Then of those which may collide, more detailed checks are done. For all outer point
found to collide past a surface line, speeds of the 2 points on those lines are updated.
Positions are not updated, so this can be done in any order for the same result.
Actually order may affect it a little since speeds are kept the same in magnitude
along certain directions but set away from eachother.
*/
public class CollisionsChangeSpeed implements GlobalChangeSpeed {
	
	public void globalChangeSpeed(SmartblobSim sim, float secondsSinceLastCall){
		Smartblob blobArray[];
		synchronized(sim.smartblobs){
			blobArray = sim.smartblobs.toArray(new Smartblob[0]);
		}
		for(int i=0; i<blobArray.length-1; i++){
			for(int j=i+1; j<blobArray.length; j++){ //for all pairs
				nextPair(blobArray[i], blobArray[j]);
			}
		}
	}
	
	public void nextPair(Smartblob a, Smartblob b){
		Rectangle ra = a.boundingRectangle();
		Rectangle rb = b.boundingRectangle();
		if(ra.intersects(rb)){
			//System.out.println("intersection: "+ra.intersection(rb));
			if(a instanceof LayeredZigzag && b instanceof LayeredZigzag){
				rectanglesIntersect((LayeredZigzag)a, (LayeredZigzag)b);
			}else{
				System.out.println("At least 1 of 2 "+Smartblob.class.getName()
					+" type unknown: "+a.getClass().getName()
					+" and "+b.getClass().getName());
			}
		}
	}
	
	
	/** Their bounding rectangles are known to intersect. The smartblobs may intersect. */
	public void rectanglesIntersect(LayeredZigzag a, LayeredZigzag b){
		//Consider only points in the intersection of their bounding rectangles.
		//TODO I'd like to only consider lines that intersect those rectangles,
		//but its easier for now to just check them all. Its small quantity.
		Rectangle intersect = a.boundingRectangle().intersection(b.boundingRectangle());
		handleIntersectsBetweenPointAndWeightedSumOfLine(intersect, a, b);
		handleIntersectsBetweenPointAndWeightedSumOfLine(intersect, b, a);
	}
	
	/** Rectangle intersect is the intersection of the 2 smartblobs bounding rectangles.
	TODO This puts equal force between point and weightedSum between 2 line ends
	depending on, closest intersection point on the line (or distances to each end?),
	and spreading the opposite force between those 2 ends.
	*/
	protected void handleIntersectsBetweenPointAndWeightedSumOfLine(Rectangle intersect, LayeredZigzag myPoints, LayeredZigzag myLines){
		int lastLayer = myPoints.layers-1;
		float getYX[] = new float[2];
		for(int p=0; p<myPoints.layerSize; p++){
			CornerData point = myPoints.corners[lastLayer][p];
			if(intersect.contains(point.x, point.y)){
				//may have crossed an outer line in the other smartblob
				TriData t = myLines.findCollision(point.y, point.x);
				if(t != null){ //collision found
					
					//Find point on infinite line its closest to, and absVal part of speed thats toward it
					SmartblobUtil.getClosestPointToInfiniteLine(getYX, t, point.y, point.x);
					//vector from [closest point on infinite line] to point.
					float vectorY = point.y-getYX[0];
					float vectorX = point.x-getYX[1];
					float vectorLen = (float)Math.sqrt(vectorY*vectorY + vectorX*vectorX);
					if(vectorLen == 0){
						//Dont bounce if its only touching the line. Wait until crosses.
						continue;
					}
					
					CornerData a = t.adjacentCorners[0], b = t.adjacentCorners[1];
					
					float aDy = getYX[0]-a.y, aDx = getYX[1]-a.x; 
					float distanceA = (float)Math.sqrt(aDy*aDy + aDx*aDx);
					float bDy = getYX[0]-b.y, bDx = getYX[1]-b.x; 
					float distanceB = (float)Math.sqrt(bDy*bDy + bDx*bDx);
					//TODO These fractions may be slightly above 1 and the other negative, but usually normal fractions.
					//By distance, they reverse their behavior when it goes past either end of line,
					//but that only happens when border of smartblob has negative curve, unlike a circle.
					float distanceSum = distanceA+distanceB;
					float fractionLineEndA = distanceA/distanceSum;
					float fractionLineEndB = 1-fractionLineEndA;
					
					//Speed of pointOnLine is a weightedSum of speeds at the line's ends.
					//TODO this is a little inaccurate if the point on the line segment is past either end.
					float speedYOfPointOnLine = a.speedY*fractionLineEndA + fractionLineEndB*b.speedY;
					float speedXOfPointOnLine = a.speedX*fractionLineEndA + fractionLineEndB*b.speedX;
					
					float ddy = point.speedY-speedYOfPointOnLine;
					float ddx = point.speedX-speedXOfPointOnLine;
					
					float normVY = vectorY/vectorLen;
					float normVX = vectorX/vectorLen;
					//"If vector is in same direction as third corner, flip it.
					//Since we already know theres a collision, always flip here.
					normVY = -normVY;
					normVX = -normVX;
					//Now (normVY,normVX) is length 1 and point outward from smartblob.
					//Get the part of speed vector aligned with normVector, then flip it.
					//TODO should that speed be difference between the 2 smartblobs at that point,
					//or do them separately? Doing them separately, at least for now.
					
					//float speedDotNorm = point.speedY*normVY + point.speedX*normVX;
					float speedDotNorm = ddy*normVY + ddx*normVX;
					
					//If speedDotNorm is positive, the smartblob is moving away (if the line was at rest)
					if(speedDotNorm < 0){
						//Like the BounceOnSimpleWall code, use absVal.
						//partOfSpeedVec is the part aligned with normVec (TODO opposite?)
						float partOfSpeedVecY = normVY*speedDotNorm;
						float partOfSpeedVecX = normVX*speedDotNorm;
						float addToSpeedY = -2*partOfSpeedVecY;
						float addToSpeedX = -2*partOfSpeedVecX;
						float addToEachSpeedY = addToSpeedY/2;
						float addToEachSpeedX = addToSpeedX/2;
						//point.speedY -= 2*partOfSpeedVecY;
						//point.speedX -= 2*partOfSpeedVecX;
						
						point.speedY += addToEachSpeedY;
						a.speedY -= fractionLineEndA*addToEachSpeedY;
						b.speedY -= fractionLineEndB*addToEachSpeedY;
						
						point.speedX += addToEachSpeedX;
						a.speedX -= fractionLineEndA*addToEachSpeedX;
						b.speedX -= fractionLineEndB*addToEachSpeedX;
						
						
						//TODO equal and opposite force, between this and weightedSum between 2 ends of line (even if it hangs past ends, just do something like 1.1*endA - .1*endB
						//"TODO use fractionLineEndA and fractionLineEndB"
						//TODO
					}
					
				}
				/*LineData lineData = myLines.findCollision(point.y, point.x);
				if(lineData != null){ //collision found
					throw new RuntimeException("TODO");
				}
				*/
			}
		}
	}
	
	/** Rectangle intersect is the intersection of the 2 smartblobs bounding rectangles.
	This only bounces the point off the line but doesnt bounce the line oppositely,
	so this is old code but still works unusually well since both smartblobs have such points
	and lines that they bounce, mostly symmetricly, on eachother,
	but it visibly doesnt work as equal and opposite forces too often.
	*/
	protected void handleIntersectsOneWay(Rectangle intersect, LayeredZigzag myPoints, LayeredZigzag myLines){
		int lastLayer = myPoints.layers-1;
		float getYX[] = new float[2];
		for(int p=0; p<myPoints.layerSize; p++){
			CornerData point = myPoints.corners[lastLayer][p];
			if(intersect.contains(point.x, point.y)){
				//may have crossed an outer line in the other smartblob
				TriData t = myLines.findCollision(point.y, point.x);
				if(t != null){ //collision found
					//Find point on infinite line its closest to, and absVal part of speed thats toward it
					SmartblobUtil.getClosestPointToInfiniteLine(getYX, t, point.y, point.x);
					//vector from [closest point on infinite line] to point.
					float vectorY = point.y-getYX[0];
					float vectorX = point.x-getYX[1];
					float vectorLen = (float)Math.sqrt(vectorY*vectorY + vectorX*vectorX);
					if(vectorLen == 0){
						//Dont bounce if its only touching the line. Wait until crosses.
						continue;
					}
					float normVY = vectorY/vectorLen;
					float normVX = vectorX/vectorLen;
					//"If vector is in same direction as third corner, flip it.
					//Since we already know theres a collision, always flip here.
					normVY = -normVY;
					normVX = -normVX;
					//Now (normVY,normVX) is length 1 and point outward from smartblob.
					//Get the part of speed vector aligned with normVector, then flip it.
					//TODO should that speed be difference between the 2 smartblobs at that point,
					//or do them separately? Doing them separately, at least for now.
					float speedDotNorm = point.speedY*normVY + point.speedX*normVX;
					//If speedDotNorm is positive, the smartblob is moving away (if the line was at rest)
					if(speedDotNorm < 0){
						//Like the BounceOnSimpleWall code, use absVal.
						//partOfSpeedVec is the part aligned with normVec (TODO opposite?)
						float partOfSpeedVecY = normVY*speedDotNorm;
						float partOfSpeedVecX = normVX*speedDotNorm;
						point.speedY -= 2*partOfSpeedVecY;
						point.speedX -= 2*partOfSpeedVecX;
						//TODO equal and opposite force, between this and weightedSum between 2 ends of line (even if it hangs past ends, just do something like 1.1*endA - .1*endB
						//TODO
					}
				}
				/*LineData lineData = myLines.findCollision(point.y, point.x);
				if(lineData != null){ //collision found
					throw new RuntimeException("TODO");
				}
				*/
			}
		}
	}

}
