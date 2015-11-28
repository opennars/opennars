package ptrman.difficultyEnvironment;

import ptrman.difficultyEnvironment.scriptAccessors.ComponentManipulationScriptingAccessor;
import ptrman.difficultyEnvironment.scriptAccessors.EnvironmentScriptingAccessor;
import ptrman.difficultyEnvironment.scriptAccessors.HelperScriptingAccessor;

/**
 *
 */
public class JavascriptDescriptor {
    public JavascriptEngine engine;

    // accessors
    public EnvironmentScriptingAccessor environmentScriptingAccessor;
    public HelperScriptingAccessor helperScriptingAccessor;
    public ComponentManipulationScriptingAccessor componentManipulationScriptingAccessor;
}
