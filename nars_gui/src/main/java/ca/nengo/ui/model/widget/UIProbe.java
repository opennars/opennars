/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "UIProbe.java". Description: 
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

package ca.nengo.ui.model.widget;

import ca.nengo.model.Node;
import ca.nengo.ui.lib.object.model.ModelObject;
import ca.nengo.ui.model.UINeoNode;
import ca.nengo.ui.model.icon.ProbeIcon;

import java.awt.*;

public abstract class UIProbe extends ModelObject {

	private ProbeIcon myIcon;
	private UINeoNode nodeAttachedTo;

	public UIProbe(UINeoNode nodeAttachedTo, Object probeModel) {
		super(probeModel);
		init(nodeAttachedTo);
	}

	private void init(UINeoNode nodeAttachedTo) {
		setSelectable(true);
		this.nodeAttachedTo = nodeAttachedTo;
		myIcon = new ProbeIcon(this);
		myIcon.configureLabel(false);
		myIcon.setLabelVisible(false);
		setIcon(myIcon);
	}

	@Override
	public void dragOffset(double dx, double dy) {
		return; // not movable
	}

	public UINeoNode getProbeParent() {
		return nodeAttachedTo;
	}

	@Override
	public abstract String getTypeName();

	public void setProbeColor(Color color) {
		myIcon.setColor(color);
	}

	@Override
	protected void prepareForDestroy() {
		super.prepareForDestroy();
		getProbeParent().removeProbe(this);
	}

}
