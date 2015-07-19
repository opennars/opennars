package nars.rover.util;

import org.jbox2d.callbacks.QueryCallback;
import org.jbox2d.collision.AABB;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;

/**
 * http://www.iforce2d.net/b2dtut/explosions
 */
public class Explosion {

    public static void applyBlastImpulse(Body body, Vec2 blastCenter, Vec2 applyPoint, float blastPower) {
        Vec2 blastDir = applyPoint.sub( blastCenter );
        float distance = blastDir.normalize();
        //ignore bodies exactly at the blast point - blast direction is undefined
        if ( distance == 0 )
            return;
        float invDistance = 1 / distance;
        float impulseMag = blastPower * invDistance * invDistance;
        body.applyLinearImpulse( blastDir.mul(impulseMag), applyPoint );
    }

    public static void explodeBlastRadius(World world, Vec2 center, float blastRadius, float blastPower) {

        final float m_blastRadiusSq = blastRadius*blastRadius;

        //find all bodies with fixtures in blast radius AABB
        QueryCallback queryCallback = new QueryCallback() {
            @Override
            public boolean reportFixture(Fixture fixture) {
                Body body = fixture.getBody();
                Vec2 bodyCom = body.getWorldCenter();

                //ignore bodies outside the blast range
                if ((bodyCom.sub(center)).lengthSquared() < m_blastRadiusSq) {
                    applyBlastImpulse(body, center, bodyCom, blastPower);
                    return true;
                }
                return false;
            }
        };

        world.queryAABB(queryCallback, new AABB(
                center.sub(new Vec2(blastRadius, blastRadius)) ,
                center.add(new Vec2(blastRadius, blastRadius))
        ));

    }

//    public void explode() {
//
//        for (int i = 0; i < numRays; i++) {
//            float angle = (i / (float)numRays) * 360 * DEGTORAD;
//            b2Vec2 rayDir( sinf(angle), cosf(angle) );
//            b2Vec2 rayEnd = center + blastRadius * rayDir;
//
//            //check what this ray hits
//            RayCastClosestCallback callback;//basic callback to record body and hit point
//            m_world->RayCast(&callback, center, rayEnd);
//            if ( callback.m_body )
//                applyBlastImpulse(callback.body, center, callback.point, (m_blastPower / (float)numRays));
//        }
//
//    }
}
