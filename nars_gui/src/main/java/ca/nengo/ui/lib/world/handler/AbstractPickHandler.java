package ca.nengo.ui.lib.world.handler;


import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.lib.world.piccolo.WorldImpl;
import org.piccolo2d.event.PBasicInputEventHandler;
import org.piccolo2d.event.PInputEvent;

import javax.swing.*;

/**
 * Abstract handler which picks and unpicks nodes with a delay
 *
 * TODO maybe this can be redesigned with an AtomicReference, async events,
 * and no additional Thread/locks
 *
 * @author Shu Wu
 */
public abstract class AbstractPickHandler extends PBasicInputEventHandler {

    private WorldObject pickedNode;

    private final WorldImpl world;
    private WorldObject nextPickedNode;

    public AbstractPickHandler(WorldImpl parent) {
        super();
        this.world = parent;

    }

//    protected abstract int getKeepPickDelay();
//
//    protected abstract int getPickDelay();

    protected WorldObject getPickedNode() {
        return pickedNode;
    }

    protected WorldImpl getWorld() {
        return world;
    }

    protected abstract void nodePicked();

    protected abstract void nodeUnPicked();

    protected abstract void processMouseEvent(PInputEvent event);

    private void pick() {
        nodeUnPicked();
        this.pickedNode = nextPickedNode;
        if (this.pickedNode != null)
            nodePicked();
    }

    protected void setSelectedNode(WorldObject selectedNode) {
        WorldObject oldNode = pickedNode;

        if (selectedNode != oldNode) {
            this.nextPickedNode = selectedNode;
            SwingUtilities.invokeLater(this::pick);
        }
    }


    @Override
    public void mouseDragged(PInputEvent event) {
        processMouseEvent(event);
    }

    @Override
    public void mouseMoved(PInputEvent event) {
        processMouseEvent(event);
    }

}
