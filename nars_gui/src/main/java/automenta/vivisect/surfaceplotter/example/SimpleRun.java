package automenta.vivisect.surfaceplotter.example;

import automenta.vivisect.surfaceplotter.JSurfacePanel;
import automenta.vivisect.surfaceplotter.surface.ArraySurfaceModel;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class SimpleRun {

    double t = 0;

    public void testSomething() {
        JSurfacePanel jsp = new JSurfacePanel();
        jsp.setTitleText("Hello");

        JFrame jf = new JFrame("test");
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.getContentPane().add(jsp, BorderLayout.CENTER);
        jf.pack();
        jf.setVisible(true);

        Random rand = new Random();
        int max = 32;
        float[][] z1 = new float[max][max];
        float[][] z2 = new float[max][max];

        ArraySurfaceModel sm = new ArraySurfaceModel();


        jsp.setModel(sm);

        new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {

                    for (int i = 0; i < max; i++) {
                        for (int j = 0; j < max; j++) {
                            if (j == 5) {
                                z1[i][j] = Float.NaN;
                            } else {
                                z1[i][j] = (float)(Math.cos(i+t/100.0)+Math.sin(j+t/50.0));
                                //z2[i][j] = rand.nextFloat() * 20 - 10f;
                            }
                        }
                    }
                    
                     sm.setValues(0f, 200f, 0f, 200f, max, z1, null);

                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ex) {

                    }

                    t += 0.1;
                }

            }

        }).start();
		//sm.doRotate();

		// canvas.doPrint();
        // sm.doCompute();
    }

    public static float f1(float x, float y) {
        // System.out.print('.');
        return (float) (Math.sin(x * x + y * y) / (x * x + y * y));
        // return (float)(10*x*x+5*y*y+8*x*y -5*x+3*y);
    }

    public static float f2(float x, float y) {
        return (float) (Math.sin(x * x - y * y) / (x * x + y * y));
        // return (float)(10*x*x+5*y*y+15*x*y-2*x-y);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                new SimpleRun().testSomething();
            }
        });

    }

}
