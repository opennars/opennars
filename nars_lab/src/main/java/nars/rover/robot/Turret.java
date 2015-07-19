package nars.rover.robot;


import nars.rover.util.Bodies;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;

import java.util.ArrayDeque;
import java.util.Deque;

public class Turret extends Robotic {

    public Turret(String id) {
        super(id);
    }

    @Override
    public RoboticMaterial getMaterial() {
        return new RoboticMaterial(this);
    }

    @Override
    protected Body newTorso() {

        return sim.create(new Vec2(), Bodies.rectangle(new Vec2(4, 1)), BodyType.DYNAMIC);
    }


    @Override
    public void step(int i) {
        super.step(i);

        if (Math.random() < 0.1f) {
            fireBullet();
        }
    }

    final int maxBullets = 16;
    final Deque<Body> bullets = new ArrayDeque(maxBullets);

    public void fireBullet(/*float ttl*/) {

//        final float now = sim.getTime();
//        Iterator<Body> ib = bullets.iterator();
//        while (ib.hasNext()) {
//            Body b = ib.next();
//            ((BulletData)b.getUserData()).diesAt
//
//        }

        final float speed = 50f;

        if (bullets.size() >= maxBullets) {
            sim.remove( bullets.removeFirst() );
        }

        Vec2 start = torso.getWorldPoint(new Vec2(4, 0));
        Body b = sim.create(start, Bodies.rectangle(0.2f, 0.4f), BodyType.DYNAMIC);

        float angle = torso.getAngle();
        Vec2 rayDir = new Vec2( (float)Math.cos(angle), (float)Math.sin(angle) );

        rayDir.mulLocal(speed);

        b.applyForce(rayDir, new Vec2(0,0));

        //float diesAt = now + ttl;
        //b.setUserData(new BulletData(diesAt));
        bullets.add(b);

//        float angle = (i / (float)numRays) * 360 * DEGTORAD;
//        b2Vec2 rayDir( sinf(angle), cosf(angle) );
//
//        b2BodyDef bd;
//        bd.type = b2_dynamicBody;
//        bd.fixedRotation = true; // rotation not necessary
//        bd.bullet = true; // prevent tunneling at high speed
//        bd.linearDamping = 10; // drag due to moving through air
//        bd.gravityScale = 0; // ignore gravity
//        bd.position = center; // start at blast center
//        bd.linearVelocity = blastPower * rayDir;
//        b2Body* body = m_world->CreateBody( &bd );
//
//        b2CircleShape circleShape;
//        circleShape.m_radius = 0.05; // very small
//
//        b2FixtureDef fd;
//        fd.shape = &circleShape;
//        fd.density = 60 / (float)numRays; // very high - shared across all particles
//        fd.friction = 0; // friction not necessary
//        fd.restitution = 0.99f; // high restitution to reflect off obstacles
//        fd.filter.groupIndex = -1; // particles should not collide with each other
//        body->CreateFixture( &fd );
    }

    public static class BulletData {
        private final float diesAt;

        public BulletData(float diesAt) {
            this.diesAt = diesAt;
        }
    }

}
