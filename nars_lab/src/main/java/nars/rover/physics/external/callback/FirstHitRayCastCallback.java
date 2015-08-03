package nars.rover.physics.external.callback;


import nars.rover.physics.external.DynamicEntity;
import org.jbox2d.callbacks.RayCastCallback;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;

/**
 * A RayCastCallback that grabs the first hit from a ray cast
 *
 * @author TranquilMarmot
 */
public class FirstHitRayCastCallback implements RayCastCallback {
	/** Pointer to first hit */
	private DynamicEntity hit;

	public float reportFixture(Fixture fixture, Vec2 point, Vec2 normal,
							   float fraction) {
		hit = (DynamicEntity)fixture.getUserData(); //PhysicsHelper.getDynamicEntity(fixture);

		// returning 0 here terminates the callback
		return 0;
	}

	/**
	 * @return First DynamicEntity hit in RayCast, null if no hits
	 */
	public DynamicEntity getHit(){
		return hit;
	}
}
