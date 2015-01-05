/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nars.rl.elsy;

import static java.lang.Double.MAX_VALUE;
import static java.lang.Double.MIN_VALUE;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Arrays.fill;

/**
 *
 * @author me
 */
public class QLearner  {
    
    private Action[] qaction;
    public QBrain brain;
    double nextReward;
    double[] sensor;
    double[] action;


    double minReward = MAX_VALUE;
    double maxReward = MIN_VALUE;
    
    public QLearner(int sensors, int actions, int... hiddenNeurons) {
        qaction = new Action[actions];
        for (int i = 0; i < actions; i++) {
            final int I = i;
            qaction[i] = new Action() {                
                @Override public int execute() { return I; }
            };
        }
        
        sensor = new double[sensors];
        action = new double[actions];
        
        brain = new QBrain(new Perception() {

            @Override
            public boolean isUnipolar() {
                return true;
            }

            @Override
            public double getReward() {
                return nextReward;
            }

            @Override
            protected void updateInputValues() {
                for (int i = 0; i < sensor.length; i++)
                    setNextValue(sensor[i]);        
            }
        }, qaction, hiddenNeurons );
        
        /*
        brain = new Brain(new DAPerception(sensor, 4) {
        
        @Override
        public boolean isUnipolar() {
        return true;
        }
        
        @Override
        public double getReward() {
        return nextReward;
        }
        
        }, qaction ); 
        */
                
        brain.reset();
    }

    public int step(double reward) {
        maxReward = max(reward, maxReward);
        minReward = min(reward, minReward);
        this.nextReward = (reward - minReward)/(maxReward-minReward)-0.5;
        brain.getPerception().perceive();
        brain.count();
        fill(action, 0.0);
        int a = brain.getAction();
        action[a] = 1.0;
        //brain.printStats();
        //System.out.println(reward + " " + a);
        //Util.printArray(brain.getInput());
        //Util.printArray(brain.getOutput());
        //Util.printArray(action);
        
        return a;
    }

    public double[] getSensor() {
        return sensor;
    }

    public double[] getAction() {
        return action;
    }
    
    public static abstract class Action {

        abstract public int execute();
    }
    
}
