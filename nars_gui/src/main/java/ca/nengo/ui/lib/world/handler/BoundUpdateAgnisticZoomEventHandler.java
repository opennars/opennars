package ca.nengo.ui.lib.world.handler;

import ca.nengo.ui.lib.moveme.BoundUpdatableSelectionBorder;
import com.gs.collections.impl.list.mutable.FastList;
import org.piccolo2d.event.PInputEvent;
import org.piccolo2d.event.PZoomEventHandler;

import java.util.List;

/**
 * Zoom handler which refreshes the display of many BoundUpdatableSelectionBorder's
 */
public class BoundUpdateAgnisticZoomEventHandler extends PZoomEventHandler {
    public List<BoundUpdatableSelectionBorder> selectionBorders = new FastList<>();

    public BoundUpdateAgnisticZoomEventHandler() {
        super();
    }

    protected void dragActivityStep(final PInputEvent event) {
        super.dragActivityStep(event);

        for( final BoundUpdatableSelectionBorder iterationBorder : selectionBorders ) {
            iterationBorder.updateBounds();
        }
    }
}
