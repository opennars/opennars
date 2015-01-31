/**
 * Copyright 1996-2004 Edwin Martin <edwin@bitstorm.nl>
 * @author Edwin Martin
 */

package nars.ca;

import java.awt.Dimension;
import java.util.Enumeration;

/**
 * Shape contains data of one (predefined) shape.
 *
 * @author Edwin Martin
 */
public class Shape {
	private final String name;
	private final int[][] shape;
	
	/**
	 * Constructa a Shape.
	 * @param name name of shape
	 * @param shape shape data
	 */
	public Shape( String name, int[][] shape ) {
		this.name = name;
		this.shape = shape;
	}
	
	/**
	 * Get dimension of shape.
	 * @return dimension of the shape in cells
	 */
	public Dimension getDimension() {
		int shapeWidth = 0;
		int shapeHeight = 0;
		for (int cell = 0; cell < shape.length; cell++) {
			if (shape[cell][0] > shapeWidth)
				shapeWidth = shape[cell][0];
			if (shape[cell][1] > shapeHeight)
				shapeHeight = shape[cell][1];
		}
		shapeWidth++;
		shapeHeight++;
		return new Dimension( shapeWidth, shapeHeight );
	}
	
	/**
	 * Get name of shape.
	 * @return name of shape
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Get shape data.
	 * Hide the shape implementation. Returns a anonymous Enumerator object.
	 * @return enumerated shape data
	 */
	public Enumeration getCells() {
		return new Enumeration() {
			private int index=0;
			public boolean hasMoreElements() {
				return index < shape.length;
			}
			public Object nextElement() {
				return shape[index++];
			}
		};
	}
	
	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return name+" ("+shape.length+" cell"+(shape.length==1?"":"s")+")";
	}
}