package ptrman.DificultyEnvironment.InteractionComponents;

import ptrman.DificultyEnvironment.EntityDescriptor;
import ptrman.DificultyEnvironment.JavascriptDescriptor;

import java.util.List;

/**
 *
 */
public interface IComponent {
    /**
     * gets called when the entity is spawned
     *
     * @param javascriptDescriptor
     */
    EntityDescriptor spawn(JavascriptDescriptor javascriptDescriptor, List<Object> parameters);

    void frameInteraction(JavascriptDescriptor javascriptDescriptor, EntityDescriptor entityDescriptor);
}
