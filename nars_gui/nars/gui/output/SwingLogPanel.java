package nars.gui.output;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import nars.core.NAR;
import nars.gui.NARControls;
import nars.io.TextOutput;

public class SwingLogPanel extends LogPanel {

    public final SwingLogText ioText;


    public static class SwingLogText extends JTextPane {
        private final DefaultStyledDocument doc;
        private final Style mainStyle;
        private Collection<String> nextOutput = new ConcurrentLinkedQueue();

        public SwingLogText() {
            super(new DefaultStyledDocument(new StyleContext()));
            doc = (DefaultStyledDocument)getDocument();
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
    
        protected void print(Color c, float size, String text, boolean bold) {
            StyleContext sc = StyleContext.getDefaultStyleContext();

            MutableAttributeSet aset = getInputAttributes();

            Font f = getFont();
            StyleConstants.setForeground(aset, c);
            //StyleConstants.setFontSize(aset, (int)(f.getSize()*size));
            StyleConstants.setBold(aset, bold);

            try {
                doc.insertString(doc.getLength(), text, null);

                getStyledDocument().setCharacterAttributes(doc.getLength() - text.length(), text.length(), aset, true);
            } catch (BadLocationException ex) {
                Logger.getLogger(NARControls.class.getName()).log(Level.SEVERE, null, ex);
            }

        }       
        
        
        String print(Class c, Object o, boolean showStamp, NAR n) {

             String s = TextOutput.getOutputString(c, o, true, showStamp, n);

             nextOutput.add(s);

             SwingUtilities.invokeLater(nextOutputRunnable);
             
             return s;
         }



         private final Runnable nextOutputRunnable = new Runnable() {
             @Override
             public void run() {
                 if (nextOutput.size() > 0) {

                     try {
                         //limitBuffer(nextOutput.baseLength());
                         limitBuffer(128);

                         for (String o : nextOutput) {
                             print(getLineColor(o), 1.0f, o + '\n', false);
                         }

                         nextOutput.clear();
                     } catch (Exception e) {
                         System.err.println(e);
                         e.printStackTrace();
                     }
                 }
             }
         };

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
    
    public SwingLogPanel(NARControls narControls) {
        super(narControls);


        ioText = new SwingLogText();


        //http://stackoverflow.com/questions/4702891/toggling-text-wrap-in-a-jtextpane        
        JPanel ioTextWrap = new JPanel(new BorderLayout());
        ioTextWrap.add(ioText);
        JScrollPane ioTextScroll = new JScrollPane(ioTextWrap);
        add(ioTextScroll, BorderLayout.CENTER);

        addPopupMenu();
        
        setConsoleStyle(ioText, true);

        ioTextWrap.setBorder(new EmptyBorder(0, 0, 0, 0));
        ioTextScroll.setBorder(new EmptyBorder(0, 0, 0, 0));
        setBackground(Color.BLACK);
    }

    @Override
    void print(final Class c, final Object o) {
        String s = ioText.print(c, o, showStamp, nar);

        if (logFile != null) {
            logFile.println(s);
        }        
    }

    
 
    
    @Override
    protected void setFontSize(double v) {
        ioText.setFont(ioText.getFont().deriveFont((float) v));
    }

    @Override
    protected void clearLog() {
        ioText.setText("");
    }

    
    
   public static void setConsoleStyle(JTextComponent c, boolean invert) {
        if (invert) {
            c.setForeground(Color.WHITE);
            c.setCaretColor(Color.WHITE);
            c.setBackground(Color.BLACK);
        } else {
            c.setForeground(Color.BLACK);
            c.setCaretColor(Color.BLACK);
            c.setBackground(Color.WHITE);

        }
        c.setBorder(new EmptyBorder(0, 0, 0, 0));
        c.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
    }

    final static String COPY = "Copy";
    final static String CUT = "Cut";
    final static String PASTE = "Paste";
    final static String SELECTALL = "Select All";

    /**
     * @see:
     * http://www.objectdefinitions.com/odblog/2007/jtextarea-with-popup-menu/
     */
    private void addPopupMenu() {

        final JPopupMenu menu = new JPopupMenu();
        final JMenuItem copyItem = new JMenuItem();
        copyItem.setAction(ioText.getActionMap().get(DefaultEditorKit.copyAction));
        copyItem.setText(COPY);

        final JMenuItem cutItem = new JMenuItem();
        cutItem.setAction(ioText.getActionMap().get(DefaultEditorKit.cutAction));
        cutItem.setText(CUT);

        final JMenuItem pasteItem = new JMenuItem(PASTE);
        pasteItem.setAction(ioText.getActionMap().get(DefaultEditorKit.pasteAction));
        pasteItem.setText(PASTE);

        final JMenuItem selectAllItem = new JMenuItem(SELECTALL);
        selectAllItem.setAction(ioText.getActionMap().get(DefaultEditorKit.selectAllAction));
        selectAllItem.setText(SELECTALL);

        menu.add(copyItem);
        menu.add(cutItem);
        menu.add(pasteItem);
        menu.add(new JSeparator());
        menu.add(selectAllItem);

        ioText.add(menu);
        ioText.addMouseListener(new PopupTriggerMouseListener(menu, ioText));

    }

    public static class PopupTriggerMouseListener extends MouseAdapter {

        private JPopupMenu popup;
        private JComponent component;

        public PopupTriggerMouseListener(JPopupMenu popup, JComponent component) {
            this.popup = popup;
            this.component = component;
        }

        //some systems trigger popup on mouse press, others on mouse release, we want to cater for both
        private void showMenuIfPopupTrigger(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popup.show(component, e.getX() + 3, e.getY() + 3);
            }
        }

        //according to the javadocs on isPopupTrigger, checking for popup trigger on mousePressed and mouseReleased 
        //should be all  that is required
        //public void mouseClicked(MouseEvent e)  
        //{
        //    showMenuIfPopupTrigger(e);
        //}
        public void mousePressed(MouseEvent e) {
            showMenuIfPopupTrigger(e);
        }

        public void mouseReleased(MouseEvent e) {
            showMenuIfPopupTrigger(e);
        }

    }

    @Override
    void limitBuffer(int incomingDataSize) {
        ioText.limitBuffer(incomingDataSize);
    }
    

    
    
}
