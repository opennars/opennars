package ptrman.dificultyEnvironment;

import org.jbox2d.dynamics.World;
import ptrman.dificultyEnvironment.interactionComponents.IComponent;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Environment {
    public List<EntityDescriptor> entities = new ArrayList<>();

    public World physicsWorld2d;

    public void stepFrame(float timedelta, JavascriptDescriptor javascriptDescriptor) {
        for( EntityDescriptor iterationEntity : entities ) {

            for( IComponent iterationComponent : iterationEntity.components ) {
                iterationComponent.frameInteraction(javascriptDescriptor, iterationEntity, timedelta);
            }
        }

        // TODO< simulate physics one step >
    }
}
