/**
 * Copyright 1996-2004 Edwin Martin <edwin@bitstorm.nl>
 * @author Edwin Martin
 */

package nars.ca;

import java.awt.Dimension;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Contains the cellgrid, the current shape and the Game Of Life algorithm that changes it.
 *
 * @author Edwin Martin
 */
public class GameOfLifeGrid implements CellGrid {
	private int cellRows;
	private int cellCols;
	private int generations;
	private static Shape[] shapes;
	/**
	 * Contains the current, living shape.
	 * It's implemented as a hashtable. Tests showed this is 70% faster than Vector.
	 */
	private Hashtable currentShape;
	private Hashtable nextShape;
	/**
	 * Every cell on the grid is a Cell object. This object can become quite large.
	 */
	private Cell[][] grid;

	/**
	 * Contructs a GameOfLifeGrid.
	 * 
	 * @param cellCols number of columns
	 * @param cellRows number of rows
	 */
	public GameOfLifeGrid(int cellCols, int cellRows) {
		this.cellCols = cellCols;
		this.cellRows = cellRows;
		currentShape = new Hashtable();
		nextShape = new Hashtable();

		grid = new Cell[cellCols][cellRows];
		for ( int c=0; c<cellCols; c++)
			for ( int r=0; r<cellRows; r++ )
				grid[c][r] = new Cell( c, r );
	}

	/**
	 * Clears grid.
	 */
	public synchronized void clear() {
		generations = 0;
		currentShape.clear();
		nextShape.clear();
	}

	/**
	 * Create next generation of shape.
	 */
	public synchronized void next() {
		Cell cell;
		int col, row;
		int neighbours;
		Enumeration en;

		generations++;
		nextShape.clear();

		// Reset cells
		en= currentShape.keys();
		while ( en.hasMoreElements() ) {
			cell = (Cell) en.nextElement();
			cell.neighbour = 0;
		}
		// Add neighbours
		// You can't walk through an hashtable and also add elements. Took me a couple of ours to figure out. Argh!
		// That's why we have a hashNew hashtable.
		en= currentShape.keys();
		while ( en.hasMoreElements() ) {
			cell = (Cell) en.nextElement();
			col = cell.col;
			row = cell.row;
			addNeighbour( col-1, row-1 );
			addNeighbour( col, row-1 );
			addNeighbour( col+1, row-1 );
			addNeighbour( col-1, row );
			addNeighbour( col+1, row );
			addNeighbour( col-1, row+1 );
			addNeighbour( col, row+1 );
			addNeighbour( col+1, row+1 );
		}
		
		// Bury the dead
		// We are walking through an enfrom we are also removing elements. Can be tricky.
		en= currentShape.keys();
		while ( en.hasMoreElements() ) {
			cell = (Cell) en.nextElement();
			// Here is the Game Of Life rule (1):
			if ( cell.neighbour != 3 && cell.neighbour != 2 ) {
				currentShape.remove( cell );
			}
		}
		// Bring out the new borns
		en= nextShape.keys();
		while ( en.hasMoreElements() ) {
			cell = (Cell) en.nextElement();
			// Here is the Game Of Life rule (2):
			if ( cell.neighbour == 3 ) {
				setCell( cell.col, cell.row, true );
			}
		}
	}
	
	/**
	 * Adds a new neighbour to a cell.
	 * 
	 * @param col Cell-column
	 * @param row Cell-row
	 */
	public synchronized void addNeighbour(int col, int row) {
		try {
			Cell cell = (Cell)nextShape.get( grid[col][row] );
			if ( cell == null ) {
				// Cell is not in hashtable, then add it
				Cell c = grid[col][row];
				c.neighbour = 1;
				nextShape.put(c, c);
			} else {
				// Else, increments neighbour count
				cell.neighbour++;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			// ignore
		}
	}
	
	/**
	 * Get enumeration of Cell's
	 */
	public Enumeration getEnum() {
		return currentShape.keys();
	}

	/**
	 * Get value of cell.
	 * @param col x-coordinate of cell
	 * @param row y-coordinate of cell
	 * @return value of cell
	 */
	public synchronized boolean getCell( int col, int row ) {
		try {
			return currentShape.containsKey(grid[col][row]);
		} catch (ArrayIndexOutOfBoundsException e) {
			// ignore
		}
		return false;
	}

	/**
	 * Set value of cell.
	 * @param col x-coordinate of cell
	 * @param row y-coordinate of cell
	 * @param c value of cell
	 */
	public synchronized void setCell( int col, int row, boolean c ) {
		try {
			Cell cell = grid[col][row];
			if ( c ) {
				currentShape.put(cell, cell);
			} else {
				currentShape.remove(cell);
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			// ignore
		}
	}
	
	/**
	 * Get number of generations.
	 * @return number of generations
	 */
	public int getGenerations() {
		return generations;
	}
	
	/**
	 * Get dimension of grid.
	 * @return dimension of grid
	 */
	public Dimension getDimension() {
		return new Dimension( cellCols, cellRows );
	}

	/**
	 * Resize grid. Reuse existing cells.
	 * @see org.bitstorm.gameoflife.CellGrid#resize(int, int)
	 */
	public synchronized void resize(int cellColsNew, int cellRowsNew) {
		if ( cellCols==cellColsNew && cellRows==cellRowsNew )
			return; // Not really a resize

		// Create a new grid, reusing existing Cell's
		Cell[][] gridNew = new Cell[cellColsNew][cellRowsNew];
		for ( int c=0; c<cellColsNew; c++)
			for ( int r=0; r<cellRowsNew; r++ )
				if ( c < cellCols && r < cellRows )
					gridNew[c][r] = grid[c][r];
				else
					gridNew[c][r] = new Cell( c, r );

		// Copy existing shape to center of new shape
		int colOffset = (cellColsNew-cellCols)/2;
		int rowOffset = (cellRowsNew-cellRows)/2;
		Cell cell;
		Enumeration en;
		nextShape.clear();
		en= currentShape.keys();
		while ( en.hasMoreElements() ) {
			cell = (Cell) en.nextElement();
			int colNew = cell.col + colOffset;
			int rowNew = cell.row + rowOffset;
			try {
				nextShape.put( gridNew[colNew][rowNew], gridNew[colNew][rowNew] );
			} catch ( ArrayIndexOutOfBoundsException e ) {
				// ignore
			}
		}

		// Copy new grid and hashtable to working grid/hashtable
		grid = gridNew;
		currentShape.clear();
		en= nextShape.keys();
		while ( en.hasMoreElements() ) {
			cell = (Cell) en.nextElement();
			currentShape.put( cell, cell );
		}
		
		cellCols = cellColsNew;
		cellRows = cellRowsNew;
	}
}