package automenta.vivisect.surfaceplotter;

import javax.swing.*;
import java.awt.*;

public class JSurfaceApplet extends JApplet {

	

	@Override
	public void init() {
			//initComponents();
			try {
			
	        SwingUtilities.invokeAndWait(this::initComponents);
	    } catch (Exception e) {
	        System.err.println("createGUI didn't successfully complete"+ e);
	        e.printStackTrace();
	    }
	}

	private void initComponents() {
		surfacePanel1 = new JSurfacePanel();

		//======== this ========
		setLayout(new BorderLayout());

		//---- surfacePanel1 ----
		surfacePanel1.setTitleText("Demo Applet");
		surfacePanel1.setBackground(Color.white);
		surfacePanel1.setTitleFont(surfacePanel1.getTitleFont().deriveFont(surfacePanel1.getTitleFont().getStyle() | Font.BOLD, surfacePanel1.getTitleFont().getSize() + 6.0f));
		surfacePanel1.setConfigurationVisible(false);
		add(surfacePanel1, BorderLayout.CENTER);

	}

	private JSurfacePanel surfacePanel1;
}
