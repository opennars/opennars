package ca.nengo.ui.lib.moveme;

import ca.nengo.ui.lib.world.World;
import ca.nengo.ui.lib.world.WorldObject;

/**
 *
 */
public class BoundUpdatableSelectionBorder extends ca.nengo.ui.lib.world.piccolo.object.SelectionBorder {
    public BoundUpdatableSelectionBorder(World world) {
        super(world);
    }

    public BoundUpdatableSelectionBorder(World world, WorldObject objSelected) {
        super(world, objSelected);
    }

    public boolean updateBounds() {
        return super.updateBounds();
    }
}
