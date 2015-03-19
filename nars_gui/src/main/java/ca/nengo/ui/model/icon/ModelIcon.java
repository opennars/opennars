/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "ModelIcon.java". Description: 
"An Icon which has a representation and an label"

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
import ca.nengo.ui.lib.object.model.ModelObject;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.lib.world.WorldObject.Listener;
import ca.nengo.ui.lib.world.piccolo.WorldObjectImpl;
import ca.nengo.ui.lib.world.piccolo.primitive.Text;

/**
 * An Icon which has a representation and an label. It is used to represent NEO
 * models.
 * 
 * @author Shu Wu
 */
public class ModelIcon extends WorldObjectImpl implements Listener {

	/**
	 * The inner icon node which contains the actual icon representation
	 */
	private final WorldObject iconReal;

	/**
	 * Label of the icon
	 */
	private final Text label;

	/**
	 * Parent of this icon
	 */
	private final ModelObject parent;
    private final boolean cacheLabel = false;//not working entirely yet

    /**
	 * Whether to show the type of model in the label
	 */
	private boolean showTypeInLabel = false;
    private boolean showingLabel = true;

    public ModelIcon(ModelObject parent, WorldObject icon) {
        this(parent,icon,true);
    }
    /**
	 * @param parent
	 *            The Model the icon is representing
	 * @param icon
	 *            the UI representation
	 * @param scale
	 *            Scale of the Icon
	 */
	public ModelIcon(ModelObject parent, WorldObject icon, boolean cacheBitmap) {
		super();
		this.parent = parent;
		this.iconReal = icon;


		addChild(icon);

		label = new Text();
		label.setConstrainWidthToTextWidth(true);
        label.setTextPaint(NengoStyle.COLOR_ICON_LABEL);
        updateLabel();

        if (cacheLabel) {
            addChildCache(label);
        }
        else {
            addChild(label);
        }

		parent.addPropertyChangeListener(Property.MODEL_CHANGED, this);

		/*
		 * The bounds of this object matches those of the real icon
		 */
		iconReal.addPropertyChangeListener(Property.FULL_BOUNDS, this);
		updateBounds();
	}

	/**
	 * Updates the bounds of this node based on the inner icon
	 */
	private void updateBounds() {
		setBounds(iconReal.localToParent(iconReal.getBounds()));
	}

	public WorldObject getBody() {
		return iconReal;
	}

	protected ModelObject getModelParent() {
		return parent;
	}

	@Override
	public void layoutChildren() {
		super.layoutChildren();

		/*
		 * Layout the icon and label
		 */
		//double iconWidth = getWidth() * getScale();
		double labelWidth = label.getWidth();
        double labelHeight = label.getHeight();

        label.setOffset( -labelWidth/2, -labelHeight/2);

	}

	/**
	 * Called when the NEO model has been updated
	 */
	protected void modelUpdated() {
		updateLabel();
	}

	/**
	 * Configures the label
	 * 
	 * @param showType
	 *            Whether to show the model type in the label
	 */
	public void configureLabel(boolean showType) {
		showTypeInLabel = showType;
		updateLabel();
	}

	@Override
	public void doubleClicked() {
		parent.doubleClicked();
	}

	@Override
	public void altClicked() {
		parent.altClicked();
	}

	/**
	 * @return the name of the label
	 */
	@Override
	public String name() {
		return label.getText();
	}

	/**
	 * @param isVisible
	 *            Whether the label is visible
	 */
	public void setLabelVisible(boolean isVisible) {
		if (!showingLabel) {
            showingLabel = true;
            addChild(label);
		} else if (showingLabel) {
            showingLabel = false;
			if (label.getParent() != null)
				label.removeFromParent();

		}
	}

	/**
	 * Updates the label text
	 */
	public void updateLabel() {
		if (showTypeInLabel) {
			if (parent.name().isEmpty())
				label.setText("unnamed " + parent.getTypeName());
			else
				label.setText(parent.name() + " (" + parent.getTypeName() + ')');
		} else {
			if (parent.name().isEmpty())
				label.setText("unnamed");
			else
				label.setText(parent.name());
		}
	}

	public void propertyChanged(Property event) {

		if (event == Property.FULL_BOUNDS) {
			updateBounds();
		} else if (event == Property.MODEL_CHANGED) {
			modelUpdated();
		}
	}

    public Text getLabel() {
        return label;
    }
}