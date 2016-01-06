//
//package nars.gui.output;
//
//import automenta.vivisect.swing.NPanel;
//import automenta.vivisect.swing.NSliderSwing;
//import nars.NAR;
//import nars.Video;
//import nars.io.out.Output;
//import nars.task.Task;
//import nars.util.event.Reaction;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//
//import static javax.swing.SwingUtilities.invokeLater;
//
///**
// *
// * @author me
// */
//public class MultiModePanel extends NPanel implements Reaction<Class,Object[]> {
//    final float activityIncrement = 0.5f; //per output
//    final float activityMomentum = 0.95f;
//
//    private final NAR nar;
//    private final Object object;
//
//    float activity = 0;
//    //private final String label;
//    private final String label;
//    MultiViewMode currentMode = null;
//
//
//    public interface MultiViewMode  {
//
//        public void setFontSize(float newSize);
//        public void output(Class c, Object s);
//    }
//
//    /** extension of SwingLogText with functionality specific to MultiLogPanel */
//    public class LogView extends SwingLogText implements MultiViewMode {
//
//        public LogView() {
//            super(nar);
//
//            SwingLogPanel.setConsoleFont(this);
//
//        }
//
//
//
//    }
//
//
//    public class GraphView2 {
//        /*         Window w = new Window("Edit",new JGraphXGraphPanel(nar));
//        w.setVisible(true);
//        w.pack();
//            */
//    }
//
//    @Override
//    protected void visibility(boolean appearedOrDisappeared) {
//        nar.event().set(this, appearedOrDisappeared, Output.DefaultOutputEvents);
//    }
//
//    @Override
//    public void event(Class event, Object[] arguments) {
//        output(event, arguments.length > 1 ? arguments : arguments[0]);
//    }
//
//
//    public MultiModePanel(NAR nar, Object object) {
//        super(new BorderLayout());
//        this.nar = nar;
//        this.object = object;
//
//
//
//
//        if (object instanceof Task) {
//            label = ((Task)object).toString();
//        }
//        else {
//            label = object.toString();
//        }
//
//        //Default View
//        setMode(new LogView());
//    }
//
//    public void setMode(MultiViewMode mode) {
//        invokeLater(new Runnable() {
//
//            @Override
//            public void run() {
//                if (currentMode!=null)
//                    remove((JComponent)currentMode);
//
//                currentMode = mode;
//
//                add((JComponent)mode, BorderLayout.CENTER);
//
//            }
//
//        });
//    }
//
//    public JMenu newMenu() {
//        JMenu m = new JMenu("\uf085");
//        m.setFont(Video.FontAwesome);
//        m.add(new JMenuItem("Statement List"));
//
//        JMenuItem log;
//        m.add(log = new JMenuItem("Log"));
//        log.addActionListener(new ActionListener() {
//            @Override public void actionPerformed(ActionEvent e) {
//                setMode(new LogView());
//            }
//        });
//
////        JMenuItem conceptNetwork;
////        m.add(conceptNetwork = new JMenuItem("Concepts Network"));
////        conceptNetwork.addActionListener(new ActionListener() {
////            @Override public void actionPerformed(ActionEvent e) {
////                setMode(new GraphView());
////            }
////        });
//
//
//        m.add(new JMenuItem("Concept List"));
//        m.add(new JMenuItem("Concept Cloud"));
//
//        m.add(new JMenuItem("Statements Network"));
//        m.add(new JMenuItem("Truth vs. Confidence"));
//
//        m.addSeparator();
//
//        NSliderSwing fontSlider;
//        fontSlider = new NSliderSwing() { // (float)11, 6.0f, 40.0f) {
//            @Override
//            public void onChange(float v) {
//                setFontSize(v);
//            }
//        };
//        fontSlider.setMin(6f);
//        fontSlider.setMax(40f);
//        fontSlider.setValue(11f);
//        JPanel fontPanel = new JPanel(new BorderLayout());
//        fontSlider.setPrefix("Font size: ");
//        fontPanel.add(fontSlider);
//        m.add(fontPanel);
//
//        JPanel priorityPanel = new JPanel(new FlowLayout());
//        priorityPanel.add(new JButton("+"));
//        priorityPanel.add(new JLabel("Priority"));
//        priorityPanel.add(new JButton("-"));
//        m.add(priorityPanel);
//
//        m.addSeparator();
//        m.add(new JMenuItem("Close"));
//
//        return m;
//    }
//
//    public void setFontSize(float newSize) {
//        if (currentMode!=null) {
//            currentMode.setFontSize(newSize);
//        }
//    }
//
//    public String getLabel() {
//        return label;
//    }
//
//    public JButton newStatusButton() {
//        JButton statusButton = new JButton(" " + getLabel()) {
//
//        Color bgColor = Color.WHITE;
//        private Color fgColor = Color.WHITE;
//
//            @Override public void paint(Graphics g) {
//                int h = getHeight();
//
//                bgColor = new Color(activity/2, activity*activity,0 );
//
//                if (object instanceof Task) {
//                    Task task = (Task)object;
//                    fgColor = new Color(
//                            0.75f + 0.25f * task.getPriority(),
//                            0.75f + 0.25f * task.getDurability(),
//                            0.75f + 0.25f * task.getQuality());
//                }
//
//                setForeground(fgColor);
//                /*g.setColor(c);
//                g.fillRect(0, 0, getWidth(), getHeight());                    */
//                super.paint(g);
//
//                g.setColor(bgColor);
//                g.fillRect(2, 2, h/2-2, h-2);
//
//
//            }
//
//        };
//
//        return statusButton;
//    }
//
//
//    public void output(final Class channel, final Object signal) {
//        if (currentMode!=null)
//            currentMode.output(channel, signal);
//
//        updateActivity(activity + activityIncrement);
//
//    }
//    public void updateActivity(float newActivity) {
//        if (activity!=newActivity) {
//            activity = newActivity;
//            if (activity > 1.0f) activity = 1.0f;
//        }
//    }
//
//    public void decayActvity() {
//        updateActivity(activity * activityMomentum);
//    }
//
//
//
// }
