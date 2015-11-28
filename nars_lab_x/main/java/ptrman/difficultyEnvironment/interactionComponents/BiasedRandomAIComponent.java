package ptrman.difficultyEnvironment.interactionComponents;

import ptrman.difficultyEnvironment.EntityDescriptor;
import ptrman.difficultyEnvironment.JavascriptDescriptor;

import java.util.Random;

/**
 * A random generator of movement of an agent.
 * The output for the controller is biased because else the actor would do a nonsensical random walk (where the sum of all movements over time is roughtly zero)
 *
 */
public class BiasedRandomAIComponent implements IComponent {
    public float ratioOfMoveRotation;
    public float timerNextMovechange;
    public float remainingTimerNextMovechange = -0.01f;
    public float angleScale;

    public TopDownViewWheeledControllerComponent topDownViewWheeledControllerComponent; // can be null

    public BiasedRandomAIComponent(float timerNextMovechange, float ratioOfMoveRotation, float angleScale) {
        this.timerNextMovechange = timerNextMovechange;
        this.ratioOfMoveRotation = ratioOfMoveRotation;
        this.angleScale = angleScale;
    }

    @Override
    public void frameInteraction(JavascriptDescriptor javascriptDescriptor, EntityDescriptor entityDescriptor, float timedelta) {
        remainingTimerNextMovechange -= timedelta;

        if( remainingTimerNextMovechange < 0.0f ) {
            remainingTimerNextMovechange = timerNextMovechange;

            if( topDownViewWheeledControllerComponent != null ) {
                topDownViewWheeledControllerComponent.relativeSpeed = -1.0f + random.nextFloat()*2.0f;
                topDownViewWheeledControllerComponent.relativeAngle = (-1.0f + random.nextFloat()*2.0f)*angleScale;
            }
        }
    }

    @Override
    public String getLongName() {
        return "BiasedRandomAIComponent";
    }

    private Random random = new Random();
}
