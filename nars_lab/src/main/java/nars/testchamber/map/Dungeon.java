package nars.testchamber.map;

import nars.testchamber.Cell;
import nars.testchamber.Cell.Material;
import nars.testchamber.Hauto;

public class Dungeon {

	// max size of the map
	int xmax; // columns
	int ymax; // rows

	// size of the map
	int _xsize;
	int _ysize;

	// number of "objects" to generate on the map
	int _objects;

	double ChanceRoom = 75;
	private final Cell[][] _dungeonMap;
	public int Corridors;

	public Dungeon(Hauto a) {
		_dungeonMap = a.readCells;
		_xsize = xmax = a.w;
		_ysize = ymax = a.h;
	}

	// setting a tile's type
	void SetCell(int x, int y, Material m) {
		float h;
		switch (m) {
			case Door :
				h = 100;
				break;
			case StoneWall :
				h = 150;
				break;
			case DirtFloor :
				h = 1;
				break;
			default :
				h = 1;
		}

		_dungeonMap[x][y].height = h;
		_dungeonMap[x][y].material = m;
	}

	/*
	 * public boolean IsWall(int x, int y, int xlen, int ylen, int xt, int yt,
	 * int d) { Func<int, int, int> a = GetFeatureLowerBound;
	 * 
	 * Func<int, int, int> b = IsFeatureWallBound; switch (d) { case Hauto.UP:
	 * return xt == a(x, xlen) || xt == b(x, xlen) || yt == y || yt == y - ylen
	 * + 1; case Hauto.RIGHT: return xt == x || xt == x + xlen - 1 || yt == a(y,
	 * ylen) || yt == b(y, ylen); case Hauto.DOWN: return xt == a(x, xlen) || xt
	 * == b(x, xlen) || yt == y || yt == y + ylen - 1; case Hauto.LEFT: return
	 * xt == x || xt == x - xlen + 1 || yt == a(y, ylen) || yt == b(y, ylen); }
	 * 
	 * }
	 */

	public static int GetFeatureLowerBound(int c, int len) {
		return c - len / 2;
	}

	public static int IsFeatureWallBound(int c, int len) {
		return c + (len - 1) / 2;
	}

	public static int GetFeatureUpperBound(int c, int len) {
		return c + (len + 1) / 2;
	}

	/*
	 * public static List<PointI> GetRoomPoints(int x, int y, int xlen, int
	 * ylen, int d) { // north and south share the same x strategy // east and
	 * west share the same y strategy Func<int, int, int> a =
	 * GetFeatureLowerBound; Func<int, int, int> b = GetFeatureUpperBound;
	 * 
	 * switch (d) { case Hauto.UP: for (var xt = a(x, xlen); xt < b(x, xlen);
	 * xt++) for (var yt = y; yt > y - ylen; yt--) yield return new PointI { X =
	 * xt, Y = yt }; break; case Hauto.RIGHT: for (var xt = x; xt < x + xlen;
	 * xt++) for (var yt = a(y, ylen); yt < b(y, ylen); yt++) yield return new
	 * PointI { X = xt, Y = yt }; break; case Hauto.DOWN: for (var xt = a(x,
	 * xlen); xt < b(x, xlen); xt++) for (var yt = y; yt < y + ylen; yt++) yield
	 * return new PointI { X = xt, Y = yt }; break; case Hauto.LEFT: for (var xt
	 * = x; xt > x - xlen; xt--) for (var yt = a(y, ylen); yt < b(y, ylen);
	 * yt++) yield return new PointI { X = xt, Y = yt }; break; default: yield
	 * break; } }
	 */

	public Material GetCellType(int x, int y) {
		return _dungeonMap[x][y].material;
	}

	public int GetRand(int min, int max) {
		return (int) (Math.random() * (max - min) + min);
	}

	public boolean MakeCorridor(int x, int y, int length, int direction) {
		// define the dimensions of the corridor (er.. only the width and
		// height..)
		int len = GetRand(2, length);
		Material Floor = Material.Corridor;

		int xtemp;
		int ytemp = 0;

		switch (direction) {
			case Hauto.UP :
				// north
				// check if there's enough space for the corridor
				// start with checking it's not out of the boundaries
				if (x < 0 || x > _xsize)
					return false;
				xtemp = x;

				// same thing here, to make sure it's not out of the boundaries
				for (ytemp = y; ytemp > (y - len); ytemp--) {
					if (ytemp < 0 || ytemp > _ysize)
						return false; // oh boho, it was!
					if (GetCellType(xtemp, ytemp) != Material.Empty)
						return false;
				}

				// if we're still here, let's start building
				Corridors++;
				for (ytemp = y; ytemp > (y - len); ytemp--) {
					SetCell(xtemp, ytemp, Floor);
				}

				break;

			case Hauto.RIGHT :
				// east
				if (y < 0 || y > _ysize)
					return false;
				ytemp = y;

				for (xtemp = x; xtemp < (x + len); xtemp++) {
					if (xtemp < 0 || xtemp > _xsize)
						return false;
					if (GetCellType(xtemp, ytemp) != Material.Empty)
						return false;
				}

				Corridors++;
				for (xtemp = x; xtemp < (x + len); xtemp++) {
					SetCell(xtemp, ytemp, Floor);
				}

				break;

			case Hauto.DOWN :
				// south
				if (x < 0 || x > _xsize)
					return false;
				xtemp = x;

				for (ytemp = y; ytemp < (y + len); ytemp++) {
					if (ytemp < 0 || ytemp > _ysize)
						return false;
					if (GetCellType(xtemp, ytemp) != Material.Empty)
						return false;
				}

				Corridors++;
				for (ytemp = y; ytemp < (y + len); ytemp++) {
					SetCell(xtemp, ytemp, Floor);
				}

				break;
			case Hauto.LEFT :
				// west
				if (ytemp < 0 || ytemp > _ysize)
					return false;
				ytemp = y;

				for (xtemp = x; xtemp > (x - len); xtemp--) {
					if (xtemp < 0 || xtemp > _xsize)
						return false;
					if (GetCellType(xtemp, ytemp) != Material.Empty)
						return false;
				}

				Corridors++;
				for (xtemp = x; xtemp > (x - len); xtemp--) {
					SetCell(xtemp, ytemp, Floor);
				}

				break;
		}

		// woot, we're still here! let's tell the other guys we're done!!
		return true;
	}

	/*
	 * public IEnumerable<Tuple<PointI, int>> GetSurroundingPoints(PointI v) {
	 * var points = new[] { Tuple.Create(new PointI { X = v.X, Y = v.Y + 1 },
	 * Hauto.UP), Tuple.Create(new PointI { X = v.X - 1, Y = v.Y },
	 * Hauto.RIGHT), Tuple.Create(new PointI { X = v.X , Y = v.Y-1 },
	 * Hauto.DOWN), Tuple.Create(new PointI { X = v.X +1, Y = v.Y },
	 * Hauto.LEFT),
	 * 
	 * }; return points.Where(p => InBounds(p.Item1)); }
	 * 
	 * public IEnumerable<Tuple<PointI, int, Tile>> GetSurroundings(PointI v) {
	 * return this.GetSurroundingPoints(v) .Select(r => Tuple.Create(r.Item1,
	 * r.Item2, this.GetCellType(r.Item1.X, r.Item1.Y))); }
	 */

	/*
	 * public boolean InBounds(int x, int y) { return x > 0 && x < this.xmax &&
	 * y > 0 && y < this.ymax; }
	 */

}