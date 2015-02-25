/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "CNEFEnsemble.java". Description:
"Advanced properties, these may not necessarily be configued, so"

The Initial Developer of the Original Code is Bryan Tripp & Centre for Theoretical Neuroscience, University of Waterloo. Copyright (C) 2006-2008. All Rights Reserved.

Alternatively, the contents of this file may be used under the terms of the GNU
Public License license (the GPL License), in which case the provisions of GPL
License are applicable  instead of those above. If you wish to allow use of your
version of this file only under the terms of the GPL License and not to allow
others to use your version of this file under the MPL, indicate your decision
by deleting the provisions above and replace  them with the notice and other
provisions required by the GPL License.  If you do not delete the provisions above,
a recipient may use your version of this file under either the MPL or the GPL License.
 */

package ca.nengo.ui.model.build;

import ca.nengo.math.ApproximatorFactory;
import ca.nengo.math.impl.GradientDescentApproximator;
import ca.nengo.math.impl.WeightedCostApproximator;
import ca.nengo.model.Node;
import ca.nengo.model.StructuralException;
import ca.nengo.model.impl.NodeFactory;
import ca.nengo.neural.nef.NEFGroup;
import ca.nengo.neural.nef.NEFGroupFactory;
import ca.nengo.neural.nef.impl.NEFGroupFactoryImpl;
import ca.nengo.ui.config.*;
import ca.nengo.ui.config.descriptors.PFloat;
import ca.nengo.ui.config.descriptors.PInt;
import ca.nengo.ui.config.descriptors.PNodeFactory;
import ca.nengo.ui.config.managers.ConfigManager.ConfigMode;
import ca.nengo.ui.config.managers.UserConfigurer;
import ca.nengo.ui.lib.util.UserMessages;
import ca.nengo.ui.lib.util.Util;
import ca.nengo.ui.model.node.UINEFGroup;
import ca.nengo.util.VectorGenerator;
import ca.nengo.util.impl.RandomHypersphereVG;
import ca.nengo.util.impl.Rectifier;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.InvalidParameterException;
import java.text.DecimalFormat;

public class CNEFEnsemble extends CNode {
    static final Property pApproximator = new PApproximator("Decoding Sign");

    static final Property pDim = new PInt("Dimensions");

    static final Property pEncodingDistribution = new PEncodingDistribution("Encoding Distribution");
    static final Property pEncodingSign = new PSign("Encoding Sign");
    static final Property pNodeFactory = new PNodeFactory("Node Factory");
    static final Property pNumOfNodes = new PInt("Number of Nodes");
    static final Property pRadius = new PFloat("Radius");
    static final Property pNoise = new PFloat("Noise",0.1f);

    /**
     * Config descriptors
     */
    static final ConfigSchemaImpl zConfig = new ConfigSchemaImpl(new Property[] { pNumOfNodes,
            pDim, pNodeFactory, pRadius }, new Property[] { pApproximator, pEncodingDistribution,
            pEncodingSign, pNoise });

    public CNEFEnsemble() {
        super();
        pDim.setDescription("Number of dimensions that are represented by the ensemble");
        pName.setDescription("Name of the ensemble");
        pApproximator.setDescription("Limit the decoders to be all positive or all negative");
        pEncodingDistribution.setDescription("Distribution of encoders, ranging from uniformly chosen (default) to all encoders aligned to an axis");
        pEncodingSign.setDescription("Limit the encoders to be all positive or all negative");
        pNodeFactory.setDescription("Type of neuron (model) to use for the ensemble" +
                "See online documentation for adding custom neuron models");
        pNumOfNodes.setDescription("Number of neurons in the ensemble");
        pRadius.setDescription("Largest magnitude that can be accurately represented by the ensemble");
        pNoise.setDescription("Expected ratio of the noise amplitude to the signal amplitude to use when solving for decoders");
    }

    protected Node createNode(ConfigResult prop, String name) {
        try {

            NEFGroupFactory ef = new NEFGroupFactoryImpl();
            Integer numOfNeurons = (Integer) prop.getValue(pNumOfNodes);
            Integer dimensions = (Integer) prop.getValue(pDim);

            /*
             * Advanced properties, these may not necessarily be configued, so
             */
            ApproximatorFactory approxFactory = (ApproximatorFactory) prop.getValue(pApproximator);
            NodeFactory nodeFactory = (NodeFactory) prop.getValue(pNodeFactory);
            Sign encodingSign = (Sign) prop.getValue(pEncodingSign);
            Float encodingDistribution = (Float) prop.getValue(pEncodingDistribution);
            Float radius = (Float) prop.getValue(pRadius);
            Float noise = (Float) prop.getValue(pNoise);

            if (nodeFactory != null) {
                ef.setNodeFactory(nodeFactory);
            }

            if (approxFactory != null) {
                ef.setApproximatorFactory(approxFactory);
            }
            if (noise != null) {
                ApproximatorFactory f=ef.getApproximatorFactory();
                if (f instanceof WeightedCostApproximator.Factory) {
                    ((WeightedCostApproximator.Factory)f).setNoise(noise);
                }
            }

            if (encodingSign != null) {
                if (encodingDistribution == null) {
                    encodingDistribution = 0f;
                }
                VectorGenerator vectorGen = new RandomHypersphereVG(true, 1, encodingDistribution);
                if (encodingSign == Sign.Positive) {
                    vectorGen = new Rectifier(vectorGen, true);
                } else if (encodingSign == Sign.Negative) {
                    vectorGen = new Rectifier(vectorGen, false);
                }
                ef.setEncoderFactory(vectorGen);
            }

            if (radius==null) {
                NEFGroup ensemble = ef.make(name, numOfNeurons, dimensions);
                return ensemble;
            } else {
                float[] radii=new float[dimensions];
                for (int i=0; i<dimensions; i++) {
                    radii[i]=radius.floatValue();
                }
                NEFGroup ensemble = ef.make(name, numOfNeurons, radii);
                return ensemble;
            }
        } catch (StructuralException e) {
        	Util.debugMsg("StructuralException: " + e.toString());

			if (e.getMessage() != null) {
				UserMessages.showWarning(e.getMessage());
			} else {
				Util.showException(e);
			}
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ConfigSchemaImpl getNodeConfigSchema() {
        return zConfig;
    }

    public String getTypeName() {
        return UINEFGroup.typeName;
    }

}

class PApproximator extends Property {

    private static final long serialVersionUID = 1L;

    private static final String TYPE_NAME = "Approximator Factory";

    public PApproximator(String name) {
        super(name);
    }

    @Override
    protected PropertyInputPanel createInputPanel() {
        return new Panel(this);
    }

    @Override
    public Class<?> getTypeClass() {
        return ApproximatorFactory.class;
    }

    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }

    private static class Panel extends PropertyInputPanel {
        private static final SignItem unconstrained = new SignItem("Unconstrained", Sign.Unconstrained);
        private static final SignItem positive = new SignItem("Positive", Sign.Positive);
        private static final SignItem negative = new SignItem("Negative", Sign.Negative);

        private static final SignItem[] items = { unconstrained, positive, negative };

        private final JComboBox comboBox;
        private final JButton setButton;

        public Panel(Property property) {
            super(property);
            comboBox = new JComboBox(items);
            setButton = new JButton(new AbstractAction("Set") {
                private static final long serialVersionUID = 1L;

                public void actionPerformed(ActionEvent e) {
                    configure();
                }
            });

            add(comboBox);
            add(setButton);

            comboBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updateApproximator();
                }
            });
            updateApproximator();
        }

        private void updateApproximator() {
            Sign sign = ((SignItem) comboBox.getSelectedItem()).getSign();

            if (sign != null) {
                if (sign == Sign.Positive || sign == Sign.Negative) {
                    setButton.setEnabled(false);
                    boolean positive = true;
                    if (sign == Sign.Negative) {
                        positive = false;
                    }
                    approximator = new GradientDescentApproximator.Factory(
                            new GradientDescentApproximator.CoefficientsSameSign(positive), false);

                } else if (sign == Sign.Unconstrained) {
                    setButton.setEnabled(true);
                    approximator = new WeightedCostApproximator.Factory(noiseLevel, NSV);
                } else {
                    Util.Assert(false, "Unsupported item");
                }

            }
        }

        /*
         * These values are used to configure a WeightedCostApproximator
         */
        private float noiseLevel = 0.1f;
        private int NSV = -1;

        private void configure() {
            try {
                Property pNoiseLevel = new PFloat("Noise level", "Ratio of the noise amplitude to the signal amplitude", noiseLevel);
                Property pNSV = new PInt("Number of Singular Values", NSV);
                ConfigResult result = UserConfigurer.configure(
                        new Property[] { pNoiseLevel, pNSV }, TYPE_NAME, this.getDialogParent(),
                        ConfigMode.STANDARD);

                noiseLevel = (Float) result.getValue(pNoiseLevel);
                NSV = (Integer) result.getValue(pNSV);
                updateApproximator();

            } catch (ConfigException e) {
                e.defaultHandleBehavior();
            }

        }

        @Override
        public ApproximatorFactory getValue() {
            return approximator;
        }

        @Override
        public boolean isValueSet() {
            return (getValue() != null);
        }

        private ApproximatorFactory approximator;

        @Override
        public void setValue(Object value) {
            // do nothing, values can't be set on this property
        }

    }

}

class PEncodingDistribution extends Property {

    private static final long serialVersionUID = 1L;

    public PEncodingDistribution(String name) {
        super(name, 0f);
    }

    @Override
    protected PropertyInputPanel createInputPanel() {
        return new Slider(this);
    }

    @Override
    public Class<Float> getTypeClass() {
        return Float.class;
    }

    @Override
    public String getTypeName() {
        return "Slider";
    }

    static class Slider extends PropertyInputPanel {
        private static final int NUMBER_OF_TICKS = 1000;

        private final JSlider sliderSwing;
        private final JLabel sliderValueLabel;

        public Slider(Property property) {
            super(property);

            JPanel sliderPanel = new JPanel();
            sliderPanel.setLayout(new BorderLayout());
            add(sliderPanel);

            sliderSwing = new JSlider(0, NUMBER_OF_TICKS);

            /*
             * Add labels
             */
            sliderSwing.setPreferredSize(new Dimension(400, (int) sliderSwing.getPreferredSize()
                    .getHeight()));
            sliderPanel.add(sliderSwing, BorderLayout.NORTH);

            JPanel labelsPanel = new JPanel();
            sliderPanel.add(labelsPanel, BorderLayout.SOUTH);

            labelsPanel.setLayout(new BorderLayout());
            labelsPanel.add(new JLabel("0.0 - Evenly Distributed"), BorderLayout.WEST);
            sliderValueLabel = new JLabel();
            sliderValueLabel.setHorizontalAlignment(SwingConstants.CENTER);
            sliderValueLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
            labelsPanel.add(sliderValueLabel, BorderLayout.CENTER);
            labelsPanel.add(new JLabel("Clustered on Axis - 1.0"), BorderLayout.EAST);
            updateSliderLabel();

            sliderSwing.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    updateSliderLabel();
                }

            });

        }

        private void updateSliderLabel() {
            DecimalFormat df = new DecimalFormat("0.000");

            sliderValueLabel.setText(" -" + df.format(getValue()) + "- ");
        }

        @Override
        public Float getValue() {
            return ((float) sliderSwing.getValue() / (float) NUMBER_OF_TICKS);
        }

        @Override
        public boolean isValueSet() {
            return true;
        }

        @Override
        public void setValue(Object value) {
            if (value instanceof Float) {
                sliderSwing.setValue((int) (((Float) value) * NUMBER_OF_TICKS));
            } else {
                throw new InvalidParameterException();
            }
        }

    }

}

enum Sign {
    Negative, Positive, Unconstrained
}

class PSign extends Property {

    private static final long serialVersionUID = 1L;

    public PSign(String name) {
        super(name);
    }

    @Override
    protected PropertyInputPanel createInputPanel() {
        return new Panel(this);
    }

    @Override
    public Class<?> getTypeClass() {
        return Sign.class;
    }

    @Override
    public String getTypeName() {
        return "Sign";
    }

    private static class Panel extends PropertyInputPanel {

        private static final SignItem[] items = { new SignItem("Unconstrained", Sign.Unconstrained),
            new SignItem("Positive", Sign.Positive), new SignItem("Negative", Sign.Negative) };

        private final JComboBox comboBox;

        public Panel(Property property) {
            super(property);
            comboBox = new JComboBox(items);
            add(comboBox);
        }

        @Override
        public Sign getValue() {
            return ((SignItem) comboBox.getSelectedItem()).getSign();
        }

        @Override
        public boolean isValueSet() {
            return true;
        }

        @Override
        public void setValue(Object value) {
            for (SignItem item : items) {
                if (value == item.getSign()) {
                    comboBox.setSelectedItem(item);
                }
            }
        }
    }

}

class SignItem {
    final String name;
    final Sign type;

    public SignItem(String name, Sign type) {
        super();
        this.name = name;
        this.type = type;
    }

    public Sign getSign() {
        return type;
    }

    @Override
    public String toString() {
        return name;
    }

}
