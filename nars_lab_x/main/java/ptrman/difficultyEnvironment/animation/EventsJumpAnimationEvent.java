package ptrman.difficultyEnvironment.animation;

import ptrman.difficultyEnvironment.EntityDescriptor;
import ptrman.difficultyEnvironment.JavascriptDescriptor;

/**
 * Jumps to a specific index in the list of the animation events of the executing AnimationComponent
 */
public class EventsJumpAnimationEvent extends AnimationEvent {
    public int destination = -1;

    @Override
    public boolean isFiring() {
        throw new InternalError("should never be called");
    }

    @Override
    public void fire(JavascriptDescriptor javascriptDescriptor, EntityDescriptor entityDescriptor) {
        throw new InternalError("should never be called");
    }
}
