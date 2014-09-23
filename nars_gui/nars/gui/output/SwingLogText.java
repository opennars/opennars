package nars.gui.output;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import nars.core.NAR;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.entity.TruthValue;
import nars.io.Output;
import nars.io.Output.OUT;


public class SwingLogText extends SwingText implements Output {
    private final NAR nar;
    public boolean showStamp = false;
    final List<LogLine> pendingDisplay = new ArrayList();

    
    public static class LogLine {
        public final Class c;
        public final Object o;

        public LogLine(Class c, Object o) {
            this.c = c;
            this.o = o;
        }
        
    }
    
    

    public SwingLogText(NAR n) {        
        super();
        
        this.nar = n;
    }

    
    @Override
    public void output(final Class c, final Object o) {
        final LogLine ll = new LogLine(c, o);
                
        synchronized (pendingDisplay) {            
            pendingDisplay.add(ll);
        }
        
        if (pendingDisplay.size() == 1) {
            SwingUtilities.invokeLater(update);
        }        
    }
    
    public final Runnable update = new Runnable() {
        
        List<LogLine> toDisplay = new ArrayList();
        
        @Override public void run() {
            limitBuffer();
            
            synchronized (pendingDisplay) {
                toDisplay.addAll(pendingDisplay);
                pendingDisplay.clear();
            }
            int displayCount = toDisplay.size();
            for (int i = 0; i < displayCount; i++) {
                LogLine l = toDisplay.get(i);
                print(l.c, l.o);
            }
            toDisplay.clear();                
            repaint();
            
        }
    };
    
    
    protected void print(final Class c, final Object o)  {        
        //Color defaultColor = Color.WHITE;

        float priority = 1f;
        if (c!=OUT.class) {
            //pad the channel name to max 6 characters, right aligned
            
            String n = c.getSimpleName();
            n = n.substring(0,Math.min(6, n.length()));
            switch (n.length()) {
                case 0: break;
                case 1: n = "     " + n; break;
                case 2: n = "    " + n; break;
                case 3: n = "   " + n; break;
                case 4: n = "  " + n; break;
                case 5: n = " " + n; break;                    
            }
            
            print(LogPanel.getChannelColor(c), n);
        }
        
        else {
            if (o instanceof Task) {
                Task t = (Task)o;
                priority = t.budget.getPriority();
                printColorBlock(LogPanel.getPriorityColor(priority), "  ");
                
                Sentence s = t.sentence;
                if (s!=null) {
                    TruthValue tv = s.truth;
                    if (tv!=null) {                    
                        printColorBlock(LogPanel.getFrequencyColor(tv.getFrequency()), "  ");
                        printColorBlock(LogPanel.getConfidenceColor(tv.getConfidence()), "  ");                        
                    }
                    else if ( t.getBestSolution()!=null) {
                        printColorBlock(LogPanel.getStatementColor('=', priority), "    ");
                    }
                    else {                        
                        printColorBlock(LogPanel.getStatementColor(s.punctuation, priority), "    ");                   
                    }
                }
            }
        }
        
        float tc = 0.75f + 0.25f * priority;
        Color textColor = new Color(tc, tc, tc);
        print(textColor, ' ' + LogPanel.getText(o, showStamp, nar) + '\n');
        try {
            setCaretPosition(getDocument().getLength());
        }
        catch (Throwable e) { }
        
    }
    
    

}
