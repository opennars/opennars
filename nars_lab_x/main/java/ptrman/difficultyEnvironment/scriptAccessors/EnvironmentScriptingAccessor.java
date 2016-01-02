package ptrman.difficultyEnvironment.scriptAccessors;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.jbox2d.callbacks.RayCastCallback;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import ptrman.difficultyEnvironment.EntityDescriptor;
import ptrman.difficultyEnvironment.Environment;
import ptrman.difficultyEnvironment.physics.Physics2dBody;

import java.util.List;

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

    public Physics2dBody physics2dCreateBodyWithShape(boolean fixed, ArrayRealVector position, List verticesPoints, float linearDamping, float angularDamping,    float mass,    float restitution, float friction) {
        PolygonShape shape = new PolygonShape();

        Vec2[] vertices = convertVerticesFromArrayRealVectorGenericList(verticesPoints);
        shape.set(vertices, vertices.length);
        //shape.m_centroid.set(bodyDef.position);

        BodyDef bodyDefinition = new BodyDef();
        bodyDefinition.linearDamping = linearDamping;
        bodyDefinition.angularDamping = angularDamping;

        if( fixed ) {
            bodyDefinition.type = BodyType.STATIC;
        }
        else {
            bodyDefinition.type = BodyType.DYNAMIC;
        }

        bodyDefinition.position.set((float)position.getDataRef()[0], (float)position.getDataRef()[1]);

        Body body = environment.physicsWorld2d.createBody(bodyDefinition);
        Fixture fixture = body.createFixture(shape, mass);
        fixture.setRestitution(restitution);
        fixture.setFriction(friction);

        return new Physics2dBody(body);
    }

    private static Vec2[] convertVerticesFromArrayRealVectorGenericList(List verticesPoints) {
        Vec2[] vectorArray = new Vec2[verticesPoints.size()];

        for( int i = 0; i < verticesPoints.size(); i++ ) {
            if( !(verticesPoints.get(i) instanceof ArrayRealVector) ) {
                throw new RuntimeException("element is a RealVector");
            }

            ArrayRealVector realVector = (ArrayRealVector)verticesPoints.get(i);
            vectorArray[i] = new Vec2((float)realVector.getDataRef()[0], (float)realVector.getDataRef()[1]);
        }

        return vectorArray;
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
