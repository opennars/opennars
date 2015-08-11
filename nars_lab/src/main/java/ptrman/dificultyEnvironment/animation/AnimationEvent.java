package ptrman.dificultyEnvironment.animation;

import ptrman.dificultyEnvironment.EntityDescriptor;
import ptrman.dificultyEnvironment.JavascriptDescriptor;

/**
 *
 */
public abstract class AnimationEvent {
    public abstract boolean isFiring();
    public abstract void fire(JavascriptDescriptor javascriptDescriptor, EntityDescriptor entityDescriptor);
}
