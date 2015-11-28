package ptrman.difficultyEnvironment;

import ptrman.difficultyEnvironment.interactionComponents.IComponent;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ComponentCollection {
    public void addComponent(IComponent component) {
        components.add(component);
    }

    public IComponent getComponentByName(String longName) {
        // TODO< use hashmap for acceleration? >
        for( IComponent iterationComponent : components ) {
            if( iterationComponent.getLongName().equals(longName) ) {
                return iterationComponent;
            }
        }

        throw new RuntimeException("Component not found");
    }

    public List<IComponent> getAsList() {
        return components;
    }

    private List<IComponent> components = new ArrayList<>();
}
