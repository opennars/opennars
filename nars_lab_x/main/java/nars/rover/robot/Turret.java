package nars.rover.robot;


import nars.rover.physics.gl.JoglAbstractDraw;
import nars.rover.util.Bodies;
import nars.rover.util.Explosion;
import nars.util.data.random.XORShiftRandom;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.contacts.Contact;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Turret extends Robotic {

    final static Random rng = new XORShiftRandom();

    final float fireProbability = 0.005f;

    public Turret(String id) {
        super(id);
    }

    @Override
    public RoboticMaterial getMaterial() {

        return new RoboticMaterial(this) {

            @Override public void before(Body b, JoglAbstractDraw d, float time) {
                super.before(b, d, time);

                if (!explosions.isEmpty()) {
                    Iterator<BulletData> ii = explosions.iterator();
                    while (ii.hasNext()) {
                        BulletData bd = ii.next();
                        if (bd.explosionTTL-- <= 0)
                            ii.remove();


                        d.drawSolidCircle(bd.getCenter(), bd.explosionTTL/8 +  rng.nextFloat() * 4, new Vec2(),
                                new Color3f(1 - rng.nextFloat()/3f,
                                            0.8f - rng.nextFloat()/3f,
                                            0f));
                    }
                }
            }
        };
    }

    @Override
    protected Body newTorso() {

        return sim.create(new Vec2(), Bodies.rectangle(new Vec2(4, 1)), BodyType.DYNAMIC);
    }



    @Override
    public void step(int i) {
        super.step(i);


        for (Body b : removedBullets) {
            bullets.remove(b);
            sim.remove(b);

            final BulletData bd = (BulletData) b.getUserData();
            bd.explode();
            explosions.add(bd);
        }
        removedBullets.clear();

        if (Math.random() < fireProbability) {
            fireBullet();
        }
    }

    final int maxBullets = 16;
    final Deque<Body> bullets = new ArrayDeque(maxBullets);
    final Deque<Body> removedBullets = new ArrayDeque(maxBullets);
    final Collection<BulletData> explosions = new ConcurrentLinkedQueue();

    public void fireBullet(/*float ttl*/) {

//        final float now = sim.getTime();
//        Iterator<Body> ib = bullets.iterator();
//        while (ib.hasNext()) {
//            Body b = ib.next();
//            ((BulletData)b.getUserData()).diesAt
//
//        }

        final float speed = 100f;


        if (bullets.size() >= maxBullets) {
            sim.remove( bullets.removeFirst() );
        }


        Vec2 start = torso.getWorldPoint(new Vec2(6.5f, 0));
        Body b = sim.create(start, Bodies.rectangle(0.4f, 0.6f), BodyType.DYNAMIC);
        b.m_mass= 0.05f;

        float angle = torso.getAngle();
        Vec2 rayDir = new Vec2( (float)Math.cos(angle), (float)Math.sin(angle) );
        rayDir.mulLocal(speed);


        //float diesAt = now + ttl;
        b.setUserData(new BulletData(b, 0));
        bullets.add(b);

        b.applyForce(rayDir, new Vec2(0,0));

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

    public class BulletData implements Collidable {
        private final float diesAt;
        private final Body bullet;
        public int explosionTTL;


        public BulletData(Body b, float diesAt) {
            this.bullet = b;
            this.diesAt = diesAt;
        }

        public void explode() {
            //System.out.println("expldoe " + bullet.getWorldCenter());
            float force = 175f;
            Explosion.explodeBlastRadius(bullet.getWorld(), bullet.getWorldCenter(), 160f,force);
            explosionTTL = (int)force/2;
        }

        public Vec2 getCenter() { return bullet.getWorldCenter(); }

        @Override public void onCollision(Contact c) {
            //System.out.println(bullet + " collided");
            removedBullets.add(bullet);
        }
    }

}
