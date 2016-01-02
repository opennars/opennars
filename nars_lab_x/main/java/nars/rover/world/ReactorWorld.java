/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.rover.world;

import nars.rover.Material;
import nars.rover.RoverWorld;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;

/**
 * @author me
 */
public class ReactorWorld extends RoverWorld {
    private final float w;
    private final float h;

    public ReactorWorld(int numFood, float w, float h) {
        super();
        this.w = w;
        this.h = h;

        p.getWorld().setAllowSleep(false);
        p.getWorld().setGravity(new Vec2(0, 9));

        float foodSpawnR = w / 1.5f;
        for (int i = 0; i < numFood; i++) {
            float minSize = 0.2f;
            float maxSize = 1.0f;
            float mass = 4.0f;
            addFood(foodSpawnR, foodSpawnR, minSize, maxSize, mass, Material.poison);
        }
        float wt = 1f;
        addWall(0, h, w, wt, 0);
        addWall(-w, 0, wt, h, 0);
        addWall(w, 0, wt, h, 0);
        addWall(0, -h, w, wt, 0);

        fireFuktonium(16);

        for (int i = 0; i < 16; i++) {
            fireExecutiveCasket();
        }
    }

    protected void fireFuktonium(int count) {
        for (int i = 0; i < count; i++)
            fireFuktonium();
    }

    protected void fireFuktonium() {
        fireDebris(1.0f, 0.2f);
    }
    protected void fireExecutiveCasket() {
        fireDebris(8f, 3f);
    }
    /**
     * fuel rod components mixed with plutonium ash mutated into a supernatural buckyball atomic compound
     */
    protected void fireDebris(float w, float h) {



        BodyDef bd = new BodyDef();
        bd.type = BodyType.DYNAMIC;
        bd.position.set(0.0f, 4.0f);

        PolygonShape box = new PolygonShape();
        box.setAsBox(w, h);

        Body m_body = p.getWorld().createBody(bd);
        m_body.createFixture(box, 1.0f);

        box.setAsBox(0.25f, 0.25f);

        // m_x = RandomFloat(-1.0f, 1.0f);
        float m_x = -0.06530577f;
        bd.position.set(m_x, 10.0f);
        bd.bullet = true;

        Body m_bullet = p.getWorld().createBody(bd);
        m_bullet.createFixture(box, 100.0f);

        m_bullet.setLinearVelocity(new Vec2(0.0f, -50.0f));


        m_body.setTransform(new Vec2(0.0f, 4.0f), 0.0f);
        m_body.setLinearVelocity(new Vec2());
        m_body.setAngularVelocity(0.0f);

        m_x = MathUtils.randomFloat(-1.0f, 1.0f);
        m_bullet.setTransform(new Vec2(m_x, 10.0f), 0.0f);
        m_bullet.setLinearVelocity(new Vec2(0.0f, -50.0f));
        m_bullet.setAngularVelocity(0.0f);
    }

}
