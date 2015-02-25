/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "FunctionInputIcon.java". Description: 
"Icon for an Function Input
  
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

package ca.nengo.ui.model.icon;

import ca.nengo.ui.lib.object.model.ModelObject;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.lib.world.piccolo.WorldObjectImpl;
import ca.nengo.ui.lib.world.piccolo.primitive.Text;

import java.awt.*;

/**
 * Icon for an Function Input
 * 
 * @author Shu Wu
 */
public class FunctionInputIcon extends ModelIcon {
	// Using "Serif" because Font.SERIF is not in Java 1.5 - TWB
	public static final Font FONT = new Font("Serif", Font.ITALIC, 45);
	public static final String TEXT = "f(t)";
	private static final int PADDING = 10;

	public FunctionInputIcon(ModelObject parent) {
		super(parent, createTextIcon());
	}

	private static WorldObject createTextIcon() {
		WorldObject titleHolder = new WorldObjectImpl();

		Text title = new Text(TEXT);
		title.setFont(FONT);

		title.setOffset(PADDING * 1.5, -0.5 * PADDING);
		titleHolder.addChild(title);

		titleHolder.setBounds(0, 0, title.getWidth() + PADDING * 2, title.getHeight());
		return titleHolder;
	}
}
