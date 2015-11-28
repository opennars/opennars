package ptrman.difficultyEnvironment.interactionComponents;

import org.jbox2d.callbacks.RayCastCallback;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;
import ptrman.difficultyEnvironment.EntityDescriptor;
import ptrman.difficultyEnvironment.JavascriptDescriptor;

/**
 * A collection of rays which shoots from a position and angle relative to the position of the physics 2d body
 */
public class TwoDimensionalRaysComponent implements IComponent {
    public float raysLength = 0.0f;
    public float raysStartDistance = 0.0f;
    public float raysSpreadAngleInRadiants = 0.0f; // from side to side
    public float[] hitDistanceFractions;

    private static class RayCastClosestCallback implements RayCastCallback {
        public boolean hittedAnything = false;

        public Vec2 normalizedDirection; // must be set from outside
        public Vec2 globalStartPosition;

        public float closestHitDistance = Float.MAX_VALUE;

        public void reset() {
            closestHitDistance = Float.MAX_VALUE;
            hittedAnything = false;
        }

        public float reportFixture(Fixture fixture, Vec2 point, Vec2 normal, float fraction) {
            final Vec2 relativeDifference = point.sub(globalStartPosition);
            final float distance = Vec2.dot(relativeDifference, normalizedDirection);

            // we ignore any points/collisions which don't make sense
            if( distance < 0.0f ) {
                return 1.0f; // continue as usual
            }

            if( distance < closestHitDistance ) {
                closestHitDistance = distance;

                // TODO< save other needed things >
            }

            hittedAnything = true;

            return 1.0f; // continue as usual
        }
    }

    public TwoDimensionalRaysComponent(float raysStartDistance, float raysLength, int numberOfRays, float raysSpreadAngleInRadiants) {
        this.raysLength = raysLength;
        this.raysStartDistance = raysStartDistance;
        this.raysSpreadAngleInRadiants = raysSpreadAngleInRadiants;

        hitDistanceFractions = new float[numberOfRays];
    }


    @Override
    public void frameInteraction(JavascriptDescriptor javascriptDescriptor, EntityDescriptor entityDescriptor, float timedelta) {
        if( entityDescriptor.physics2dBody == null ) {
            return;
        }

        final float halfRaysSpreadAngleInRadiants = raysSpreadAngleInRadiants / 2.0f;

        for( int rayIndex = 0; rayIndex < getNumberOfRays(); rayIndex++ ) {
            final float rayIndexAsFraction = (float)rayIndex / (float)getNumberOfRays();
            final float rayRelativeAngleInRadiants = -halfRaysSpreadAngleInRadiants + rayIndexAsFraction * raysSpreadAngleInRadiants;

            final Vec2 rayDirection = getNormalizedDirectionOfAngleInRadiants(rayRelativeAngleInRadiants);

            final Vec2 relativeRayStartPosition = rayDirection.mul(raysStartDistance);
            final Vec2 relativeRayEndPosition = rayDirection.mul(raysStartDistance+raysLength);

            final Vec2 rayGlobalStartPosition = entityDescriptor.physics2dBody.body.getWorldPoint(relativeRayStartPosition);
            final Vec2 rayGlobalStopPosition = entityDescriptor.physics2dBody.body.getWorldPoint(relativeRayEndPosition);

            collisionCallback.reset();

            collisionCallback.normalizedDirection = rayGlobalStopPosition.sub(rayGlobalStartPosition);
            // normalize
            collisionCallback.normalizedDirection = collisionCallback.normalizedDirection.mul(1.0f / collisionCallback.normalizedDirection.length());

            try {
                entityDescriptor.physics2dBody.body.getWorld().raycast(collisionCallback, rayGlobalStartPosition, rayGlobalStopPosition);
            } catch (Exception e) {
                System.err.println("Phys2D raycast: " + e + " " + rayGlobalStartPosition + " " + rayGlobalStopPosition);
                e.printStackTrace();
            }

            if (collisionCallback.hittedAnything) {
                final float closestHitDistance = collisionCallback.closestHitDistance;

                hitDistanceFractions[rayIndex] = closestHitDistance / raysLength;
            }
            else {
                hitDistanceFractions[rayIndex] = Float.MAX_VALUE;
            }
        }

    }

    private int getNumberOfRays() {
        return hitDistanceFractions.length;
    }

    @Override
    public String getLongName() {
        return "RaysComponent";
    }

    private RayCastClosestCallback collisionCallback = new RayCastClosestCallback();

    // helper
    private static Vec2 getNormalizedDirectionOfAngleInRadiants(final float angle) {
        return new Vec2((float) Math.cos(angle), (float) Math.sin(angle));
    }
}
