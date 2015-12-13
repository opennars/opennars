package nars.nal.meta;

import com.google.common.base.Joiner;

/**
 * Created by me on 12/11/15.
 */ /* <(&|, precon1, precon2, ...) =/> (&|, fork1, fork2, ... ) > */
public final class RuleBranch {

    public final PreCondition[] precondition; //precondition sequence

    public final RuleBranch[] children;

    public RuleBranch(PreCondition[] precondition, RuleBranch[] children) {
        this.precondition = precondition;
        this.children = children.length > 0 ? children : null;
    }

    @Override
    public String toString() {
        return
                "<(&|, " +
                        Joiner.on(", ").join(precondition) +
                ") =/> " +
                "(&|," +
                ((children != null) ?
                        Joiner.on(", ").join(children) : "End") +
                ")>";
    }
}
