/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "IconImage.java". Description: 
"Just like PImage, except it semantically zooms (ie"

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
import ca.nengo.ui.lib.world.piccolo.WorldObjectImpl;
import ca.nengo.ui.lib.world.piccolo.primitive.PXImage;
import org.piccolo2d.util.PBounds;
import org.piccolo2d.util.PPaintContext;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.net.URL;

public class IconImage extends WorldObjectImpl {

	public IconImage(String arg0) {
		super(new IconImageNode(arg0));
		setPickable(false);
	}

	public IconImage(URL arg0) {
		super(new IconImageNode(arg0));

	}
}

/**
 * Just like PImage, except it semantically zooms (ie. at low scales, it does
 * not paint its bitmap)
 * 
 * @author Shu Wu
 */
class IconImageNode extends PXImage {

	private static final long serialVersionUID = 1L;
	private static final Ellipse2D.Float TEMP_ELLIPSE = new Ellipse2D.Float();

	public static final boolean ENABLE_SEMANTIC_ZOOM = false;

	private transient GeneralPath path;

	private PBounds originalBounds;

	private final double prevScale = 0;

	public IconImageNode() {
		super();
		init();
	}

	public IconImageNode(Image arg0) {
		super(arg0);
		init();
	}

	public IconImageNode(String arg0) {
		super(arg0);
		init();
	}

	public IconImageNode(URL arg0) {
		super(arg0);
		init();
	}

	private void init() {
		path = new GeneralPath();
		originalBounds = getBounds();
	}

	private void updatePath(double scale) {
		double origWidth = originalBounds.getWidth();
		double origHeight = originalBounds.getHeight();
		double width = origWidth * scale;
		double height = origWidth * scale;
		double offsetX = (origWidth - width) / 2f;
		double offsetY = (origHeight - height) / 2f;

		path.reset();
		TEMP_ELLIPSE.setFrame(offsetX, offsetY, width, height);
		path.append(TEMP_ELLIPSE, false);

	}

	@Override
	protected void paint(PPaintContext aPaintContext) {
		double s = aPaintContext.getScale();

		Graphics2D g2 = aPaintContext.getGraphics();

		if (ENABLE_SEMANTIC_ZOOM && s < NengoStyle.SEMANTIC_ZOOM_LEVEL) {
			if (s != prevScale) {
				double delta = 1 - ((NengoStyle.SEMANTIC_ZOOM_LEVEL - s) / NengoStyle.SEMANTIC_ZOOM_LEVEL);

				updatePath(1 / delta);
			}
			g2.setPaint(NengoStyle.COLOR_FOREGROUND);
			g2.fill(path);

			// g2.fill(getBoundsReference());

		} else {
			super.paint(aPaintContext);
		}

	}
}
