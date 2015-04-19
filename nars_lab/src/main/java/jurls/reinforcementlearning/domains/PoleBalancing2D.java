/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.reinforcementlearning.domains;

import javax.swing.*;
import java.util.Arrays;

/**
 *
 * @author thorsten
 */
public class PoleBalancing2D implements RLDomain {

    private final Physics2D physics2D;
    private final Point agentPoint;
    private final Point pendulumPoint;
    private final PhysicsRenderer physicsRenderer;

    double speed = 0.5;
    double dt = 0.0005;
    double gravity = 0.05;

    int maxX = 800;
    int minAgentX = 50;
    int maxAgentX = 750;
    double poleLength = 175;
    double decay = 0.999;
    double decay2 = 0.999;


    public PoleBalancing2D() {
        physics2D = new Physics2D(gravity, 300);
        physicsRenderer = new PhysicsRenderer();
        physicsRenderer.physics2D = physics2D;
        agentPoint = new Point(400, 300, 0, 0, decay2, 0);
        pendulumPoint = new Point(400, 300-poleLength, 0, 0, decay, decay);
        Connection c = new Connection(poleLength, agentPoint, pendulumPoint);
        physics2D.points.add(agentPoint);
        physics2D.points.add(pendulumPoint);
        physics2D.connections.add(c);
    }

    @Override
    public double[] observe() {
        double[] o = new double[]{
            (agentPoint.x)/(maxX),
            //agentPoint.y/600.0,
            (pendulumPoint.x)/(maxX),
            (pendulumPoint.y - agentPoint.y)/(poleLength*1.2),
                //Math.signum(pendulumPoint.vx),
            pendulumPoint.vx*dt,
            pendulumPoint.vy*dt
        };
        //System.out.println(Arrays.toString(o));
        return o;
    }

    @Override
    public double reward() {

        return (300 - pendulumPoint.y)/300.0;
    }

    @Override
    public void takeAction(int action) {
        takeAction3(action);
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
    public void worldStep() {

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
