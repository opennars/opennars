/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "Tooltip.java". Description: 
"UI Object which builds itself from a ToolTipBuilder
  
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

package ca.nengo.ui.model.tooltip;

import ca.nengo.ui.lib.NengoStyle;
import ca.nengo.ui.lib.world.PaintContext;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.lib.world.piccolo.WorldObjectImpl;
import org.piccolo2d.nodes.PText;

import java.util.List;

/**
 * UI Object which builds itself from a ToolTipBuilder
 * 
 * @author Shu Wu
 */
public class Tooltip extends WorldObjectImpl {

	private final TooltipBuilder tooltipBuilder;
	private final double tooltipWidth;
	public static final double DEFAULT_WIDTH = 250;
    private PText tag;

    public Tooltip(TooltipBuilder tooltipBuilder) {
		this(tooltipBuilder, DEFAULT_WIDTH);
	}

	public Tooltip(TooltipBuilder tooltipBuilder, double width) {
		super();

		this.tooltipBuilder = tooltipBuilder;
		this.tooltipWidth = width;
        set(tooltipBuilder);
	}

	public void set(TooltipBuilder tooltipBuilder) {
        int layoutY = 0;


        if (tag == null) {
            tag = new PText(tooltipBuilder.getName());
            tag.setConstrainWidthToTextWidth(false);
            tag.setTextPaint(NengoStyle.COLOR_FOREGROUND);
            tag.setFont(NengoStyle.FONT_LARGE);
            tag.setWidth(tooltipWidth);
            getPNode().addChild(tag);
        }
        else {
            tag.setText(tooltipBuilder.getName());
        }

        layoutY += tag.getHeight() + 10;

        //Collection<WorldObject> existingChildren = getChildren();
        for (WorldObject o : getChildren())
            removeChild(o);

		List<ITooltipPart> parts = tooltipBuilder.getParts();
        int numParts = parts.size();
        for (int i = 0; i < numParts; i++) {
            ITooltipPart part = parts.get(i);
			WorldObject wo = part.toWorldObject(tooltipWidth);

			wo.setOffset(wo.getOffset().getX(), layoutY);

			addChild(wo);

			layoutY += wo.getHeight();
		}

		setBounds(parentToLocal(getFullBoundsClone()));
	}


    @Override
    public void paint(PaintContext paintContext) {
        super.paint(paintContext);
    }

}
