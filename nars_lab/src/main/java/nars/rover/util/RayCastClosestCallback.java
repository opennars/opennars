package nars.rover.util;

import org.jbox2d.callbacks.RayCastCallback;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;

/**
 * Created by me on 7/19/15.
 */
public class RayCastClosestCallback implements RayCastCallback {


    public boolean m_hit;
    public Vec2 m_point;
    Vec2 m_normal;
    public Body body;

    public void init() {
        m_hit = false;
    }

    public float reportFixture(Fixture fixture, Vec2 point, Vec2 normal, float fraction) {
        Body body = fixture.getBody();
        //Object userData = body.getUserData();
        this.body = body;

//        if (userData != null) {
//          int index = (Integer) userData;
//          if (index == 0) {
//            // filter
//            return -1f;
//          }
//        }

        m_hit = true;
        m_point = point;
        m_normal = normal;

        return fraction;
    }

}


