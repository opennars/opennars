/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.reinforcementlearning.domains.follow;

import jurls.reinforcementlearning.domains.RLDomain;
import nars.predict.Discretize;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author thorsten
 */
public class Follow1D implements RLDomain {

    final int numActions = 6;

     //if movement, should be an odd number so the middle value = 0 (no movement)
    
    final double acceleration = 0.03;
    final double decelerationFactor = 0.25;
    
    
    private final int history = 256;


    final int historyPoints = 2; //includes current value
    
    final int historyInterval = history / (historyPoints+1); //how many history points to skip for each observation
    
    @Override
    public int numActions() {
        return numActions;
    }

    private class RenderComponent extends JComponent {

        private final List<Double> _positions = Collections.synchronizedList(new ArrayList<Double>(history));
        private final List<Double> _targets = Collections.synchronizedList(new ArrayList<Double>(history));

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

            final int margin = 10;
            
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

    private final List<Double> positions = Collections.synchronizedList(new ArrayList<Double>(history));
    private final List<Double> targets = Collections.synchronizedList(new ArrayList<Double>(history));
    private final double maxPos = 1.0;
    private double myPos = 0.5;
    private double targetPos = 0.5;
    private double myV = 0;
    private double targetV = 0;
    private final RenderComponent renderComponent = new RenderComponent();
    int time = 0;

    double observation[];
    @Override
    public double[] observe() {
        if (observation == null) {
            observation = new double[historyPoints*numActions];
        }
        Arrays.fill(observation, -1);
        double my = 0, target = 0;
        for (int i = 0; i < historyPoints; i++) {
            int j = positions.size() - 1 - (i * historyInterval);
            if (j >= 0) {
                my = positions.get(j);
                target = targets.get(j);
            }
            //observation[i+historyPoints] = my - 0.5;
            int index = Discretize.i(target, numActions);
            observation[i*numActions+index] = 1;
        }
        //System.out.println(Arrays.toString(observation));
        return observation;
    }

    @Override
    public double reward() {
        double dist = Math.abs(myPos - targetPos) / maxPos;
        return 1.0 - dist * 3f;
    }

    public void updateTarget(int time) {        
        updateTargetSine(time);
        //updateTargetXOR(time);
        //updateTargetRandom();
    }

            
    public void updateTargetRandom(int cycle) {        
        final double targetAcceleration = 0.01;
        targetPos += targetV;
        targetV += (Math.random() - 0.5) * targetAcceleration;        
    }
    public void updateTargetXOR(int cycle) {        
        int complexity = 10;
        double speed = 0.1;
        double scale = 1.0;
        double v = ( ((int)(speed * cycle )%complexity ^ 0xf3f24f)%complexity * scale / complexity);
        targetPos = v;
    }

    public void updateTargetSine(int cycle) {
        double speed = 0.1;
        double scale = 1.0f;
        double v = (0.5f + 0.5f * Math.sin( (speed * cycle / (Math.PI*2)) )) * scale;
        targetPos = v;
    }

    @Override
    public void takeAction(int action) {
        takeActionPosition(action);
        //takeActionAccelerating(action);


    }
    protected void takeActionPosition(int action) {
        myPos = (action / ((double)(numActions-1))) * maxPos;
    }

    protected void takeActionAccelerating(int action) {
        double a = Math.round(action - (numActions/2d));
        double direction = (a)/(numActions/2d);

        if (direction==0) {
            //decelerate on zero
            myV *= decelerationFactor;
        }
        else {
            myV = direction * acceleration;
        }
        myPos += myV;

    }

    @Override
    public void worldStep() {

        if (myPos > maxPos) {
            myPos = maxPos;
            myV = 0;
        }
        if (myPos < 0) {
            myPos = 0;
            myV = 0;
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
