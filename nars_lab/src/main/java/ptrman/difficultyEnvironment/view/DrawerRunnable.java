package ptrman.difficultyEnvironment.view;

import nars.rover.physics.ContactPoint;
import nars.rover.physics.TestbedPanel;
import nars.rover.physics.TestbedSettings;
import org.jbox2d.callbacks.DebugDraw;
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.Collision;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.joints.MouseJoint;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public class DrawerRunnable implements Runnable {
    public DrawerRunnable() {
        identity.setIdentity();
        for (int i = 0; i < MAX_CONTACT_POINTS; i++) {
            points[i] = new ContactPoint();
        }

        //camera = new PhysicsCamera(getDefaultCameraPos(), getDefaultCameraScale(), ZOOM_SCALE_DIFF);
    }

    @Override
    public void run() {
        // TODO< need to be set from outside >
        float timeStep = 0.0f;

        if( !panel.render() ) {
            return;
        }

        if (mouseTracing && mouseJoint == null) {
            final float delay = 0.1f;
            acceleration.x = 2 / delay * (1 / delay * (mouseWorld.x - mouseTracerPosition.x) - mouseTracerVelocity.x);
            acceleration.y = 2 / delay * (1 / delay * (mouseWorld.y - mouseTracerPosition.y) - mouseTracerVelocity.y);
            mouseTracerVelocity.x += timeStep * acceleration.x;
            mouseTracerVelocity.y += timeStep * acceleration.y;
            mouseTracerPosition.x += timeStep * mouseTracerVelocity.x;
            mouseTracerPosition.y += timeStep * mouseTracerVelocity.y;
            pshape.m_p.set(mouseTracerPosition);
            pshape.m_radius = 2;
            pshape.computeAABB(paabb, identity, 0);
        }

        if (mouseJoint != null) {
            mouseJoint.getAnchorB(p1);
            Vec2 p2 = mouseJoint.getTarget();

            draw.drawSegment(p1, p2, mouseColor);
        }

        if (false /*settings.getSetting(TestbedSettings.DrawContactPoints).enabled*/) {
            final float k_impulseScale = 0.1f;
            final float axisScale = 0.3f;

            // TODO< need to be get from DrawingContactListener >
            int pointCount = 0;

            for (int i = 0; i < pointCount; i++) {

                ContactPoint point = points[i];

                if (point.state == Collision.PointState.ADD_STATE) {
                    draw.drawPoint(point.position, 10f, color1);
                } else if (point.state == Collision.PointState.PERSIST_STATE) {
                    draw.drawPoint(point.position, 5f, color2);
                }

                if (settings.getSetting(TestbedSettings.DrawContactNormals).enabled) {
                    p1.set(point.position);
                    p2.set(point.normal).mulLocal(axisScale).addLocal(p1);
                    draw.drawSegment(p1, p2, color3);

                } else if (settings.getSetting(TestbedSettings.DrawContactImpulses).enabled) {
                    p1.set(point.position);
                    p2.set(point.normal).mulLocal(k_impulseScale).mulLocal(point.normalImpulse).addLocal(p1);
                    draw.drawSegment(p1, p2, color5);
                }

                if (settings.getSetting(TestbedSettings.DrawFrictionImpulses).enabled) {
                    Vec2.crossToOutUnsafe(point.normal, 1, tangent);
                    p1.set(point.position);
                    p2.set(tangent).mulLocal(k_impulseScale).mulLocal(point.tangentImpulse).addLocal(p1);
                    draw.drawSegment(p1, p2, color5);
                }
            }
        }

        panel.paintScreen();

        drawingQueued.set(false);
    }

    AtomicBoolean drawingQueued = new AtomicBoolean(false);

    public TestbedPanel panel;

    // mouse physics
    private MouseJoint mouseJoint;
    protected boolean mouseTracing;

    final Vec2 p1 = new Vec2();
    final Vec2 p2 = new Vec2();

    private final Vec2 mouseWorld = new Vec2();
    private Vec2 mouseTracerPosition = new Vec2();
    private Vec2 mouseTracerVelocity = new Vec2();

    private final Vec2 acceleration = new Vec2();

    private final Vec2 tangent = new Vec2();

    public static final int MAX_CONTACT_POINTS = 4048;
    public final ContactPoint[] points = new ContactPoint[MAX_CONTACT_POINTS];


    private TestbedSettings settings;

    private DebugDraw draw;

    // colors
    private final Color3f color1 = new Color3f(.3f, .95f, .3f);
    private final Color3f color2 = new Color3f(.3f, .3f, .95f);
    private final Color3f color3 = new Color3f(.9f, .9f, .9f);
    private final Color3f color4 = new Color3f(.6f, .61f, 1);
    private final Color3f color5 = new Color3f(.9f, .9f, .3f);
    private final Color3f mouseColor = new Color3f(0f, 1f, 0f);



    // unknown

    private final CircleShape pshape = new CircleShape();

    private final AABB paabb = new AABB();

    // misc
    private final Transform identity = new Transform();
}
