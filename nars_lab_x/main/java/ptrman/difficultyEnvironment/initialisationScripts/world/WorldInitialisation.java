package ptrman.difficultyEnvironment.initialisationScripts.world;

import ptrman.difficultyEnvironment.Environment;
import ptrman.difficultyEnvironment.JavascriptDescriptor;
import ptrman.difficultyEnvironment.helper.JavascriptAccessorHelper;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class WorldInitialisation {
    public static void executeWorldInitialisationScript(Environment environment, JavascriptDescriptor javascriptDescriptor, String script) {
        JavascriptAccessorHelper.resetAndAccessAccessor(javascriptDescriptor);

        javascriptDescriptor.engine.loadString(script);

        List<Object> parameters = new ArrayList<>();
        parameters.add(environment);

        javascriptDescriptor.engine.invokeFunction("initializeWorld", parameters);
    }
}
