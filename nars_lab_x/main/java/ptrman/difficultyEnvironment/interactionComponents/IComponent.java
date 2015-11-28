package ptrman.difficultyEnvironment.interactionComponents;

import ptrman.difficultyEnvironment.EntityDescriptor;
import ptrman.difficultyEnvironment.JavascriptDescriptor;

/**
 *
 */
public interface IComponent {
    void frameInteraction(JavascriptDescriptor javascriptDescriptor, EntityDescriptor entityDescriptor, float timedelta);
    String getLongName();
}
