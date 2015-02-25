/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "NodeFactoryPanel.java". Description:
"Input Panel for selecting and configuring a Node Factory

  @author Shu Wu"

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

package ca.nengo.ui.config.panels;

import ca.nengo.config.ClassRegistry;
import ca.nengo.math.impl.IndicatorPDF;
import ca.nengo.model.impl.NodeFactory;
import ca.nengo.neural.neuron.impl.*;
import ca.nengo.neural.neuron.impl.PoissonSpikeGenerator.LinearNeuronFactory;
import ca.nengo.neural.neuron.impl.PoissonSpikeGenerator.SigmoidNeuronFactory;
import ca.nengo.ui.config.*;
import ca.nengo.ui.config.descriptors.PBoolean;
import ca.nengo.ui.config.descriptors.PFloat;
import ca.nengo.ui.lib.util.UserMessages;
import ca.nengo.ui.model.build.AbstractConstructable;
import ca.nengo.ui.model.build.ModelFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.Constructor;

/**
 * Input Panel for selecting and configuring a Node Factory
 * 
 * @author Shu Wu
 */
public class NodeFactoryPanel extends PropertyInputPanel {

    private static final ConstructableNodeFactory[] NodeFactoryItems = new ConstructableNodeFactory[] {
        new CLinearNeuronFactory(), new CSigmoidNeuronFactory(), new CLIFNeuronFactory(),
        new CALIFNeuronFactory(), new CSpikingNeuronFactory() };

    private JComboBox factorySelector;

    private NodeFactory myNodeFactory;

    private ConstructableNodeFactory selectedItem;

    /**
     * @param property TODO
     */
    public NodeFactoryPanel(Property property) {
        super(property);
        init();
    }

    private void configureNodeFactory() {
        selectedItem = (ConstructableNodeFactory) factorySelector.getSelectedItem();

        try {
            NodeFactory model = (NodeFactory) ModelFactory.constructModel(selectedItem);
            setValue(model);
        } catch (ConfigException e) {
            e.defaultHandleBehavior();
        } catch (Exception e) {
            UserMessages.showError("Could not configure Node Factory: " + e.getMessage());
        }
    }

    private void init() {

        factorySelector = new JComboBox(NodeFactoryItems);
        add(factorySelector);

        /*
         * Reset value if the combo box selection has changed
         */
        factorySelector.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (factorySelector.getSelectedItem() != selectedItem) {
                    setValue(null);
                }
            }

        });

        JButton configureBtn = new JButton(new AbstractAction("Set") {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent arg0) {
                configureNodeFactory();
            }
        });

        add(configureBtn);

    }

    @Override
    public Object getValue() {
        return myNodeFactory;
    }

    @Override
    public boolean isValueSet() {
        if (myNodeFactory != null) {
            return true;
        } else {
            setStatusMsg("Node Factory must be set");
            return false;
        }
    }

    @Override
    public void setValue(Object value) {
        if (value == null) {
            myNodeFactory = null;
            return;
        }

        if (value instanceof NodeFactory) {
            myNodeFactory = (NodeFactory) value;
            setStatusMsg("");

            /*
             * Update the combo box selector with the selected Node Factory
             */
            boolean foundComboItem = false;
            for (ConstructableNodeFactory nodeFactoryItem : NodeFactoryItems) {

                if (nodeFactoryItem.getType().isInstance(myNodeFactory)) {
                    selectedItem = nodeFactoryItem;
                    factorySelector.setSelectedItem(selectedItem);
                    foundComboItem = true;
                    break;
                }
            }
            if (!foundComboItem) {
                throw new IllegalArgumentException("Unsupported Node Factory");
            }

        } else {
            throw new IllegalArgumentException("Value is not a Node Factory");
        }
    }
}

abstract class ConstructableNodeFactory extends AbstractConstructable {
	
	// Parameters common to many neurons
    static final Property pInterceptDefault = new PIndicatorPDF("Intercept","Range of the uniform distribution of neuron x-intercepts, (typically -1 to 1)");
    static final Property pMaxRateDefault = new PIndicatorPDF("Max rate [Hz]","Maximum neuron firing rate [10 to 100Hz for cortex]");
    static final Property pTauRCDefault = new PFloat("tauRC [s]","Membrane time constant, in seconds [typically ~0.02s]");
    static final Property pTauRefDefault = new PFloat("tauRef [s]","Refractory period, in seconds [typically ~0.002s]");
	
    private final String name;
    private final Class<? extends NodeFactory> type;

    public ConstructableNodeFactory(String name, Class<? extends NodeFactory> type) {
        super();
        this.name = name;
        this.type = type;
    }

    protected final Object configureModel(ConfigResult configuredProperties) throws ConfigException {
        NodeFactory nodeFactory = createNodeFactory(configuredProperties);

        if (!getType().isInstance(nodeFactory)) {
            throw new ConfigException("Expected type: " + getType().getSimpleName() + " Got: "
                    + nodeFactory.getClass().getSimpleName());
        } else {
            return nodeFactory;
        }
    }

    abstract protected NodeFactory createNodeFactory(ConfigResult configuredProperties)
            throws ConfigException;

    public Class<? extends NodeFactory> getType() {
        return type;
    }

    public final String getTypeName() {
        return name;
    }

    @Override
    public String toString() {
        return this.name;
    }

}

class CALIFNeuronFactory extends ConstructableNodeFactory {
    static final Property pIncN = new PIndicatorPDF("IncN","Increment of adaptation-related ion with each spike");
    static final Property pTauN = new PFloat("tauN [s]","Time constant of adaptation-related ion, in seconds");
    
    static final Property pIntercept = pInterceptDefault;
    static final Property pMaxRate = pMaxRateDefault;
    static final Property pTauRC = pTauRCDefault;
    static final Property pTauRef = pTauRefDefault;

    static final ConfigSchema zConfig = new ConfigSchemaImpl(new Property[] { pTauRC,
            pTauN, pTauRef, pMaxRate, pIntercept, pIncN });

    public CALIFNeuronFactory() {
        super("Adapting LIF Neuron", ALIFNeuronFactory.class);
    }

    @Override
    protected NodeFactory createNodeFactory(ConfigResult configuredProperties) {
        Float tauRC = (Float) configuredProperties.getValue(pTauRC);
        Float tauRef = (Float) configuredProperties.getValue(pTauRef);
        Float tauN = (Float) configuredProperties.getValue(pTauN);
        IndicatorPDF maxRate = (IndicatorPDF) configuredProperties.getValue(pMaxRate);
        IndicatorPDF intercept = (IndicatorPDF) configuredProperties.getValue(pIntercept);
        IndicatorPDF incN = (IndicatorPDF) configuredProperties.getValue(pIncN);

        return new ALIFNeuronFactory(maxRate, intercept, incN, tauRef, tauRC, tauN);
    }

    @Override
    public ConfigSchema getSchema() {
        return zConfig;
    }

}

class CLIFNeuronFactory extends ConstructableNodeFactory {
	
    static final Property pIntercept = pInterceptDefault;
    static final Property pMaxRate = pMaxRateDefault;
    static final Property pTauRC = pTauRCDefault;
    static final Property pTauRef = pTauRefDefault;

    static final ConfigSchema zConfig = new ConfigSchemaImpl(new Property[] { pTauRC,
            pTauRef, pMaxRate, pIntercept });

    public CLIFNeuronFactory() {
        super("LIF Neuron", LIFNeuronFactory.class);
    }

    @Override
    protected NodeFactory createNodeFactory(ConfigResult configuredProperties) {
        Float tauRC = (Float) configuredProperties.getValue(pTauRC);
        Float tauRef = (Float) configuredProperties.getValue(pTauRef);
        IndicatorPDF maxRate = (IndicatorPDF) configuredProperties.getValue(pMaxRate);
        IndicatorPDF intercept = (IndicatorPDF) configuredProperties.getValue(pIntercept);

        return new LIFNeuronFactory(tauRC, tauRef, maxRate, intercept);
    }

    @Override
    public ConfigSchema getSchema() {
        return zConfig;
    }

}

/**
 * Constructable Linear Neuron Factory
 * 
 * @author Shu Wu
 */
class CLinearNeuronFactory extends ConstructableNodeFactory {

    static final Property pIntercept = pInterceptDefault;
    static final Property pMaxRate = pMaxRateDefault;
    
    static final Property pRectified = new PBoolean("Rectified","Whether to constrain the neuron outputs to be positive");
    static final ConfigSchema zConfig = new ConfigSchemaImpl(new Property[] {
            pMaxRate, pIntercept, pRectified });

    public CLinearNeuronFactory() {
        super("Linear Neuron", LinearNeuronFactory.class);
    }

    @Override
    protected NodeFactory createNodeFactory(ConfigResult configuredProperties) {
        IndicatorPDF maxRate = (IndicatorPDF) configuredProperties.getValue(pMaxRate);
        IndicatorPDF intercept = (IndicatorPDF) configuredProperties.getValue(pIntercept);
        Boolean rectified = (Boolean) configuredProperties.getValue(pRectified);

        LinearNeuronFactory factory = new PoissonSpikeGenerator.LinearNeuronFactory(maxRate,
                intercept, rectified);

        return factory;
    }

    @Override
    public ConfigSchema getSchema() {
        return zConfig;
    }

}

class CSigmoidNeuronFactory extends ConstructableNodeFactory {

    static final Property pInflection = new PIndicatorPDF("Inflection","Range of x-values for the center point of the sigmoid");

    static final Property pMaxRate = pMaxRateDefault;
    static final Property pSlope = new PIndicatorPDF("Slope","Range of slopes for the sigmoid");
    static final ConfigSchema zConfig = new ConfigSchemaImpl(new Property[] { pSlope,
            pInflection, pMaxRate });

    public CSigmoidNeuronFactory() {
        super("Sigmoid Neuron", SigmoidNeuronFactory.class);
    }

    @Override
    protected NodeFactory createNodeFactory(ConfigResult configuredProperties) {
        IndicatorPDF slope = (IndicatorPDF) configuredProperties.getValue(pSlope);
        IndicatorPDF inflection = (IndicatorPDF) configuredProperties.getValue(pInflection);
        IndicatorPDF maxRate = (IndicatorPDF) configuredProperties.getValue(pMaxRate);

        return new PoissonSpikeGenerator.SigmoidNeuronFactory(slope, inflection, maxRate);
    }

    @Override
    public ConfigSchema getSchema() {
        return zConfig;
    }

}

/**
 * Customizable Neuron Factory Description Schema
 * 
 * @author Shu Wu
 */
class CSpikingNeuronFactory extends ConstructableNodeFactory {
    private static final Property pBias = new PIndicatorPDF("bias","Range of biases for the spiking neurons");
    private static final Property pScale = new PIndicatorPDF("scale","Range of gains for the spiking neurons");

    private static PListSelector getClassSelector(String selectorName, Class<?>[] classes) {
        ClassWrapper[] classWrappers = new ClassWrapper[classes.length];

        for (int i = 0; i < classes.length; i++) {
            classWrappers[i] = new ClassWrapper(classes[i]);
        }

        return new PListSelector(selectorName, classWrappers);
    }

    private Property pSpikeGenerator;

    private Property pSynapticIntegrator;

    public CSpikingNeuronFactory() {
        super("Customizable Neuron", SpikingNeuronFactory.class);
    }

    private Object constructFromClass(Class<?> type) throws ConfigException {
        try {
            Constructor<?> ct = type.getConstructor();
            try {
                return ct.newInstance();
            } catch (Exception e) {
                throw new ConfigException("Error constructing " + type.getSimpleName() + ": "
                        + e.getMessage());
            }
        } catch (SecurityException e1) {
            e1.printStackTrace();
            throw new ConfigException("Security Exception");
        } catch (NoSuchMethodException e1) {
            throw new ConfigException("Cannot find zero-arg constructor for: "
                    + type.getSimpleName());
        }
    }

    @Override
    protected NodeFactory createNodeFactory(ConfigResult configuredProperties)
            throws ConfigException {
        Class<?> synapticIntegratorClass = ((ClassWrapper) configuredProperties
                .getValue(pSynapticIntegrator)).getWrapped();
        Class<?> spikeGeneratorClass = ((ClassWrapper) configuredProperties
                .getValue(pSpikeGenerator)).getWrapped();

        IndicatorPDF scale = (IndicatorPDF) configuredProperties.getValue(pScale);
        IndicatorPDF bias = (IndicatorPDF) configuredProperties.getValue(pBias);

        /*
         * Construct Objects from Classes
         */
        SynapticIntegratorFactory synapticIntegratorFactory = (SynapticIntegratorFactory) constructFromClass(synapticIntegratorClass);
        SpikeGeneratorFactory spikeGeneratorFactory = (SpikeGeneratorFactory) constructFromClass(spikeGeneratorClass);

        return new SpikingNeuronFactory(synapticIntegratorFactory, spikeGeneratorFactory, scale,
                bias);
    }

    @Override
    public ConfigSchema getSchema() {
        /*
         * Generate these descriptors Just-In-Time, to show all possible
         * implementations in ClassRegistry
         */
        java.util.List<Class<?>> var = ClassRegistry.getInstance()
                .getImplementations(SynapticIntegratorFactory.class);
        pSynapticIntegrator = getClassSelector("Synaptic Integrator", var.toArray(new Class<?>[var.size()]));
        java.util.List<Class<?>> var2 = ClassRegistry.getInstance()
                .getImplementations(SpikeGeneratorFactory.class);
        pSpikeGenerator = getClassSelector("Spike Generator", var2.toArray(new Class<?>[var2.size()]));

        return new ConfigSchemaImpl(new Property[] { pSynapticIntegrator,
                pSpikeGenerator, pScale, pBias });
    }

    /**
     * Wraps a Class as a list item
     */
    private static class ClassWrapper {
        final Class<?> type;

        public ClassWrapper(Class<?> type) {
            super();
            this.type = type;
        }

        public Class<?> getWrapped() {
            return type;
        }

        @Override
        public String toString() {
            /*
             * Return a name string that is at most two atoms long
             */
            String canonicalName = type.getCanonicalName();
            String[] nameAtoms = canonicalName.split("\\.");
            if (nameAtoms.length > 2) {
                return nameAtoms[nameAtoms.length - 2] + '.' + nameAtoms[nameAtoms.length - 1];

            } else {
                return canonicalName;
            }

        }
    }
}

class PIndicatorPDF extends Property {

    private static final long serialVersionUID = 1L;

    public PIndicatorPDF(String name, String description) {
        super(name, description);
    }

    @Override
    protected PropertyInputPanel createInputPanel() {
        return new Panel(this);
    }

    @Override
    public Class<IndicatorPDF> getTypeClass() {
        return IndicatorPDF.class;
    }

    @Override
    public String getTypeName() {
        return "Indicator PDF";
    }

    private static class Panel extends PropertyInputPanel {
        final JTextField highValue;
        final JTextField lowValue;

        public Panel(Property property) {
            super(property);

            add(new JLabel("Low: "));
            lowValue = new JTextField(10);
            add(lowValue);

            add(new JLabel("High: "));
            highValue = new JTextField(10);
            add(highValue);

        }

        @Override
        public Object getValue() {
            String minStr = lowValue.getText();
            String maxStr = highValue.getText();

            if (minStr == null || maxStr == null) {
                return null;
            }

            try {
                Float min = new Float(minStr);
                Float max = new Float(maxStr);

                return new IndicatorPDF(min, max);
            } catch (NumberFormatException e) {
            	setStatusMsg("Invalid number format");
                return null;
            } catch (IllegalArgumentException e) {
            	setStatusMsg("Low must be less than or equal to high");
                return null;
            }

        }

        @Override
        public boolean isValueSet() {
            if (getValue() != null) {
                return true;
            } else {
                return false;
            }

        }

        @Override
        public void setValue(Object value) {
            if (value instanceof IndicatorPDF) {
                IndicatorPDF pdf = (IndicatorPDF) value;
                lowValue.setText((new Float(pdf.getLow())).toString());
                highValue.setText((new Float(pdf.getHigh())).toString());
            }
        }
    }

}

class PListSelector extends Property {
    private static final long serialVersionUID = 1L;

    private final Object[] items;

    public PListSelector(String name, Object[] items) {
        super(name);
        this.items = items;

    }

    @Override
    protected PropertyInputPanel createInputPanel() {
        return new Panel(this, items);
    }

    @Override
    public Class<Object> getTypeClass() {
        return Object.class;
    }

    @Override
    public String getTypeName() {
        return "List";
    }

    private static class Panel extends PropertyInputPanel {
        private final JComboBox comboBox;

        public Panel(Property property, Object[] items) {
            super(property);
            comboBox = new JComboBox(items);
            add(comboBox);
        }

        @Override
        public Object getValue() {
            return comboBox.getSelectedItem();
        }

        @Override
        public boolean isValueSet() {
            return true;
        }

        @Override
        public void setValue(Object value) {
            comboBox.setSelectedItem(value);
        }

    }

}