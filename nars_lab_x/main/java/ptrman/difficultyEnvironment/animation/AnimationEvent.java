package ptrman.difficultyEnvironment.animation;

import ptrman.difficultyEnvironment.EntityDescriptor;
import ptrman.difficultyEnvironment.JavascriptDescriptor;

/**
 *
 */
public abstract class AnimationEvent {
    public abstract boolean isFiring();
    public abstract void fire(JavascriptDescriptor javascriptDescriptor, EntityDescriptor entityDescriptor);
}
