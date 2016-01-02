package nars.checkers;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 *
 * @author Arjen Hoogesteger
 * @version 0.4
 */
public class Game {
	private static final char TURN_DARK = 0x000F;
	private static final char TURN_LIGHT = 0x00F0;
	public final int HEIGHT;
	public final int WIDTH;

	private final ArrayList<int[]> movementHistory = new ArrayList<>();
	private final Piece[][] pieces;
	private char turn = TURN_DARK;	// dark is basically an alias for player 1
	private int remainingJumpX = -1, remainingJumpY = -1;

	public Game() 	{
		this(8,8);
	}

	public Game(int width, int height) 	{
		WIDTH = width;
		HEIGHT = height;
            pieces = (Piece[][]) Array.newInstance(Piece.class, WIDTH, HEIGHT);
            
		// create initial context
		for(int i = 0; i < WIDTH; i++)
		{
			for(int j = 0; j < HEIGHT; j++)
			{
				// create and set pieces
				if(i % 2 == j % 2)
				{
					if(j < (HEIGHT / 2) - 1)
						pieces[i][j] = Piece.createLightPiece();
					else if(j > HEIGHT / 2)
						pieces[i][j] = Piece.createDarkPiece();
					else
						pieces[i][j] = null;
				}
			}
		}
	}

	/**
	 * Returns whether or not it is the light player's turn.
	 * @return true if light player's turn, otherwise false
	 */
	public boolean isTurnLight()
	{
		return turn == TURN_LIGHT;
	}

	/**
	 * Returns whether or not it is the dark player's turn.
	 * @return true if dark player's turn, otherwise false
	 */
	public boolean isTurnDark()
	{
		return turn == TURN_DARK;
	}


	/**
	 * 
	 * @return
	 */
	public Piece[][] getPieces()
	{
		return pieces;
	}

	/**
	 *
	 * @return
	 */
	public boolean hasRemainingJump()
	{
		return remainingJumpX > -1 && remainingJumpY > -1;
	}

	/**
	 * 
	 * @return
	 */
	public ArrayList<int[]> getMovementHistory()
	{
		return movementHistory;
	}

	/**
	 * 
	 * @param srcX
	 * @param srcY
	 * @param dstX
	 * @param dstY
	 * @return
	 */
	public boolean move(int srcX, int srcY, int dstX, int dstY)
	{
		if(isValidMove(srcX, srcY, dstX, dstY))
		{
			if(srcX - dstX == 2 || srcX - dstX == -2)
			{
				// jump
				jump(srcX, srcY, dstX, dstY);

				if(pieceCouldJumpToFrom(dstX, dstY).isEmpty())
				{
					advanceTurn();
					if(hasRemainingJump())
						unsetRemainingJump();	// no remaining jumps from here
				}
				else
					setRemainingJump(dstX, dstY);	// player's next move has to be a jump from here
			}
			else
			{
				// move
				pieces[dstX][dstY] = pieces[srcX][srcY];
				pieces[srcX][srcY] = null;
				advanceTurn();	// other player to move next
			}

			if(!(pieces[dstX][dstY] instanceof King)) // has yet to be crowned perhaps
			{
				if((dstY == HEIGHT - 1) && pieces[dstX][dstY].isLight())
					pieces[dstX][dstY] = King.createLightKing();
				else if(dstY == 0 && pieces[dstX][dstY].isDark())
					pieces[dstX][0] = King.createDarkKing();
			}

			movementHistory.add(new int[]{srcX, srcY, dstX, dstY});

			return true;
		}

		return false;
	}

	/**
	 *
	 * @param srcX
	 * @param srcY
	 * @return
	 */
	public ArrayList<int[]> pieceCouldJumpToFrom(int srcX, int srcY)
	{
		ArrayList<int[]> destinations = new ArrayList<>();

		if(pieces[srcX][srcY].isLight() || pieces[srcX][srcY] instanceof King)
		{
			if(srcY <= HEIGHT - 3)		// otherwise simply impossible to jump ..
			{
				if(srcX + 2 < WIDTH)	// jump right
				{
					// check if the destination is empty
					if(pieces[srcX + 2][srcY + 2] == null)
					{
						// check if there is an enemy in front of us
						if(pieces[srcX + 1][srcY + 1] != null && !pieces[srcX + 1][srcY + 1].getColor().equals(pieces[srcX][srcY].getColor()))
							destinations.add(new int[]{srcX + 2, srcY + 2});
					}
				}

				if(srcX - 2 >= 0)		// jump left
				{
					// check if the destination is empty
					if(pieces[srcX - 2][srcY + 2] == null)
					{
						// check if there is an enemy in front of us
						if(pieces[srcX - 1][srcY + 1] != null && !pieces[srcX - 1][srcY + 1].getColor().equals(pieces[srcX][srcY].getColor()))
							destinations.add(new int[]{srcX - 2, srcY + 2});
					}
				}
			}
		}

		if(pieces[srcX][srcY].isDark() || pieces[srcX][srcY] instanceof King)	// we're allowed to jump backwards
		{
			if(srcY >= 2)	// otherwise simply impossible to jump ..
			{
				if(srcX + 2 < WIDTH)	// jump right
				{
					// check if the destination is empty
					if(pieces[srcX + 2][srcY - 2] == null)
					{
						// check if there is an enemy in front of us
						if(pieces[srcX + 1][srcY - 1] != null && !pieces[srcX + 1][srcY - 1].getColor().equals(pieces[srcX][srcY].getColor()))
							destinations.add(new int[]{srcX + 2, srcY - 2});
					}
				}

				if(srcX - 2 >= 0)		// jump left
				{
					// check if the destination is empty
					if(pieces[srcX - 2][srcY - 2] == null)
					{
						// check if there is an enemy in front of us
						if(pieces[srcX - 1][srcY - 1] != null && !pieces[srcX - 1][srcY - 1].getColor().equals(pieces[srcX][srcY].getColor()))
							destinations.add(new int[]{srcX - 2, srcY - 2});
					}
				}
			}
		}

		return destinations;
	}

	/**
	 *
	 * @param srcX
	 * @param srcY
	 * @return
	 */
	public ArrayList<int[]> pieceCouldMoveToFrom(int srcX, int srcY)
	{
		ArrayList<int[]> destinations = new ArrayList<>();

		if(pieces[srcX][srcY] != null)
		{
			if(pieces[srcX][srcY].isLight() || pieces[srcX][srcY] instanceof King)
			{
				// upwards
				if(srcY + 1 < HEIGHT)
				{
					if(srcX + 1 < WIDTH)
					{
						if(pieces[srcX + 1][srcY + 1] == null)
							destinations.add(new int[]{srcX + 1, srcY + 1});	// right
					}

					if(srcX - 1 >= 0)
					{
						if(pieces[srcX - 1][srcY + 1] == null)
							destinations.add(new int[]{srcX - 1, srcY + 1});	// left
					}
				}
			}

			if(pieces[srcX][srcY].isDark() || pieces[srcX][srcY] instanceof King)
			{
				// downwards
				if(srcY - 1 >= 0)
				{
					if(srcX + 1 < WIDTH)
					{
						if(pieces[srcX + 1][srcY - 1] == null)
							destinations.add(new int[]{srcX + 1, srcY - 1});	// right
					}

					if(srcX - 1 >= 0)
					{
						if(pieces[srcX - 1][srcY - 1] == null)
							destinations.add(new int[]{srcX - 1, srcY - 1});	// left
					}
				}
			}
		}

		return destinations;
	}

//	/**
//	 *
//	 * @return
//	 */
//	public ArrayList<Game> getPossibleFollowingContexts()
//	{
//		ArrayList<Game> contexts = new ArrayList<>();
//
//		return contexts;
//	}

	/**
	 * 
	 * @param srcX
	 * @param srcY
	 * @param dstX
	 * @param dstY
	 */
	private void jump(int srcX, int srcY, int dstX, int dstY)
	{
		int targetX = srcX < dstX ? srcX + 1 : srcX - 1;
		int targetY = srcY < dstY ? srcY + 1 : srcY - 1;
		
		pieces[dstX][dstY] = pieces[srcX][srcY];		
		pieces[targetX][targetY] = null;
		pieces[srcX][srcY] = null;
	}

	/**
	 * 
	 * @return
	 */
	private boolean canJump()
	{
		for(int i = 0; i < WIDTH; i++)
		{
			for(int j = 0; j < HEIGHT; j++)
			{
				if(pieces[i][j] != null)
				{
					if(isTurnLight() && pieces[i][j].isLight() && !pieceCouldJumpToFrom(i, j).isEmpty() || isTurnDark() && pieces[i][j].isDark() && !pieceCouldJumpToFrom(i, j).isEmpty())
						return true;
				}
			}
		}

		return false;
	}

	/**
	 * 
	 * @param srcX
	 * @param srcY
	 * @param dstX
	 * @param dstY
	 * @return
	 */
	private boolean isValidMove(int srcX, int srcY, int dstX, int dstY)
	{
		if(pieces[srcX][srcY] != null)
		{
			if((pieces[srcX][srcY].isLight() && isTurnLight()) || (pieces[srcX][srcY].isDark() && isTurnDark()))
			{
				ArrayList<int[]> dsts = new ArrayList<>();

				if(hasRemainingJump())
				{
					if(srcX == remainingJumpX && srcY == remainingJumpY)
						dsts = pieceCouldJumpToFrom(srcX, srcY);
				}
				else
					dsts = canJump() ? pieceCouldJumpToFrom(srcX, srcY) : pieceCouldMoveToFrom(srcX, srcY);

				for(int[] dst : dsts)
				{
					if(dst[0] == dstX && dst[1] == dstY)
						return true;
				}
			}
		}

		return false;
	}

	/**
	 * When this function is called it becomes the other player's turn. This function
	 * is used in the <code>move</code> function.
	 */
	private void advanceTurn()
	{
		turn = (char) (turn ^ 0x00FF);


	}

	/**
	 *
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean setRemainingJump(int x, int y)
	{
		if(x > -1 && y > -1)
		{
			remainingJumpX = x;
			remainingJumpY = y;

			return true;
		}

		return false;
	}

	/**
	 *
	 */
	private void unsetRemainingJump()
	{
		remainingJumpX = -1;
		remainingJumpY = -1;
	}

	@Override
	public boolean equals(Object o)
	{
		if(o instanceof Game)
		{
			Piece[][] p = ((Game) o).getPieces();

			for(int i = 0; i < WIDTH; i++)
			{
				for(int j = 0; j < HEIGHT; j++)
				{
					if(p[i][j] == null ^ pieces[i][j] == null)
						return false;
					else if(p[i][j] != null && pieces[i][j] != null)
					{
						if(p[i][j] instanceof King ^ pieces[i][j] instanceof King)
							return false;
						if(!p[i][j].getColor().equals(pieces[i][j].getColor()))
							return false;
					}
				}
			}
		}
		else
			return false;

		return true;
	}
}
