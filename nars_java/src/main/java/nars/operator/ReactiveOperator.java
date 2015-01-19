package nars.operator;

import nars.core.NAR;
import nars.event.AbstractReaction;
import nars.event.Reaction;
import nars.logic.nal8.Operator;

/**
 * This class combines Operator with a Reaction model to
 * provide a convenient API for defining an Operator that reacts
 * to both its own invocations and additional specific events.
 */
abstract public class ReactiveOperator extends Operator implements Reaction {

    private AbstractReaction reaction;

    public ReactiveOperator(String name) {
        super(name);
    }

    /** events to react to */
    abstract public Class[] getEvents();

    @Override
    public boolean setEnabled(NAR n, boolean enabled) {
        if (reaction == null) {
            reaction = new AbstractReaction(n, false, getEvents()) {

                @Override public void event(Class event, Object[] args) {
                    ReactiveOperator.this.event(event, args);
                }
            };
        }

        reaction.setActive(enabled);

        if (enabled) on(n); else off(n);

        return true;
    }

    protected void on(NAR n) {

    }

    protected void off(NAR n) {

    }

    @Override
    abstract public void event(Class event, Object[] args);

}
