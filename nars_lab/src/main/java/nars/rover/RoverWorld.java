/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.rover;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;

/**
 *
 * @author me
 */
abstract public class RoverWorld {
    protected PhysicsModel p;


    public void init(PhysicsModel p) {
        this.p = p;
    }

    
    public void addFood(float w, float h, float minSize, float maxSize, float mass, Material m) {
        float x = (float) Math.random() * w - w / 2f;
        float y = (float) Math.random() * h - h / 2f;
        float bw = (float) (minSize + Math.random() * (maxSize - minSize));
        float bh = (float) (minSize + Math.random() * (maxSize - minSize));
        float a = 0;
        Body b = addBlock(x * 2.0f, y * 2.0f, bw, bh, a, mass);
        b.applyAngularImpulse((float) Math.random());
        b.setUserData(m);
    }

    public Body addWall(float x, float y, float w, float h, float a) {
        Body b = addBlock(x, y, w, h, a, 0);
        b.setUserData(Material.wall);
        return b;
    }

    public Body addBlock(float x, float y, float w, float h, float a, float mass) {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(w, h);
        BodyDef bd = new BodyDef();
        if (mass != 0) {
            bd.linearDamping=(0.95f);
            bd.angularDamping=(0.8f);
            bd.type = BodyType.DYNAMIC;
        } else {
            bd.type = BodyType.STATIC;
        }
        bd.position.set(x, y);
        Body body = p.getWorld().createBody(bd);
        Fixture fd = body.createFixture(shape, mass);
        fd.setRestitution(1f);
        return body;
    }
    
}
