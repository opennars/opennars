package nars.gui.output.graph.deprecated;

//package nars.gui.output.graph;
//
//import automenta.vivisect.swing.NPanel;
//import automenta.vivisect.swing.PCanvas;
//import java.awt.BorderLayout;
//import javax.swing.JPanel;
//import nars.core.EventEmitter.Observer;
//import nars.core.Events.FrameEnd;
//import nars.core.NAR;
//
//
//public class ProcessingGraphPanel extends NPanel implements Observer {
//
//    public float maxNodeSize = 1000f;
//    float nodeSize = 200f;
//    static final float boostScale = 6.0f;
//    
//
//    
//    PCanvas app = null;
//    private final NAR nar;
//    JPanel menu;
//
//    public ProcessingGraphPanel(NAR n, PCanvas graphCanvas) {
//        super(new BorderLayout());
//
//        this.app = graphCanvas;
//        this.nar = n;
//    }
//
//    public JPanel getDisplayControls() {
//        return ((NARGraphDisplay)app.getDisplay()).getControls();
//    }    
//    
//    protected void init() {        
//
//        this.setSize(1000, 860);//initial size of the window
//        this.setVisible(true);
//
//        menu = getDisplayControls();
//
//        add(menu, BorderLayout.NORTH);
//        add(app, BorderLayout.CENTER);
//        
//
//        //menu.add...
////        //final int numLevels = ((Bag<Concept>)n.memory.concepts).levels;
////        NSlider maxLevels = new NSlider(1, 0, 1) {
////            @Override
////            public void onChange(float v) {
////                app.minPriority = (float) (1.0 - v);
////                //app.setUpdateNext();
////            }
////        };
////        maxLevels.setPrefix("Min Level: ");
////        maxLevels.setPreferredSize(new Dimension(80, 25));
////        menu.add(maxLevels);
//        
//
//    }
//
//    @Override
//    protected void onShowing(boolean showing) {
//        if (showing) {
//            init();
//            nar.memory.event.on(FrameEnd.class, this);
//        } else {
//            nar.memory.event.off(FrameEnd.class, this);
//
//            app.stop();
//            app.destroy();
//            removeAll();
//            app = null;
//        }
//    }
//
//    @Override
//    public void event(Class event, Object[] arguments) {
//        if (app != null) {
//            app.setUpdateNext();
//            app.updateGraph();
//        }
//    }
//
//}
