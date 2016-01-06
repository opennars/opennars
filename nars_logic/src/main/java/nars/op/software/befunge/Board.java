package nars.op.software.befunge;

/**
 * Created by didrik on 30.12.2014.
 */
public class Board {

	private final char[][] board;

	Board() {
		board = new char[25][80];
	}

	char get(int y, int x) {
		return board[y][x];
	}

	void put(int y, int x, char c) {
		board[y][x] = c;
	}
}
