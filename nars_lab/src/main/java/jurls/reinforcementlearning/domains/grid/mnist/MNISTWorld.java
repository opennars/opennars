/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jurls.reinforcementlearning.domains.grid.mnist;

import jurls.reinforcementlearning.domains.grid.World;

import java.io.IOException;

/**
 *
 * @author me
 */
public class MNISTWorld extends MNIST implements World {

    int currentImage = 0, currentFrame = -1;
    int cycle = 0;
    static final int maxDigit = 2;
    
    @SuppressWarnings("HardcodedFileSeparator")
    public MNISTWorld(String path, int maxImages, int maxDigit) throws IOException {
        super("/home/me/Downloads", maxImages, maxDigit);
    }

    
    @Override
    public String getName() {
        return "MNIST";
    }

    @Override
    public int getNumSensors() {
        return 28*28;
    }

    @Override
    public int getNumActions() {
        return maxDigit+1;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    //int bits[] = new int[4];    
    
    MNISTImage i;
    MNISTImage blank = new MNISTImage(28,28);
    
    MNISTImage retina = new MNISTImage(28,28);
    int nextColumn = 0;
    
    @Override
    public double step(double[] action, double[] sensor) {
        
        if (cycle % trainingCyclesPerImage == 0) {
            currentFrame++;
            if (currentFrame % 10 == 0) {
                i = blank;
                if (trainingCyclesPerImage < maxTrainingCyclesPerImage)
                    trainingCyclesPerImage+=5;
            }
            else {
                i = images.get(currentImage++);                
                currentImage %= images.size();
            }
            nextColumn = 0;
        }
        
        /*
        if (nextColumn<28) {
            if (cycle % scrollCycles == 0) {
                retina.scrollRight(i, nextColumn);
                nextColumn++;
            }
        }
        else {
        }
        retina.toArray(sensor, noise);
        */
        i.toArray(sensor, noise);
        
        
        double threshold = 0.75;
        
        int a = -1;        
        for (int x = 0; x < action.length; x++) {
            boolean active = (action[x] > threshold);
            if (active) {
                a = x;
            }            
        }
        //a = a-1;

        
        double r;
        
        if (i.label == -1) {
            r = a == -1 ? 1.0 : -1.0;
        }
        else {
            r = (a < 0) || (a > 9) ? -1.0 : 1.0 / (1 + Math.abs(a - i.label));
        }
      
        
        System.out.print(cycle + " " + currentFrame + ' ' + currentImage + " label=" + i.label + ": " + a + ' ' + r + " [");
        //printArray(action);
        
        
        cycle++;
        
        return r;
    }
    
    //int scrollCycles = 2;
    int maxTrainingCyclesPerImage = 256, trainingCyclesPerImage = 1;
    static final double noise = 0.01;
    
    @SuppressWarnings("HardcodedFileSeparator")
    public static void main(String[] args) throws Exception {
        
        MNISTWorld m = new MNISTWorld("/home/me/Downloads", 800, maxDigit);
//        
//        
//        
//        DABeccaAgent a = new DABeccaAgent() {            
//            @Override
//            public int getReducedSensors(int worldSensors) {
//                //return (int)Math.sqrt(worldSensors);
//                return maxDigit*6;
//            }            
//
//            @Override
//            public void init(World world) {
//                super.init(world); //To change body of generated methods, choose Tools | Templates.
//                pretrain(m.getImageVectors(), 50, 2, 0.1, noise);
//            }
//
//
//            @Override
//            public void update(double lastReward, int time) {
//                super.update(lastReward, time);
//                
//                double e = 0.15 + 1/(1.0 + time/1000.0)*0.25;
//                if (time%1000 == 0)
//                    System.out.println(time + " exploration=" + e);
//                getHub().setEXPLORATION(e);
//                
//            }
//        };
//        
//        
//        BeccaAgent b = new BeccaAgent() {            
//
//
//            @Override
//            public void update(double lastReward, int time) {
//                super.update(lastReward, time);
//                
//                double e = 0.15 + 1/(1.0 + time/1000.0)*0.7;
//                if (time%1000 == 0)
//                    System.out.println(time + " exploration=" + e);
//                getHub().setEXPLORATION(e);
//                
//            }
//        };
//        
//                
//        
//        
//        new Simulation(a, m, 0);
        
    }

}
