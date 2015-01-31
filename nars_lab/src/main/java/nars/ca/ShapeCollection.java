/**
 * Copyright 1996-2004 Edwin Martin <edwin@bitstorm.nl>
 * @author Edwin Martin
 */

package nars.ca;

/**
 * Contains some nice Game Of Life shapes.
 * @author Edwin Martin
 */
public class ShapeCollection {
	private static final Shape CLEAR;
	private static final Shape GLIDER;
	private static final Shape SMALLEXPL;
	private static final Shape EXPLODER;
	private static final Shape CELL10;
	private static final Shape FISH;
	private static final Shape PUMP;
	private static final Shape SHOOTER;
	private static final Shape[] COLLECTION;

	static {
		CLEAR = new Shape("Clear", new int[][] {} );
		GLIDER = new Shape("Glider", new int[][] {{1,0}, {2,1}, {2,2}, {1,2}, {0,2}});
		SMALLEXPL = new Shape("Small Exploder", new int[][] {{0,1}, {0,2}, {1,0}, {1,1}, {1,3}, {2,1}, {2,2}});
		EXPLODER = new Shape("Exploder", new int[][] {{0,0}, {0,1}, {0,2}, {0,3}, {0,4}, {2,0}, {2,4}, {4,0}, {4,1}, {4,2}, {4,3}, {4,4}});
		CELL10 = new Shape("10 Cell Row", new int[][] {{0,0}, {1,0}, {2,0}, {3,0}, {4,0}, {5,0}, {6,0}, {7,0}, {8,0}, {9,0}});
		FISH = new Shape("Lightweight spaceship", new int[][] {{0,1}, {0,3}, {1,0}, {2,0}, {3,0}, {3,3}, {4,0}, {4,1}, {4,2}});
		PUMP = new Shape("Tumbler", new int[][] {{0,3}, {0,4}, {0,5}, {1,0}, {1,1}, {1,5}, {2,0}, {2,1}, {2,2}, {2,3}, {2,4}, {4,0}, {4,1}, {4,2}, {4,3}, {4,4}, {5,0}, {5,1}, {5,5}, {6,3}, {6,4}, {6,5}});
		SHOOTER = new Shape("Gosper Glider Gun", new int[][] {{0,2}, {0,3}, {1,2}, {1,3}, {8,3}, {8,4}, {9,2}, {9,4}, {10,2}, {10,3}, {16,4}, {16,5}, {16,6}, {17,4}, {18,5}, {22,1}, {22,2}, {23,0}, {23,2}, {24,0}, {24,1}, {24,12}, {24,13}, {25,12}, {25,14}, {26,12}, {34,0}, {34,1}, {35,0}, {35,1}, {35,7}, {35,8}, {35,9}, {36,7}, {37,8}});
		COLLECTION = new Shape[] {CLEAR, GLIDER, SMALLEXPL, EXPLODER, CELL10, FISH, PUMP, SHOOTER};
	}

	/**
	 * Get array of shapes.
	 * 
	 * It's not tamper-proof, but that's okay.
	 * @return collection of shapes
	 */
	public static Shape[] getShapes() {
		return COLLECTION;
	}
	
	/**
	 * Get shape by its name.
	 * @param name name of shape
	 * @return shape object
	 * @throws ShapeException if no shape with this name exist
	 */
	public static Shape getShapeByName( String name ) throws ShapeException {
		Shape[] shapes = getShapes();
		for ( int i = 0; i < shapes.length; i++ ) {
			if ( shapes[i].getName().equals( name )  )
				return shapes[i];
		}
		throw ( new ShapeException("Unknown shape: "+name) );
	}
}