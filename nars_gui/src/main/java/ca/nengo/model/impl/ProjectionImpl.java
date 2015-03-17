/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "ProjectionImpl.java". Description:
"Default implementation of Projection.

  TODO: unit tests

  @author Bryan Tripp"

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
 * Created on May 5, 2006
 */
package ca.nengo.model.impl;

import ca.nengo.math.Function;
import ca.nengo.math.impl.AbstractFunction;
import ca.nengo.math.impl.ConstantFunction;
import ca.nengo.math.impl.IdentityFunction;
import ca.nengo.math.impl.PostfixFunction;
import ca.nengo.model.*;
import ca.nengo.model.impl.NetworkImpl.SourceWrapper;
import ca.nengo.model.impl.NetworkImpl.TargetWrapper;
import ca.nengo.neural.nef.NEFGroup;
import ca.nengo.neural.nef.impl.BiasSource;
import ca.nengo.neural.nef.impl.BiasTarget;
import ca.nengo.neural.nef.impl.DecodedSource;
import ca.nengo.neural.nef.impl.DecodedTarget;
import ca.nengo.util.MU;
import ca.nengo.util.ScriptGenException;

import java.util.HashMap;

/**
 * Default implementation of <code>Projection</code>.
 *
 * TODO: unit tests
 *
 * @author Bryan Tripp
 */
public class ProjectionImpl implements Projection {

	private static final long serialVersionUID = 1L;

	private final NSource mySource;
	private final NTarget myTarget;
	private final Network myNetwork;

	private boolean myBiasIsEnabled;
	private NEFGroup myInterneurons;
	private BiasSource myBiasOrigin;
	private BiasTarget myDirectBT;
	private BiasTarget myIndirectBT;
	private DecodedTarget myInterneuronTermination;

	/**
	 * @param source  The Origin at the start of this Projection
	 * @param target  The Termination at the end of this Projection
	 * @param network The Network of which this Projection is a part
	 */
	public ProjectionImpl(NSource source, NTarget target, Network network) {
		mySource = source;
		myTarget = target;
		myNetwork = network;

		myBiasIsEnabled = false;
		myInterneurons = null;
		myDirectBT = null;
		myIndirectBT = null;
	}

	/**
	 * @see ca.nengo.model.Projection#getSource()
	 */
	public NSource getSource() {
		return mySource;
	}

	/**
	 * @see ca.nengo.model.Projection#getTarget()
	 */
	public NTarget getTarget() {
		return myTarget;
	}

	/**
	 * @see ca.nengo.model.Projection#biasIsEnabled()
	 */
	public boolean biasIsEnabled() {
		return myBiasIsEnabled;
	}

	/**
	 * @see ca.nengo.model.Projection#enableBias(boolean)
	 */
	public void enableBias(boolean enable) {
		if (myInterneurons != null) {
			myDirectBT.setEnabled(enable);
			myIndirectBT.setEnabled(enable);
			myBiasIsEnabled = enable;
		}
	}

	/**
	 * @see ca.nengo.model.Projection#getNetwork()
	 */
	public Network getNetwork() {
		return myNetwork;
	}

	/**
	 * @throws StructuralException if the origin and termination are not decoded
	 * @see ca.nengo.model.Projection#addBias(int, float, float, boolean, boolean)
	 */
	public void addBias(int numInterneurons, float tauInterneurons, float tauBias, boolean excitatory, boolean optimize) throws StructuralException {
		if ( !(mySource instanceof DecodedSource) || !(myTarget instanceof DecodedTarget)) {
			throw new RuntimeException("This feature is only implemented for projections from DecodedOrigins to DecodedTerminations");
		}

		DecodedSource baseOrigin = (DecodedSource) mySource;
		DecodedTarget baseTermination = (DecodedTarget) myTarget;
		NEFGroup pre = (NEFGroup) baseOrigin.getNode();
		NEFGroup post = (NEFGroup) baseTermination.getNode();

		myBiasOrigin = pre.addBiasOrigin(baseOrigin, numInterneurons, getUniqueNodeName(post.name() + '_' + baseTermination.getName()), excitatory);
		myInterneurons = myBiasOrigin.getInterneurons();
		myNetwork.addNode(myInterneurons);
		BiasTarget[] bt = post.addBiasTerminations(baseTermination, tauBias, myBiasOrigin.getDecoders(), baseOrigin.getDecoders());
		myDirectBT = bt[0];
		myIndirectBT = bt[1];
		if (!excitatory) {
            myIndirectBT.setStaticBias(new float[]{-1});
        }
		float[][] tf = new float[][]{new float[]{0, 1/tauInterneurons/tauInterneurons}, new float[]{2/tauInterneurons, 1/tauInterneurons/tauInterneurons}};
		myInterneuronTermination = (DecodedTarget) myInterneurons.addDecodedTermination("bias", MU.I(1), tf[0], tf[1], 0, false);

		myNetwork.addProjection(myBiasOrigin, myDirectBT);
		myNetwork.addProjection(myBiasOrigin, myInterneuronTermination);
		myNetwork.addProjection(myInterneurons.getSource(NEFGroup.X), myIndirectBT);

		if (optimize) {
			float[][] baseWeights = MU.prod(post.getEncoders(), MU.prod(baseTermination.getTransform(), MU.transpose(baseOrigin.getDecoders())));
			myBiasOrigin.optimizeDecoders(baseWeights, myDirectBT.getBiasEncoders(), excitatory);
			myBiasOrigin.optimizeInterneuronDomain(myInterneuronTermination, myIndirectBT);
		}

		myBiasIsEnabled = true;
	}

	private String getUniqueNodeName(String base) {
		String result = base;
		boolean done = false;
		int c = 2;
		Node[] nodes = myNetwork.getNodes();
		while (!done) {
			done = true;
			for (Node node : nodes) {
				if (node.name().equals(result)) {
					done = false;
					result = base + c++;
				}
			}
		}
		return result;
	}

	/**
	 * @see ca.nengo.model.Projection#removeBias()
	 */
	public void removeBias() {
		try {
			DecodedSource baseOrigin = (DecodedSource) mySource;
			DecodedTarget baseTermination = (DecodedTarget) myTarget;
			NEFGroup pre = (NEFGroup) baseOrigin.getNode();
			NEFGroup post = (NEFGroup) baseTermination.getNode();

			myNetwork.removeProjection(myDirectBT);
			myNetwork.removeProjection(myIndirectBT);
			myNetwork.removeProjection(myInterneuronTermination);
			myNetwork.removeNode(myInterneurons.name());

			pre.removeDecodedOrigin(myBiasOrigin.getName());
			post.removeDecodedTermination(myDirectBT.getName());
			post.removeDecodedTermination(myIndirectBT.getName());

			myBiasIsEnabled = false;
		} catch (StructuralException e) {
			throw new RuntimeException("Error while trying to remove bias (this is probably a bug in ProjectionImpl)", e);
		}
	}

	/**
	 * @see ca.nengo.model.Projection#getWeights()
	 */
	public float[][] getWeights() {
		float[][] result = null;

		if ( (mySource instanceof DecodedSource) && (myTarget instanceof DecodedTarget)) {
			float[][] encoders = ((NEFGroup) myTarget.getNode()).getEncoders();
			float[][] transform = ((DecodedTarget) myTarget).getTransform();
			float[][] decoders = ((DecodedSource) mySource).getDecoders();
			result = MU.prod(encoders, MU.prod(transform, MU.transpose(decoders)));

			if (myBiasIsEnabled) {
				float[] biasEncoders = myDirectBT.getBiasEncoders();
				float[][] biasDecoders = myBiasOrigin.getDecoders();
				float[][] weightBiases = MU.prod(MU.transpose(new float[][]{biasEncoders}), MU.transpose(biasDecoders));
				result = MU.sum(result, weightBiases);
			}
		} else if (myTarget instanceof DecodedTarget) {
			float[][] encoders = ((NEFGroup) myTarget.getNode()).getEncoders();
			float[][] transform = ((DecodedTarget) myTarget).getTransform();
			result = MU.prod(encoders, transform);
		} else {
			//TODO: add getWeights() to Termination, implement in EnsembleTermination from LinearExponentialTermination.getWeights()
			throw new RuntimeException("Not implemented for non-DecodedTerminations");
		}

		return result;
	}
	
	public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException {
		
	    StringBuilder py = new StringBuilder();
	    
	    String pythonNetworkName = scriptData.get("prefix") 
	    			+ getNetwork().name().replaceAll("\\p{Blank}|\\p{Punct}", scriptData.get("spaceDelim").toString());
	    
	    py.append(String.format("%1s.connect(", pythonNetworkName));
	    
	    StringBuilder originNodeFullName = new StringBuilder();
	    NSource tempSource = mySource;

	    while(tempSource instanceof SourceWrapper)
	    {
	    	originNodeFullName.append(tempSource.getNode().name()).append('.');
	    	tempSource = ((SourceWrapper) tempSource).getWrappedOrigin();
	    }
	    
	    StringBuilder terminationNodeFullName = new StringBuilder();
	    NTarget tempTarget = myTarget;

	    while(tempTarget instanceof TargetWrapper)
	    {
	    	terminationNodeFullName.append(tempTarget.getNode().name()).append('.');
	    	tempTarget = ((TargetWrapper) tempTarget).getWrappedTermination();
	    }
	    
	    DecodedTarget dTermination;
	    StringBuilder transformString = new StringBuilder();
	    
	    transformString.append('[');
	    if(tempTarget instanceof DecodedTarget)
	    {
	    	dTermination = (DecodedTarget) tempTarget;
	    	transformString.append(getTransformScript(dTermination, "transform = ".length()));
	    	terminationNodeFullName.append(tempTarget.getNode().name());
	    }
	    else if(tempTarget instanceof GroupTarget &&
	    		tempTarget.getNode() instanceof NetworkArrayImpl)
	    {
	    	terminationNodeFullName.deleteCharAt(terminationNodeFullName.length()-1);
	    	
	    	boolean first = true;
	    	for(Node node : ((NetworkArrayImpl) tempTarget.getNode()).getNodes())
	    	{
	    		if(first)
	    		{
	    			first = false;
	    		}else {
	    			transformString.append(",\n").append(new String(new char["transform = ".length() + 1]).replace("\0", " "));
	    		}
	    		
	    		// this relies on the decoded terminations in the child nodes having the 
	    		// same name as the ensemble termination that contains them
	    		try{
	    			dTermination = (DecodedTarget) node.getTarget(tempTarget.getName());
	    		}catch(Exception e){
	    			dTermination = null;
	    		}
	    		
	    		transformString.append(getTransformScript(dTermination, "transform = ".length() + 1));
	    	}
	    }
	    else
	    {
	    	throw new ScriptGenException("Trying to generate script of non decoded termination which is not supported.");
	    }

	    transformString.append("]\n");
	   
	    // Now handle origin function if there is one
	    
	    String functionName = "";
	    if(tempSource instanceof BasicSource && tempSource.getNode() instanceof FunctionInput)
	    {
	    	originNodeFullName.append(tempSource.getNode().name());
	    }
	    else
	    {
		    DecodedSource dOrigin;
		    if(tempSource instanceof DecodedSource)
		    {
		    	dOrigin = (DecodedSource) tempSource;
		    	originNodeFullName.append(tempSource.getNode().name());
		    }
		    else if(tempSource instanceof NetworkArrayImpl.ArraySource &&
		    		tempSource.getNode() instanceof NetworkArrayImpl)
		    {
		    	originNodeFullName.deleteCharAt(originNodeFullName.length()-1);
		    	Node node = (((NetworkArrayImpl)(tempSource.getNode())).getNodes()[0]);
		    	
		    	try{
		    		dOrigin = (DecodedSource) node.getSource(tempSource.getName());
		    	}catch(StructuralException e){
		    		dOrigin = null;
		    	}
		    }
		    else
		    {
		    	throw new ScriptGenException("Trying to generate script of non decoded origin which is not supported.");
		    }
		    
		    functionName = addFunctionScript(py, dOrigin);
	    }
	    
	    py.append("\'").append(originNodeFullName).append('\'');
	    py.append(", \'").append(terminationNodeFullName).append('\'');
	    
	    py.insert(0, "transform = " + transformString);
	    py.append(", transform=transform");
	    
	    if(functionName != ""){
	    	py.append(", func=").append(functionName);
	    }
	    
	    py.append(")\n\n");
	    
	    return py.toString();
	}
	
	String getTransformScript(DecodedTarget dTermination, int offset) {
		StringBuilder transformString = new StringBuilder();
		float[][] transform = dTermination.getTransform();
	    
	    for(int i = 0; i < transform.length; i++)
	    {
	    	if(i != 0) {
	    		transformString.append(",\n ").append(new String(new char[offset]).replace("\0", " "));
	    	}
	    	
	    	transformString.append('[');
	    	
	    	for(int j = 0; j < transform[i].length; j++)
	    	{
	    		if(j != 0)
		    		transformString.append(", ");
		    	
		    	transformString.append(transform[i][j]);
	    	}
	    	
	    	transformString.append(']');
	    }
	    
	    return transformString.toString();
	}
	
	String addFunctionScript(StringBuilder py, DecodedSource dOrigin) throws ScriptGenException
	{
		StringBuilder funcString = new StringBuilder();
	    boolean first = true;
	    
	    Function[] fns = dOrigin.getFunctions();
	    
	    boolean allIdentity = true;
	    for(Function f: fns)
	    {
	    	 if(!(f instanceof IdentityFunction))
	    	 {
	    		 allIdentity = false;
	    		 break;
	    	 }
	    }
	    
	    if(allIdentity){
	    	return "";
	    }

//	    String n = fns[0].getClass().getCanonicalName();
    	if(fns.length > 0 && fns[0].getClass().getCanonicalName() == "org.python.proxies.nef.functions$PythonFunction$3")
    	{
    		AbstractFunction absFun = (AbstractFunction) fns[0];
    		String code = absFun.getCode();
    		
    		StringBuilder indentedCode = new StringBuilder();
    		
    		String[] split = code.split("\n");
    		
    		int i = 0;
    		while(split[0].startsWith(" ", i)){
    			i++;
    		}
    		
    		if(i > 0){
	    		for(String s: split){
	    			indentedCode.append(s.substring(i)).append('\n');
	    		}
    		}
    		
    		code = indentedCode.toString();
    		
    		if(code != ""){
    			py.insert(0, code);
    		}else{
    			throw new ScriptGenException("Trying to generate script of non user-defined function on an origin which is not supported.");
    		}
    		
    		
    		return absFun.getName();
    	}
    	
	    for(Function f: fns)
	    {
	    	String exp;
	    	if(f instanceof PostfixFunction)
	    	{
	    		PostfixFunction pf = (PostfixFunction) f;
	    		exp = pf.getExpression();
	    		
	    		exp=exp.replaceAll("\\^","**");
	    		exp=exp.replaceAll("!"," not ");
	    		exp=exp.replaceAll("&"," and ");
	    		exp=exp.replaceAll("\\|"," or ");
	    		exp=exp.replaceAll("ln","log");
	    		
	    		for(int j = 0; j < f.getDimension(); j++)
	    		{
	    			String find = 'x' + Integer.toString(j);
	    			String replace = "x["+ Integer.toString(j) + ']';
	    			exp=exp.replaceAll(find, replace);
	    		}
	    	}
	    	else if(f instanceof IdentityFunction)
	    	{
	    		exp = "x[" + Integer.toString(((IdentityFunction) f).getIdentityDimension()) + ']';
	    	}
	    	else if(f instanceof ConstantFunction)
	    	{
	    		exp = Float.toString(((ConstantFunction) f).getValue());
	    	}
	    	else
	    	{
	    		throw new ScriptGenException("Trying to generate script of non user-defined function on an origin which is not supported.");
	    	}
	    	
	    	if (first)
    		{
    			funcString.append(exp);
    			first = false;
    		}
    		else
    		{
    			funcString.append(", ").append(exp);
    		}
	    }
	    
    	py.insert(0, "    return [" + funcString + "]\n\n");
	    py.insert(0, "def function(x):\n");
	    
	    return "function";
	}
}
