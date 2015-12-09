/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.reinforcementlearning.domains.follow;

import jurls.reinforcementlearning.domains.RLEnvironment;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * 2 point controller version of follow1D
 */
public class Follow1DTwoPoint implements RLEnvironment {

    final int numActions = 2;

    //final int discretization = 3;

     //if movement, should be an odd number so the middle value = 0 (no movement)
    
    double speed = 0.05;
    double targetSpeed = 1;
    double closeThresh = speed * 2; //threshold that distance must be less than to receive positive rewards

    private final int history = 64;


    final int historyPoints = 1; //includes current value
    
    final int historyInterval = history / (historyPoints+1); //how many history points to skip for each observation
    
    @Override
    public int numActions() {
        return numActions;
    }

    private class RenderComponent extends JComponent {

        private final List<Double> _positions = Collections.synchronizedList(new ArrayList<>(history));
        private final List<Double> _targets = Collections.synchronizedList(new ArrayList<>(history));

        @Override
        protected void paintComponent(Graphics g) {
            _positions.clear();
            _positions.addAll(positions);
            
            _targets.clear();
            _targets.addAll(targets);
             
            g.setColor(Color.black);

            g.fillRect(
                    0, 0, getWidth(), getHeight());

            int prevY, prevX, i;

            int margin = 10;
            
            prevX = 0;
            prevY = 0;
            i = 0;

            g.setColor(Color.green);
            for (double _y : _targets) {
                int x = i * getWidth() / history;
                int y = (int) (_y * (getHeight()-margin) / maxPos) + margin/2;
                g.drawLine(prevX, prevY, x, y);
                ++i;
                prevX = x;
                prevY = y;
            }

            prevX = 0;
            prevY = 0;
            i = 0;

            g.setColor(Color.white);
            for (double _y : _positions) {
                int x = i * getWidth() / history;
                int y = (int) (_y * (getHeight()-margin) / maxPos) + margin/2;
                g.drawLine(prevX, prevY, x, y);
                ++i;
                prevX = x;
                prevY = y;
            }
        }
    }

    private final List<Double> positions = Collections.synchronizedList(new ArrayList<>(history));
    private final List<Double> targets = Collections.synchronizedList(new ArrayList<>(history));
    private final double maxPos = 1.0;
    private double myPos = 0.5;
    private double targetPos = 0.5;
    private double targetV = 0;
    private final RenderComponent renderComponent = new RenderComponent();
    int time = 0;

    double[] observation;
    @Override
    public double[] observe() {
        if (observation == null) {
            observation = new double[historyPoints*2];
        }
        //Arrays.fill(observation, -1);
        double my = 0, target = 0;
        if (positions.isEmpty()) return observation;

        for (int i = 0; i < historyPoints;) {
            int j = positions.size() - 1 - (i * historyInterval);
            my = positions.get(j);
            target = targets.get(j);
            //observation[i+historyPoints] = my - 0.5;
            //int index = Discretize.i(target, discretization);

//            for (int k = 0; k < discretization; k++) {
//                double v = Discretize.pSmoothDiscrete(target, k, discretization);
//                observation[i * discretization + k] = v;
//            }
            observation[i++] = 2 * ( target - 0.5 );
            observation[i++] = 2 * ( my - 0.5 );
        }
        return observation;
    }

    double lastDist = Double.NaN;

    @Override
    public double getReward() {
        return getRewardAbsolute();
    }

    public double getRewardDelta() {
        double dist = Math.abs(myPos - targetPos) / maxPos;

        double delta;
        delta = !Double.isFinite(lastDist) ? 0 : dist - lastDist;

        lastDist = dist;

        double reward = -delta * 10;
        return reward;
    }



    @Override
    public float getMaxReward() {
        return (float)closeThresh;
    }

    public double getRewardAbsolute() {

        double dist = Math.abs(myPos - targetPos) / maxPos;
        return dist < closeThresh ? closeThresh - dist : -(dist);
    }

    public void updateTarget(int time) {        
        //updateTargetSine(time);
        updateTargetXOR(time);
        //updateTargetRandom(time);
    }

            
    public void updateTargetRandom(int cycle) {        
        double targetAcceleration = 0.002;
        targetPos += targetV * speed;
        targetV += (Math.random() - 0.5) * targetAcceleration;        
    }
    public void updateTargetXOR(int cycle) {        
        int complexity = 10;
        double scale = 1.0;
        double v = ( ((int)(speed * targetSpeed * cycle )%complexity ^ 0xf3f24f)%complexity * scale / complexity);
        targetPos = v;
    }

    public void updateTargetSine(int cycle) {
        double scale = 1.0f;
        double v = (0.5f + 0.5f * Math.sin( (speed * cycle / (Math.PI*2)) )) * scale;
        targetPos = v;
    }

    @Override
    public boolean takeAction(int action) {
        int direction = action == 0 ? -1: 1;
        return takeActionVelocity(direction);
    }

    protected boolean takeActionVelocity(int direction) {

        double myV;
        myV = direction == 0 ? 0 : direction * speed;
        myPos += myV;

        //TODO detect bump and do not report succesful act
        return true;

    }

    @Override
    public void frame() {

        if (myPos > maxPos) {
            myPos = maxPos;
            //myV = -myV/2; //bounce
        }
        if (myPos < 0) {
            myPos = 0;
            //myV = -myV/2; //bounce -- may not work well, if it oscillates
        }


        updateTarget(time);
        if (targetPos > maxPos) {
            targetPos = maxPos;
            targetV = 0;
        }
        if (targetPos < 0) {
            targetPos = 0;
            targetV = 0;
        }

        positions.add(myPos);
        while (positions.size() > history) {
            positions.remove(0);
        }

        targets.add(targetPos);
        while (targets.size() > history) {
            targets.remove(0);
        }
        
        time++;
    }

    @Override
    public JComponent component() {
        return renderComponent;
    }

}
