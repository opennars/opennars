package ptrman.difficultyEnvironment.animation;

import ptrman.difficultyEnvironment.EntityDescriptor;
import ptrman.difficultyEnvironment.JavascriptDescriptor;
import ptrman.difficultyEnvironment.helper.JavascriptAccessorHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * AnimationEvent which executes a javascript just once (in the context of the entity)
 */
public class ExecuteJavascriptAnimationEvent extends AnimationEvent {
    public ExecuteJavascriptAnimationEvent(String script) {
        super();
        this.script = script;
    }

    public static ExecuteJavascriptAnimationEvent createWithScriptString(String script) {
        return new ExecuteJavascriptAnimationEvent(script);
    }

    @Override
    public boolean isFiring() {
        return true;
    }

    @Override
    public void fire(JavascriptDescriptor javascriptDescriptor, EntityDescriptor entityDescriptor) {
        JavascriptAccessorHelper.resetAndAccessAccessor(javascriptDescriptor);

        javascriptDescriptor.engine.loadString(script);

        List<Object> parameters = new ArrayList<>();
        parameters.add(entityDescriptor);

        javascriptDescriptor.engine.invokeFunction("animationEvent", parameters);
    }

    private final String script;
}
