package ptrman.dificultyEnvironment;

import ptrman.dificultyEnvironment.physics.Physics2dBody;

/**
 *
 */
public class EntityDescriptor {
    public ComponentCollection components = new ComponentCollection();

    public Physics2dBody physics2dBody; // can be null
}
