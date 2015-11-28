//package nars.rover.physics.external.callback;
//
//import com.badlogic.gdx.math.Vec2;
//import com.badlogic.gdx.physics.box2d.Fixture;
//import com.badlogic.gdx.physics.box2d.RayCastCallback;
//
///**
// * Counts the number of hits a ray cast has
// *
// * @author TranquilMarmot
// */
//public class HitCountRayCastCallback implements RayCastCallback {
//	private int hitCount;
//
//	public HitCountRayCastCallback() {
//		hitCount = 0;
//	}
//
//	@Override
//	public float reportRayFixture(Fixture fixture, Vec2 point,
//			Vec2 normal, float fraction) {
//		hitCount++;
//		// NOTE if you ever want to use these you have to make copies since box2d reuses the vector objects
//		//DynamicEntity ent = PhysicsHelper.getDynamicEntity(fixture);
//		//Vec2 p = new Vec2(point);
//		//Vec2 n = new Vec2(normal);
//
//		// return 1 to continue through all hits
//		return 1;
//	}
//
//	public int hitCount(){
//		return hitCount;
//	}
//
//	public void reset(){
//		hitCount = 0;
//	}
//}
