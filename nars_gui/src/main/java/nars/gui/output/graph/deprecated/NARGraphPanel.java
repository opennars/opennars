///*
//* Here comes the text of your license
//* Each line should be prefixed with  *
//*/
//package nars.gui.output.graph;
//
//import automenta.vivisect.graph.AnimatingGraphVis;
//import automenta.vivisect.swing.NPanel;
//import automenta.vivisect.swing.NSlider;
//import automenta.vivisect.swing.PCanvas;
//import nars.NAR;
//import nars.gui.WrapLayout;
//
//import javax.swing.*;
//import java.awt.*;
//
///**
//*
//* @author me
//*/
//public class NARGraphPanel extends NPanel {
//    private final NAR nar;
//    private AnimatingGraphVis vis;
//    private PCanvas canvas;
//    private JPanel visControl, layoutControl, canvasControl;
//    private JComponent menu;
//    private JPanel graphControl;
//
//    float paintFPS = 25f;
//
//    public NARGraphPanel(NAR n, int temporaryForDefaultLayout) {
//        this(n);
//        NARGraphVis vis = new NARGraphVis(n) {
//            @Override
//            public void setMode(NARGraphVis.GraphMode g) {
//                super.setMode(g);
//                //doLayout();
//                //updateUI();
//            }
//
//            @Override
//            public GraphMode getInitialMode() {
//                GraphMode g = new ConceptGraphMode();
//                return g;
//            }
//        };
//
//        visControl = vis.newStylePanel();
//        canvasControl = newCanvasPanel();
//        layoutControl = vis.newLayoutPanel();
//        graphControl = vis.newGraphPanel();
//
//
//
//
//        init(vis);
//    }
//
//
//    public NARGraphPanel(NAR n) {
//        super(new BorderLayout());
//
//        this.nar = n;
//    }
//
//    public NARGraphPanel(NAR n, AnimatingGraphVis vis) {
//        this(n);
//        init(vis);
//    }
//
//
//    protected void init(AnimatingGraphVis vis) {
//        this.vis = vis;
//
//        //vis = ;
//        canvas = new PCanvas(vis) {
//
//            @Override
//            public void render() {
//                //MULTITHREADED RENDERING LOOP:
//                if (!isPredrawing())
//                    nar.memory.taskLater(super::predraw);
//
//                super.render();
//
//            }
//        };
//
//        menu = new JPanel(new WrapLayout(FlowLayout.LEFT));
//        menu.setOpaque(false);
//        if (graphControl!=null) {
//            menu.add(graphControl);
//            menu.add(visControl);
//            menu.add(canvasControl);
//            menu.add(layoutControl);
//        }
//
//
//
//        add(canvas, BorderLayout.CENTER);
//
//
//        add(menu, BorderLayout.NORTH);
//
//        canvas.setFrameRate(paintFPS);
//        canvas.loop();
//        canvas.renderEveryFrame(true);
//
//    }
//
//
//    @Override  protected void visibility(boolean appearedOrDisappeared) {
//        canvas.predraw();
//    }
//
//
//    protected JPanel newCanvasPanel() {
//        JPanel m = new JPanel(new FlowLayout(FlowLayout.LEFT));
//
//        NSlider blur = new NSlider(0, 0, 1.0f) {
//            @Override
//            public void onChange(float v) {
//                canvas.setMotionBlur(v);
//            }
//        };
//        blur.setPrefix("Blur: ");
//        blur.setPreferredSize(new Dimension(60, 25));
//        m.add(blur);
//
//        return m;
//    }
// }
