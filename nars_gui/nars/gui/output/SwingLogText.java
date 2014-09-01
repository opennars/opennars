package nars.gui.output;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import nars.core.NAR;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.entity.TruthValue;
import nars.io.Output.OUT;


public class SwingLogText extends JTextPane {
    private final NAR nar;

    /** # of messages to buffer in log */
    private int BufferLength = 512;
    private final int baseFontSize;
    
    public static class LogLine {
        public final Class c;
        public final Object o;

        public LogLine(Class c, Object o) {
            this.c = c;
            this.o = o;
        }
        
    }
    
    public boolean showStamp = false;
    
    private final DefaultStyledDocument doc;
    private final Style mainStyle;
    private Deque<LogLine> nextOutput = new ArrayDeque();

    public SwingLogText(NAR n) {        
        super(new DefaultStyledDocument(new StyleContext()));
        
        this.nar = n;
        
        doc = (DefaultStyledDocument) getDocument();
        setEditable(false);
        // Create and add the main document style
        Style defaultStyle = getStyle(StyleContext.DEFAULT_STYLE);
        mainStyle = doc.addStyle("MainStyle", defaultStyle);
        //StyleConstants.setLeftIndent(mainStyle, 16);
        //StyleConstants.setRightIndent(mainStyle, 16);
        //StyleConstants.setFirstLineIndent(mainStyle, 16);
        //StyleConstants.setFontFamily(mainStyle, "serif");
        //StyleConstants.setFontSize(mainStyle, 12);
        doc.setLogicalStyle(0, mainStyle);
        
        this.baseFontSize = getFont().getSize();


        
    }

    final List<LogLine> pendingDisplay = new ArrayList();
    
    public void out(final Class c, final Object o) {
        final LogLine ll = new LogLine(c, o);
        synchronized (nextOutput) {
            nextOutput.offer(ll);
            if (nextOutput.size() == BufferLength)
                nextOutput.removeLast();
        }
        
        if (isVisible()) {
            synchronized (pendingDisplay) {
                if (pendingDisplay.size() == 0) {                
                    SwingUtilities.invokeLater(update);
                }
                pendingDisplay.add(ll);
            }
        }
    }
    
    public final Runnable update = new Runnable() {
        @Override public void run() {
            synchronized (pendingDisplay) {
                for (LogLine l : pendingDisplay) {             
                    print(l.c, l.o);
                }
                pendingDisplay.clear();
            }
        }
    };
    
    protected void print(final Color color, final String text, final boolean bold, float fontSize)  {                
        fontSize *= baseFontSize;
        
        StyleContext sc = StyleContext.getDefaultStyleContext();
        MutableAttributeSet aset = getInputAttributes();
        
        StyleConstants.setForeground(aset, color);
        StyleConstants.setBackground(aset, Color.BLACK);
        StyleConstants.setFontSize(aset, (int)fontSize);
        StyleConstants.setBold(aset, bold);

        try {
            doc.insertString(doc.getLength(), text, aset);            
        } catch (BadLocationException ex) {
            Logger.getLogger(SwingLogText.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    protected void printColorBlock(final Color color, int num, float fontSize)  {                
        fontSize *= baseFontSize;
        
        StyleContext sc = StyleContext.getDefaultStyleContext();
        MutableAttributeSet aset = getInputAttributes();
        
        StyleConstants.setBackground(aset, color);
        StyleConstants.setFontSize(aset, (int)fontSize);        

        String s;
        switch (num) {
            case 1: s = " "; break;
            case 2: s = "  "; break;
            case 3: s = "   "; break;
                //TODO handle > 3
            default: 
                s = "    "; break;
        }
            
        try {
            doc.insertString(doc.getLength(), s, aset);            
        } catch (BadLocationException ex) {         }        
    }
    
    protected void print(final Class c, final Object o)  {        
        Color defaultColor = Color.WHITE;

        float fontScale = 2f;
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
            
            print(LogPanel.getChannelColor(c), n, false, fontScale);        
        }
        
        else {
            if (o instanceof Task) {
                Task t = (Task)o;
                priority = t.budget.getPriority();
                printColorBlock(LogPanel.getPriorityColor(priority), 2, fontScale);
                
                Sentence s = t.sentence;
                if (s!=null) {
                    TruthValue tv = s.truth;
                    if (tv!=null) {                    
                        printColorBlock(LogPanel.getFrequencyColor(tv.getFrequency()), 2, fontScale);
                        printColorBlock(LogPanel.getConfidenceColor(tv.getConfidence()), 2, fontScale);                        
                    }
                    else {
                        
                        printColorBlock(LogPanel.getStatementColor(s.punctuation, priority), 4, fontScale);                   
                    }
                }
            }
        }
        
        float tc = 0.75f + 0.25f * priority;
        Color textColor = new Color(tc, tc, tc);
        print(textColor, ' ' + LogPanel.getText(o, showStamp, nar) + '\n', false, fontScale);
    }
    
    

    public void print(Class c, Object o, boolean showStamp, NAR n) {        
        //limitBuffer(nextOutput.baseLength());
        limitBuffer(128);
        out(c, o);        
    }
    

    void limitBuffer(int incomingDataSize) {
        Document doc = getDocument();
        int overLength = doc.getLength() + incomingDataSize - LogPanel.maxIOTextSize;
        if (overLength > 0) {
            try {
                doc.remove(0, overLength);
            } catch (BadLocationException ex) {
            }
        }
    }
}
