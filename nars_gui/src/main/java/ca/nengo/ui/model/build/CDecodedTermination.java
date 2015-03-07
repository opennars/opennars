/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "CDecodedTermination.java". Description: 
""

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

import ca.nengo.model.StructuralException;
import ca.nengo.model.NTarget;
import ca.nengo.neural.nef.NEFGroup;
import ca.nengo.ui.config.*;
import ca.nengo.ui.config.descriptors.PBoolean;
import ca.nengo.ui.config.descriptors.PFloat;
import ca.nengo.ui.config.descriptors.PTerminationWeights;
import ca.nengo.ui.model.widget.UIDecodedTarget;

public class CDecodedTermination extends ProjectionConstructor {
	private static final Property pIsModulatory = new PBoolean("Is Modulatory");

	private static final Property pTauPSC = new PFloat("tauPSC [s]","Synaptic time constant, in seconds");
	private final NEFGroup nefEnsembleParent;

	private Property pTransformMatrix;

	public CDecodedTermination(NEFGroup nefEnsembleParent) {
		super();
		this.nefEnsembleParent = nefEnsembleParent;
	}

	@Override
	public ConfigSchema getSchema() {
		pTransformMatrix = new PTerminationWeights("Weights", nefEnsembleParent.getDimension());

		Property[] zProperties = { pName, pTransformMatrix, pTauPSC, pIsModulatory };
		return new ConfigSchemaImpl(zProperties);

	}

	public String getTypeName() {
		return UIDecodedTarget.typeName;
	}

	@Override
	protected boolean IsNameAvailable(String name) {
		try {
			return nefEnsembleParent.getTarget(name) == null;
		} catch (StructuralException e) {
			return false;
		}
	}

	@Override
	protected Object createModel(ConfigResult configuredProperties, String uniqueName) throws ConfigException {
				

		NTarget term = null;
		try {
			term = nefEnsembleParent.addDecodedTermination(uniqueName,
					(float[][]) configuredProperties.getValue(pTransformMatrix), (Float) configuredProperties
							.getValue(pTauPSC), (Boolean) configuredProperties.getValue(pIsModulatory));

		} catch (StructuralException e) {
			e.printStackTrace();
		}

		return term;
	}

}
