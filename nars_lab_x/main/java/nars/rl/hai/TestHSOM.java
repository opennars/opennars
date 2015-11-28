/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nars.rl.hai;

import java.util.Arrays;

/**
 *
 * @author me
 */
public class TestHSOM {

    public TestHSOM() {
        double CHANGE_PROBABILITY = 0.02;
        int size = 3;
        int target = 0;
        int position = 0;
        int cycles = 15000;
        double[] vision = new double[size*2];

        HsomQBrain h = new HsomQBrain(size, 3);
        int nextAction = -1;
        
        for (int i = 0; i < cycles; i++) {
            
            double reward = 1.0 / (1.0 + Math.abs(target - position));

            Arrays.fill(vision, 0);
            vision[target] = 1.0; //target position in 1st half of vision
            vision[size + position] = 1.0; //current position in 2nd half of vision
            
            System.out.println(Arrays.toString(vision) + " = " + reward + " <- " + nextAction);
            
            
            nextAction = h.act(vision, reward);
            
            if (nextAction == 0) {
                position++;
            }
            else if (nextAction == 1) {
                position--;
            }
            else {
                //nothing
            }
            
            if (position < 0) position = size-1;
            if (position >= size) position = 0;
         
            if (Math.random() < CHANGE_PROBABILITY) {
                target += Math.random() > 0.5 ? -1 : +1;
                if (target < 0) target = size-1;
                if (target >= size) target = 0;                
            }
            
        }
        
    }
    
    public static void main(String[] args) {
        new TestHSOM();
    }
    
}
