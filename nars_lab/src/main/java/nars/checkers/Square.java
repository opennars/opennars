package nars.checkers;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author Arjen Hoogesteger
 * @author Elio Tolhoek
 * @version 0.2
 */
public class Square extends JComponent
{
	private final Color color;
	private Piece piece = null;
	private boolean selected = false;
	private boolean targeted = false;

    // the matching coordinates on the board
    private final int x;
	private final int y;

	/**
	 * Creates a new Square instance.
	 * @param color the square's color
	 * @param x the matching x coordinate of the board
	 * @param y the matching y coordinate of the board
	 */
	public Square(Color color, int x, int y)
	{
		this.color = color;
        this.x = x;
        this.y = y;
	}

	/**
	 * Sets a square to be targeted.
	 */
	public void target()
	{
		targeted = true;
		repaint();
	}

	/**
	 * Sets a square to not be targeted (anymore).
	 */
	public void detarget()
	{
		targeted = false;
		repaint();
	}

	/**
	 * Sets a square to be selected.
	 */
	public void select()
	{
		selected = true;
		repaint();
	}

	/**
	 * Sets a square to not be selected (anymore).
	 */
	public void deselect()
	{
		selected = false;
		repaint();
	}

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);		
		
		if(selected)
			g.setColor(new Color(115, 255, 31));	// color in case selected
		else if(targeted)
			g.setColor(new Color(86, 114, 255));	// color in case targeted
		else
			g.setColor(color);						// regular color
		
		g.fillRect(0, 0, getPreferredSize().width, getPreferredSize().height);
		
        if(piece != null)
            piece.draw(g);
    }

	/**
	 * Returns the square's color.
	 * @return the color
	 */
	public Color getColor()
	{
		return color;
	}
	
	/**
	 * Sets the piece for this square.
	 * @param piece the piece
	 */
	public void setPiece(Piece piece)
	{
		this.piece = piece;
	}

	/**
	 * Returns the piece for this square.
	 * @return the piece
	 */
	public Piece getPiece()
	{
		return piece;
	}

    /**
	 * Returns the matching x coordinate of the board.
     * @return the x coordinate
     */
    public int getCoordinateX()
    {
        return x;
    }

    /**
	 * Returns the matching y coordinate of the board.
     * @return the y coordinate
     */
    public int getCoordinateY()
    {
        return y;
    }

	@Override
	public String toString()
	{
		String occupant = piece != null ? piece.toString() : "EMPTY";
		return "[" + x + ", " + y + "] : " + occupant;
	}
}