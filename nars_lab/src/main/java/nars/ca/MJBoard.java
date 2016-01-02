package nars.ca;// Mirek's Java Cellebration
// http://www.mirekw.com
//
// The board

import java.awt.*;
import java.awt.image.MemoryImageSource;
import java.util.StringTokenizer;

//-----------------------------------------------------------------------
//-----------------------------------------------------------------------
//class MJBoard extends Canvas implements Runnable
class MJBoard extends Panel implements Runnable {
	public static final int MAX_X = 800; // Universe limits
	public static final int MAX_Y = 600;
	public static final int MAX_CLO = 255; // max. state
	public static final int MAX_CELLSIZE = 32; // max. cell size, in pixels
	public Thread caThread = null;
	public final MJPalette mjPal; // color palette
	private boolean InitDone = false;
	private final MJCellUI mjUI; // the user interface object (parent)
	public int AnimDelay = 100; // default delay between cycles
	public int RefreshStep = 1; // refresh every 1 cycle
	public int CrrGame; // current rules family, LIFE, GENE, etc.
	public int GameType = MJRules.GAMTYP_2D; // 1D or 2D
	public String RuleName; // current rule name
	public String RuleDef; // current rule definition
	public int Cycle; // cycle number
	public int StatesCount = 2; // count of states, 0..n
	public int Population; // count of alive cells
	public final int[] Populations = new int[MAX_CLO + 1]; // count of cells in each
	// state
	private int lastX = 0, lastY = 0; // mouse handling
	public boolean IsRunning = false; // is an animation running?
	public boolean DrawGrid = true; // draw grid
	public int ColoringMethod = 1; // 1 - standard, 2 - alternate
	public int CrrState = 1; // active state (color)

	// local controls
	private final Panel pnlBotm; // bottom panel, for scrollbar and buttons
	private Scrollbar hSbar = null, vSbar = null; // scrollbars
	private final int sbarWidth = 16; // scrollbars width
	private final Button btnZoomIn = new Button("+");
	private final Button btnZoomOut = new Button("-");
	private final Button btnFit = new Button("F");

	// Universe, board
	public final Point UnivSize; // universe size, can be partially invisible
	public int CellSize = 5; // size of cells
	private final short[][] crrState; // the cells - current state
	private final short[][] tmpState; // the cells - temporary state
	private final short[][] bakState; // backup, for rewinding
	private final Point ViewOrg; // view origin - left-top point
	private final Point ViewSize; // actual adjusted view size, in pixels
	private final Point CellsInView; // counts of visible cells in X and Y
	private final Point LastPanelSize;// last panel size, for resizing handling

	// Graphics
	private int[] screen;
	private Image offImg; // the image

	//private Graphics offGrx; // the image's graphics
	private MemoryImageSource offSrs;
	private int OfsX = 0;
	private int OfsY = 0;

	// Rules
	public boolean WrapAtEdges = true; // wrap at edges
	public int i1DLastRow; // 1-D last generated row
	public final RuleGene RGene;
	public final RuleLife RLife;
	public final RuleVote RVote;
	public final RuleCyclic RCyclic;
	public final RuleWLife RWLife;
	public final Rule1DTotal R1DTo;
	public final Rule1DBin R1DBin;
	public final RuleNeumBin RNeumBin;
	public final RuleGenBin RGenBin;
	public final RuleRTab RRtab;
	public final RuleLgtL RLgtL;
	public final RuleMarg RMarg;
	public final RuleUser RUser;

	// misc
	public final MJDiversities Div; // diversities system (black hole, nova,

	// injection, etc.)

	// ----------------------------------------------------------------
	// Constructor
	MJBoard(MJCellUI mui) {
		int i;

		mjUI = mui; // the user interface object
		mjPal = new MJPalette();
		CrrGame = MJRules.GAME_LIFE; // Standard Conway-like game
		crrState = new short[MAX_X + 1][MAX_Y + 1];
		tmpState = new short[MAX_X + 1][MAX_Y + 1];
		bakState = new short[MAX_X + 1][MAX_Y + 1];
		ViewOrg = new Point(20, 20); // view left-top corner, in cells
		UnivSize = new Point(0, 0); // CA universe size (counts of cells in X and Y)
		ViewSize = new Point(0, 0); // last view size, in pixels
		CellsInView = new Point(0, 0); // counts of visible cells in X and Y
		LastPanelSize = new Point(0, 0); // last panel size, for resizing handling

		setLayout(new BorderLayout(0, 0));
		pnlBotm = new Panel();
		add("South", pnlBotm);

		pnlBotm.setLayout(new GridLayout(1, 4)); // horizontal layout for 4 items
		pnlBotm.add(btnZoomIn);
		pnlBotm.add(btnZoomOut);
		hSbar = new Scrollbar(Scrollbar.HORIZONTAL);
		pnlBotm.add(hSbar);
		pnlBotm.add(btnFit);
		vSbar = new Scrollbar(Scrollbar.VERTICAL);
		add("East", vSbar);

		InitBoard(160, 120, 5);
		InitDone = true;

		// rules
		i1DLastRow = 0; // 1-D last generated row
		RGene = new RuleGene();
		RLife = new RuleLife();
		RVote = new RuleVote();
		RCyclic = new RuleCyclic();
		R1DTo = new Rule1DTotal();
		R1DBin = new Rule1DBin();
		RNeumBin = new RuleNeumBin();
		RGenBin = new RuleGenBin();
		RRtab = new RuleRTab();
		RWLife = new RuleWLife();
		RLgtL = new RuleLgtL();
		RMarg = new RuleMarg();
		RUser = new RuleUser();

		// misc
		Div = new MJDiversities(); // diversities system (black hole, nova, injection, etc.)
	}

	// ----------------------------------------------------------------
	// start animation
	public void start() {
		mjUI.btnRunStop.setLabel("STOP");
		mjUI.itmRunStop.setLabel("Stop  (Enter)");
		if (caThread == null) {
			caThread = new Thread(this, "MJCell");
			caThread.setPriority(Thread.NORM_PRIORITY);
			caThread.start();
		}
	}

	// ----------------------------------------------------------------
	// stop animation
	public void stop() {
		mjUI.btnRunStop.setLabel("START");
		mjUI.itmRunStop.setLabel("Start  (Enter)");
		caThread = null;
	}

	// ----------------------------------------------------------------
	// The animation endless loop
	@Override
	public void run() {
		int iDelay; // in milliseconds
		boolean doRedraw;
		Thread thisThread = Thread.currentThread();
		while (thisThread == caThread) {
			// perform one cycle
			doRedraw = false;
			if (OneCycle() > 0) // any cells changed?
			{
				switch (RefreshStep) {
				case 1: // always
					doRedraw = true; // always show the universe
					break;

				case -1: // every full page
					if (GameType == MJRules.GAMTYP_1D) {
						if (i1DLastRow % UnivSize.y == 0) // at the end
							doRedraw = true;
					} else {
						if (Cycle % UnivSize.y == 0) // every UnivSize.y cycles
							doRedraw = true;
					}
					break;
				default: // every n cycles
					if (Cycle % RefreshStep == 0)
						doRedraw = true;
					break;
				}
			} else {
				stop();
			}

			mjUI.UpdateUI(); // update the user interface
			if (doRedraw) {
				RedrawBoard(false);
				//}
				//
				//if (RefreshStep == 1)
				//{
				// some waiting
				//iDelay = (AnimDelay > 0) ? AnimDelay : 5;
				iDelay = AnimDelay;
				try {
					Thread.sleep(iDelay);
				} catch (InterruptedException e) {
				}
			}
		}
		mjUI.UpdateUI(); // update the user interface
		if (RefreshStep != 1)
			RedrawBoard(false); // show the universe at the end
	}

	// ----------------------------------------------------------------
	// run one cycle or defined count of steps
	public void SingleStep() {
		int i, iCnt;
		if (RefreshStep > 0) {
			iCnt = RefreshStep;
		} else // pagewise
		{
			iCnt = UnivSize.y - i1DLastRow; // the rest of the page
			if (iCnt <= 0)
				iCnt = UnivSize.y; // the whole page
		}

		stop();
		for (i = 1; i <= iCnt; i++) {
			if (OneCycle() == 0) // any changes?
				break;
		}
		RedrawBoard(false); // show the universe
		mjUI.UpdateUI(); // update the user interface
	}

	// ----------------------------------------------------------------
	// Procedure performs one CA cycle. It does not draw anything.
	// Return value: count of modified cells.
	int OneCycle() {
		int i, j;
		int modCnt = 0;

		try {
			// Diversities enabled?
			if (Div.m_Enabled)
				Div.Perform(true, this); // true - before pass

			switch (CrrGame) {
			case MJRules.GAME_LIFE: // Standard Conway-like game
				modCnt = RLife.OnePass(UnivSize.x, UnivSize.y, WrapAtEdges,
						ColoringMethod, crrState, tmpState, this);
				break;

			case MJRules.GAME_VOTE: // Vote for life
				modCnt = RVote.OnePass(UnivSize.x, UnivSize.y, WrapAtEdges,
						ColoringMethod, crrState, tmpState, this);
				break;

			case MJRules.GAME_WLIF: // Weighted life
				modCnt = RWLife.OnePass(UnivSize.x, UnivSize.y, WrapAtEdges,
						ColoringMethod, crrState, tmpState, this);
				break;

			case MJRules.GAME_GENE: // Generations
				modCnt = RGene.OnePass(UnivSize.x, UnivSize.y, WrapAtEdges,
						ColoringMethod, crrState, tmpState, this);
				break;

			case MJRules.GAME_RTBL: // Rules table
				modCnt = RRtab.OnePass(UnivSize.x, UnivSize.y, WrapAtEdges,
						ColoringMethod, crrState, tmpState, this);
				break;

			case MJRules.GAME_CYCL: // Cyclic CA
				modCnt = RCyclic.OnePass(UnivSize.x, UnivSize.y, WrapAtEdges,
						ColoringMethod, crrState, tmpState);
				break;

			case MJRules.GAME_1DTO: // 1D totalistic
				modCnt = R1DTo.OnePass(UnivSize.x, UnivSize.y, WrapAtEdges,
						ColoringMethod, crrState, tmpState, this);
				break;

			case MJRules.GAME_1DBI: // 1D binary
				modCnt = R1DBin.OnePass(UnivSize.x, UnivSize.y, WrapAtEdges,
						ColoringMethod, crrState, tmpState, this);
				break;

			case MJRules.GAME_NMBI: // Neumann binary
				modCnt = RNeumBin.OnePass(UnivSize.x, UnivSize.y, WrapAtEdges,
						ColoringMethod, crrState, tmpState);
				break;

			case MJRules.GAME_GEBI: // General binary
				modCnt = RGenBin.OnePass(UnivSize.x, UnivSize.y, WrapAtEdges,
						ColoringMethod, crrState, tmpState, this);
				break;

			case MJRules.GAME_LGTL: // Larger than Life
				modCnt = RLgtL.OnePass(UnivSize.x, UnivSize.y, WrapAtEdges,
						ColoringMethod, crrState, tmpState, this);
				break;

			case MJRules.GAME_MARG: // Margolus
				modCnt = RMarg.OnePass(UnivSize.x, UnivSize.y, WrapAtEdges,
						ColoringMethod, crrState, tmpState, this);
				break;

			case MJRules.GAME_USER: // User DLL
				modCnt = RUser.OnePass(UnivSize.x, UnivSize.y, WrapAtEdges,
						ColoringMethod, crrState, tmpState);
				break;
			}

			// set cells back
			if (GameType == MJRules.GAMTYP_2D) // 2D, the whole board
			{
				for (i = 0; i < UnivSize.x; i++) {
					for (j = 0; j < UnivSize.y; j++) {
						SetCell(i, j, tmpState[i][j]);
					}
				}
			} else // 1D, only the current row
			{
				for (i = 0; i < UnivSize.x; i++) {
					SetCell(i, i1DLastRow, tmpState[i][i1DLastRow]);
				}
			}

			// Diversities enabled?
			if (Div.m_Enabled)
				Div.Perform(false, this); // false - after pass
		} catch (Exception exc) {
        }
		if (modCnt > 0)
			Cycle++; // next cycle performed

		return modCnt;
	} // OneCycle()

	// ----------------------------------------------------------------
	// Set one cell
	// This should be *the only* fuction that modifies the board (crrState)
	public final void SetCell(int x, int y, short bState) {
		if ((x >= 0) && (y >= 0) && (x < UnivSize.x) && (y < UnivSize.y)) {
			if (crrState[x][y] != bState) {
				Populations[crrState[x][y]]--;
				Populations[bState]++;
				if (0 == crrState[x][y])
					Population++;
				else if (0 == bState)
					Population--;
				crrState[x][y] = bState;
			}
		}
	}

	// ----------------------------------------------------------------
	// Set one cell
	public final void SetCell(CACell cell) {
		if ((cell.x >= 0) && (cell.y >= 0) && (cell.x < UnivSize.x)
				&& (cell.y < UnivSize.y)) {
			if (crrState[cell.x][cell.y] != cell.state) {
				Populations[crrState[cell.x][cell.y]]--;
				Populations[cell.state]++;
				if (0 == crrState[cell.x][cell.y])
					Population++;
				else if (0 == cell.state)
					Population--;
				crrState[cell.x][cell.y] = cell.state;
			}
		}
	}

	// ----------------------------------------------------------------
	// Get one cell's value
	public short GetCell(int x, int y) {
		if ((x >= 0) && (y >= 0) && (x < UnivSize.x) && (y < UnivSize.y)) {
			return crrState[x][y];
		}
		return 0;
	}

	// ----------------------------------------------------------------
	// Set size of the board
	public void SetBoardSize(int sizX, int sizY) {
		InitBoard(sizX, sizY, CellSize);
	}

	// ----------------------------------------------------------------
	// Set parameters of the board
	public void InitBoard(int sizX, int sizY, int cellSiz) {
		boolean fOldRun = caThread != null;
		boolean fNewSize = false;

		if (sizX > MAX_X)
			sizX = MAX_X;
		if (sizY > MAX_Y)
			sizY = MAX_Y;
		if (sizX < 10)
			sizX = 10;
		if (sizY < 10)
			sizY = 10;
		// stop for a while
		stop();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}

		if ((sizX != UnivSize.x) || (sizY != UnivSize.y)) {
			int i, j;
			UnivSize.x = sizX;
			UnivSize.y = sizY;

			// clear the universe outside of the active area
			for (i = UnivSize.x; i <= MAX_X; i++)
				for (j = 0; j <= MAX_Y; j++)
					crrState[i][j] = 0;

			for (i = 0; i <= UnivSize.x; i++)
				for (j = UnivSize.y; j <= MAX_Y; j++)
					crrState[i][j] = 0;

			fNewSize = true;
			UpdatePopulation();
			Cycle = 0; // start counting from 0
		}
		CellSize = cellSiz;

		if (RecalcLayout()) // recalc all board basic parameters
		{
			if (fNewSize) // center the board
			{
				CenterBoard();
			}

			// prepare the offscreen buffer
			screen = new int[ViewSize.x * ViewSize.y]; // all pixels
			offSrs = new MemoryImageSource(ViewSize.x, ViewSize.y, screen, 0,
					ViewSize.x);
			offSrs.setAnimated(true);
			offImg = createImage(offSrs);
			//offImg = createImage(ViewSize.x, ViewSize.y);
			//offGrx = offImg.getGraphics();

			// prepare the board
			UpdateScrollbars(); // update scrollbars values
			//setBackground(Color.lightGray);
			setBackground(Color.gray);
			hide();
			show(); // only this trick repaints the panel
			ValidateBoard(); // validate all parameters
			RedrawBoard(true); // show the universe
		}
		if (InitDone) {
			mjUI.UpdateGridUI(); // update the grid UI
			mjUI.UpdateUI(); // update the general UI
		}
		if (fOldRun)
			start();
	}

	// ----------------------------------------------------------------
	// Recalculate the basic board parameters
	public boolean RecalcLayout() {
		boolean retVal = true;
		int wdt, hgt, iTmp;
		wdt = getSize().width; // panel size, in pixels
		hgt = getSize().height;
		LastPanelSize.x = wdt;
		LastPanelSize.y = hgt;

		// first reshape scrollbars
		pnlBotm.reshape(0, hgt - sbarWidth, wdt, sbarWidth);
		btnZoomIn.reshape(0, 0, sbarWidth, sbarWidth);
		btnZoomOut.reshape(sbarWidth + 1, 0, sbarWidth, sbarWidth);
		iTmp = btnZoomIn.getSize().width + btnZoomOut.getSize().width;
		hSbar.reshape(iTmp + 1, 0, wdt - sbarWidth - iTmp - 1, sbarWidth);
		btnFit.reshape(wdt - sbarWidth, 0, sbarWidth, sbarWidth);
		vSbar.reshape(wdt - sbarWidth, 0, sbarWidth, hgt - sbarWidth);

		// now process the remaining space
		wdt -= sbarWidth;
		hgt -= sbarWidth;
		if ((wdt > CellSize) && (hgt > CellSize)) {
			if (wdt >= UnivSize.x * CellSize) // everything fits in OX
			{
				wdt = UnivSize.x * CellSize;
				ViewOrg.x = 0;
				OfsX = (LastPanelSize.x - sbarWidth - wdt) / 2;
			} else {
				OfsX = 0;
			}
			if (hgt >= UnivSize.y * CellSize) // everything fits in OY
			{
				hgt = UnivSize.y * CellSize;
				ViewOrg.y = 0;
				OfsY = (LastPanelSize.y - sbarWidth - hgt) / 2;
			} else {
				OfsY = 0;
			}
			ViewSize.x = wdt - (wdt % CellSize);
			ViewSize.y = hgt - (hgt % CellSize);
			CellsInView.x = wdt / CellSize; // count of visible cells
			CellsInView.y = hgt / CellSize;
			//System.out.println("From RecalcLayout()");
			//System.out.println("ViewSize: " + String.valueOf(ViewSize.x) +
			// "x" + String.valueOf(ViewSize.y));
			//System.out.println("CellsInView: " +
			// String.valueOf(CellsInView.x) + "x" +
			// String.valueOf(CellsInView.y));
		} else // too small board
		{
			ViewSize.x = 0;
			ViewSize.y = 0;
			retVal = false;
		}
		return retVal;
	}

	// ----------------------------------------------------------------
	// Make full backup of the current board state
	public void MakeBackup() {
		for (int i = 0; i < UnivSize.x; i++) {
            System.arraycopy(crrState[i], 0, bakState[i], 0, UnivSize.y);
		}
		mjUI.itmRewind.setEnabled(true);
	}

	// ----------------------------------------------------------------
	// Restore last backup
	public void RestoreBackup() {
		stop();
		for (int i = 0; i < UnivSize.x; i++) {
            System.arraycopy(bakState[i], 0, crrState[i], 0, UnivSize.y);
		}
		Cycle = 0; // start counting from 0
		UpdatePopulation();
		RedrawBoard(true); // show the empty universe
	}

	// ----------------------------------------------------------------
	// count cells
	private void UpdatePopulation() {
		int i, iCol, iRow;
		short bVal;

		// reset counters
		Population = 0;
		for (i = 0; i <= MAX_CLO; i++)
			Populations[i] = 0;
		// count population
		for (iCol = 0; iCol < UnivSize.x; iCol++) {
			for (iRow = 0; iRow < UnivSize.y; iRow++) {
				bVal = GetCell(iCol, iRow);
				Populations[bVal]++;
				if (bVal != 0) {
					Population++;
				}
			}
		}
	}

	// ----------------------------------------------------------------
	// update scrollbars values from the board settings
	private void UpdateScrollbars() {
		//              pos siz min max
		hSbar.setValues(ViewOrg.x, CellsInView.x, 0, UnivSize.x);
		vSbar.setValues(ViewOrg.y, CellsInView.y, 0, UnivSize.y);
	}

	// ----------------------------------------------------------------
	// ensure that all board parameters are valid
	public void ValidateBoard() {
		if (ViewOrg.x > UnivSize.x - CellsInView.x)
			ViewOrg.x = UnivSize.x - CellsInView.x;
		if (ViewOrg.y > UnivSize.y - CellsInView.y)
			ViewOrg.y = UnivSize.y - CellsInView.y;
		if (ViewOrg.x < 0)
			ViewOrg.x = 0;
		if (ViewOrg.y < 0)
			ViewOrg.y = 0;
	}

	// ----------------------------------------------------------------
	// center the board
	public void CenterBoard() {
		Point ctrPnt = new Point(0, 0);
		ctrPnt.x = UnivSize.x / 2;
		ctrPnt.y = UnivSize.y / 2;
		ViewOrg.x = ctrPnt.x - (CellsInView.x / 2);
		ViewOrg.y = GameType == MJRules.GAMTYP_2D ? ctrPnt.y - (CellsInView.y / 2) : i1DLastRow;
		ValidateBoard(); // validate all parameters
	}

	// ----------------------------------------------------------------
	// pan the board
	public void Pan(int dx, int dy) {
		ViewOrg.x += dx;
		ViewOrg.y += dy;
		ValidateBoard(); // validate all parameters
		RedrawBoard(true); // show the universe
		UpdateScrollbars(); // update scrollbars values
	}

	// ----------------------------------------------------------------
	// calculate the cells' bounging rectangle
	public Rectangle CalcPatternRect() {
		Rectangle rct = new Rectangle(MAX_X, MAX_Y, 0, 0);
		int iCol, iRow;

		for (iCol = 0; iCol < UnivSize.x; iCol++)
			for (iRow = 0; iRow < UnivSize.y; iRow++)
				if (GetCell(iCol, iRow) != 0) {
					if (rct.x > iCol)
						rct.x = iCol;
					if (rct.x + rct.width - 1 < iCol)
						rct.width = iCol - rct.x + 1;
					if (rct.y > iRow)
						rct.y = iRow;
					if (rct.y + rct.height - 1 < iRow)
						rct.height = iRow - rct.y + 1;
				}
		return rct;
	}

	// ----------------------------------------------------------------
	// Center the board around the (ix, iy) point
	public void CenterPoint(int ix, int iy, boolean fRedraw) {
		Point oldOrg = new Point(ViewOrg);

		CellsInView.x = (LastPanelSize.x - sbarWidth - 1) / CellSize; // vsb.
		// cells
		// count
		CellsInView.y = (LastPanelSize.y - sbarWidth - 1) / CellSize;

		// determine the proper left top corner
		ViewOrg.x = ix - (CellsInView.x / 2);
		ViewOrg.y = iy - (CellsInView.y / 2);

		ValidateBoard(); // validate all parameters

		// if to redraw and anything has changed
		if ((fRedraw) && (oldOrg != ViewOrg)) {
			InitBoard(UnivSize.x, UnivSize.y, CellSize);
		}
	}

	// ----------------------------------------------------------------
	// fit the pattern in the view:
	// center the pattern and select the best cell size
	public void Fit(boolean fRedraw) {
		Rectangle rct = new Rectangle();
		int iCtrX, iCtrY, iFac;
		double fac, facX, facY;

		rct = CalcPatternRect();

		if (rct.width >= 0) // any cell exists
		{
			facX = LastPanelSize.x / rct.width;
			facY = LastPanelSize.y / rct.height;

			fac = facX < facY ? facX : facY;

			// select the biggest size that covers the full pattern
			//noinspection IfStatementWithTooManyBranches
			if (fac >= 12)
				iFac = 11;
			else if (fac >= 10)
				iFac = 9;
			else if (fac >= 7)
				iFac = 7;
			else if (fac >= 5)
				iFac = 5;
			else if (fac >= 4)
				iFac = 4;
			else if (fac >= 3)
				iFac = 3;
			else if (fac >= 2)
				iFac = 2;
			else
				iFac = 1;

			CellSize = iFac;

			// put the pattern in the center
			iCtrX = rct.x + rct.width / 2;
			iCtrY = rct.y + rct.height / 2;
			CenterPoint(iCtrX, iCtrY, fRedraw); // center the given point
		} else // no cells
		{
			CellSize = 5; // default cell size
			CenterBoard();
			if (fRedraw) {
				InitBoard(UnivSize.x, UnivSize.y, CellSize);
			}
		}
        btnZoomIn.setEnabled(CellSize < MAX_CELLSIZE);
		btnZoomOut.setEnabled(CellSize > 1);
	}

	// ----------------------------------------------------------------
	// zoom in (true) / out (false)
	public void Zoom(boolean fIn) {
		Point ctrPnt = new Point(0, 0);
		int orgCellSize = CellSize;
		ctrPnt.x = ViewOrg.x + CellsInView.x / 2;
		ctrPnt.y = ViewOrg.y + CellsInView.y / 2;
		if (fIn) {
			//noinspection IfStatementWithTooManyBranches
			if (CellSize >= 20)
				CellSize += 4;
			else if (CellSize >= 11)
				CellSize += 3;
			else if (CellSize >= 5)
				CellSize += 2;
			else
				CellSize++;
			if (CellSize > MAX_CELLSIZE)
				CellSize = MAX_CELLSIZE;
		} else {
			//noinspection IfStatementWithTooManyBranches
			if (CellSize > 20)
				CellSize -= 4;
			else if (CellSize > 11)
				CellSize -= 3;
			else if (CellSize > 5)
				CellSize -= 2;
			else
				CellSize--;
			if (CellSize < 1)
				CellSize = 1;
		}
		btnZoomIn.setEnabled(CellSize < MAX_CELLSIZE);
		btnZoomOut.setEnabled(CellSize > 1);
		if (CellSize != orgCellSize) {
			RecalcLayout();
			ViewOrg.x = ctrPnt.x - (CellsInView.x / 2);
			ViewOrg.y = ctrPnt.y - (CellsInView.y / 2);
			InitBoard(UnivSize.x, UnivSize.y, CellSize);
		}
	}

	// ----------------------------------------------------------------
	// zoom in
	public void CellsBigger() {
		Zoom(true);
	}

	// ----------------------------------------------------------------
	// zoom out
	public void CellsSmaller() {
		Zoom(false);
	}

	// ----------------------------------------------------------------
	// handle events we are interested in
	@Override
	public boolean handleEvent(Event e) {
		if ((e.target == hSbar) || (e.target == vSbar)) // one of scrollbars
		{
			ViewOrg.x = hSbar.getValue();
			ViewOrg.y = vSbar.getValue();
			Pan(0, 0); // validate and redisplay
			return true;
		}
		return super.handleEvent(e);
	}

	// ----------------------------------------------------------------
	// button action
	@Override
	public boolean action(Event e, Object arg) {
		if (e.target == btnZoomIn) // zoom in - bigger cells
		{
			CellsBigger();
		} else if (e.target == btnZoomOut) // zoom out - smaller cells
		{
			CellsSmaller();
		} else if (e.target == btnFit) // fit the pattern
		{
			Fit(true);
		}
		return true;
	}

	// ----------------------------------------------------------------
	// put the pixel buffer to the screen
	@Override
	public void paint(Graphics g) {
		if ((LastPanelSize.x != getSize().width)
				|| (LastPanelSize.y != getSize().height)) {
			InitBoard(UnivSize.x, UnivSize.y, CellSize); // resized, update
			// board parameters
		}
		if (ViewSize.x > 0) {
			offSrs.newPixels();
			g.drawImage(offImg, OfsX, OfsY, null);
		}
	}

	// ----------------------------------------------------------------
	// avoid flickering
	@Override
	public void update(Graphics g) {
		paint(g);
	}

	// ----------------------------------------------------------------
	// prepare the pixels buffer and call repaint() to show it
	public void RedrawBoard(boolean fDrawAll) {
		int i, j, ic, jc, newClo, iTmpCol;
		int dx = CellSize * CellsInView.x;
		int ixCellSize;
		boolean fDrawGrid = (DrawGrid && (CellSize > 4));
		int iMinY, iMaxY; // min/max Y to be displayed (optimization)

		if (fDrawAll || (GameType == MJRules.GAMTYP_2D)) {
			iMinY = 0;
			iMaxY = CellsInView.y;
		} else {
			iMinY = i1DLastRow - ViewOrg.y; // 1D - paint only the current row
			iMaxY = iMinY + 1;
		}

		try {
			for (i = 0; i < CellsInView.x; i++) {
				ixCellSize = i * CellSize; // x offset
				for (j = iMinY; j < iMaxY; j++) {
					newClo = mjPal.Palette[crrState[ViewOrg.x + i][ViewOrg.y
							+ j]];
					// now paint the cells - set proper 'screen' array fields
					if ((fDrawAll)
							|| (screen[(j * CellSize) * dx + i * CellSize] != newClo)) {
						switch (CellSize) {
						case 1:
							screen[j * dx + ixCellSize] = newClo;
							break;
						case 2:
							screen[iTmpCol = (j * CellSize + 0) * dx
									+ ixCellSize] = newClo;
							screen[++iTmpCol] = newClo;
							screen[iTmpCol = (j * CellSize + 1) * dx
									+ ixCellSize] = newClo;
							screen[++iTmpCol] = newClo;
							break;
						default:
							for (ic = 0; ic < CellSize; ic++) {
								iTmpCol = (j * CellSize + ic) * dx + ixCellSize;
								for (jc = 0; jc < CellSize; jc++) {
									if (fDrawGrid
											&& ((ic == CellSize - 1) || (jc == CellSize - 1))) {
										screen[iTmpCol++] = ((ic == CellSize - 1) && ((ViewOrg.y + j) % 5 == 0))
												|| ((jc == CellSize - 1) && ((ViewOrg.x + i) % 5 == 0)) ? mjPal.GridColor[1] : mjPal.GridColor[0];
										// grid
										// line
									} else {
										screen[iTmpCol++] = newClo;
									}
								}
							}
							break;
						}
					}
				}
			}
			repaint(); // 'screen' buffer ready, go paint
		} catch (Exception exc) {
        }
	}

	// ----------------------------------------------------------------
	// Set the animation delay, 0..1000
	public void setAnimDelay(int newDelay) {
		if (newDelay < 0)
			newDelay = 0;
		if (newDelay > 1000)
			newDelay = 1000;
		AnimDelay = newDelay;
	}

	// ----------------------------------------------------------------
	// Interprete and apply the rule definition
	public void SetRule(int iGame, String sRuleNam, String sRuleDef) {
		sRuleDef = sRuleDef.trim();
		if (sRuleDef.isEmpty())
			return;

		GameType = MJRules.GAMTYP_2D; // most are 2-dimensional

		//noinspection UseOfStringTokenizer
		StringTokenizer st;
		String sTok;
		char cChar;
		int i, iNum = 1;
		int iCharVal;

		CrrGame = iGame;
		switch (CrrGame) {
		case MJRules.GAME_LIFE: // Standard Conway-like game
			RLife.InitFromString(sRuleDef);
			sRuleDef = RLife.GetAsString(); // get possibly corrected rules
			// string
			break;

		case MJRules.GAME_VOTE: // Vote for life
			RVote.InitFromString(sRuleDef);
			sRuleDef = RVote.GetAsString(); // get possibly corrected rules
			// string
			break;

		case MJRules.GAME_GENE: // Generations
			RGene.InitFromString(sRuleDef);
			sRuleDef = RGene.GetAsString(); // get possibly corrected rules
			// string
			SetStatesCount(RGene.iClo);
			break;

		case MJRules.GAME_WLIF: // Weighted life
			RWLife.InitFromString(sRuleDef);
			sRuleDef = RWLife.GetAsString(); // get possibly corrected rules
			// string
			if (RWLife.isHist)
				SetStatesCount(RWLife.iClo);
			break;

		case MJRules.GAME_RTBL: // Rules table
			RRtab.InitFromString(sRuleDef);
			sRuleDef = RRtab.GetAsString(); // get possibly corrected rules
			// string
			SetStatesCount(RRtab.iClo);
			break;

		case MJRules.GAME_CYCL: // Cyclic CA
			RCyclic.InitFromString(sRuleDef);
			sRuleDef = RCyclic.GetAsString(); // get possibly corrected rules
			// string
			SetStatesCount(RCyclic.iClo);
			break;

		case MJRules.GAME_1DTO: // 1D totalistic
			GameType = MJRules.GAMTYP_1D; // this one is 1-dimensional
			R1DTo.InitFromString(sRuleDef);
			sRuleDef = R1DTo.GetAsString(); // get possibly corrected rules
			// string
			if (R1DTo.isHist)
				SetStatesCount(R1DTo.iClo);
			break;

		case MJRules.GAME_1DBI: // 1D binary
			GameType = MJRules.GAMTYP_1D; // this one is 1-dimensional
			R1DBin.InitFromString(sRuleDef);
			sRuleDef = R1DBin.GetAsString(); // get possibly corrected rules
			// string
			break;

		case MJRules.GAME_NMBI:
			RNeumBin.InitFromString(sRuleDef);
			sRuleDef = RNeumBin.GetAsString(); // get possibly corrected rules
			// string
			SetStatesCount(RNeumBin.iClo);
			break;

		case MJRules.GAME_GEBI: // General binary
			RGenBin.InitFromString(sRuleDef);
			sRuleDef = RGenBin.GetAsString(); // get possibly corrected rules
			// string
			SetStatesCount(RGenBin.iClo);
			break;

		case MJRules.GAME_LGTL:
			RLgtL.InitFromString(sRuleDef);
			sRuleDef = RLgtL.GetAsString(); // get possibly corrected rules
			// string
			if (RLgtL.isHist)
				SetStatesCount(RLgtL.iClo);
			break;

		case MJRules.GAME_MARG:
			RMarg.InitFromString(sRuleDef);
			sRuleDef = RMarg.GetAsString(); // get possibly corrected rules
			// string
			if (RMarg.isHist)
				SetStatesCount(RMarg.iClo);
			break;

		case MJRules.GAME_USER:
			RUser.InitFromString(sRuleDef);
			sRuleDef = RUser.GetAsString(); // get possibly corrected rules
			// string
			SetStatesCount(RUser.iClo);
			break;
		}
		RuleName = sRuleNam; // store current rule name
		RuleDef = sRuleDef; // store current rule definition
		mjUI.UpdateColorsUI();
	} // SetRule()

	// ----------------------------------------------------------------
	// Mouse click; if inside the board, draw a cell
	@Override
	public boolean mouseDown(Event p1, int p2, int p3) {
		if ((p2 >= OfsX) && (p3 >= OfsY)
				&& (p2 <= OfsX + UnivSize.x * CellSize)
				&& (p3 <= OfsY + UnivSize.y * CellSize)) {
			lastX = (p2 - OfsX) / CellSize + ViewOrg.x;
			lastY = (p3 - OfsY) / CellSize + ViewOrg.y;

			// toggle the cell
			//if (crrState[lastX][lastY] > 0)
			//  SetCell(lastX, lastY, (short)0);
			//else
			SetCell(lastX, lastY, (short) CrrState); // active state (color)
			RedrawBoard(false); // show the universe
			mjUI.UpdateUI(); // update the user interface
			return true;
		} else {
			return super.mouseDown(p1, p2, p3);
		}
	}

	// ----------------------------------------------------------------
	// Mouse is dragged, draw a line
	@Override
	public boolean mouseDrag(Event p1, int p2, int p3) {
		if ((p2 >= OfsX) && (p3 >= OfsY)
				&& (p2 <= OfsX + UnivSize.x * CellSize)
				&& (p3 <= OfsY + UnivSize.y * CellSize)) {
			int x = (p2 - OfsX) / CellSize + ViewOrg.x;
			int y = (p3 - OfsY) / CellSize + ViewOrg.y;
			DrawLine(lastX, lastY, x, y);
			lastX = x;
			lastY = y;
			RedrawBoard(false); // show the universe
			mjUI.UpdateUI(); // update the user interface
			return true;
		} else {
			return super.mouseDrag(p1, p2, p3);
		}
	}

	// ----------------------------------------------------------------
	// Draw a line joining two given points
	public void DrawLine(int x1, int y1, int x2, int y2) {
		int shortDiff, longDiff; // short dimension and long dimension
		int xDiff, yDiff;
		int xRight, yDown;
		int x, y;
		boolean across; // across or down
		int wrap, i, j;

		xDiff = Math.abs(x2 - x1);
		yDiff = Math.abs(y2 - y1);

		if (xDiff > yDiff) {
			across = true;
			shortDiff = yDiff;
			longDiff = xDiff;
		} else {
			across = false;
			shortDiff = xDiff;
			longDiff = yDiff;
		}

		xRight = x2 > x1 ? 1 : -1;
		yDown = y2 > y1 ? 1 : -1;

		j = 0;
		wrap = 0;

		for (i = 0; i < longDiff; i++) {
			if (across) {
				x = x1 + (i * xRight);
				y = y1 + (j * yDown);
			} else {
				x = x1 + (j * xRight);
				y = y1 + (i * yDown);
			}

			SetCell(x, y, (short) CrrState); // active state (color)
			wrap += shortDiff;
			if (wrap >= longDiff) {
				j++;
				wrap %= longDiff;
			}
		}
		SetCell(x2, y2, (short) CrrState); // active state (color)
	}

	// ----------------------------------------------------------------
	// randomize one cell
	public void RandomizeOneCell(int x, int y, double maxVal) {
		short newStt;

		if (mjUI.chkMon.getState()) // mono, only 1 state
		{
			if (Math.random() <= maxVal)
				SetCell(x, y, (short) CrrState); // active state (color)
		} else // color
		{
			if (mjUI.chkUni.getState()) // uniform, full board with all states
			{
				newStt = (short) (Math.ceil(Math.random() * StatesCount) - 1);
				SetCell(x, y, newStt); // set
			} else // all states
			{
				if (Math.random() <= maxVal) {
					newStt = (short) Math.ceil(Math.random()
							* (StatesCount - 1));
					SetCell(x, y, newStt); // set
				}
			}
		}
	}

	// ----------------------------------------------------------------
	public final int RAND_ALL = 1;

	public final int RAND_VIEW = 2;

	// ----------------------------------------------------------------
	// Fill the board with random cells. sHow is a string like "65%"
	public void Randomize(String sHow, int what) {
		int i, j, minX, maxX, minY, maxY;
		double maxVal = 0.25;
		boolean fOldRun = (caThread != null);
		stop();
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
		}

		switch (what) {
		case RAND_VIEW:
			minX = ViewOrg.x;
			maxX = ViewOrg.x + CellsInView.x - 1;
			minY = ViewOrg.y;
			maxY = ViewOrg.y + CellsInView.y - 1;
			break;
		case RAND_ALL:
		default:
			minX = 0;
			maxX = UnivSize.x - 1;
			minY = 0;
			maxY = UnivSize.y - 1;
			break;
		}
		sHow = sHow.substring(0, sHow.length() - 1); // "65%" -> "65"
		i = Integer.parseInt(sHow.trim());
		maxVal = i / 100.0;
		if (!mjUI.chkAdd.getState()) // should clear before
		{
			Clear(false);
		}

		if (GameType == MJRules.GAMTYP_2D) {
			for (i = minX; i <= maxX; i++)
				for (j = minY; j <= maxY; j++)
					RandomizeOneCell(i, j, maxVal);
		} else // 1D
		{
			for (i = minX; i <= maxX; i++)
				RandomizeOneCell(i, i1DLastRow, maxVal);
		}
		mjUI.vDescr.clear(); // no old description
		RedrawBoard(true); // show the universe
		MakeBackup(); // store the pattern of eventual rewinding
		if (fOldRun)
			start();
		Cycle = 0; // start counting from 0
	}

	// ----------------------------------------------------------------
	// Seed the board with cells
	public void Seed(String sHow) {
		int i, j;
		int ctrX, ctrY;
		boolean fOldRun = caThread != null;
		stop();
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
		}

		if (!mjUI.chkAdd.getState()) // should clear before
		{
			Clear(false);
		}
		// now seed the universe
		ctrX = UnivSize.x / 2;
		ctrY = UnivSize.y / 2;
		if (sHow.startsWith("BLK")) // block of cells
		{
			sHow = sHow.substring(3).trim();
			int iPos = sHow.indexOf('x');
			if (iPos >= 0) {
				int dx = Integer.valueOf(sHow.substring(0, iPos));
				int dy = Integer.valueOf(
                        sHow.substring(iPos + 1, sHow.length()));
				if (GameType == MJRules.GAMTYP_2D) {
					for (i = 0; i < dx; i++)
						for (j = 0; j < dy; j++)
							SetCell(ctrX - (dx / 2) + i, ctrY - (dy / 2) + j,
									(short) CrrState); // active state (color)
				} else // 1D
				{
					for (i = 0; i < dx; i++)
						SetCell(ctrX - (dx / 2) + i, i1DLastRow,
								(short) CrrState); // active state (color)
				}
			}
		} else if (sHow.startsWith("FRM")) // frame of cells
		{
			sHow = sHow.substring(3).trim();
			int iPos = sHow.indexOf('x');
			if (iPos >= 0) {
				int dx = Integer.valueOf(sHow.substring(0, iPos));
				int dy = Integer.valueOf(
                        sHow.substring(iPos + 1, sHow.length()));
				if (GameType == MJRules.GAMTYP_2D) {
					for (i = 0; i < dx; i++) {
						SetCell(ctrX - (dx / 2) + i, ctrY - (dy / 2),
								(short) CrrState); // active state (color)
						SetCell(ctrX - (dx / 2) + i, ctrY - (dy / 2) + dy - 1,
								(short) CrrState); // active state (color)
					}
					for (j = 1; j < dy - 1; j++) {
						SetCell(ctrX - (dx / 2), ctrY - (dy / 2) + j,
								(short) CrrState); // active state (color)
						SetCell(ctrX - (dx / 2) + dx - 1, ctrY - (dy / 2) + j,
								(short) CrrState); // active state (color)
					}
				} else // 1D
				{
					SetCell(ctrX - (dx / 2), i1DLastRow, (short) CrrState); // active
					// state
					// (color)
					SetCell(ctrX + (dx / 2) - 1, i1DLastRow, (short) CrrState); // active
					// state
					// (color)
				}
			}
		}
		CenterBoard(); // put (0,0) to the center
		mjUI.vDescr.clear(); // no old description
		RedrawBoard(true); // show the universe
		MakeBackup(); // store the pattern of eventual rewinding
		UpdateScrollbars(); // update scrollbars values
		if (fOldRun)
			start();
		Cycle = 0; // start counting from 0
	}

	// ----------------------------------------------------------------
	// Empty the board
	public void Clear(boolean fRedraw) {
		int i, j;
		stop();
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
		}
		for (i = 0; i <= MAX_X; ++i)
			for (j = 0; j <= MAX_Y; ++j)
				SetCell(i, j, (short) 0);

		// no alive cells
		Population = 0;
		Populations[0] = UnivSize.x * UnivSize.y; // all cells have state 0
		for (i = 1; i <= MAX_CLO; i++)
			Populations[i] = 0;
		Cycle = 0; // start counting from 0
		i1DLastRow = 0; // 1-D last generated row
		if (fRedraw)
			RedrawBoard(true); // show the empty universe
	}

	// ----------------------------------------------------------------
	// Set the count of states
	public void SetStatesCount(int iSttCnt) {
		if ((iSttCnt >= 2) && (iSttCnt <= MAX_CLO + 1)) {
			StatesCount = iSttCnt;
			// recreate the palette for the new count of states
			mjPal.ActivatePalette(mjPal.PalName, StatesCount);
			mjUI.UpdateColorsUI();
		}
	}

	// ----------------------------------------------------------------
	// Set the active state (used for drawing)
	public void SetCrrState(int iCrrState) {
		if ((iCrrState >= 0) && (iCrrState < StatesCount)) {
			CrrState = iCrrState;
			mjUI.UpdateColorsUI();
		}
	}
	// ----------------------------------------------------------------
	// ----------------------------------------------------------------
}