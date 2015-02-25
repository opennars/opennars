/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "EnsembleIcon.java". Description: 
"Icon for an Ensemble
  
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

import ca.nengo.ui.lib.NengoStyle;
import ca.nengo.ui.lib.world.PaintContext;
import ca.nengo.ui.lib.world.piccolo.WorldObjectImpl;
import ca.nengo.ui.model.node.UIGroup;

import java.awt.*;

/**
 * Icon for an Ensemble
 * 
 * @author Shu Wu
 */
public class GroupIcon extends NodeContainerIcon {
	public GroupIcon(UIGroup parent) {

		super(parent, new VectorIcon());

	}

	@Override
	protected int getNodeCountNormalization() {
		return 1000;
	}

	private static class VectorIcon extends WorldObjectImpl {

		private static final int CircleDiameter = 16;
		private static final int Padding = 4;

		public VectorIcon() {
			super();
			int iconSize = 44 + CircleDiameter + Padding * 2;
			this.setBounds(0, 0, iconSize, iconSize);
		}

		@Override
		public void paint(PaintContext paintContext) {
			super.paint(paintContext);

			Graphics2D g2 = paintContext.getGraphics();

			g2.setColor(NengoStyle.COLOR_FOREGROUND);
			g2.translate(Padding, Padding);

			g2.fillOval(2, 9, CircleDiameter, CircleDiameter);
			g2.fillOval(1, 34, CircleDiameter, CircleDiameter);
			g2.fillOval(26, 0, CircleDiameter, CircleDiameter);
			g2.fillOval(22, 22, CircleDiameter, CircleDiameter);
			g2.fillOval(44, 21, CircleDiameter, CircleDiameter);
			g2.fillOval(28, 44, CircleDiameter, CircleDiameter);
		}
	}
}
