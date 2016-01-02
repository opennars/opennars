package automenta.vivisect.surfaceplotter.beans;

import javax.swing.*;
import java.awt.*;


/** A panel that rely on its GridBagLayout to provide "smart" increment for scrolling, implementing the {@link Scrollable} interface 
 * @author eric
 *
 */
public class JScrollablePanel extends JPanel implements Scrollable {

	private int blockIncrement = 5; 

	/** Block increment is used the scrollpane block increment is blockIncrement* unitIncrement
	 * 
	 * @return
	 */
	public int getBlockIncrement() {
		return blockIncrement;
	}

	public void setBlockIncrement(int blockIncrement) {
		this.blockIncrement = blockIncrement;
	}

	
	/**
	 * 
	 */
	public JScrollablePanel() {
	}

	/**
	 * @param isDoubleBuffered
	 */
	public JScrollablePanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
	}

	/**
	 * @param layout
	 * @param isDoubleBuffered
	 */
	public JScrollablePanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
	}

	/**
	 * @param layout
	 */
	public JScrollablePanel(LayoutManager layout) {
		super(layout);
	}

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		return blockIncrement *getScrollableUnitIncrement(visibleRect, orientation, direction);
	}
	
	
	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		
		
		// TODO improve using other layouts (like gridlayout, or flowlayout, boxlayout etc.)
		if (getLayout() instanceof GridBagLayout)
		{
			// access the grid bag layout and use the rows and cols size to handle increments
			GridBagLayout layout = (GridBagLayout) getLayout() ;
			Point p = layout.location(visibleRect.x, visibleRect.y);
			int[][] dims = layout.getLayoutDimensions();
			Point origin = layout.getLayoutOrigin();
			
			if (orientation == SwingConstants.VERTICAL) { // looking for the row height
				if (direction<0) // use the previous row, as we are going backward
					p.y-=1; 
				else p.y+=1; // use the next one 
				if (p.y< dims[1].length && p.y>=0)
					{// compute the next position
					int pos = origin.y;
					for(int i=0;i<p.y;i++) pos+=dims[1][i];
					// pos is know the coordinate of the next row , return the delta to it
					return Math.abs( pos-visibleRect.y );
					
					}
				else return getSize().height/dims[1].length ; // use an average number
			}
			else {
				if (direction<0) // use the previous row, as we are going backward
					p.x-=1; 
				else p.x+=1; // p is the next one.
				if (p.x< dims[0].length && p.x>=0) {
					int pos = origin.x;
					for(int i=0;i<p.x;i++) pos += dims[0][i];
					return Math.abs( pos-visibleRect.x );
					 
				}
				else return getSize().width/dims[0].length ; // use an average number
			}
		}
		return 1;
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		if (getParent() instanceof JViewport) {
		    return (getParent().getHeight() > getPreferredSize().height);
		}
		return false;
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		if (getParent() instanceof JViewport) {
		    return (getParent().getWidth() > getPreferredSize().width);
		}
		return false;
	}

	
	
	
	
	
	
	
}
