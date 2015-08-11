package ptrman.difficultyEnvironment.initialisationScripts.entity;

import ptrman.difficultyEnvironment.EntityDescriptor;
import ptrman.difficultyEnvironment.JavascriptDescriptor;
import ptrman.difficultyEnvironment.helper.JavascriptAccessorHelper;

import java.util.List;

/**
 *
 */
public class EntitySpawner {
    /**
     * gets called when the entity is spawned
     *
     * @param javascriptDescriptor
     */
    public static EntityDescriptor spawn(JavascriptDescriptor javascriptDescriptor, String spawnScript, List<Object> parameters) {
        JavascriptAccessorHelper.resetAndAccessAccessor(javascriptDescriptor);

        javascriptDescriptor.engine.loadString(spawnScript);

        Object resultOfscriptFunctionCallAsObject = javascriptDescriptor.engine.invokeFunction("spawn", parameters);

        EntityDescriptor resultOfscriptFunctionCall = (EntityDescriptor)resultOfscriptFunctionCallAsObject;

        if( resultOfscriptFunctionCall == null ) {
            throw new RuntimeException("result wasn't a EntityDescriptor");
        }

        return resultOfscriptFunctionCall;
    }

}
