package ptrman.dificultyEnvironment;

import ptrman.dificultyEnvironment.interactionComponents.IComponent;
import ptrman.dificultyEnvironment.physics.Physics2dBody;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class EntityDescriptor {
    public List<IComponent> components = new ArrayList<>();

    public Physics2dBody physics2dBody; // can be null
}
