package ca.nengo.ui.lib.moveme;

import ca.nengo.ui.lib.world.World;

/**
 *
 */
public class BoundUpdatableSelectionBorder extends ca.nengo.ui.lib.world.piccolo.object.SelectionBorder {
    public BoundUpdatableSelectionBorder(World world) {
        super(world);
    }

    public boolean updateBounds() {
        return super.updateBounds();
    }
}
