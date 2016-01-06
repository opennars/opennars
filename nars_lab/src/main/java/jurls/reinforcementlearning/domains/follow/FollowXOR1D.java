///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package jurls.reinforcementlearning.domains;
//
//import java.util.Arrays;
//import javafx.stage.Stage;
//
///**
// *
// * @author me
// */
//public class FollowXOR1D extends Follow1Dnew implements Runnable {
//
//    int levels = 5;
//
//    public static void main(String[] args) {
//        launch(args);
//    }
//    private double[] obs;
//
//    public double[] observe() {
//        if (obs == null)
//            obs = new double[ levels + levels ];
//
//        Arrays.fill(obs, 0);
//
//        double range = maxPos - minPos;
//        double sharp = 4;
//        for (int i = 0; i < levels; i++) {
//            /*double distToMe = 1.0 / (1.0 + 3 * Math.abs( myPos - i ));
//            double distToTarget = 1.0 / (1.0 + 3 * Math.abs( targetPos - i ));*/
//            double x = ((double)i) / (levels-1) * range;
//
//            double distToMe = Math.exp( -((myPos-x)*(myPos-x))/(2*range / sharp) );
//            double distToTarget = Math.exp( -((targetPos-x)*(targetPos-x))/(2*range / sharp) );
//            ;
//            obs[i] = distToMe;
//            obs[i + levels] = distToTarget;
//        }
//
//
//        //System.out.println(Arrays.toString(obs));
//
//        return obs;
//    }
//
//
//    @Override
//    public void start(Stage stage) {
//        super.start(stage);
//    }
//
//    @Override
//    protected double getTarget(double currentValue, int cycle) {
//        int complexity = 50;
//        double speed = 0.01;
//        double scale = 9.0;
//        double v = ( ((int)(speed * cycle))%complexity ^ 0xf3f24f)%complexity * scale / complexity;
//
//        //v = (v - 0.5);
//        return v;
//    }
//
// }
