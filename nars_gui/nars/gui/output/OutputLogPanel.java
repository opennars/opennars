package nars.gui.output;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
import nars.gui.NARControls;
import nars.gui.NPanel;
import nars.gui.NSlider;
import nars.io.Output;

/**
 *
 * @author me
 */


public class OutputLogPanel extends NPanel implements Output {
    private final DefaultStyledDocument doc;
    private final JTextPane ioText;
    private final Style mainStyle;
    private final NAR nar;
    int maxIOTextSize = (int) 8E6;
    private boolean showErrors = false;
    private boolean showStamp = false;
    

    private Collection nextOutput = new ConcurrentLinkedQueue();

    public OutputLogPanel(NAR s) {
        super();
        setLayout(new BorderLayout());
        
        this.nar = s;
        
        
        StyleContext sc = new StyleContext();
        doc = new DefaultStyledDocument(sc);
        
        ioText = new JTextPane(doc);
        ioText.setEditable(false);
        

        // Create and add the main document style
        Style defaultStyle = sc.getStyle(StyleContext.DEFAULT_STYLE);
        mainStyle = sc.addStyle("MainStyle", defaultStyle);
        //StyleConstants.setLeftIndent(mainStyle, 16);
        //StyleConstants.setRightIndent(mainStyle, 16);
        //StyleConstants.setFirstLineIndent(mainStyle, 16);
        //StyleConstants.setFontFamily(mainStyle, "serif");
        //StyleConstants.setFontSize(mainStyle, 12);
        doc.setLogicalStyle(0, mainStyle);

        //http://stackoverflow.com/questions/4702891/toggling-text-wrap-in-a-jtextpane        
        JPanel ioTextWrap = new JPanel(new BorderLayout());
        ioTextWrap.add(ioText);
        
        add(new JScrollPane(ioTextWrap), BorderLayout.CENTER);
        
        
        
        JPanel menu = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        final JCheckBox showErrorBox = new JCheckBox("Show Errors");
        showErrorBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showErrors = showErrorBox.isSelected();
            }
        });
        menu.add(showErrorBox);
        
        final JCheckBox showStampBox = new JCheckBox("Show Stamp");
        showStampBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showStamp = showStampBox.isSelected();
            }
        });
        menu.add(showStampBox);        
        
        final NSlider fontSlider = new NSlider(ioText.getFont().getSize(), 6, 40) {

            @Override
            public void onChange(double v) {
                ioText.setFont(ioText.getFont().deriveFont((float)v));
            } 
         
        };        
        fontSlider.setPrefix("Font size: ");
        menu.add(fontSlider);
        
        
        add(menu, BorderLayout.SOUTH);

        addContainerListener(new ContainerListener() {

            @Override
            public void componentAdded(ContainerEvent e) {
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
            }
        });

    }

    @Override
    protected void onShowing(boolean showing) {
        if (showing)
            nar.addOutput(this);        
        else
            nar.removeOutput(this);                
    }

    
    @Override
    public void output(final Class c, Object o) {

        if ((!showErrors) && (c == ERR.class)) {
            return;
        }

        if (o instanceof Exception) {
            o = (o.toString() + " @ " + Arrays.asList(((Exception) o).getStackTrace()));
        }
        
        nextOutput.add(c.getSimpleName() + ": ");
        nextOutput.add(o);
        nextOutput.add('\n');
        SwingUtilities.invokeLater(nextOutputRunnable);

    }

    void limitBuffer(int incomingDataSize) {
        Document doc = ioText.getDocument();
        int overLength = doc.getLength() + incomingDataSize - maxIOTextSize;

        if (overLength > 0) {
            try {
                doc.remove(0, overLength);
            } catch (BadLocationException ex) {
            }
        }
    }

    protected void print(Color c, float size, String text, boolean bold) {
        StyleContext sc = StyleContext.getDefaultStyleContext();

        MutableAttributeSet aset = ioText.getInputAttributes();


        Font f = ioText.getFont();
        StyleConstants.setForeground(aset, c);
        //StyleConstants.setFontSize(aset, (int)(f.getSize()*size));
        StyleConstants.setBold(aset, bold);
        
        try {
            doc.insertString(doc.getLength(), text, null);

            ioText.getStyledDocument().setCharacterAttributes(doc.getLength() - text.length(), text.length(), aset, true);
        } catch (BadLocationException ex) {
            Logger.getLogger(NARControls.class.getName()).log(Level.SEVERE, null, ex);
        }

    }    
    
    private Runnable nextOutputRunnable = new Runnable() {
        @Override
        public void run() {
            if (nextOutput.size() > 0) {

                //limitBuffer(nextOutput.length());
                limitBuffer(128);

                for (Object o : nextOutput) {
                    if ((o instanceof String) || (o instanceof Character))
                        print(Color.BLACK, 1.0f, o.toString(), false);
                    else if (o instanceof Sentence) {
                        Sentence s = (Sentence)o;
                        
                        float conf = 0.5f, freq = 0.5f;
                        if (s.getTruth() != null) {
                            conf = s.getTruth().getConfidence();
                            freq = s.getTruth().getFrequency();                            
                        }
                        
                        float contentSize = 1f; //0.75f+conf;
                        
                        Color contentColor = Color.getHSBColor(0.5f + (freq-0.5f)/2f, 1.0f, 0.05f + 0.5f - conf/4f);                        
                        print(contentColor, contentSize, s.getContent().toString() + s.getPunctuation(), s.isQuestion());
                        
                        if (s.getTruth()!=null) {
                            Color truthColor = Color.getHSBColor(freq, 0, 0.25f - conf/4f);
                            print(truthColor, contentSize, s.getTruth().toString(), false);
                        }
                        if ((showStamp) && (s.getStamp()!=null)) {
                            Color stampColor = Color.GRAY;
                            print(stampColor, contentSize, s.getStamp().toString(), false);
                        }
                    }
                    else {
                        print(Color.BLACK, 1.0f, o.toString(), false);
                    }
                }

                nextOutput.clear();
            }
        }
    };
    
    
}
