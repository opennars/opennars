package ptrman.difficultyEnvironment.interactionComponents;

import ptrman.difficultyEnvironment.EntityDescriptor;
import ptrman.difficultyEnvironment.JavascriptDescriptor;
import ptrman.difficultyEnvironment.helper.JavascriptAccessorHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Component where the logic is fully implemented in javascript
 */
public class JavascriptComponent implements IComponent {
    public static JavascriptComponent createFromRawSourcecode(String spawnScript, String frameInteractionScript) {
        return new JavascriptComponent(spawnScript, frameInteractionScript);
    }

    private JavascriptComponent(String spawnScript, String frameInteractionScript) {
        this.spawnScript = spawnScript;
        this.frameInteractionScript = frameInteractionScript;
    }

    public void frameInteraction(JavascriptDescriptor javascriptDescriptor, EntityDescriptor entityDescriptor, float timedelta) {
        JavascriptAccessorHelper.resetAndAccessAccessor(javascriptDescriptor);

        javascriptDescriptor.engine.loadString(frameInteractionScript);

        List<Object> parameters = new ArrayList<>();
        parameters.add(entityDescriptor);

        javascriptDescriptor.engine.invokeFunction("frameInteraction", parameters);
    }

    @Override
    public String getLongName() {
        return "JavascriptComponent";
    }

    private final String frameInteractionScript;
    private final String spawnScript;
}
