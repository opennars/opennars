/*
 * Created by JFormDesigner on Tue May 17 09:54:22 CEST 2011
 */

package automenta.vivisect.surfaceplotter;

import automenta.vivisect.surfaceplotter.beans.JGridBagScrollPane;
import automenta.vivisect.surfaceplotter.surface.AbstractSurfaceModel;
import automenta.vivisect.surfaceplotter.surface.JSurface;
import automenta.vivisect.surfaceplotter.surface.SurfaceModel;
import automenta.vivisect.surfaceplotter.surface.SurfaceModel.PlotColor;
import automenta.vivisect.surfaceplotter.surface.SurfaceModel.PlotType;
import automenta.vivisect.surfaceplotter.surface.VerticalConfigurationPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Main panel to display a surface plot.
 * 
 * @author eric
 */
public class JSurfacePanel extends JPanel {

	public JSurfacePanel() {
		this(createDefaultSurfaceModel());
	}

	/**
	 * @return
	 */
	private static SurfaceModel createDefaultSurfaceModel() {
		DefaultSurfaceModel sm = new DefaultSurfaceModel();

		sm.setPlotFunction2(false);
		
		sm.setCalcDivisions(50);
		sm.setDispDivisions(50);
		sm.setContourLines(10);

		sm.setXMin(-3);
		sm.setXMax(3);
		sm.setYMin(-3);
		sm.setYMax(3);

		sm.setBoxed(false);
		sm.setDisplayXY(false);
		sm.setExpectDelay(false);
		sm.setAutoScaleZ(true);
		sm.setDisplayZ(false);
		sm.setMesh(false);
		sm.setPlotType(PlotType.SURFACE);
		sm.setFirstFunctionOnly(true);
		//sm.setPlotType(PlotType.WIREFRAME);
		//sm.setPlotType(PlotType.CONTOUR);
		//sm.setPlotType(PlotType.DENSITY);

		sm.setPlotColor(PlotColor.SPECTRUM);
		//sm.setPlotColor(PlotColor.DUALSHADE);
		//sm.setPlotColor(PlotColor.FOG);
		//sm.setPlotColor(PlotColor.OPAQUE);
		sm.setMapper(new Mapper() {
			@Override
			public  float f1(float x, float y)
			{
				float r = x*x+y*y;
				
				if (r == 0 ) return 1.0f;
				return (float)( Math.sin(r)/(r));
			}
			
			@Override
			public  float f2(float x, float y)
			{
				return (float)(Math.sin(x*y));
			}
		});
		sm.plot().execute();
		return sm;

	}
	public JSurfacePanel(SurfaceModel model) {
		super(new BorderLayout());
		initComponents();

		String name = (String) configurationToggler.getValue(Action.NAME);
		getActionMap().put(name, configurationToggler);
		getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), name);

		setModel(model);

	}

	public void setModel(SurfaceModel model) {
		if (model instanceof AbstractSurfaceModel)
			configurationPanel.setModel((AbstractSurfaceModel) model);
		else {
			scrollpane.setVisible(false);
			configurationPanel.setModel(null);
		}
		surface.setModel(model);
	}

	/**
	 * @return
	 * @see Component#getFont()
	 */
	public Font getTitleFont() {
		return title.getFont();
	}

	/**
	 * @return
	 * @see JLabel#getIcon()
	 */
	public Icon getTitleIcon() {
		return title.getIcon();
	}

	/**
	 * @return
	 * @see JLabel#getText()
	 */
	public String getTitleText() {
		return title.getText();
	}

	/**
	 * @return
	 * @see Component#isVisible()
	 */
	public boolean isTitleVisible() {
		return title.isVisible();
	}

	/**
	 * @param font
	 * @see JComponent#setFont(Font)
	 */
	public void setTitleFont(Font font) {
		title.setFont(font);
	}

	/**
	 * @param icon
	 * @see JLabel#setIcon(Icon)
	 */
	public void setTitleIcon(Icon icon) {
		title.setIcon(icon);
	}

	/**
	 * @param text
	 * @see JLabel#setText(String)
	 */
	public void setTitleText(String text) {
		title.setText(text);
	}

	/**
	 * @param aFlag
	 * @see JComponent#setVisible(boolean)
	 */
	public void setTitleVisible(boolean aFlag) {
		title.setVisible(aFlag);
	}

	/**
	 * @return
	 * @see Component#isVisible()
	 */
	public boolean isConfigurationVisible() {
		return scrollpane.isVisible();
	}

	/**
	 * @param aFlag
	 * @see JComponent#setVisible(boolean)
	 */
	public void setConfigurationVisible(boolean aFlag) {
		scrollpane.setVisible(aFlag);
		invalidate();
		revalidate();
	}

	private void toggleConfiguration() {
		setConfigurationVisible(!isConfigurationVisible());
		if (!isConfigurationVisible())
			surface.requestFocusInWindow();
	}

	public JSurface getSurface() {
		return surface;
	}

	private void mousePressed() {
		surface.requestFocusInWindow();
	}

	private void surfaceMouseClicked(MouseEvent e) {
		if (e.getClickCount() >= 2)
			toggleConfiguration();
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
//		ResourceBundle bundle = ResourceBundle.getBundle("net.ericaro.surfaceplotter.JSurfacePanel");
		title = new JLabel();
		surface = new JSurface();
		scrollpane = new JGridBagScrollPane();
		configurationPanel = new VerticalConfigurationPanel();
		//noinspection CloneableClassWithoutClone
		configurationToggler = new AbstractAction(){
			@Override
			public void actionPerformed(ActionEvent e){toggleConfiguration();}};

		//======== this ========
		setName("this");
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 0.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 1.0, 1.0E-4};

		//---- title ----
		title.setText("");
		title.setHorizontalTextPosition(SwingConstants.CENTER);
		title.setHorizontalAlignment(SwingConstants.CENTER);
		title.setBackground(Color.white);
		title.setOpaque(true);
		title.setFont(title.getFont().deriveFont(title.getFont().getSize() + 4.0f));
		title.setName("title");
		add(title, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));

		//---- surface ----
		surface.setToolTipText("");
		surface.setInheritsPopupMenu(true);
		surface.setName("surface");
		surface.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				surfaceMouseClicked(e);
			}
			@Override
			public void mousePressed(MouseEvent e) {
				JSurfacePanel.this.mousePressed();
			}
		});
		add(surface, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));

		//======== scrollpane ========
		scrollpane.setWidthFixed(true);
		scrollpane.setName("scrollpane");

		//---- configurationPanel ----
		configurationPanel.setNextFocusableComponent(this);
		configurationPanel.setName("configurationPanel");
		scrollpane.setViewportView(configurationPanel);
		add(scrollpane, new GridBagConstraints(1, 0, 1, 2, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));

		//---- configurationToggler ----
		configurationToggler.putValue(Action.NAME, "configurationToggler.Name");
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}
	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JLabel title;
	private JSurface surface;
	private JGridBagScrollPane scrollpane;
	private VerticalConfigurationPanel configurationPanel;
	private AbstractAction configurationToggler;
	// JFormDesigner - End of variables declaration //GEN-END:variables

}
