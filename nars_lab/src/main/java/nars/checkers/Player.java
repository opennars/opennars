package nars.checkers;

import java.util.ArrayList;

/**
 *
 * @author Arjen Hoogesteger
 * @version 0.2
 */
@SuppressWarnings("AbstractClassWithoutAbstractMethods")
public abstract class Player
{
	private final String name;
	private Board board;
	private boolean hasTurn = false;
	private final ArrayList<PlayerListener> listeners = new ArrayList<>();

	/**
	 * Creates a new Player object.
	 * @param name the player's name
	 */
	@SuppressWarnings("ConstructorNotProtectedInAbstractClass")
	public Player(String name)
	{
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * 
	 * @param l
	 */
	public void addListener(PlayerListener l)
	{
		listeners.add(l);
	}

	/**
	 *
	 * @param board
	 */
	public void setBoard(Board board)
	{
		this.board = board;
	}

	/**
	 * 
	 * @return
	 */
	public Board getBoard()
	{
		return board;
	}

	/**
	 * Returns the name of the player.
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * 
	 */
	public void takeTurn()
	{
		hasTurn = true;
	}

	/**
	 *
	 */
	public void stopTurn()
	{
		hasTurn = false;

		for(PlayerListener l : listeners)
			l.finishedTurn(this);
	}

	/**
	 * 
	 * @return
	 */
	public boolean hasTurn()
	{
		return hasTurn;
	}
}
