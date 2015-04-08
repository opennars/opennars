/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.reinforcementlearning.domains;

import javax.swing.JComponent;

/**
 *
 * @author thorsten
 */
public class PoleBalancing2D implements RLDomain {

    private final Physics2D physics2D;
    private final Point agentPoint;
    private final Point pendulumPoint;
    private final PhysicsRenderer physicsRenderer;

    public PoleBalancing2D() {
        physics2D = new Physics2D(0.1, 300);
        physicsRenderer = new PhysicsRenderer();
        physicsRenderer.physics2D = physics2D;
        agentPoint = new Point(400, 300, 0, 0, 0.99, 0);
        pendulumPoint = new Point(401, 50, 0, 0, 0.99, 0.99);
        Connection c = new Connection(250, agentPoint, pendulumPoint);
        physics2D.points.add(agentPoint);
        physics2D.points.add(pendulumPoint);
        physics2D.connections.add(c);
    }

    @Override
    public double[] observe() {
        return new double[]{
            agentPoint.x,
            agentPoint.y,
            pendulumPoint.x,
            pendulumPoint.y
        };
    }

    @Override
    public double reward() {
        return 300 - pendulumPoint.y;
    }

    @Override
    public void takeAction(int action) {
        agentPoint.vx += 2 * (action - 0.5);
    }

    @Override
    public void worldStep() {
        physics2D.step(1.0);
        if (agentPoint.x < 50) {
            agentPoint.x = 50;
        }
        if (agentPoint.x > 750) {
            agentPoint.x = 750;
        }
    }

    @Override
    public JComponent component() {
        return physicsRenderer;
    }

    @Override
    public int numActions() {
        return 2;
    }
}
