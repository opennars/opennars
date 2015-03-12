package ca.nengo.ui.lib.world.piccolo.primitive;

import ca.nengo.ui.lib.NengoStyle;
import ca.nengo.ui.lib.world.piccolo.WorldObjectImpl;

import java.net.URL;

public class Image extends WorldObjectImpl {
	private PXImage imageNode;

	public Image(String fileName) {
		super(new PXImage(fileName));
		init();
	}

	public Image(URL url) {
		super(new PXImage(url));
		init();
	}

	public void init() {
        imageNode = (PXImage) getPNode();
		setPaint(NengoStyle.COLOR_BACKGROUND2);
		setPickable(false);
	}

	public boolean isLoadedSuccessfully() {

		if (imageNode.getImage() != null) {
			return true;
		} else {
			return false;
		}

	}

}
