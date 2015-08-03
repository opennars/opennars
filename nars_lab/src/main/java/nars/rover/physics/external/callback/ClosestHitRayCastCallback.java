package nars.rover.physics.external.callback;


import nars.rover.physics.external.DynamicEntity;
import org.jbox2d.callbacks.RayCastCallback;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;

/**
 * A callback to get the closest hit from a ray trace
 * 
 * @author TranquilMarmot
 */
public class ClosestHitRayCastCallback implements RayCastCallback {
	/** Where the ray cast is coming from */
	private Vec2 origin;
	
	/** Used to keep track of the closest hit */
	private float closestDist;
	
	/** Pointer to closest hit */
	private DynamicEntity closest;
	
	/** Details about hit */
	private Vec2 normalOnClosest, pointOnClosest;
	
	/**
	 * Create a new ClosestCallback
	 * @param origin Origin of ray cast
	 */
	public ClosestHitRayCastCallback(Vec2 origin){
		this.origin = origin;
		closestDist = Float.MAX_VALUE;
		/*
		 * According to the libgdx docs, "The Vec2 instances
		 * passed to the callback will be reused for future calls
		 * so make a copy of them!"
		 * So we set the values of these whenever we get a closer result
		 * rather than just setting the pointers
		 */
		normalOnClosest = new Vec2(0.0f, 0.0f);
		pointOnClosest = new Vec2(0.0f, 0.0f);
	}
	
	public float reportFixture(Fixture fixture, Vec2 point, Vec2 normal,
								  float fraction) {
		// check if this hit is any closer than the closest one
		float dist = fixture.getBody().getPosition().sub(origin).lengthSquared();
		if(dist <= closestDist*closestDist){
			closestDist = dist;
			closest = (DynamicEntity) fixture.getUserData();// PhysicsHelper.getDynamicEntity(fixture);
			pointOnClosest.set(point);
			normalOnClosest.set(normal);
		}
		
		// returning 1 here continues on to the next hit in the ray cast
		return 1;
	}
	
	/** @return The closest DynamicEntity to the origin of the ray cast */
	public DynamicEntity getClosestHit(){ return closest; }
	
	/** @return Normal of hit on closest entity */
	public Vec2 normalOnClosest(){ return normalOnClosest; }
	
	/** @return Point of hit on closest entity */
	public Vec2 pointOnClosest(){ return pointOnClosest; }
	
	/**
	 * Reset the callback to be used again
	 * NOTE: This must be called every time this
	 * callback gets re-used!!!
	 * @param newOrigin New origin location
	 */
	public void reset(Vec2 newOrigin){
		this.origin.set(newOrigin);
		closest = null;
		closestDist = Float.MAX_VALUE;
	}

}
