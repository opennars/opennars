/*
 * Created by JFormDesigner on Thu Oct 07 17:39:55 CEST 2010
 */

package automenta.vivisect.surfaceplotter.surface;

import automenta.vivisect.surfaceplotter.DefaultSurfaceModel;
import automenta.vivisect.surfaceplotter.beans.JBindedCheckBox;
import automenta.vivisect.surfaceplotter.beans.JBindedRadioButton;
import automenta.vivisect.surfaceplotter.beans.JScrollablePanel;
import automenta.vivisect.surfaceplotter.beans.ModelSource;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * A Vertical Configuration panel for the {@link DefaultSurfaceModel}.
 * 
 * @author eric
 */
public class VerticalConfigurationPanel extends JScrollablePanel {
	public VerticalConfigurationPanel() {
		initComponents();
	}

	public void setModel(AbstractSurfaceModel model) {
		modelSource1.setSurfaceModel(model);
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		// ResourceBundle bundle =
		// ResourceBundle.getBundle("net.ericaro.surfaceplotter.surface.VerticalConfigurationPanel");
		label1 = new JLabel();
		boxed = new JBindedCheckBox();
		scaleBox = new JBindedCheckBox();
		label3 = new JLabel();
		displayXY = new JBindedCheckBox();
		displayZ = new JBindedCheckBox();
		label6 = new JLabel();
		displayGrids = new JBindedCheckBox();
		mesh = new JBindedCheckBox();
		expectDelay = new JBindedCheckBox();
		label4 = new JLabel();
		hiddenMode = new JBindedRadioButton();
		spectrumMode = new JBindedRadioButton();
		grayScaleMode = new JBindedRadioButton();
		dualShadeMode = new JBindedRadioButton();
		fogMode = new JBindedRadioButton();
		label5 = new JLabel();
		wireframeType = new JBindedRadioButton();
		surfaceType = new JBindedRadioButton();
		contourType = new JBindedRadioButton();
		densityType = new JBindedRadioButton();
		label2 = new JLabel();
		firstFunctionOnly = new JBindedRadioButton();
		secondFunctionOnly = new JBindedRadioButton();
		bothFunction = new JBindedRadioButton();
		modelSource1 = new ModelSource();
		abstractSurfaceModel1 = new DefaultSurfaceModel();

		// ======== this ========
		setBorder(new EmptyBorder(6, 6, 6, 6));
		setBackground(Color.white);
		setAlignmentY(0.0F);
		setLayout(new GridBagLayout());
		((GridBagLayout) getLayout()).columnWidths = new int[]{6, 0, 0};
		((GridBagLayout) getLayout()).rowHeights = new int[]{0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		((GridBagLayout) getLayout()).columnWeights = new double[]{0.0, 1.0,
				1.0E-4};
		((GridBagLayout) getLayout()).rowWeights = new double[]{0.0, 0.0, 0.0,
				0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

		// ---- label1 ----
		label1.setText(("label1.text"));
		label1.setFont(label1.getFont().deriveFont(
				label1.getFont().getStyle() | Font.BOLD));
		label1.setBackground(Color.white);
		add(label1, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						6, 0, 0, 0), 0, 0));

		// ---- boxed ----
		boxed.setText(("boxed.text"));
		boxed.setBackground(Color.white);
		boxed.setSourceBean(modelSource1);
		boxed.setPropertyName("boxed");
		boxed.setMargin(new Insets(0, 0, 0, 0));
		add(boxed, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));

		// ---- scaleBox ----
		scaleBox.setText(("scaleBox.text"));
		scaleBox.setBackground(Color.white);
		scaleBox.setSourceBean(modelSource1);
		scaleBox.setPropertyName("scaleBox");
		scaleBox.setMargin(new Insets(0, 0, 0, 0));
		add(scaleBox, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));

		// ---- label3 ----
		label3.setText(("label3.text"));
		label3.setFont(label3.getFont().deriveFont(
				label3.getFont().getStyle() | Font.BOLD));
		label3.setBackground(Color.white);
		add(label3, new GridBagConstraints(0, 3, 2, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(6, 0, 0, 0), 0, 0));

		// ---- displayXY ----
		displayXY.setText(("displayXY.text"));
		displayXY.setBackground(Color.white);
		displayXY.setSourceBean(modelSource1);
		displayXY.setPropertyName("displayXY");
		displayXY.setMargin(new Insets(0, 0, 0, 0));
		add(displayXY, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));

		// ---- displayZ ----
		displayZ.setText(("displayZ.text"));
		displayZ.setBackground(Color.white);
		displayZ.setSourceBean(modelSource1);
		displayZ.setPropertyName("displayZ");
		displayZ.setMargin(new Insets(0, 0, 0, 0));
		add(displayZ, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));

		// ---- label6 ----
		label6.setText(("label6.text"));
		label6.setFont(label6.getFont().deriveFont(
				label6.getFont().getStyle() | Font.BOLD));
		label6.setBackground(Color.white);
		add(label6, new GridBagConstraints(0, 6, 2, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						6, 0, 0, 0), 0, 0));

		// ---- displayGrids ----
		displayGrids.setText(("displayGrids.text"));
		displayGrids.setBackground(Color.white);
		displayGrids.setSourceBean(modelSource1);
		displayGrids.setPropertyName("displayGrids");
		displayGrids.setMargin(new Insets(0, 0, 0, 0));
		add(displayGrids, new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));

		// ---- mesh ----
		mesh.setText(("mesh.text"));
		mesh.setBackground(Color.white);
		mesh.setSourceBean(modelSource1);
		mesh.setPropertyName("mesh");
		mesh.setMargin(new Insets(0, 0, 0, 0));
		add(mesh, new GridBagConstraints(1, 8, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));

		// ---- expectDelay ----
		expectDelay.setText(("expectDelay.text"));
		expectDelay.setBackground(Color.white);
		expectDelay.setSourceBean(modelSource1);
		expectDelay.setPropertyName("expectDelay");
		expectDelay.setMargin(new Insets(0, 0, 0, 0));
		add(expectDelay, new GridBagConstraints(1, 9, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));

		// ---- label4 ----
		label4.setText(("label4.text"));
		label4.setFont(label4.getFont().deriveFont(
				label4.getFont().getStyle() | Font.BOLD));
		label4.setBackground(Color.white);
		add(label4, new GridBagConstraints(0, 10, 2, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(6, 0, 0, 0), 0, 0));

		// ---- hiddenMode ----
		hiddenMode.setText(("hiddenMode.text"));
		hiddenMode.setBackground(Color.white);
		hiddenMode.setSourceBean(modelSource1);
		hiddenMode.setPropertyName("hiddenMode");
		hiddenMode.setMargin(new Insets(0, 0, 0, 0));
		add(hiddenMode, new GridBagConstraints(1, 11, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));

		// ---- spectrumMode ----
		spectrumMode.setText(("spectrumMode.text"));
		spectrumMode.setBackground(Color.white);
		spectrumMode.setSourceBean(modelSource1);
		spectrumMode.setPropertyName("spectrumMode");
		spectrumMode.setMargin(new Insets(0, 0, 0, 0));
		add(spectrumMode, new GridBagConstraints(1, 12, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));

		// ---- grayScaleMode ----
		grayScaleMode.setText(("grayScaleMode.text"));
		grayScaleMode.setBackground(Color.white);
		grayScaleMode.setSourceBean(modelSource1);
		grayScaleMode.setPropertyName("grayScaleMode");
		grayScaleMode.setMargin(new Insets(0, 0, 0, 0));
		add(grayScaleMode, new GridBagConstraints(1, 13, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));

		// ---- dualShadeMode ----
		dualShadeMode.setText(("dualShadeMode.text"));
		dualShadeMode.setBackground(Color.white);
		dualShadeMode.setSourceBean(modelSource1);
		dualShadeMode.setPropertyName("dualShadeMode");
		dualShadeMode.setMargin(new Insets(0, 0, 0, 0));
		add(dualShadeMode, new GridBagConstraints(1, 14, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));

		// ---- fogMode ----
		fogMode.setText(("fogMode.text"));
		fogMode.setBackground(Color.white);
		fogMode.setSourceBean(modelSource1);
		fogMode.setPropertyName("fogMode");
		fogMode.setMargin(new Insets(0, 0, 0, 0));
		add(fogMode, new GridBagConstraints(1, 15, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));

		// ---- label5 ----
		label5.setText(("label5.text"));
		label5.setFont(label5.getFont().deriveFont(
				label5.getFont().getStyle() | Font.BOLD));
		label5.setBackground(Color.white);
		add(label5, new GridBagConstraints(0, 16, 2, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(6, 0, 0, 0), 0, 0));

		// ---- wireframeType ----
		wireframeType.setText(("wireframeType.text"));
		wireframeType.setBackground(Color.white);
		wireframeType.setSourceBean(modelSource1);
		wireframeType.setPropertyName("wireframeType");
		wireframeType.setMargin(new Insets(0, 0, 0, 0));
		add(wireframeType, new GridBagConstraints(1, 17, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));

		// ---- surfaceType ----
		surfaceType.setText(("surfaceType.text"));
		surfaceType.setBackground(Color.white);
		surfaceType.setSourceBean(modelSource1);
		surfaceType.setPropertyName("surfaceType");
		surfaceType.setMargin(new Insets(0, 0, 0, 0));
		add(surfaceType, new GridBagConstraints(1, 18, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));

		// ---- contourType ----
		contourType.setText(("contourType.text"));
		contourType.setBackground(Color.white);
		contourType.setSourceBean(modelSource1);
		contourType.setPropertyName("contourType");
		contourType.setMargin(new Insets(0, 0, 0, 0));
		add(contourType, new GridBagConstraints(1, 19, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));

		// ---- densityType ----
		densityType.setText(("densityType.text"));
		densityType.setBackground(Color.white);
		densityType.setSourceBean(modelSource1);
		densityType.setPropertyName("densityType");
		densityType.setMargin(new Insets(0, 0, 0, 0));
		add(densityType, new GridBagConstraints(1, 20, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));

		// ---- label2 ----
		label2.setText(("label2.text"));
		label2.setFont(label2.getFont().deriveFont(
				label2.getFont().getStyle() | Font.BOLD));
		label2.setBackground(Color.white);
		add(label2, new GridBagConstraints(0, 21, 2, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(6, 0, 0, 0), 0, 0));

		// ---- firstFunctionOnly ----
		firstFunctionOnly.setText(("firstFunctionOnly.text"));
		firstFunctionOnly.setBackground(Color.white);
		firstFunctionOnly.setSourceBean(modelSource1);
		firstFunctionOnly.setPropertyName("firstFunctionOnly");
		firstFunctionOnly.setProperty(true);
		firstFunctionOnly.setMargin(new Insets(0, 0, 0, 0));
		add(firstFunctionOnly, new GridBagConstraints(1, 22, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));

		// ---- secondFunctionOnly ----
		secondFunctionOnly.setText(("secondFunctionOnly.text"));
		secondFunctionOnly.setBackground(Color.white);
		secondFunctionOnly.setSourceBean(modelSource1);
		secondFunctionOnly.setPropertyName("secondFunctionOnly");
		secondFunctionOnly.setProperty(true);
		secondFunctionOnly.setMargin(new Insets(0, 0, 0, 0));
		add(secondFunctionOnly, new GridBagConstraints(1, 23, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));

		// ---- bothFunction ----
		bothFunction.setText(("bothFunction.text"));
		bothFunction.setBackground(Color.white);
		bothFunction.setSourceBean(modelSource1);
		bothFunction.setPropertyName("bothFunction");
		bothFunction.setMargin(new Insets(0, 0, 0, 0));
		add(bothFunction, new GridBagConstraints(1, 24, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));

		// ---- modelSource1 ----
		modelSource1.setSurfaceModel(abstractSurfaceModel1);

		// ---- abstractSurfaceModel1 ----
		abstractSurfaceModel1.setSecondFunctionOnly(true);

		// ---- buttonGroup1 ----
		ButtonGroup buttonGroup1 = new ButtonGroup();
		buttonGroup1.add(hiddenMode);
		buttonGroup1.add(spectrumMode);
		buttonGroup1.add(grayScaleMode);
		buttonGroup1.add(dualShadeMode);
		buttonGroup1.add(fogMode);

		// ---- buttonGroup2 ----
		ButtonGroup buttonGroup2 = new ButtonGroup();
		buttonGroup2.add(wireframeType);
		buttonGroup2.add(surfaceType);
		buttonGroup2.add(contourType);
		buttonGroup2.add(densityType);

		// ---- buttonGroup3 ----
		ButtonGroup buttonGroup3 = new ButtonGroup();
		buttonGroup3.add(firstFunctionOnly);
		buttonGroup3.add(secondFunctionOnly);
		buttonGroup3.add(bothFunction);
		// JFormDesigner - End of component initialization
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JLabel label1;
	private JBindedCheckBox boxed;
	private JBindedCheckBox scaleBox;
	private JLabel label3;
	private JBindedCheckBox displayXY;
	private JBindedCheckBox displayZ;
	private JLabel label6;
	private JBindedCheckBox displayGrids;
	private JBindedCheckBox mesh;
	private JBindedCheckBox expectDelay;
	private JLabel label4;
	private JBindedRadioButton hiddenMode;
	private JBindedRadioButton spectrumMode;
	private JBindedRadioButton grayScaleMode;
	private JBindedRadioButton dualShadeMode;
	private JBindedRadioButton fogMode;
	private JLabel label5;
	private JBindedRadioButton wireframeType;
	private JBindedRadioButton surfaceType;
	private JBindedRadioButton contourType;
	private JBindedRadioButton densityType;
	private JLabel label2;
	private JBindedRadioButton firstFunctionOnly;
	private JBindedRadioButton secondFunctionOnly;
	private JBindedRadioButton bothFunction;
	private ModelSource modelSource1;
	private DefaultSurfaceModel abstractSurfaceModel1;
	// JFormDesigner - End of variables declaration //GEN-END:variables
}
