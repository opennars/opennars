//package nars.gui.output;
//
//import nars.Video;
//
//import javax.swing.*;
//import javax.swing.text.*;
//import java.awt.*;
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
//public class SwingText extends JTextPane {
//
//    /**
//     * # of messages to buffer in log
//     */
//    int maxLineWidth = 400;
//    protected final float baseFontScale = 1.0f;
//    protected final DefaultStyledDocument doc;
//    protected final Style mainStyle;
//
//    public SwingText() {
//        super(new DefaultStyledDocument(new StyleContext()));
//
//        doc = (DefaultStyledDocument) getDocument();
//        setEditable(false);
//
//        // Create and add the main document style
//        Style defaultStyle = getStyle(StyleContext.DEFAULT_STYLE);
//        mainStyle = doc.addStyle("MainStyle", defaultStyle);
//
//        //StyleConstants.setLeftIndent(mainStyle, 16);
//        //StyleConstants.setRightIndent(mainStyle, 16);
//        //StyleConstants.setFirstLineIndent(mainStyle, 16);
//        //StyleConstants.setFontFamily(mainStyle, Video.monofont.getFamily());
//        //StyleConstants.setFontSize(mainStyle, 16);
//        doc.setLogicalStyle(0, mainStyle);
//
//        addMouseListener(new ClickableLineHandler(this));
//
//    }
//
//    public void print(final Color color, final CharSequence text) {
//        //System.out.println("print:: " + text);
//        print(color, null, text, null);
//    }
//
//    public void print(final Color color, final CharSequence text, Action a) {
//        print(color, null, text, a);
//    }
//
//    public void print(final Color color, final Color bgColor, CharSequence text, Action action) {
//
//        MutableAttributeSet aset = getInputAttributes();
//        StyleConstants.setForeground(aset, color);
//
//
//        StyleConstants.setBackground(aset, bgColor != null ? bgColor : Video.transparent /* Color.BLACK */);
//
//        //StyleConstants.setUnderline(aset, false);
//        //StyleConstants.setBold(aset, bold);
//
//        try {
//            MutableAttributeSet attr;
//            if (action == null) {
//                attr = aset;
//            } else {
//                //http://stackoverflow.com/questions/16131811/clickable-text-in-a-jtextpane
//                Style link = doc.addStyle(null, StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE));
//                StyleConstants.setForeground(link, color);
//                //StyleConstants.setUnderline(tlink, true);
//                //StyleConstants.setBold(tlink, true);
//                link.addAttribute("linkact", action);
//                attr = link;
//            }
//            doc.insertString(doc.getLength(), text.toString(), attr);
//        } catch (BadLocationException ex) {
//            Logger.getLogger(SwingText.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//    }
//
//    public void printBlock(final Color color, final String s) {
//
//
//        //StyleContext sc = StyleContext.getDefaultStyleContext();
//        MutableAttributeSet aset = getInputAttributes();
//        StyleConstants.setBackground(aset, color);
//        try {
//            int l = doc.getLength();
//            doc.insertString(l, s, aset);
//        } catch (BadLocationException ex) {
//            Logger.getLogger(SwingLogText.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//
//    //    public void print(Class c, Object o, boolean showStamp, NAR n) {
//    //        //limitBuffer(nextOutput.baseLength());
//    //        out(c, o);
//    //    }
//    void limitBuffer() {
//        Document doc = getDocument();
//        int overLength = doc.getLength() - LogPanel.maxIOTextSize;
//        if (overLength > 0) {
//            try {
//                doc.remove(0, overLength + LogPanel.clearMargin);
//            } catch (BadLocationException ex) {
//            }
//        }
//    }
//
//    public void setFontSize(float v) {
//        setFont(Video.monofont.deriveFont(v));
//    }
//
//    private static class ClickableLineHandler extends MouseAdapter {
//
//        private final SwingText swingText;
//
//        public ClickableLineHandler(SwingText swingText) {
//            this.swingText = swingText;
//        }
//
//        @Override
//        public void mouseClicked(MouseEvent e) {
//            Element ele = swingText.doc.getCharacterElement(swingText.viewToModel(e.getPoint()));
//            AttributeSet as = ele.getAttributes();
//            Action fla = (Action) as.getAttribute("linkact");
//            if (fla != null) {
//                fla.actionPerformed(null);
//            }
//        }
//
//    }
// }
