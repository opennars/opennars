package ca.nengo.plot;

import javax.swing.*;
import java.awt.*;

/** 
 * A swing Icon that paints itself a single, solid color.   
 * 
 * Immutable. 
 * */
public class SolidColorIcon implements Icon {
	private Color _color;
	private int _width, _height;
	
	public SolidColorIcon(Color color_, int width_, int height_) {
		_color = color_;
		_width = width_;
		_height = height_;
	}
	
	public int getIconWidth() {
		return _width;
	}
	
	public int getIconHeight() {
		return _height;
	}
	
	public void paintIcon(Component c, Graphics g, int x, int y) {
		Color oldColor = g.getColor();
		g.setColor(_color);
		g.fillRect(x, y, _width, _height);
		g.setColor(oldColor);
	}
}

