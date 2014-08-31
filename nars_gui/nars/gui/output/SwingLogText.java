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


public class SwingLogText extends JTextPane {
    private final NAR nar;

    /** # of messages to buffer in log */
    private int BufferLength = 512;
    
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
        fontSize *= getFont().getSize();
        
        StyleContext sc = StyleContext.getDefaultStyleContext();
        MutableAttributeSet aset = getInputAttributes();
        
        StyleConstants.setForeground(aset, color);
        StyleConstants.setFontSize(aset, (int)fontSize);
        StyleConstants.setBold(aset, bold);

        try {
            doc.insertString(doc.getLength(), text, null);
            
            //getStyledDocument().setCharacterAttributes(doc.getLength() - text.length(), text.length(), aset, true);            
        } catch (BadLocationException ex) {
            Logger.getLogger(SwingLogText.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    protected void print(final Class c, final Object o)  {        
        Color defaultColor = Color.WHITE;

        print(LogPanel.getChannelColor(c), c.getSimpleName() + ": ", false, 1);        
        print(defaultColor, LogPanel.getText(o, showStamp, nar) + '\n', false, 1);        
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
