package nars.plugin.mental;

/**
 * To rememberAction an internal action as an operation
 * <p>
 * called from Concept
 * @param task The task processed
 */
public class FullInternalExperience extends InternalExperience {

    @Override
    public boolean isFull() {
        return true;
    }

}