package automenta.vivisect.surfaceplotter.example;

import automenta.vivisect.surfaceplotter.JSurfacePanel;
import automenta.vivisect.surfaceplotter.Mapper;
import automenta.vivisect.surfaceplotter.ProgressiveSurfaceModel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;

/**
 * @author User #1
 */
public class Sample extends JPanel {
	private final ProgressiveSurfaceModel model;
	public Sample() {
		initComponents();
		
		//DefaultSurfaceModel model = new DefaultSurfaceModel();
		model = new ProgressiveSurfaceModel() ;
		surfacePanel1.setModel(model);
		
		model.setMapper(new Mapper() {
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
		model.plot().execute();
	}
	private void button1ActionPerformed() {
		JFileChooser jfc = new JFileChooser();
		try {
			// if (jfc.showSaveDialog(surfacePanel1) ==
			// JFileChooser.APPROVE_OPTION )
			// surfacePanel1.getSurface().doExportSVG( jfc.getSelectedFile());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Sample sample = new Sample();
		JFrame jf = new JFrame("sample");
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.getContentPane().add(sample);
		jf.pack();
		jf.setVisible(true);
	}

	private void slider1StateChanged(ChangeEvent e) {
		if (!slider1.getValueIsAdjusting())
			model.setCurrentDefinition(slider1.getValue());
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		surfacePanel1 = new JSurfacePanel();
		toolBar1 = new JToolBar();
		button1 = new JButton();
		slider1 = new JSlider();

		//======== this ========
		setLayout(new BorderLayout());

		//---- surfacePanel1 ----
		surfacePanel1.setTitleText("title");
		surfacePanel1.setBackground(Color.white);
		surfacePanel1.setTitleFont(surfacePanel1.getTitleFont().deriveFont(surfacePanel1.getTitleFont().getStyle() | Font.BOLD, surfacePanel1.getTitleFont().getSize() + 6.0f));
		surfacePanel1.setConfigurationVisible(false);
		add(surfacePanel1, BorderLayout.CENTER);

		//======== toolBar1 ========

		//---- button1 ----
		button1.setText("export SVG");
		button1.addActionListener(e -> button1ActionPerformed());
		toolBar1.add(button1);

		//---- slider1 ----
		slider1.setMaximum(6);
		slider1.setValue(0);
		slider1.setPaintTicks(true);
		slider1.setSnapToTicks(true);
		slider1.setMinorTickSpacing(1);
		slider1.setMajorTickSpacing(1);
		slider1.setPaintLabels(true);
		slider1.addChangeListener(this::slider1StateChanged);
		toolBar1.add(slider1);
		add(toolBar1, BorderLayout.NORTH);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}
	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JSurfacePanel surfacePanel1;
	private JToolBar toolBar1;
	private JButton button1;
	private JSlider slider1;
	// JFormDesigner - End of variables declaration //GEN-END:variables
}
