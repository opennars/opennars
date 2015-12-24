package nars.guifx.graph2;

import nars.guifx.graph2.source.SpaceGrapher;

import java.util.function.Consumer;

/**
 * Created by me on 12/24/15.
 */
public interface EdgeRenderer<E> extends Consumer<E> {
    /**
     * called before any update begins
     */
    void reset(SpaceGrapher g);
}
