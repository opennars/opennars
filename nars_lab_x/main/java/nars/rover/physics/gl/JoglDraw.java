package nars.rover.physics.gl;

import nars.rover.physics.PhysicsCamera;
import nars.rover.physics.PhysicsController;

/**
 *
 */
public class JoglDraw extends JoglAbstractDraw {
    private JoglAbstractPanel panel;

    public JoglDraw(JoglAbstractPanel panel) {
        this.panel = panel;
    }

    @Override
    protected PhysicsCamera getPhysicsCamera() {
        PhysicsCamera p = null;

        PhysicsController controller = panel.controller;
        if( controller != null ) {
            p = controller.getCamera();
        }

        return p;
    }
}
