//package nars.gui.output.chart;
//
//import automenta.vivisect.swing.NPanel;
//import nars.event.Reaction;
//import nars.core.Events;
//import nars.core.NAR;
//import nars.logic.entity.Concept;
//import nars.logic.entity.Sentence;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.image.BufferedImage;
//
//
///**
// * 2D bubble chart with configurable dimensions
// * TODO reimplement colorization
// */
//public class BubbleChart extends NPanel implements Reaction {
//    private final NAR nar;
//    private BufferedImage buffer;
//    private boolean needsPaint;
//
//    public enum Dimension {
//        One, Time, Frequency, Confidence, Priority, Complexity
//    }
//
//    public Dimension x = Dimension.Frequency;
//    public Dimension y = Dimension.Confidence;
//    public Dimension size = Dimension.One;
//
//    public BubbleChart(NAR n) {
//        super(new BorderLayout());
//        this.nar = n;
//    }
//
//    @Override
//    protected void onShowing(boolean showing) {
//        if (showing) {
//            nar.memory.event.on(Events.FrameEnd.class, this);
//        }
//        else {
//            nar.memory.event.off(Events.FrameEnd.class, this);
//        }
//    }
//
//    @Override public void event(Class event, Object... arguments) {
//        update();
//    }
//
//    @Override
//    protected void paintComponent(Graphics g) {
//        if (buffer!=null) {
//            g.drawImage(buffer, 0, 0, null);
//        }
//    }
//
//
//
//
//
//    public void update() {
//        int w = getWidth();
//        int h = getHeight();
//        if ((buffer == null) || (buffer.getWidth()!=w) || (buffer.getHeight()!=h)) {
//            buffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
//        }
//
//        Graphics2D g = (Graphics2D)buffer.getGraphics();
//        g.setPaint(Color.BLACK);
//        g.fillRect(0, 0, w, h);
//
//        float minPriority = 0.02f;
//
//        float asize = (w + h)/2;
//        float esize = 0.05f * asize;
//        float minEsize = (int)(0.01f * asize);
//
//        int border = (int)esize/2;
//
//        int dw = w - border*2;
//        int dh = h - border*2;
//
//        float conceptOpacity = 0.65f;
//        float statementOpacity = 0.9f;
//
//        for (final Concept c : nar.memory.concepts) {
//            float priority = c.getPriority();
//            if (priority < minPriority)
//                continue;
//            if (c.beliefs.size() > 0) {
//                int minX = Integer.MAX_VALUE;
//                int minY = Integer.MAX_VALUE;
//                int maxX = Integer.MIN_VALUE;
//                int maxY = Integer.MIN_VALUE;
//                //Color cc = Video.getColor(c.term.toString(), 0.5f, 0.6f);
//
//                for (Sentence s : c.beliefs) {
//                    if (s.truth!=null) {
//                        float freq = s.truth.getFrequency();
//                        float conf = s.truth.getConfidence();
//
//                        int x = (int)(freq * dw);
//                        int y = dh - (int)(conf * dh);
//
//                        int px = (int)Math.max(minEsize, (int)(c.getPriority() * esize));
//                        int py = px;
//
//                        //Color color = new Color(Video.(cc, statementOpacity * c.getPriority());
//                        //g.setPaint(color);
//                        int ix = x-px/2;
//                        int iy = y-py/2;
//                        int jx = x+px/2;
//                        int jy = y+py/2;
//                        g.fillOval(border+ix, border+iy, px, py);
//
//
//                        if (ix < minX) minX = ix;
//                        if (iy < minY) minY = iy;
//                        if (jx > maxX) maxX = jx;
//                        if (jy > maxY) maxY = jy;
//                    }
//                }
//
//
//                int padding = 4;
//                int cw = padding * 2 + maxX-minX;
//                int ch = padding * 2 + maxY-minY;
//
//                float areafactor = Math.max(1f, asize/(cw + ch));
//                //Color occ = Video.getColor(cc, conceptOpacity * c.getPriority()/areafactor);
//                //g.setPaint(occ);
//                g.fillOval(border+minX-padding, border+minY-padding, cw, ch);
//            }
//        }
//        if (!needsPaint) {
//            needsPaint = true;
//            SwingUtilities.invokeLater(paintLater);
//        }
//    }
//
//    final Runnable paintLater = new Runnable() {
//            @Override public void run() {
//                repaint();
//                needsPaint = false;
//            }
//    };
//
// }
