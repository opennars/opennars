package nars.ca;// Mirek's Java Cellebration
// http://www.mirekw.com
//
// Open the CA pattern file

import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

public class MJOpen {
	private final MJCellUI mjUI;
	private final MJBoard mjb;

	private List<CACell> m_vCells; // cells found in the pattern file
	private List<String> m_vDescr; // pattern description
	private List<String> m_vDiv; // diversities

	private String m_sGame; // family of rules
	private String m_sRules; // the rule
	private int m_rectMinX, m_rectMaxX, m_rectMinY, m_rectMaxY;
	private int m_Speed; // game speed
	private int m_BSizeX, m_BSizeY; // board size
	private int m_Wrap; // wrapping state
	private int m_CColors; // count of colors
	private int m_Coloring; // coloring method
	private String m_sPalette; // colors palette file name
	private double m_dMclVsn; // MCL file version

	// ----------------------------------------------------------------
	// Constructor
	public MJOpen(MJCellUI cmjui, MJBoard cmjb) {
		mjUI = cmjui;
		mjb = cmjb;
	}

	// ----------------------------------------------------------------
	// Main function opening a file
	@SuppressWarnings("HardcodedFileSeparator")
	public boolean OpenFile(String sFileName) {
		String sBff, sFilePath;
		MJTools mjT;
		Vector vLines;
		boolean fOk = false;
		int i;

		m_vCells = new Vector();
		vLines = new Vector();
		m_vDescr = new Vector(); // description
		m_vDiv = new Vector(); // diversities

		m_sRules = "";
		m_sGame = MJRules.GAME_LIFE_Name; // Life
		m_Speed = -1; // game speed
		m_BSizeX = -1; // board size
		m_BSizeY = m_BSizeX;
		m_Wrap = -1; // wrapping state
		m_CColors = -1; // count of colors
		m_Coloring = -1; // default
		m_sPalette = ""; // colors palette file name
		m_dMclVsn = 0; // MCL file version

		sFileName = CorrectFileName(sFileName);
		sFilePath = mjUI.sBaseURL + "p/" + sFileName;
		//System.out.println(sFilePath);

		mjT = new MJTools();
		if (mjT.LoadTextFile(sFilePath, vLines)) // load the file into the vector
		{
			if (!vLines.isEmpty()) // was anything read?
			{
				// find the first non-comment line
				i = 0;
				String sFirstLine = "";
				while (i < vLines.size()) {
					sBff = (String) vLines.elementAt(i);
					if ((sBff.isEmpty()) || (sBff.startsWith("#C"))
							|| (sBff.startsWith("#D"))
							|| (sBff.length() > 0 && sBff.charAt(0) == '!')) {
						i++; // comment found, go on searching
					} else {
						sFirstLine = sBff;
						i = vLines.size(); // stop the loop
					}
				}

				// try to recognize the file type
				if (sFirstLine.startsWith("#Life 1.05") || sFirstLine.startsWith("#Life 1.02") || sFirstLine.startsWith("#P") || sFirstLine.startsWith("#MCLife"))
					fOk = ReadLife105(vLines);
				else if (sFirstLine.startsWith("#MCell") || sFirstLine.startsWith("#Life 1.06"))
					fOk = ReadMCL(vLines);
				else if (sFirstLine.startsWith("#Life 1.05b") || sFirstLine.length() > 0 && sFirstLine.charAt(0) == 'x')
					fOk = ReadLife106(vLines);

				if (!fOk)
					fOk = ReadLife105(vLines);
				if (!fOk)
					fOk = ReadLife106(vLines);
				if (!fOk)
					fOk = ReadMCL(vLines);
				if (!fOk)
					fOk = ReadRLE(vLines);

				if (fOk) // pattern loaded
				{
					if (!m_vCells.isEmpty()) // any cells found
						AddPattern();
				} else {
					System.out
							.println("Unrecognized file format: " + sFilePath);
				}
			} else {
				System.out.println("Empty pattern file: " + sFilePath);
			}
		}

		return fOk;
	}

	// ----------------------------------------------------------------
	// Make the file name Netscape-friendly
	private String CorrectFileName(String sFileName) {
		String sNew;
		sNew = sFileName.replace(' ', '_');
		sNew = sNew.replace('\'', '_');
		return sNew;
	}

	// ----------------------------------------------------------------
	// Everything's loaded, put the pattern on the board
	private void AddPattern() {
		int minX, maxX, minY, maxY;
		int x, y, sizeX, sizeY, dx, dy;
		int i, state;
		CACell cell = new CACell();

		// set the pattern description
		mjUI.vDescr.clear();
		mjUI.vDescr = m_vDescr;

		// determine the pattern size
		CalcMinRectangle();
		sizeX = m_rectMaxX - m_rectMinX;
		sizeY = m_rectMaxY - m_rectMinY;
		if ((m_BSizeX > 0) && (m_BSizeY > 0)) // board size specified
		{
			mjb.SetBoardSize(m_BSizeX, m_BSizeY);
		} else // pattern does not specify the size
		{
			mjb.InitBoard(sizeX + 120, sizeY + 120, mjb.CellSize);
		}

		// diversities must be activated separately for each file 
		mjb.Div.m_Enabled = false;
		for (i = 0; i < m_vDiv.size(); i++)
			mjb.Div.ItemFromString(m_vDiv.get(i),
					mjb.UnivSize.x, mjb.UnivSize.y);

		// add all cells
		mjb.Clear(false);
		dx = mjb.UnivSize.x / 2 - (m_rectMaxX + m_rectMinX) / 2 - 1;
		dy = mjb.GameType == MJRules.GAMTYP_2D ? mjb.UnivSize.y / 2 - (m_rectMaxY + m_rectMinY) / 2 - 1 : 0;
		for (i = 0; i < m_vCells.size(); i++) {
			cell = m_vCells.get(i);
			x = cell.x + dx;
			y = cell.y + dy;
			mjb.SetCell(x, y, cell.state);
		}

		// wrapping at edges
		if (m_Wrap >= 0)
			mjUI.SetWrapping(m_Wrap != 0);

		// count of colors
		if (m_CColors >= 2)
			mjb.SetStatesCount(m_CColors);

		// coloring method
		if (m_Coloring > 0)
			mjUI.SetColoringMethod(m_Coloring);

		// color palette
		if (!m_sPalette.isEmpty())
			mjUI.SetColorPalette(m_sPalette);

		// set rules
		if (!m_sRules.isEmpty()) {
			mjb.SetRule(mjUI.mjr.GetGameIndex(m_sGame), "", m_sRules);
		}

		mjb.RedrawBoard(true);
		mjUI.UpdateUI();
		mjUI.UpdateColorsUI();
		mjb.MakeBackup(); // store the pattern of eventual rewinding
		//mjb.Fit(true);
	}

	// ----------------------------------------------------------------
	// Calculate the cells' minimal rectangle
	private void CalcMinRectangle() {
		CACell cell = new CACell();
		m_rectMinX = m_rectMinY = 999999;
		m_rectMaxX = m_rectMaxY = -999999;

		for (CACell m_vCell : m_vCells) {
			cell = m_vCell;

			if (m_rectMinX > cell.x)
				m_rectMinX = cell.x;
			if (m_rectMaxX < cell.x)
				m_rectMaxX = cell.x;
			if (m_rectMinY > cell.y)
				m_rectMinY = cell.y;
			if (m_rectMaxY < cell.y)
				m_rectMaxY = cell.y;
		}
	}

	// ----------------------------------------------------------------
	// ----------------------------------------------------------------
	// Open the pattern in extended Life 1.05 format
	// This version interprets also mixed LIFE/RLE files
	private boolean ReadLife105(Vector vLines) {
		String bff;
		int i;
		boolean fOk = false;

		iBlkX = 0;
		iBlkY = 0;
		iRow105 = 0;

		// analyze all lines
		for (i = 0; i < vLines.size(); i++) {
			bff = (String) vLines.elementAt(i);
			if (ProcessOneLIF105Line(bff))
				fOk = true; // any cell added
		}

		return fOk;
	}

	// ----------------------------------------------------------------
	// *.lif, *.life files line parser
	// Return true if at least one cell was added
	int iRow105;
	int iBlkX, iBlkY; // block left-top corner

	@SuppressWarnings("HardcodedFileSeparator")
	boolean ProcessOneLIF105Line(String bff) {
		boolean fOk = false;
		int iPos;
		String sTok;
		int iCol;
		int i, j, iNum;

		bff = bff.trim();

		if (!bff.isEmpty()) {
			// special characters?
			if ((bff.charAt(0) == '#') || (bff.charAt(0) == '!')
					|| (bff.charAt(0) == '/')) {
				//noinspection IfStatementWithTooManyBranches
				if (bff.startsWith("#P")) // the block position
				{
					//noinspection UseOfStringTokenizer
					StringTokenizer st = new StringTokenizer(bff);
					st.nextToken(); // #P
					iRow105 = 0;
					if (st.hasMoreTokens())
						iBlkX = Integer.parseInt(st.nextToken());
					if (st.hasMoreTokens())
						iBlkY = Integer.parseInt(st.nextToken());
				} else if (bff.startsWith("#N")) // standard rules
				{
					m_sRules = "23/3"; // standard Conway's rules
				} else if (bff.startsWith("#R")) // specific rules
				{
					//noinspection UseOfStringTokenizer
					StringTokenizer st = new StringTokenizer(bff);
					st.nextToken(); // #R
					if (st.hasMoreTokens())
						m_sRules = st.nextToken();
				} else if (bff.startsWith("#S")) // speed
				{
					//noinspection UseOfStringTokenizer
					StringTokenizer st = new StringTokenizer(bff);
					st.nextToken(); // #S
					if (st.hasMoreTokens())
						m_Speed = Integer.parseInt(st.nextToken());
				} else if (bff.startsWith("#D") || bff.startsWith("#C")) // description
				{
					sTok = bff.substring(2);
					if (!sTok.isEmpty()) // remove one leading blank
						if (sTok.charAt(0) == ' ')
							sTok = sTok.substring(1);
					m_vDescr.add(sTok); // add the comment line
				} else if (bff.length() > 0 && bff.charAt(0) == '!') // description
				{
					sTok = bff.substring(1);
					if (!sTok.isEmpty()) // remove one leading blank
						if (sTok.charAt(0) == ' ')
							sTok = sTok.substring(1);
					m_vDescr.add(sTok); // add the comment line
				}
			} else // cells data line
			{
				iCol = 0;
				iNum = 0;
				for (i = 0; i < bff.length(); i++) {
					if ((bff.charAt(i) >= '0') && (bff.charAt(i) <= '9')) {
						iNum = iNum * 10 + (bff.charAt(i) - '0');
					} else {
						if (iNum == 0)
							iNum = 1;
						// alive cell?
						if ((bff.charAt(i) == '*') || (bff.charAt(i) == 'o')
								|| (bff.charAt(i) == 'O')) {
							for (j = 0; j <= iNum - 1; j++)
								m_vCells.add(new CACell(
                                        iCol + j + iBlkX, iRow105 + iBlkY,
                                        (short) 1));
							fOk = true;
							iCol = iCol + iNum;
						} else // blank
						{
							iCol = iCol + iNum;
						}
						iNum = 0;
					}
				} // end of line
				iRow105++;
			}
		}
		return fOk;
	}

	// ----------------------------------------------------------------
	// ----------------------------------------------------------------
	// Open the pattern in MCL format
	private int iIniColMCL;
	private int iColMCL, iRowMCL;
	private int iNumMCL; // number of repetitions

	private boolean ReadMCL(Vector vLines) {
		String bff;
		int i;
		boolean fOk = false;

		iColMCL = 0;
		iRowMCL = 0;
		iNumMCL = 0;
		iIniColMCL = 0;

		// determine the file version
		bff = (String) vLines.elementAt(0);
		if (bff.startsWith("#MCLife ")) // '#MCLife 1.0'
		{
			bff = bff.substring(8);
			m_dMclVsn = Double.valueOf(bff);
		}
		if (bff.startsWith("#MCell ")) // '#MCell 2.0' or later
		{
			bff = bff.substring(7);
			m_dMclVsn = Double.valueOf(bff);
		}

		// analyze all lines
		m_sRules = ""; // no default rule
		for (i = 0; i < vLines.size(); i++) {
			bff = (String) vLines.elementAt(i);
			if (ProcessOneMCLLine(bff))
				fOk = true; // any cell added
		}

		return fOk;
	}

	// ----------------------------------------------------------------
	// *.MCL files line parser
	// Return True if at least one cell was added or any important
	// MCL file keyword was found
	@SuppressWarnings("HardcodedFileSeparator")
	private boolean ProcessOneMCLLine(String bff) {
		boolean fOk = false;
		int i, j;
		String sTok;
		int iAdd = 0; // for states > 24

		bff = bff.trim();

		if (!bff.isEmpty()) {
			//noinspection IfStatementWithTooManyBranches
			if (bff.startsWith("#RULE")) // specific rules
			{
				sTok = bff.substring(5);
				// long rule can be splitted into several lines
				m_sRules = m_sRules + sTok.trim(); // set rules
				fOk = true; // rule defined
			} else if (bff.startsWith("#GAME")) // game
			{
				m_sGame = bff.substring(5).trim();
				fOk = true; // game defined
			} else if (bff.startsWith("#DIV")) // diversities
			{
				sTok = bff.substring(4).trim();
				m_vDiv.add(sTok);
			} else if (bff.startsWith("#D")) // description
			{
				sTok = bff.substring(2);
				if (!sTok.isEmpty()) // remove one leading blank
					if (sTok.charAt(0) == ' ')
						sTok = sTok.substring(1);
				m_vDescr.add(sTok); // add the comment line
			} else if (bff.startsWith("#BOARD")) // board size,  "999x999"
			{
				sTok = bff.substring(6).trim();
				//noinspection UseOfStringTokenizer
				StringTokenizer st = new StringTokenizer(sTok, "x", false);
				if (st.hasMoreTokens()) {
					String sTmp = st.nextToken();
					m_BSizeX = Integer.valueOf(sTmp);
					if (st.hasMoreTokens()) {
						sTmp = st.nextToken();
						m_BSizeY = Integer.valueOf(sTmp);
					}
				}
			} else if (bff.startsWith("#SPEED")) // speed
			{
				sTok = bff.substring(6).trim();
				m_Speed = Integer.valueOf(sTok);
			} else if (bff.startsWith("#WRAP")) // wrap at edges
			{
				sTok = bff.substring(5).trim();
				m_Wrap = Integer.valueOf(sTok);
			} else if (bff.startsWith("#CCOLORS")) // count of colors
			{
				sTok = bff.substring(8).trim();
				m_CColors = Integer.valueOf(sTok);
			} else if (bff.startsWith("#COLORING")) // coloring method
			{
				sTok = bff.substring(9).trim();
				m_Coloring = Integer.valueOf(sTok);
			} else if (bff.startsWith("#PALETTE")) // colors palette file name
			{
				sTok = bff.substring(8).trim();
				m_sPalette = sTok;
			} else if (bff.startsWith("#L")) // data line
			{
				bff = bff.substring(2).trim();
				for (i = 0; i < bff.length(); i++) {
					if ((bff.charAt(i) >= '0') && (bff.charAt(i) <= '9')) {
						iNumMCL = iNumMCL * 10 + (bff.charAt(i) - '0');
					} else {
						if (iNumMCL == 0)
							iNumMCL = 1;
						switch (bff.charAt(i)) {
						case '$':
							iRowMCL = iRowMCL + iNumMCL;
							iColMCL = iIniColMCL;
							iNumMCL = 0;
							break;

						case '.': // blank
							iColMCL = iColMCL + iNumMCL;
							iNumMCL = 0;
							break;

						default:
							if ((bff.charAt(i) >= 'a')
									&& (bff.charAt(i) <= 'j')) {
								// a: 25..48, b: 49..72, c: 73..96, ..., j: 241..255
								iAdd = (bff.charAt(i) - 'a' + 1) * 24;
							} else if ((bff.charAt(i) >= 'A')
									&& (bff.charAt(i) <= 'X')) {
								// cell in state 1..24
								for (j = 0; j < iNumMCL; j++) {
									m_vCells
											.add(new CACell(
                                                    iColMCL + j,
                                                    iRowMCL,
                                                    (short) (bff.charAt(i) - 'A' + 1 + iAdd)));
								}
								iColMCL = iColMCL + iNumMCL;
								fOk = true; // any cell added
								iAdd = 0;
								iNumMCL = 0;
							} else {
								iNumMCL = 0;
							}
							break;
						}
					}
				}
			} // data line
			else if (bff.startsWith("#N")) // standard rules
			{
				m_sGame = MJRules.GAME_LIFE_Name;
				m_sRules = "23/3"; // standard Conway's rules
			}
		} // bff.length

		return fOk;
	}

	// ----------------------------------------------------------------
	// ----------------------------------------------------------------
	// Read the file in RLE format
	private boolean fEndFlg; // '!' found, start of the comment
	private boolean fXYFound; // x= y= line found
	private int iCol, iRow, iniCol;
	private int iNum; // number of repetitions

	private boolean ReadRLE(Vector vLines) {
		String bff;
		int i;
		boolean fOk = false;

		iCol = 0;
		iRow = 0;
		iNum = 0;
		iniCol = 0;
		fEndFlg = false;
		fXYFound = false;

		// analyze all lines
		for (i = 0; i < vLines.size(); i++) {
			bff = (String) vLines.elementAt(i);
			if (ProcessOneRLELine(bff))
				fOk = true; // any cell added
		}
		return fOk;
	}

	// ----------------------------------------------------------------
	// *.l, *.rle files line parser
	// Return True if at least one cell was added
	private boolean ProcessOneRLELine(String bff) {
		boolean fOk = false;
		int i, j, iTmp;
		String sTok;

		bff = bff.trim();

		if (bff.startsWith("#D") || bff.startsWith("#C")) // strange description
		{
			sTok = bff.substring(2);
			if (!sTok.isEmpty()) // remove one leading blank
				if (sTok.charAt(0) == ' ')
					sTok = sTok.substring(1);
			m_vDescr.add(sTok); // add the comment line
		} else {
			if (fEndFlg) // past the end - the description
			{
				m_vDescr.add(bff); // add the comment line
			} else {
				if (!bff.isEmpty()) {
					if ((!fXYFound) && bff.length() > 0 && bff.charAt(0) == 'x') // the first line
					{
						fXYFound = true;
						fOk = true; // any line processed

						//noinspection UseOfStringTokenizer
						StringTokenizer stcomma = new StringTokenizer(bff, ",");
						while (stcomma.hasMoreTokens()) {
							String t = stcomma.nextToken();
							//noinspection UseOfStringTokenizer
							StringTokenizer stequal = new StringTokenizer(t,
									"= ");
							String tokenType = stequal.nextToken();
							String tokenValue = stequal.nextToken();

                            switch (tokenType) {
                                case "x":
// X size
                                    iniCol = iCol = -(Math.abs(Integer
                                            .parseInt(tokenValue)) / 2);
                                    break;
                                case "y":
// Y size
                                    iRow = -(Math.abs(Integer.parseInt(tokenValue)) / 2);
                                    break;
                                case "rule":
                                case "rules":
                                    m_sRules = tokenValue;
                                    break;
                                case "skip":
                                    // ?
                                    break;
                                case "fps":
                                    m_Speed = Integer.parseInt(tokenValue);
                                    break;
                            }
						}
					} else // the normal (not first) line
					{
						for (i = 0; (i < bff.length()) && (!fEndFlg); i++) {
							if ((bff.charAt(i) >= '0')
									&& (bff.charAt(i) <= '9')) {
								iNum = iNum * 10 + (bff.charAt(i) - '0');
							} else {
								if (iNum == 0)
									iNum = 1;
								switch (bff.charAt(i)) {
								case '$':
									iRow = iRow + iNum;
									iCol = iniCol;
									break;

								case 'b':
								case 'B':
								case '.':
									iCol = iCol + iNum; // blank
									break;

								case '!': // end flag, the rest is the description
									fEndFlg = true;
									break;

								default: // probably a cell
									if (((bff.charAt(i) >= 'a') && (bff
											.charAt(i) <= 'z'))
											|| ((bff.charAt(i) >= 'A') && (bff
													.charAt(i) <= 'Z'))) {
										switch (bff.charAt(i)) {
										case 'x':
										case 'X':
											iTmp = 2;
											break;

										case 'y':
										case 'Y':
											iTmp = 3;
											break;

										case 'z':
										case 'Z':
											iTmp = 4;
											break;

										default:
											iTmp = 1;
											break;
										}
										for (j = 0; j <= iNum - 1; j++)
											m_vCells.add(new CACell(iCol
                                                    + j, iRow, (short) iTmp));

										iCol = iCol + iNum;
										fOk = true; // any cell added
									}
									break;
								}
								iNum = 0;
							} // not digit
						} // for
					} // (not) first line
				} // Length(bff)
			} // !endFlg
		}
		return fOk;
	}

	// ----------------------------------------------------------------
	// ----------------------------------------------------------------
	// Life 1.06 - DOS format, cell coordinates (x y)
	// Return True if at least one cell was added
	boolean ReadLife106(Vector vLines) {
		String bff;
		int i;
		boolean fOk = false;

		iCol = 0;
		iRow = 0;
		iNum = 0;
		iniCol = 0;
		fEndFlg = false;
		fXYFound = false;

		// analyze all lines
		for (i = 0; i < vLines.size(); i++) {
			bff = (String) vLines.elementAt(i);
			if (ProcessOneLIF106Line(bff))
				fOk = true; // any cell added
		}

		return fOk;
	}

	// ----------------------------------------------------------------
	// DOS Life 1.06 files line parser
	// Return true if at least one cell was added
	@SuppressWarnings("HardcodedFileSeparator")
	boolean ProcessOneLIF106Line(String bff) {
		int iCol, iRow;
		int iPos;
		String sTok;
		boolean fOk = false;

		bff = bff.trim();

		if (!bff.isEmpty()) {
			// a keyword or a comment?
			if ((bff.charAt(0) == '#') || (bff.charAt(0) == '/')
					|| (bff.charAt(0) == '!')) {
				//noinspection IfStatementWithTooManyBranches
				if (bff.startsWith("#N")) // standard rules
				{
					m_sRules = "23/3"; // standard Conway's rules
				} else if (bff.startsWith("#R")) // specific rules
				{
					//noinspection UseOfStringTokenizer
					StringTokenizer st = new StringTokenizer(bff);
					st.nextToken(); // #R
					if (st.hasMoreTokens())
						m_sRules = st.nextToken();
				} else if (bff.startsWith("#S")) // speed
				{
					//noinspection UseOfStringTokenizer
					StringTokenizer st = new StringTokenizer(bff);
					st.nextToken(); // #S
					if (st.hasMoreTokens())
						m_Speed = Integer.parseInt(st.nextToken());
				} else if (bff.startsWith("#D") || bff.startsWith("#C")) {
					// description
					sTok = bff.substring(2);
					if (!sTok.isEmpty()) // remove one leading blank
						if (sTok.charAt(0) == ' ')
							sTok = sTok.substring(1);
					m_vDescr.add(sTok); // add the comment line
				} else if (bff.length() > 0 && bff.charAt(0) == '!') {
					// description
					sTok = bff.substring(1);
					if (!sTok.isEmpty()) // remove one leading blank
						if (sTok.charAt(0) == ' ')
							sTok = sTok.substring(1);
					m_vDescr.add(sTok); // add the comment line
				}
			} else // a cell
			{
				//noinspection UseOfStringTokenizer
				StringTokenizer st = new StringTokenizer(bff);
				if (st.hasMoreTokens()) {
					iCol = Integer.parseInt(st.nextToken());
					if (st.hasMoreTokens()) {
						iRow = Integer.parseInt(st.nextToken());
						m_vCells.add(new CACell(iCol, iRow, (short) 1));
						fOk = true;
					}
				}
			}
		}
		return fOk;
	}
	// ----------------------------------------------------------------

}