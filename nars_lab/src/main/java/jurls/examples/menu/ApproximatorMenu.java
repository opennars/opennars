/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.examples.menu;

import jurls.core.approximation.*;

import javax.swing.*;

/**
 * 
 * @author thorsten2
 */
public class ApproximatorMenu extends RLMenu {

	public class NumberOfBitsMenu extends JMenu {

		public JRadioButtonMenuItem bits8 = new JRadioButtonMenuItem(
				new MyAction("8"));
		public JRadioButtonMenuItem bits10 = new JRadioButtonMenuItem(
				new MyAction("10"));
		public JRadioButtonMenuItem bits12 = new JRadioButtonMenuItem(
				new MyAction("12"));
		public JRadioButtonMenuItem bits16 = new JRadioButtonMenuItem(
				new MyAction("16"));

		public NumberOfBitsMenu(String direction) {
			super("No. " + direction + " bits");

			ButtonGroup bg = new ButtonGroup();
			bg.add(bits8);
			bg.add(bits10);
			bg.add(bits12);
			bg.add(bits16);

			add(bits8);
			add(bits10);
			add(bits12);
			add(bits16);

			bits10.setSelected(true);
		}

		public int getNumberOfBits() {
			if (bits8.isSelected()) {
				return 8;
			}
			if (bits10.isSelected()) {
				return 10;
			}
			if (bits12.isSelected()) {
				return 12;
			}
			if (bits16.isSelected()) {
				return 16;
			}

			return -1;
		}
	}

	public class ActivationFunctionMenu extends JMenu {

		public JRadioButtonMenuItem atan = new JRadioButtonMenuItem(
				new MyAction("atan"));
		public JRadioButtonMenuItem tanh = new JRadioButtonMenuItem(
				new MyAction("tanh"));
		public JRadioButtonMenuItem logistic = new JRadioButtonMenuItem(
				new MyAction("logistic sigmoid"));
		public JRadioButtonMenuItem rbf = new JRadioButtonMenuItem(
				new MyAction("radial basis function"));

		public ActivationFunctionMenu(String layer) {
			super("Activation Function For " + layer + " layer");

			ButtonGroup bg = new ButtonGroup();
			bg.add(atan);
			bg.add(tanh);
			bg.add(rbf);
			bg.add(logistic);

			add(atan);
			add(tanh);
			add(logistic);
			add(rbf);
		}

		public ActivationFunctionFactory getFactory() {
			if (atan.isSelected()) {
				return new ATanSigmoidFactory();
			}
			if (tanh.isSelected()) {
				return new TanhSigmoidFactory();
			}
			if (logistic.isSelected()) {
				return new LogisticSigmoidFactory();
			}
			if (rbf.isSelected()) {
				return new RadialBasisFunctionFactory();
			}

			return null;
		}
	}

	public JRadioButtonMenuItem fourier = new JRadioButtonMenuItem(
			new MyAction("Fourier Basis"));
	public JRadioButtonMenuItem wavelet = new JRadioButtonMenuItem(
			new MyAction("Wavelet"));
	public JRadioButtonMenuItem neuralNet = new JRadioButtonMenuItem(
			new MyAction("Neural Net"));
	public JRadioButtonMenuItem cnf = new JRadioButtonMenuItem(new MyAction(
			"CNF Function"));
	public ActivationFunctionMenu hidden = new ActivationFunctionMenu("hidden");
	public ActivationFunctionMenu output = new ActivationFunctionMenu("output");
	public JCheckBoxMenuItem normalizeInput = new JCheckBoxMenuItem(
			new MyAction("Normalize Input"));
	public JCheckBoxMenuItem normalizeOutput = new JCheckBoxMenuItem(
			new MyAction("Normalize Output"));
	public NumberOfBitsMenu numberOfInputBitsMenu = new NumberOfBitsMenu(
			"input");
	public NumberOfBitsMenu numberOfOutputBitsMenu = new NumberOfBitsMenu(
			"output");
	public ObjectListMenu featuresMenu = new ObjectListMenu("No. Features", 2,
			1, 3, 5, 7, 9);

	public ApproximatorMenu(boolean onlyDiffable) {
		super("Approximation Function");

		hidden.addActionListener(new MyAction(""));
		output.addActionListener(new MyAction(""));
		featuresMenu.addActionListener(new MyAction(""));
		numberOfInputBitsMenu.addActionListener(new MyAction(""));
		numberOfOutputBitsMenu.addActionListener(new MyAction(""));

		ButtonGroup bg = new ButtonGroup();
		bg.add(fourier);
		bg.add(wavelet);
		bg.add(neuralNet);
		bg.add(cnf);

		add(featuresMenu);
		addSeparator();
		add(fourier);
		add(wavelet);
		addSeparator();
		add(neuralNet);
		add(hidden);
		add(output);
		addSeparator();
		add(cnf);
		add(numberOfInputBitsMenu);
		add(numberOfOutputBitsMenu);
		addSeparator();
		add(normalizeInput);
		add(normalizeOutput);

		fourier.setSelected(true);
		hidden.atan.setSelected(true);
		output.atan.setSelected(true);
		normalizeOutput.setSelected(false);
		normalizeInput.setSelected(false);

		cnf.setEnabled(!onlyDiffable);
		numberOfInputBitsMenu.setEnabled(!onlyDiffable);
		numberOfOutputBitsMenu.setEnabled(!onlyDiffable);
	}

	public ParameterizedFunctionGenerator getFunctionGenerator(
            ApproxParameters approxParameters
    ) {
        return (int numInputVectorElements) -> {
            int numFeatures = (int) featuresMenu.getObject();

            ParameterizedFunction f = null;

            if (wavelet.isSelected()) {
                f = new GradientFitter(
                        approxParameters,
                        new DiffableFunctionMarshaller(
                                Generator.generateWavelets(numFeatures),
                                numInputVectorElements
                        )
                );
            }
            if (fourier.isSelected()) {
                f = new GradientFitter(
                        approxParameters,
                        new DiffableFunctionMarshaller(
                                Generator.generateFourierBasis(numFeatures),
                                numInputVectorElements
                        )
                );
            }
            if (neuralNet.isSelected()) {
                f = new GradientFitter(
                        approxParameters,
                        new DiffableFunctionMarshaller(
                                Generator.generateFFNN(
                                        hidden.getFactory(),
                                        output.getFactory(),
                                        numFeatures
                                ),
                                numInputVectorElements
                        )
                );
            }
            if (cnf.isSelected()) {
                f = new CNFBooleanFunction(
                        numberOfInputBitsMenu.getNumberOfBits(),
                        numberOfOutputBitsMenu.getNumberOfBits(),
                        numInputVectorElements
                );
            }

            if (normalizeInput.isSelected()) {
                f = new InputNormalizer(f);
            }

            if (normalizeOutput.isSelected()) {
                f = new OutputNormalizer(f);
            }

            return f;
        };
    }
}
