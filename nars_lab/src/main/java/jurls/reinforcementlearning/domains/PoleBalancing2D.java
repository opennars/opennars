/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.reinforcementlearning.domains;

import javax.swing.*;

/**
 *
 * @author thorsten
 */
public class PoleBalancing2D implements RLEnvironment {

    private final Physics2D physics2D;
    private final Point agentPoint;
    private final Point pendulumPoint;
    private final PhysicsRenderer physicsRenderer;

    double speed = 0.4;
    double dt = 0.2;
    double gravity = 0.2;

    int maxX = 800;
    int minAgentX = 50;
    int maxAgentX = 750;
    double poleLength = 175;
    double decay = 0.995;
    double decay2 = 0.995;
    double agentY = 300;


    public PoleBalancing2D() {
        physics2D = new Physics2D(gravity, 300);
        physicsRenderer = new PhysicsRenderer();
        physicsRenderer.physics2D = physics2D;
        agentPoint = new Point((minAgentX + maxAgentX) / 2.0, agentY, 0, 0, decay2, 0);
        pendulumPoint = new Point((minAgentX + maxAgentX)/2.0, agentY-poleLength, 0, 0, decay, decay);
        Connection c = new Connection(poleLength, agentPoint, pendulumPoint);
        physics2D.points.add(agentPoint);
        physics2D.points.add(pendulumPoint);
        physics2D.connections.add(c);
    }

    @Override
    public double[] observe() {
        //System.out.println(Arrays.toString(o));
        return new double[]{
            (agentPoint.x)/(maxX),
            //agentPoint.y/600.0,
            (pendulumPoint.x)/(maxX),
            (pendulumPoint.y - agentPoint.y)/(poleLength*1.1),
            Math.atan2(pendulumPoint.y, pendulumPoint.x),

                //Math.atan2(pendulumPoint.vy, pendulumPoint.vx),
                //Math.signum(pendulumPoint.vx),
            //pendulumPoint.vx*dt,
            //pendulumPoint.vy*dt
        };
    }

    @Override
    public double getReward() {

        return (agentPoint.y - pendulumPoint.y)/poleLength;
    }

    @Override
    public boolean takeAction(int action) {
        takeAction3(action);
        return true;
    }

//    public void takeAction2(int action) {
//        agentPoint.vx += speed * 2 * (action - 0.5);
//        System.out.println(agentPoint.vx + " " + agentPoint.x);
//    }

    /* -1, 0, +1 */
    double dvx = 0;

    public void takeAction3(int action) {
        action--;

        dvx += speed * (action);

        //System.out.println(agentPoint.vx + " " + agentPoint.x);
    }

    @Override
    public int numActions() {
        return 3;
    }

    @Override
    public void frame() {

        agentPoint.vx += dvx;
        dvx = 0;

        physics2D.step(dt);
        if (agentPoint.x < minAgentX) {
            agentPoint.x = minAgentX;
            agentPoint.vx = 0;
            //agentPoint.vx = -agentPoint.vx; //bounce
        }
        if (agentPoint.x > maxAgentX) {
            agentPoint.x = maxAgentX;
            agentPoint.vx = 0;
            //agentPoint.vx = -agentPoint.vx; //bounce
        }

    }

    @Override
    public JComponent component() {
        return physicsRenderer;
    }


}
