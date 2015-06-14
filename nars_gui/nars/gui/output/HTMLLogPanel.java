package nars.gui.output;

import java.awt.BorderLayout;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javax.swing.JLabel;
import nars.gui.NARControls;
import nars.io.TextOutput;

/**
 *
 * @author me
 */
public class HTMLLogPanel extends LogPanel {

    private WebView webView;

    public HTMLLogPanel(NARControls narcontrol) {
        super(narcontrol);

        add(new JLabel("Loading..."), BorderLayout.CENTER);

        final JFXPanel fxPanel = new JFXPanel();
        add(fxPanel, BorderLayout.CENTER);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                initFX(fxPanel);
            }
        });
    }

    private void initFX(JFXPanel fxPanel) {
        Scene scene = createScene();
        fxPanel.setScene(scene);
    }

    private Scene createScene() {
        webView = new WebView();
        Scene scene = new Scene(webView, Color.BLACK);
        webView.getEngine().loadContent("<html><body style='font-family: Arial, sans; background-color: black; color: white'><div id='content'></div></body></html>");
        return (scene);
    }

    @Override
    protected void setFontSize(double v) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void clearLog() {
        js("document.getElementById('content').innerHTML='';");
    }
    
    protected void js(final String js) {
        Platform.runLater(new Runnable() {
            @Override public void run() { webView.getEngine().executeScript(js); }
        });        
    }

    @Override
    void print(Class c, Object o) {
        
        String s = TextOutput.getOutputString(c, o, true, showStamp, nar);
        if (logFile != null) {
            logFile.println(s);
        }
        
        StringBuilder h = getOutputHTML(c, o, true, showStamp);

        js( new StringBuilder().append("var e = document.createElement('div'); e.innerHTML=\"").append(h).append("\";  document.getElementById('content').appendChild(e);").toString() );
        
        scrollBottom();
    }

    protected void scrollBottom() {
        js("window.scrollTo(0, document.body.scrollHeight)");        
    }
    
    public StringBuilder getOutputHTML(Class c, Object o, boolean showChannel, boolean showStamp) {
        StringBuilder b = new StringBuilder();
        if (c == OUT.class) {            
            b.append(TextOutput.getOutputHTML(c, o, true, showStamp, nar));
        }
        else {
            b.append("<pre>").append(TextOutput.escapeHTML(TextOutput.getOutputString(c, o, true, showStamp, nar))).append("</pre>");
            
        }
        return b;
    }

    @Override
    void limitBuffer(int incomingDataSize) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }


}
