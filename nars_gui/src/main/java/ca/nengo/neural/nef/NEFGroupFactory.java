/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "NEFEnsembleFactory.java". Description: 
"Provides a convenient and configurable way to create NEFEnsembles"

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
 * Created on 20-Feb-07
 */
package ca.nengo.neural.nef;

import ca.nengo.math.ApproximatorFactory;
import ca.nengo.model.StructuralException;
import ca.nengo.model.impl.NodeFactory;
import ca.nengo.util.VectorGenerator;

/**
 * Provides a convenient and configurable way to create NEFEnsembles. 
 *  
 * @author Bryan Tripp
 */
public interface NEFGroupFactory {

	/**
	 * @return The NodeFactory used to create Nodes that make up new Ensembles
	 */
	public NodeFactory getNodeFactory();

	/**
	 * @param factory NodeFactory to be used to create Nodes that make up new Ensembles
	 */
	public void setNodeFactory(NodeFactory factory);

	/**
	 * @return The VectorGenerator used to create encoding vectors that are associated with 
	 * 		each Node in a new Ensemble
	 */
	public VectorGenerator getEncoderFactory();
	
	/**
	 * @param factory A VectorGenerator to be used to create encoding vectors that are associated 
	 * 		with each Node in a new Ensemble
	 */
	public void setEncoderFactory(VectorGenerator factory);
	
	/**
	 * @return The VectorGenerator used to generate the vector states at which decoding functions are 
	 * 		evaluated
	 */
	public VectorGenerator getEvalPointFactory();

	/**
	 * @param factory A VectorGenerator to be used to generate the vector states at which decoding 
	 * 		functions are evaluated
	 */
	public void setEvalPointFactory(VectorGenerator factory);

	/**
	 * @return The factory that creates LinearApproximators used in decoding ensemble output  
	 */
	public ApproximatorFactory getApproximatorFactory();
	
	/**
	 * @param factory A factory for creating the LinearApproximators used in decoding ensemble output 
	 */
	public void setApproximatorFactory(ApproximatorFactory factory);	
	
	/**
	 * @param name Name of the NEFEnsemble
	 * @param n Number of neurons in the ensemble
	 * @param dim Dimension of the ensemble. 
	 * @return NEFEnsemble containing Neurons generated with the default NeuronFactory   
	 * @throws StructuralException if there is any error attempting to create the ensemble
	 */
	public NEFGroup make(String name, int n, int dim) throws StructuralException;
	
	/**
	 * @param name Name of the NEFEnsemble
	 * @param n Number of neurons in the ensemble
	 * @param radii Radius of encoded region in each dimension 
	 * @return NEFEnsemble containing Neurons generated with the default NeuronFactory   
	 * @throws StructuralException if there is any error attempting to create the ensemble
	 */
	public NEFGroup make(String name, int n, float[] radii) throws StructuralException;
	
	/**
	 * Loads an NEFEnsemble, or creates and saves it.
	 *   
	 * @param name Name of the NEFEnsemble
	 * @param n Number of neurons in the ensemble
	 * @param radii Radius of encoded region in each dimension.
	 * @param storageName Name for storage (eg filename, db key; may have to be more fully qualified than 
	 * 		name param, if ensembles belonging to multiple networks are stored in the same place)
	 * @param overwrite If false, loads the ensemble if it can be found in storage. 
	 * 		If true, creates a new ensemble regardless and overwrites any existing ensemble. 
	 * @return Either new NEFEnsemble generated according to specs and with default NeuronFactory, or 
	 * 		a previously-created ensemble loaded from storage   
	 * @throws StructuralException if there is any error attempting to create the ensemble
	 */
	public NEFGroup make(String name, int n, float[] radii, String storageName, boolean overwrite) throws StructuralException;
	
	/**
	 * Loads an NEFEnsemble, or creates and saves it.
	 *
	 * @param name Name of the NEFEnsemble
	 * @param n Number of neurons in the ensemble
	 * @param dim Dimension of the ensemble.
	 * @param storageName Name for storage (eg filename, db key; may have to be more fully qualified than
	 * 		name param, if ensembles belonging to multiple networks are stored in the same place)
	 * @param overwrite If false, loads the ensemble if it can be found in storage.
	 * 		If true, creates a new ensemble regardless and overwrites any existing ensemble.
	 * @return Either new NEFEnsemble generated according to specs and with default NeuronFactory, or
	 * 		a previously-created ensemble loaded from storage
	 * @throws StructuralException if there is any error attempting to create the ensemble
	 */
	public NEFGroup make(String name, int n, int dim, String storageName, boolean overwrite) throws StructuralException;
}
