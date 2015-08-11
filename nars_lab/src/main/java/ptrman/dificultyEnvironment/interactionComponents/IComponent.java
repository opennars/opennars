package ptrman.dificultyEnvironment.interactionComponents;

import ptrman.dificultyEnvironment.EntityDescriptor;
import ptrman.dificultyEnvironment.JavascriptDescriptor;

/**
 *
 */
public interface IComponent {
    void frameInteraction(JavascriptDescriptor javascriptDescriptor, EntityDescriptor entityDescriptor, float timedelta);
    String getLongName();
}
