package ptrman.difficultyEnvironment.interactionComponents;

import ptrman.difficultyEnvironment.EntityDescriptor;
import ptrman.difficultyEnvironment.JavascriptDescriptor;

/**
 *
 */
public class TopDownViewWheeledControllerComponent extends AbstractControllerComponent {
    public float relativeSpeed = 0.0f; // from -1.0f to 1.0f
    public float maxSpeed;
    public TopDownViewWheeledPhysicsComponent physicsComponent;
    public float relativeAngle = 0.0f;

    public TopDownViewWheeledControllerComponent(final float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    @Override
    public void frameInteraction(JavascriptDescriptor javascriptDescriptor, EntityDescriptor entityDescriptor, float timedelta) {
        if( physicsComponent == null ) {
            return;
        }

        physicsComponent.thrust(relativeAngle * timedelta, maxSpeed * relativeSpeed);
    }

    @Override
    public String getLongName() {
        return "TopDownViewWheeledControllerComponent";
    }
}
