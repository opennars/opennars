package nars.checkers;

/**
 *
 * @author Arjen Hoogesteger
 * @version 0.1
 */
public class HumanPlayer extends Player
{
	/**
	 * 
	 * @param name
	 */
	public HumanPlayer(String name)
	{
		super(name);
	}

	@Override
	public void takeTurn()
	{
		super.takeTurn();
		System.out.println(getName() + "'s turn!");
		getBoard().enableMouseListener();	// we're human and need the board to listen to us
	}
}
