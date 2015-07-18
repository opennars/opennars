package nars.checkers;

import java.awt.*;

/**
 *
 * @author Arjen Hoogesteger
 * @version 0.1
 */
public class King extends Piece
{
	/**
	 * 
	 * @return
	 */
	public static King createLightKing()
	{
		return new King(Piece.LIGHT);
	}

	/**
	 * 
	 * @return
	 */
	public static King createDarkKing()
	{
		return new King(Piece.DARK);
	}

	/**
	 * 
	 * @param color
	 */
	protected King(Color color)
	{
		super(color);
	}

	@Override
	public void draw(Graphics g)
	{
		super.draw(g);

		if(isLight())
			g.setColor(Piece.DARK);
		else
			g.setColor(Piece.LIGHT);

		// both arrays must have same size
		int[] xcoords = {1, 8, 16, 23, 31, 28, 3};
		int[] ycoords = {0, 8, 0, 8, 0, 20, 20};

		for(int i = 0; i < xcoords.length; i++)
			xcoords[i] += 14; // add x-axis offset

		for(int i = 0; i < ycoords.length; i++)
			ycoords[i] += 21; // add y-axis offset
		
		g.drawPolygon(xcoords, ycoords, xcoords.length); // draw our precious crown
	}

	@Override
	public String toString() {
		return super.toString() + "_KING";
	}
}
