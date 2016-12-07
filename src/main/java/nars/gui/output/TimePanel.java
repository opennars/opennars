//package nars.gui.output;
//
//import java.awt.Dimension;
//import java.util.Map;
//import java.util.TreeMap;
//import static java.util.stream.Collectors.toList;
//import javax.swing.BoxLayout;
//import javax.swing.JComponent;
//import javax.swing.JFrame;
//import javax.swing.JScrollPane;
//import javax.swing.JTextPane;
//import nars.core.EventEmitter.Observer;
//import nars.core.Events.CycleEnd;
//import nars.core.NAR;
//import nars.core.build.DefaultNARBuilder;
//import nars.entity.Task;
//import nars.gui.NPanel;
//import nars.gui.Window;
//import nars.io.TextOutput;
//
///**
// *
// * @author me
// */
//
//
//public class TimePanel extends NPanel implements Observer {
//    
//    private final NAR nar;
//    int cyclesShown = 60;
//    Map<Long,String> cycleSummary = new TreeMap();
//    
//    public TimePanel(NAR n) {
//        super();
//        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
//        this.nar = n;
//        
//        update();
//    }
//    
//    public void update() {
//        removeAll();
//        
//        long now = nar.memory.getTime();
//        String s = String.join("\n", nar.memory.newTasks.stream().map(t -> t.toString()).collect(toList()));
//        cycleSummary.put(now, s);
//            
//    
//        for (int i = cyclesShown - 1; i >= 0; i--) {
//            long cycle = nar.memory.getTime() - i;
//            if (cycle < 0)
//                continue;
//            
//            JComponent cp = getTimeSlice(cycle);
//            cp.setMaximumSize(new Dimension(350, 800));            
//            add(new JScrollPane(cp));
//        }
//        
//        validate();
//    }
//    
//    public JComponent getTimeSlice(long cycle) {
//        JTextPane p = new JTextPane();
//        
//        String t = "Cycle: " + cycle + "\n";
//        //TODO realtime stamp
//        for (Task task : nar.memory.executive.shortTermMemory) {
//            if (task.getCreationTime() == cycle)
//                t += "STM Task: " + task.toStringExternal() + "\n";
//        }
//        
//        String cs = cycleSummary.get(cycle);
//        if (cs!=null)
//            t += cs + "\n";
//        
//        p.setText(t);
//        return p;
//    }
//    
//
//    
//    @Override
//    protected void onShowing(boolean showing) {
//     
//        if (showing) {
//            nar.memory.event.on(CycleEnd.class, this);
//        }
//        else {
//            nar.memory.event.off(CycleEnd.class, this);
//        }                    
//    }
//    
//    
//    
//    public static void main(String[] args) {
//        
//        NAR n = new DefaultNARBuilder().build();
//        
//        Window w = new Window("TimePanel", new JScrollPane(new TimePanel(n), JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
//        w.setSize(800, 200);
//        w.setVisible(true);
//        w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        
//        new TextOutput(n, System.out);
//        
//        n.addInput("a. :|:");
//        n.addInput("6");
//        n.addInput("b. :|:");
//        n.addInput("6");
//        n.addInput("c. :|:");
//        n.finish(20);
//        
////        n.addInput("<a --> b>. :|:");
////        n.addInput("5");
////        n.addInput("<b --> c>. :|:");
////        n.addInput("5");
////        n.addInput("<c --> a>. :|:");
////        n.finish(1);
//        
//    }
//
//    @Override
//    public void event(Class event, Object... arguments) {
//        update();
//    }
//
//    
//    
//}
