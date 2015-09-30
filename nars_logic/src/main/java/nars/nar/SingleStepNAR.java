package nars.nar;

/**
 * Default set of NAR parameters which have been classically used for development.
 * <p>
 * WARNING this Seed is not immutable yet because it extends Param,
 * which is supposed to be per-instance/mutable. So do not attempt
 * to create multiple NAR with the same Default seed model
 */
public class SingleStepNAR extends Default {

    public SingleStepNAR() {
        super();
    }


}
