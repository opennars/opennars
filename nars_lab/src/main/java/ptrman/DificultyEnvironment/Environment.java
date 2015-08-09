package ptrman.DificultyEnvironment;

import org.jbox2d.dynamics.World;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Environment {
    public List<EntityDescriptor> entities = new ArrayList<>();

    public World physicsWorld2d;
}
