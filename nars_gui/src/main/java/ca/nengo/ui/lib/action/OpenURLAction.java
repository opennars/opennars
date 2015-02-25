package ca.nengo.ui.lib.action;

import ca.nengo.ui.lib.util.UIEnvironment;

import javax.swing.*;
import java.io.IOException;

public class OpenURLAction extends StandardAction {
    
    static final String[] browsers = {"google-chrome", "chromium", "firefox",
    	"opera", "konqueror"};
	
    private static final long serialVersionUID = 1L;
    private final String url;

    public OpenURLAction(String helpstring, String url) {
        super("Open URL", helpstring);

        this.url = url;
    }

    @Override
    protected void action() throws ActionException {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.startsWith("win")) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + this.url);
            } else if (os.startsWith("mac")){ 
                Runtime.getRuntime().exec("open " + this.url);
            } else {
            	Boolean ran = false;
            	for (String browser : browsers) {
            		try {
                		if (Runtime.getRuntime().exec(new String[]
                				{"which", browser }).waitFor() == 0) {
                			Runtime.getRuntime().exec(new String[] { browser, url });
                			ran = true;
                			break;
                		}
            		} catch (InterruptedException e) {
            			ran = false;
            		}
            	}
            	if (!ran) {
            		JOptionPane.showMessageDialog(UIEnvironment.getInstance(),
            				"Could not open browser automatically. " + 
            				"Please navigate to" + this.url,
            				"URL can't be opened", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
