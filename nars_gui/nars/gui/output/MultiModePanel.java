
package nars.gui.output;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import static javax.swing.SwingUtilities.invokeLater;
import nars.core.NAR;
import nars.entity.Task;
import nars.gui.NARSwing;
import nars.gui.NSlider;
import nars.io.Output;

/**
 *
 * @author me
 */
public class MultiModePanel extends JPanel implements Output {
    final float activityIncrement = 0.5f; //per output
    final float activityMomentum = 0.95f;

    private final NAR nar;
    private final Object object;
    float activity = 0;
    //private final String label;
    private final String label;
    MultiViewMode currentMode = null;


    public interface MultiViewMode extends Output {

        public void setFontSize(float newSize);
        
    }
    
    /** extension of SwingLogText with functionality specific to MultiLogPanel */
    public class LogView extends SwingLogText implements MultiViewMode {

        public LogView() {
            super(nar);
            
            SwingLogPanel.setConsoleStyle(this, true);
            
        }


        
    }

    
    public class GraphView extends ProcessingGraphPanel implements MultiViewMode, Runnable {

        public GraphView() {
            super(nar);
            
        }

        
        @Override
        public void setFontSize(float newSize) {
        }

        @Override
        public void output(Class channel, Object signal) {
            addItem(signal);
            
            update();
            
            /*if (!needsUpdate)*/ {
                needsUpdate = true;
                SwingUtilities.invokeLater(this);            
            }
        }
        
        boolean needsUpdate = false;
        
        @Override
        public void run() {
            needsUpdate = false;
            redraw();
        }

        
        
        
    }
    

    public MultiModePanel(NAR nar, Object object) {
        super(new BorderLayout());
        this.nar = nar;
        this.object = object;
  
          
        if (object instanceof Task) {
            label = ((Task)object).sentence.toString();
        }
        else {
            label = object.toString();
        }
        
        setMode(new LogView());
    }
    
    public void setMode(MultiViewMode mode) {
        invokeLater(new Runnable() {

            @Override
            public void run() {
                if (currentMode!=null)
                    remove((JComponent)currentMode);

                currentMode = mode;

                add((JComponent)mode, BorderLayout.CENTER);
                
            }
            
        });
    }
    
    public JMenu newMenu() {
        JMenu m = new JMenu("\uf085");
        m.setFont(NARSwing.FontAwesome);
        m.add(new JMenuItem("Statement List"));
        
        JMenuItem log;
        m.add(log = new JMenuItem("Log"));
        log.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                setMode(new LogView());
            }            
        });

        JMenuItem conceptNetwork;
        m.add(conceptNetwork = new JMenuItem("Concepts Network"));
        conceptNetwork.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                setMode(new GraphView());
            }            
        });
        
        
        m.add(new JMenuItem("Concept List"));
        m.add(new JMenuItem("Concept Cloud"));        
        
        m.add(new JMenuItem("Statements Network"));
        m.add(new JMenuItem("Truth vs. Confidence"));        
        
        m.addSeparator();
        
        NSlider fontSlider;
        fontSlider = new NSlider() { // (float)11, 6.0f, 40.0f) {
            @Override
            public void onChange(float v) {
                setFontSize(v);
            }
        };
        fontSlider.setMin(6f);
        fontSlider.setMax(40f);
        fontSlider.setValue(11f);
        JPanel fontPanel = new JPanel(new BorderLayout());        
        fontSlider.setPrefix("Font size: ");
        fontPanel.add(fontSlider);
        m.add(fontPanel);
        
        JPanel priorityPanel = new JPanel(new FlowLayout());        
        priorityPanel.add(new JButton("+"));
        priorityPanel.add(new JLabel("Priority"));
        priorityPanel.add(new JButton("-"));
        m.add(priorityPanel);
        
        m.addSeparator();
        m.add(new JMenuItem("Close"));
        
        return m;
    }
    
    public void setFontSize(float newSize) {
        if (currentMode!=null) {
            currentMode.setFontSize(newSize);
        }
    }
            
    public String getLabel() {
        return label;
    }
        
    public JButton newStatusButton() {
        JButton statusButton = new JButton(getLabel()) {

        Color bgColor = Color.WHITE;
        private Color fgColor = Color.WHITE;
            
            @Override public void paint(Graphics g) {
                int h = getHeight();

                bgColor = new Color(activity/2, activity*activity,0 );
                
                if (object instanceof Task) {
                    Task task = (Task)object;
                    fgColor = new Color(
                            0.75f + 0.25f * task.getPriority(), 
                            0.75f + 0.25f * task.getDurability(),                             
                            0.75f + 0.25f * task.getQuality());
                }
                
                setForeground(fgColor);                    
                /*g.setColor(c);
                g.fillRect(0, 0, getWidth(), getHeight());                    */
                super.paint(g);

                g.setColor(bgColor);
                g.fillRect(2, 2, h/2-2, h-2);


            }

        };
        
        return statusButton;        
    }

    @Override
    public void output(final Class channel, final Object signal) {
        if (currentMode!=null)
            currentMode.output(channel, signal);
        
        updateActivity(activity + activityIncrement);
        
    }
    public void updateActivity(float newActivity) {
        if (activity!=newActivity) {
            activity = newActivity;
            if (activity > 1.0f) activity = 1.0f;
        }
    }

    public void decayActvity() {
        updateActivity(activity * activityMomentum);
    }
    
    
    
}
