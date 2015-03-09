/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "NEFEnsembleFactoryImpl.java". Description:
"Default implementation of NEFEnsembleFactory"

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

/*
 * Created on 4-Mar-07
 */
package ca.nengo.neural.nef.impl;

import ca.nengo.io.FileManager;
import ca.nengo.math.ApproximatorFactory;
import ca.nengo.math.Function;
import ca.nengo.math.impl.IdentityFunction;
import ca.nengo.math.impl.IndicatorPDF;
import ca.nengo.math.impl.WeightedCostApproximator;
import ca.nengo.model.Node;
import ca.nengo.model.SimulationMode;
import ca.nengo.model.StructuralException;
import ca.nengo.model.impl.NodeFactory;
import ca.nengo.neural.nef.NEFGroup;
import ca.nengo.neural.nef.NEFGroupFactory;
import ca.nengo.neural.nef.NEFNode;
import ca.nengo.neural.neuron.Neuron;
import ca.nengo.neural.neuron.impl.LIFNeuronFactory;
import ca.nengo.util.MU;
import ca.nengo.util.VectorGenerator;
import ca.nengo.util.VisiblyChangesUtils;
import ca.nengo.util.impl.RandomHypersphereVG;
import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.IOException;

/**
 * Default implementation of NEFEnsembleFactory.
 *
 * @author Bryan Tripp
 */
public class NEFGroupFactoryImpl implements NEFGroupFactory, java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger ourLogger = LogManager.getLogger(NEFGroupFactoryImpl.class);

	private ApproximatorFactory myApproximatorFactory;
	private VectorGenerator myEncoderFactory;
	private VectorGenerator myEvalPointFactory;
	private NodeFactory myNodeFactory;
	private transient File myDatabase;

	/**
	 * Default constructor. Sets up factories.
	 */
	public NEFGroupFactoryImpl() {
		myApproximatorFactory = new WeightedCostApproximator.Factory(0.1f);
		myEncoderFactory = new RandomHypersphereVG(true, 1f, 0f);
		myEvalPointFactory = new RandomHypersphereVG(false, 1f, 0f);
		myNodeFactory = new LIFNeuronFactory(.02f, .002f, new IndicatorPDF(200f, 400f), new IndicatorPDF(-.9f, .9f));
	}

	/**
	 * @see ca.nengo.neural.nef.NEFGroupFactory#getApproximatorFactory()
	 */
	public ApproximatorFactory getApproximatorFactory() {
		return myApproximatorFactory;
	}

	/**
	 * @see ca.nengo.neural.nef.NEFGroupFactory#getEncoderFactory()
	 */
	public VectorGenerator getEncoderFactory() {
		return myEncoderFactory;
	}

	/**
	 * @see ca.nengo.neural.nef.NEFGroupFactory#getEvalPointFactory()
	 */
	public VectorGenerator getEvalPointFactory() {
		return myEvalPointFactory;
	}

	/**
	 * @see ca.nengo.neural.nef.NEFGroupFactory#getNodeFactory()
	 */
	public NodeFactory getNodeFactory() {
		return myNodeFactory;
	}

	/**
	 * Stops the factory from printing out information to console during make process.
	 */
	public void beQuiet() {
		if(myApproximatorFactory instanceof WeightedCostApproximator.Factory) {
            ((WeightedCostApproximator.Factory)myApproximatorFactory).setQuiet(true);
        } else {
            System.out.println("beQuiet() not supported by this approximator factory");
        }
	}

	/**
	 * @see ca.nengo.neural.nef.NEFGroupFactory#make(java.lang.String, int, int)
	 */
	public NEFGroup make(String name, int n, int dim) throws StructuralException {
		float[] radii = MU.uniform(1, dim, 1)[0];
		return doMake(name, n, radii, 0);
	}

	/**
	 * @see ca.nengo.neural.nef.NEFGroupFactory#make(java.lang.String, int, float[])
	 */
	public NEFGroup make(String name, int n, float[] radii) throws StructuralException {
		return doMake(name, n, radii, 0);
	}

	/**
	 * @see ca.nengo.neural.nef.NEFGroupFactory#make(java.lang.String, int, int, java.lang.String, boolean)
	 */
	public NEFGroup make(String name, int n, int dim, String storageName, boolean overwrite) throws StructuralException {
        float[] radii = MU.uniform(1, dim, 1)[0];
        return make(name, n, radii, storageName, overwrite);
    }

	/**
	 * @see ca.nengo.neural.nef.NEFGroupFactory#make(java.lang.String, int, int, java.lang.String, boolean)
	 */
	public NEFGroup make(String name, int n, float[] radii, String storageName, boolean overwrite) throws StructuralException {
        int dim = radii.length;
		NEFGroup result = null;

        if( storageName.length() > 0 ){
            File ensembleFile = new File(myDatabase, storageName + '.' + FileManager.ENSEMBLE_EXTENSION);

            FileManager fm = new FileManager();

            if (!overwrite && ensembleFile.exists() && ensembleFile.canRead()) {
                try {
                    result = (NEFGroup) fm.load(ensembleFile);

                    result.setName(name);
                    if(result.getNodes().length != n) {
                        ourLogger.warn("Number of nodes in ensemble loaded from file does not match requested number of nodes");
                    }
                    if(result.getDimension() != dim) {
                        ourLogger.warn("Dimension of ensemble loaded from file does not match requested dimension");
                    }
                } catch (Exception e) {
                    ourLogger.error("Failed to load file " + ensembleFile.getAbsolutePath() + ". New ensemble will be created.", e);
                }
            }
            if (result == null) {
                result = doMake(name, n, radii, 0);

                try {
                    // Set the ensemble's factory to null to allow saving with customized ensemble factories
                    result.setEnsembleFactory(null);
                    fm.save(result, ensembleFile);
                } catch (IOException e) {
                    ourLogger.error("Failed to save file " + ensembleFile.getAbsolutePath(), e);
                }
            }
		}
        else{
            result = doMake(name, n, radii, 0);
        }

        // Set the resulting ensemble's factory to this. It must be noted that this can be a good thing or
        // bad thing. It is possible that you want the original ensemble factory, in which case this will
        // fail. Although, with this implementation, it is possible to change the ensemble factory of the
        // ensemble after loading, which might be a good thing.
		result.setEnsembleFactory(this);
		return result;
	}

	/**
	 * @see ca.nengo.neural.nef.NEFGroupFactory#setApproximatorFactory(ca.nengo.math.ApproximatorFactory)
	 */
	public void setApproximatorFactory(ApproximatorFactory factory) {
		myApproximatorFactory = factory;
	}

	/**
	 * @see ca.nengo.neural.nef.NEFGroupFactory#setEncoderFactory(ca.nengo.util.VectorGenerator)
	 */
	public void setEncoderFactory(VectorGenerator factory) {
		myEncoderFactory = factory;
	}

	/**
	 * @see ca.nengo.neural.nef.NEFGroupFactory#setEvalPointFactory(ca.nengo.util.VectorGenerator)
	 */
	public void setEvalPointFactory(VectorGenerator factory) {
		myEvalPointFactory = factory;
	}

	/**
	 * @see ca.nengo.neural.nef.NEFGroupFactory#setNodeFactory(ca.nengo.model.impl.NodeFactory)
	 */
	public void setNodeFactory(NodeFactory factory) {
		myNodeFactory = factory;
	}

    //common make(...) implementation
    private NEFGroup doMake(String name, int n, float[] radii, int attempts) throws StructuralException {
        while (true) {

            try {
                int dim = radii.length;
                NEFNode[] nodes = new NEFNode[n];

                if (n < 1) {
                    ourLogger.error("Calling doMake with n = " + n);
                }

                for (int i = 0; i < n; i++) {
                    Node node = myNodeFactory.make("node" + i);
                    if (!(node instanceof NEFNode)) {
                        throw new StructuralException("Nodes must be NEFNodes");
                    }
                    nodes[i] = (NEFNode) node;

                    nodes[i].setMode(SimulationMode.CONSTANT_RATE);
                    if (!nodes[i].getMode().equals(SimulationMode.CONSTANT_RATE)) {
                        throw new StructuralException("Neurons in an NEFEnsemble must support CONSTANT_RATE mode");
                    }

                    nodes[i].setMode(SimulationMode.DEFAULT);
                }

                float[][] encoders = myEncoderFactory.genVectors(n, dim);
                float[][] evalPoints = getEvalPointFactory().genVectors(getNumEvalPoints(dim), dim);
                NEFGroup result = construct(name, nodes, encoders, myApproximatorFactory, evalPoints, radii);

                addDefaultOrigins(result);

                result.setEnsembleFactory(this);

                return result;
            } catch (RuntimeException re) {
                // a singular gamma matrix can produce a runtime exception.  If this occurs,
                // call make again.
                if (re.getMessage() != null && re.getMessage().equals("Matrix is singular.")) {
                    if (attempts < 10) {
                        attempts = attempts + 1;
                    } else {
                        throw new StructuralException("Error creating ensemble: With the given parameters, there is insufficient\nneural activity to construct a decoder (the activity matrix is singular)");
                    }
                } else {
                    System.err.println(re);
                    return (null);
                }
            }
        }
    }

	/**
	 * This method is exposed so that it can be over-ridden to change behaviour.
	 *
	 * @param dim the dimension of the state represented by an Ensemble
	 * @return The number of points at which to approximate decoded functions
	 */
	protected int getNumEvalPoints(int dim) {
		int[] pointsPerDim = new int[]{0, 1000, 1000};
		int pts=(dim < pointsPerDim.length) ? pointsPerDim[dim] : dim*500;
		if (pts>5000)
         {
            pts=5000;  // there seems to be no advantage to going to more than 1000 points,
        }
		                         //  even for high dimensions (~500), but let's go with 5000 to be sure
		return pts;
	}

	/**
	 * This method is exposed so that it can be over-ridden to change behaviour.
	 *
	 * @param name Name of new Ensemble
	 * @param nodes Nodes that make up Ensemble
	 * @param encoders Encoding vector for each Node
	 * @param af Factory that produces LinearApproximators for decoding Ensemble output
	 * @param evalPoints States at which Node output is evaluated for decoding purposes
	 * @param radii Radius of encoded area in each dimension
	 * @return New NEFEnsemble with given parameters
	 * @throws StructuralException
	 */
	protected NEFGroup construct(String name, NEFNode[] nodes, float[][] encoders, ApproximatorFactory af, float[][] evalPoints, float[] radii)
			throws StructuralException {
		if (!VisiblyChangesUtils.isValidName(name)) throw new StructuralException("name '"+name+"' must not contain '.' or ':'");
		return new NEFGroupImpl(name, nodes, encoders, af, evalPoints, radii);
	}

	/**
	 * Adds standard decoded Origins to the given NEFEnsemble
	 *
	 * This method is exposed so that it can be over-ridden to change behaviour.
	 *
	 * @param ensemble A new NEFEnsemble
	 * @throws StructuralException
	 */
	protected void addDefaultOrigins(NEFGroup ensemble) throws StructuralException {
		Function[] functions = new Function[ensemble.getDimension()];
		for (int i = 0; i < functions.length; i++) {
			functions[i] = new IdentityFunction(ensemble.getDimension(), i);
		}

		ensemble.addDecodedOrigin(NEFGroup.X, functions, Neuron.AXON);
	}

	/**
	 * @return Directory for saving / loading ensembles
	 */
	public File getDatabase() {
		return myDatabase;
	}

	/**
 	 * @param database New directory for saving / loading ensembles
	 */
	public void setDatabase(File database) {
		if ( !database.isDirectory() ) {
			throw new IllegalArgumentException("Database must be a file directory");
		}

		myDatabase = database;
	}

}
