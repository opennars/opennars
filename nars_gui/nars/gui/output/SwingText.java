package nars.gui.output;

import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import nars.gui.NARSwing;

public class SwingText extends JTextPane {

    /**
     * # of messages to buffer in log
     */
    int maxLineWidth = 120;
    protected final float baseFontScale = 1.0f;
    protected final DefaultStyledDocument doc;
    protected final Style mainStyle;

            
    public SwingText() {
        super(new DefaultStyledDocument(new StyleContext()));

        doc = (DefaultStyledDocument) getDocument();
        setEditable(false);

        // Create and add the main document style
        Style defaultStyle = getStyle(StyleContext.DEFAULT_STYLE);
        mainStyle = doc.addStyle("MainStyle", defaultStyle);

        //StyleConstants.setLeftIndent(mainStyle, 16);
        //StyleConstants.setRightIndent(mainStyle, 16);
        //StyleConstants.setFirstLineIndent(mainStyle, 16);
        StyleConstants.setFontFamily(mainStyle, NARSwing.monofont.getFamily());
        //StyleConstants.setFontSize(mainStyle, 16);

        doc.setLogicalStyle(0, mainStyle);

    }

    public void print(final Color color, final String text) {
        //System.out.println("print:: " + text);
        print(color, null, text);
    }

    public void print(final Color color, final Color bgColor, String text) {
        if (text.length() > maxLineWidth) {
            text = text.substring(0,maxLineWidth);
        }

        MutableAttributeSet aset = getInputAttributes();
        StyleConstants.setForeground(aset, color);
        StyleConstants.setBackground(aset, bgColor != null ? bgColor : Color.BLACK);
        //StyleConstants.setBold(aset, bold);
        try {
            doc.insertString(doc.getLength(), text, aset);
        } catch (BadLocationException ex) {
            Logger.getLogger(SwingText.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void printColorBlock(final Color color, final String s) {

        //StyleContext sc = StyleContext.getDefaultStyleContext();
        MutableAttributeSet aset = getInputAttributes();
        StyleConstants.setBackground(aset, color);
        try {
            int l = doc.getLength();
            doc.insertString(l, s, aset);
        } catch (BadLocationException ex) {
            Logger.getLogger(SwingLogText.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //    public void print(Class c, Object o, boolean showStamp, NAR n) {
    //        //limitBuffer(nextOutput.baseLength());
    //        out(c, o);
    //    }
    void limitBuffer() {
        Document doc = getDocument();
        int overLength = doc.getLength() - LogPanel.maxIOTextSize;
        if (overLength > 0) {
            try {
                doc.remove(0, overLength);
            } catch (BadLocationException ex) {
            }
        }
    }

    public void setFontSize(float v) {
        setFont(NARSwing.monofont.deriveFont(v));
    }

}
