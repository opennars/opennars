package ptrman.dificultyEnvironment.scriptAccessors;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.jbox2d.callbacks.RayCastCallback;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import ptrman.dificultyEnvironment.EntityDescriptor;
import ptrman.dificultyEnvironment.Environment;
import ptrman.dificultyEnvironment.physics.Physics2dBody;

/**
 *
 */
public class EnvironmentScriptingAccessor {
    // TODO< find nearest intersection to given point in the constructor >
    private class MyRaycastCallback implements RayCastCallback {
        public ArrayRealVector nearestPoint;


        @Override
        public float reportFixture(Fixture fixture, Vec2 point, Vec2 normal, float fraction) {
            nearestPoint = new ArrayRealVector(new double[]{point.x, point.y});

            return 0;
        }
    }

    private final Environment environment;

    public enum EnumShapeType {
        BOX
    }

    public EnvironmentScriptingAccessor(Environment entry) {
        this.environment = entry;
    }



    public EntityDescriptor createNewEntity(ArrayRealVector direction) {
        EntityDescriptor createdEntity = new EntityDescriptor();
        // createdEntity.direction = direction

        return createdEntity;
    }



    // must be called before using the 2d physics
    public void physics2dCreateWorld() {
        environment.physicsWorld2d = new World(new Vec2(0.0f, 0.0f));
    }

    public Physics2dBody physics2dCreateBody(boolean fixed, String shapeType, ArrayRealVector position, ArrayRealVector size, float radius, float density, float friction) {
        BodyDef bodyDefinition = new BodyDef();
        bodyDefinition.position.set(new Vec2((float) position.getDataRef()[0], (float) position.getDataRef()[1]));

        if( fixed ) {
            bodyDefinition.type = BodyType.STATIC;
        }
        else {
            bodyDefinition.type = BodyType.DYNAMIC;
        }

        Body body = environment.physicsWorld2d.createBody(bodyDefinition);

        PolygonShape polygonShape = new PolygonShape();

        if( shapeType.equals("BOX") ) {
            polygonShape.setAsBox((float)size.getDataRef()[0] * 0.5f, (float)size.getDataRef()[1] * 0.5f);
        }
        else {
            throw new InternalError();
        }

        if( fixed ) {
            body.createFixture(polygonShape, 0.0f);
        }
        else {
            FixtureDef fixture = new FixtureDef();
            fixture.shape = polygonShape;
            fixture.density = density;
            fixture.friction = friction;
            body.createFixture(fixture);
        }

        return new Physics2dBody(body);
    }

    public void physics2dSetLinearDamping(Physics2dBody body, float damping) {
        body.body.setLinearDamping(damping);
    }

    public void physics2dApplyForce(Physics2dBody body, ArrayRealVector force) {
        body.body.applyForce(new Vec2((float) force.getDataRef()[0], (float) force.getDataRef()[1]), body.body.getLocalCenter());
    }

    public ArrayRealVector physics2dNearestRaycast(ArrayRealVector a, ArrayRealVector direction, float distance) {
        MyRaycastCallback rayCastCallback = new MyRaycastCallback();

        final ArrayRealVector b = a.add(direction.mapMultiply(distance));

        environment.physicsWorld2d.raycast(rayCastCallback, new Vec2((float)a.getDataRef()[0], (float)a.getDataRef()[1]), new Vec2((float)b.getDataRef()[0], (float)b.getDataRef()[1]));

        return rayCastCallback.nearestPoint;
    }

}
