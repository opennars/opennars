/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "NetworkIcon.java". Description: 
"Icon for a Network
  
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
import ca.nengo.ui.model.node.UINetwork;

import java.awt.*;

/**
 * Icon for a Network
 * 
 * @author Shu Wu
 */
public class NetworkIcon extends NodeContainerIcon {

	public NetworkIcon(UINetwork parent) {
		super(parent, new VectorIcon());

	}

	@Override
	protected int getNodeCountNormalization() {
		return 1000;
	}

	private static class VectorIcon extends WorldObjectImpl {
		private static final int NumberOfNodeColumns = 2;
		private static final int NumberOfNodeRows = 3;
		private static final int LineWidth = 4;
		private static final int CircleDiameter = 18;
		private static final int CircleRadius = CircleDiameter / 2;
		private static final int RowHeight = 30;
		private static final int ColumnWidth = 75;
		private static final int Padding = 4;

		public VectorIcon() {
			super();
			this.setBounds(0, 0, Padding * 2 + CircleDiameter + (NumberOfNodeColumns - 1) * ColumnWidth, Padding * 2
					+ CircleDiameter + (NumberOfNodeRows - 1) * RowHeight);
		}

		@Override
		public void paint(PaintContext paintContext) {
			super.paint(paintContext);

			Graphics2D g2 = paintContext.getGraphics();
			g2.setStroke(new BasicStroke(LineWidth));
			g2.setColor(NengoStyle.COLOR_FOREGROUND);
			g2.translate(Padding, Padding);

			// Draw grid
			for (int rowCount = 0; rowCount < NumberOfNodeRows; rowCount++) {
				int yPosition = rowCount * RowHeight;

				g2.drawLine(CircleRadius, yPosition + CircleRadius, ColumnWidth + CircleRadius, yPosition
						+ CircleRadius);

				for (int columnCount = 0; columnCount < NumberOfNodeColumns; columnCount++) {

					g2.fillOval(columnCount * ColumnWidth, yPosition, CircleDiameter, CircleDiameter);

				}
			}

			// Draw diagonal line: top-left to bottom-right
			g2.drawLine(CircleRadius, CircleRadius, CircleRadius + ColumnWidth * (NumberOfNodeColumns - 1),
					CircleRadius + RowHeight * (NumberOfNodeRows - 1));

			// Draw diagonal line: bottom-left to top-right
			g2.drawLine(CircleRadius, CircleRadius + RowHeight * (NumberOfNodeRows - 1), CircleRadius + ColumnWidth
					* (NumberOfNodeColumns - 1), CircleRadius);

		}
	}
}
