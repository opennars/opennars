package automenta.vivisect.surfaceplotter.beans;

import javax.swing.*;
import java.awt.*;


/** A scrollpane suitable for gridbag layout.
 * <p>When a GridBagLayout has not enough room to layout its component, it moves
 * from preferredsize to minimum size. Standard scrollpane have a very small minimumsize, and it DOES not depend on the viewport view.
 * <p> Therefore, this scrollpane does not collapse to a very small minimum size.
 * <p> if one direction is set to fixed (either <pre>widthFixed</pre> <pre>heightFixed</pre> ) then the minimum size for this
 * direction = the preffered size.
 * 
 * @author eric
 *
 */
public class JGridBagScrollPane extends JScrollPane{

	private boolean widthFixed =false;
	private boolean heightFixed = false;

	
	
	
	public JGridBagScrollPane() {
	}




	public JGridBagScrollPane(Component view, int vsbPolicy, int hsbPolicy) {
		super(view, vsbPolicy, hsbPolicy);
	}




	public JGridBagScrollPane(Component view) {
		super(view);
	}




	public JGridBagScrollPane(int vsbPolicy, int hsbPolicy) {
		super(vsbPolicy, hsbPolicy);
	}




	@Override
	public Dimension getMinimumSize() {
			Dimension min = super.getMinimumSize();
			int w = min.width;
			int h = min.height;
			if (widthFixed){
				w = getPreferredSize().width; // the content size
				w+= getVerticalScrollBar().getWidth();
			}
			if (heightFixed){
				h = getPreferredSize().height; // the content size
				h+= getHorizontalScrollBar().getHeight();
			}
			
			return new Dimension(w, h);
	}


	@Override
	public void layout() {
		boolean vertical = getVerticalScrollBar().isVisible();
		super.layout();
		if (vertical != getVerticalScrollBar().isVisible() ){
			getParent().invalidate() ;
			getParent().validate();
		}
	}




	public boolean isWidthFixed() {
		return widthFixed;
	}



	/** 
	 * 
	 * @param widthFixed true to force the minimum width to preferred's width.
	 */
	public void setWidthFixed(boolean widthFixed) {
		firePropertyChange("widthFixed", this.widthFixed , this.widthFixed = widthFixed);
		invalidate();
		if (getParent()!=null) 
			getParent().validate();
	}




	public boolean isHeightFixed() {
		return heightFixed;
	}



	/**
	 * 
	 * @param heightFixed true to force the minimum height to preferred's height.
	 */
	public void setHeightFixed(boolean heightFixed) {
		firePropertyChange("heightFixed", this.heightFixed , this.heightFixed = heightFixed);
		if (getParent()!=null) 
			getParent().validate();
	}

	
	
	
}
