/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.narclear;

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
    private final PhysicsModel p;

    public RoverWorld(PhysicsModel p) {
        super();
        
        this.p = p;
    }

    
    public void addFood(float w, float h) {
        float x = (float) Math.random() * w - w / 2f;
        float y = (float) Math.random() * h - h / 2f;
        float minSize = 0.25f;
        float maxSize = 0.75f;
        float bw = (float) (minSize + Math.random() * (maxSize - minSize));
        float bh = (float) (minSize + Math.random() * (maxSize - minSize));
        float a = 0;
        float mass = 0.25f;
        Body b = addBlock(x * 2.0f, y * 2.0f, bw, bh, a, mass);
        b.applyAngularImpulse((float) Math.random());
        b.setUserData(Rover2.Material.Food);
    }

    public Body addWall(float x, float y, float w, float h, float a) {
        Body b = addBlock(x, y, w, h, a, 0);
        b.setUserData(Rover2.Material.Wall);
        return b;
    }

    public Body addBlock(float x, float y, float w, float h, float a, float mass) {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(w, h);
        BodyDef bd = new BodyDef();
        if (mass != 0) {
            bd.setLinearDamping(0.95f);
            bd.setAngularDamping(0.8f);
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
