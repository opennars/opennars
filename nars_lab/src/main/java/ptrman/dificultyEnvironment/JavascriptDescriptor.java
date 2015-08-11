package ptrman.dificultyEnvironment;

import ptrman.dificultyEnvironment.scriptAccessors.ComponentManipulationScriptingAccessor;
import ptrman.dificultyEnvironment.scriptAccessors.EnvironmentScriptingAccessor;
import ptrman.dificultyEnvironment.scriptAccessors.HelperScriptingAccessor;

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
