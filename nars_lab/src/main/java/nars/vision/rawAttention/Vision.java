//package nars.vision.rawAttention;
//
//import nars.NAR;
//import nars.gui.NARSwing;
//import nars.nar.Default;
//import nars.task.Task;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//
///**
// *
// */
//public class Vision extends JFrame {
//    private final NAR nar;
//
//    public static void main(String[] args) {
//        NAR nar = new NAR(new Default().setActiveConcepts(1000));
//
//        new NARSwing(nar);
//
//        Vision g = new Vision(nar);
//
//    }
//
//    private class TimerActionListener implements ActionListener {
//        private final Vision vision;
//
//        public TimerActionListener(Vision entry) {
//            this.vision = entry;
//        }
//
//        @Override
//        public void actionPerformed(ActionEvent e) {
//            vision.timestep();
//        }
//    }
//
//    private void timestep() {
//        // TODO< timestep of perception simulation >
//
//        String narsese = PerceptionToNarseseTransformer.translateToNarsese(perceptionDescriptor.sampleSensors(), "primitivevision");
//
//        Task t = nar.task(narsese);
//        t.getTruth().setFrequency(1f);
//        t.getTruth().setConfidence(0.5f);
//
//        nar.frame(1);
//        nar.frame(1);
//        nar.frame(1);
//        nar.frame(1);
//        nar.frame(1);
//    }
//
//    public Vision(NAR nar) {
//        super("NLP");
//
//        this.nar = nar;
//
//        perceptionDescriptor.createPixelMap(100, 80);
//
//        // just a dot for testing
//        perceptionDescriptor.pixelMap[3][3] = true;
//
//
//
//        pixelGridCanvas.perceptionDescriptor = perceptionDescriptor;
//
//        final int radiusLogarithmDistribution = 2; // TODO< real value for radiusLogarithmDistribution >
//        perceptionDescriptor.populateSensorOffsetsFor(3, 3, radiusLogarithmDistribution);
//
//        perceptionDescriptor.visionCenterPosition = new int[]{3, 3};
//        perceptionDescriptor.recalculatePerceptionMap();
//
//        setSize(300, 300);
//        setLocation(50, 50);
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        setLayout(new GridLayout(1, 1));
//        pixelGridCanvas.setSize(100, 100);
//        add(pixelGridCanvas);
//
//        pixelGridCanvas.pixelSize = 5;
//
//        pixelGridCanvas.repaint();
//
//        pack();
//        setVisible(true);
//
//        Timer timer = new Timer(1000, new TimerActionListener(this));
//        timer.start();
//    }
//
//    private PerceptionDescriptor perceptionDescriptor = new PerceptionDescriptor();
//    private PixelGridCanvas pixelGridCanvas = new PixelGridCanvas();
//}
