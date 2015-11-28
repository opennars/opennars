package ptrman.difficultyEnvironment;

import org.jbox2d.dynamics.World;
import ptrman.difficultyEnvironment.interactionComponents.IComponent;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Environment {
    public List<EntityDescriptor> entities = new ArrayList<>();

    public World physicsWorld2d;

    public void stepFrame(float timedelta, JavascriptDescriptor javascriptDescriptor) {
        doFrameInteractionsForAllEntities(timedelta, javascriptDescriptor);
        physicsTimestep(timedelta);
    }

    private void doFrameInteractionsForAllEntities(float timedelta, JavascriptDescriptor javascriptDescriptor) {
        for( EntityDescriptor iterationEntity : entities ) {

            for( IComponent iterationComponent : iterationEntity.components.getAsList() ) {
                iterationComponent.frameInteraction(javascriptDescriptor, iterationEntity, timedelta);
            }
        }
    }

    private void physicsTimestep(float timedelta) {
        // TODO< pull settings from some container >
        boolean allowSleep = true;
        boolean warmStarting = true;
        boolean SubStepping = true;
        boolean ContinuousCollision = true;

        int velocityIterations = 5;
        int positionIterations = 5;

        // the physics 2d world doesn't have to be initialized
        if( physicsWorld2d == null ) {
            return;
        }

        physicsWorld2d.setAllowSleep(allowSleep);
        physicsWorld2d.setWarmStarting(warmStarting);
        physicsWorld2d.setSubStepping(SubStepping);
        physicsWorld2d.setContinuousPhysics(ContinuousCollision);

        physicsWorld2d.step(timedelta, velocityIterations, positionIterations);
    }
}
