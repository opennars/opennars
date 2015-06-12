package nars.vision.rawAttention;

import nars.NAR;
import nars.gui.NARSwing;
import nars.model.impl.Default;

import javax.swing.*;
import java.awt.*;

/**
 *
 */
public class Vision extends JFrame {
    private final NAR nar;

    public static void main(String[] args) {
        NAR nar = new NAR(new Default().setSubconceptBagSize(1000));

        new NARSwing(nar);

        Vision g = new Vision(nar);

    }

    public Vision(NAR nar) {
        super("NLP");

        this.nar = nar;

        perceptionDescriptor.createPixelMap(100, 80);

        setSize(300, 300);
        setLocation(0, 0);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(1, 1));
        add(pixelGridCanvas);

        pack();
        setVisible(true);
    }

    private PerceptionDescriptor perceptionDescriptor = new PerceptionDescriptor();
    private PixelGridCanvas pixelGridCanvas = new PixelGridCanvas();
}
