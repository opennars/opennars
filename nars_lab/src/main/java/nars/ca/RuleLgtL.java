package nars.ca;// Mirek's Java Cellebration
// http://www.mirekw.com
//
// Larger than Life rules

import java.util.StringTokenizer;

public class RuleLgtL {
	public boolean isHist; // with history?
	public int iClo; // count of states
	public int iRng; // range
	public int iNgh; // neighbourhood type
	public int iSMin, iSMax; // surviving rules
	public int iBMin, iBMax; // birth rules
	public boolean isCentr; // use the center (middle) cell?

	public static final int MAX_RANGE = 10;

	// ----------------------------------------------------------------
	public RuleLgtL() {
		ResetToDefaults();
	}

	// ----------------------------------------------------------------
	// Set default parameters
	public void ResetToDefaults() {
		isHist = false; // with history?
		iClo = 2; // count of colors
		iRng = 5; // range
		iNgh = MJRules.NGHTYP_MOOR; // neighbourhood type
		iSMin = 34;
		iSMax = 58; // surviving rules
		iBMin = 34;
		iBMax = 45; // birth rules
		isCentr = true; // use the center (middle) cell?
	}

	// ----------------------------------------------------------------
	// Parse the rule string
	// Example: "R3,C0,M1,S34..58,B34..45,NM"
	public void InitFromString(String sStr) {
		// noinspection UseOfStringTokenizer
		StringTokenizer st;
		String sTok, sBff;
		int i, iTmp;
		ResetToDefaults();

		st = new StringTokenizer(sStr, ",", true);
		while (st.hasMoreTokens()) {
			sTok = st.nextToken().toUpperCase();
			sTok = sTok.trim();
			// System.out.println(sTok);

			// noinspection IfStatementWithTooManyBranches
			if (sTok.length() > 0 && sTok.charAt(0) == 'R') // range
			{
				iRng = Integer.valueOf(sTok.substring(1));
			} else if (sTok.length() > 0 && sTok.charAt(0) == 'C') // states
																	// (history)
			{
				i = Integer.valueOf(sTok.substring(1));
				if (i >= 3) {
					isHist = true; // history, get the states count
					iClo = i;
				} else
					isHist = false; // states count is meaningless
			} else if (sTok.length() > 0 && sTok.charAt(0) == 'M') // center
																	// cell?
			{
				isCentr = (Integer.valueOf(sTok.substring(1)) > 0);
			} else if (sTok.startsWith("NM")) // Moore neighbourhood
			{
				iNgh = MJRules.NGHTYP_MOOR;
			} else if (sTok.startsWith("NN")) // von Neumann neighbourhood
			{
				iNgh = MJRules.NGHTYP_NEUM;
			} else if (sTok.length() > 0 && sTok.charAt(0) == 'S') // surviving
																	// rules
			{
				if (sTok.length() >= 4) {
					iTmp = sTok.indexOf("..");
					if (iTmp >= 0) {
						sBff = sTok.substring(1, iTmp);
						iSMin = Integer.valueOf(sBff);
						sBff = sTok.substring(iTmp + 2);
						iSMax = Integer.valueOf(sBff);
					}
				}
			} else if (sTok.length() > 0 && sTok.charAt(0) == 'B') // birth
																	// rules
			{
				if (sTok.length() >= 4) {
					iTmp = sTok.indexOf("..");
					if (iTmp >= 0) {
						iBMin = Integer.valueOf(sTok.substring(1, iTmp));
						iBMax = Integer.valueOf(sTok.substring(iTmp + 2));
					}
				}
			}
		}

		// no more tokens
		Validate(); // now correct parameters
	}

	// ----------------------------------------------------------------
	//
	public void InitFromPrm(boolean is_Hist, int i_Clo, int i_Rng, int i_Ngh,
			int i_SMin, int i_SMax, int i_BMin, int i_BMax, boolean is_Centr) {
		isHist = is_Hist; // with history?
		iClo = i_Clo; // count of colors
		iRng = i_Rng; // range
		iNgh = i_Ngh; // neighbourhood type
		iSMin = i_SMin;
		iSMax = i_SMax; // surviving rules
		iBMin = i_BMin;
		iBMax = i_BMax; // birth rules
		isCentr = is_Centr; // use the center (middle) cell?

		Validate(); // now correct parameters
	}

	// ----------------------------------------------------------------
	// Create the rule string
	// Example: "R3,C0,M1,S34..58,B34..45,NM"
	public String GetAsString() {
		String sBff;
		int ih;

		// correct parameters first
		Validate();

		// range
		sBff = 'R' + String.valueOf(iRng);

		// states
		ih = isHist ? iClo : 0;
		sBff = sBff + ",C" + ih;

		// center cell
		sBff = isCentr ? sBff + ",M1" : sBff + ",M0";

		// S rules
		sBff = sBff + ",S" + iSMin + ".." + iSMax;

		// B rules
		sBff = sBff + ",B" + iBMin + ".." + iBMax;

		// neighbourhood
		sBff = iNgh == MJRules.NGHTYP_NEUM ? sBff + ",NN" : sBff + ",NM";

		return sBff;
	}

	// ----------------------------------------------------------------
	// Check the validity of the parameters, correct them if necessary.
	public void Validate() {
		int i, iMax;

		if (iClo < 2)
			iClo = 2;
		else if (iClo > MJBoard.MAX_CLO)
			iClo = MJBoard.MAX_CLO;

		if (iRng < 1)
			iRng = 1;
		else if (iRng > MAX_RANGE)
			iRng = MAX_RANGE;

		if (iNgh != MJRules.NGHTYP_NEUM)
			iNgh = MJRules.NGHTYP_MOOR; // default - Moore neighbourhood

		iMax = isCentr ? 1 : 0;
		for (i = 1; i <= iRng; i++)
			// calculate the max. threshold
			iMax = iMax + i * 8;

		iSMin = BoundInt(1, iSMin, iMax);
		iSMax = BoundInt(1, iSMax, iMax);
		iBMin = BoundInt(1, iBMin, iMax);
		iBMax = BoundInt(1, iBMax, iMax);
	}

	// ----------------------------------------------------------------
	private int BoundInt(int iMin, int iVal, int iMax) {
		if (iVal < iMin)
			return iMin;
		if (iVal > iMax)
			return iMax;
		return iVal;
	}

	// ----------------------------------------------------------------
	// Perform one pass of the rule
	public int OnePass(int sizX, int sizY, boolean isWrap, int ColoringMethod,
			short[][] crrState, short[][] tmpState, MJBoard mjb) {
		short bOldVal, bNewVal;
		int modCnt = 0;
		int i, j, iCnt;
		int[] lurd = new int[4]; // 0-left, 1-up, 2-right, 3-down
		int[] xVector = new int[21]; // 0..9, 10, 11..20
		int[] yVector = new int[21]; // 0..9, 10, 11..20
		int colL, colR, rowT, rowB;
		int ic, ir, iTmp;
		int iTmpC, iTmpR, iTmpBlobC, iTmpBlobR;
		int ctrCol, ctrRow;
		boolean fMoore = (iNgh == MJRules.NGHTYP_MOOR); // Moore neighbourhood?
														// Else von Neumann.

		for (i = 0; i < sizX; i++) {
			for (j = 0; j < sizY; j++) {
				// prepare vectors holding proper rows and columns
				// of the n-range neighbourhood
				xVector[10] = i;
				yVector[10] = j;
				for (iTmp = 1; iTmp <= iRng; iTmp++) {
					colL = i - iTmp;
					xVector[10 - iTmp] = colL >= 0 ? colL : sizX + colL;

					colR = i + iTmp;
					xVector[10 + iTmp] = colR < sizX ? colR : colR - sizX;

					rowT = j - iTmp;
					yVector[10 - iTmp] = rowT >= 0 ? rowT : sizY + rowT;

					rowB = j + iTmp;
					yVector[10 + iTmp] = rowB < sizY ? rowB : rowB - sizY;
				}
				bOldVal = crrState[i][j];
				bNewVal = bOldVal; // default - no change
				if (bNewVal >= iClo)
					bNewVal = (short) (iClo - 1);

				iCnt = 0; // count of firing neighbours
				if (isHist) {
					if (bOldVal <= 1) // can survive or be born
					{
						for (ic = 10 - iRng; ic <= 10 + iRng; ic++) {
							for (ir = 10 - iRng; ir <= 10 + iRng; ir++) {
								if ((isCentr) || (ic != i) || (ir != j)) {
									if ((fMoore)
											|| ((Math.abs(ic - 10) + Math
													.abs(ir - 10)) <= iRng)) {
										if (crrState[xVector[ic]][yVector[ir]] == 1) {
											iCnt++;
										}
									}
								}
							}
						}
						// determine the new cell state
						if (bOldVal == 0) // was dead
						{
							if ((iCnt >= iBMin) && (iCnt <= iBMax)) // rules for
																	// birth
								bNewVal = 1; // birth
						} else // was 1 - alive
						{
							if ((iCnt >= iSMin) && (iCnt <= iSMax)) // rules for
																	// surviving
							{
								bNewVal = 1;
							} else // isolation or overpopulation
							{
								bNewVal = bOldVal < (iClo - 1)
										? (short) (bOldVal + 1)
										: 0;
							}
						}
					} else // was older than 1
					{
						bNewVal = bOldVal < (iClo - 1)
								? (short) (bOldVal + 1)
								: 0;
					}
				} else // no history
				{
					for (ic = 10 - iRng; ic <= 10 + iRng; ic++) {
						for (ir = 10 - iRng; ir <= 10 + iRng; ir++) {
							if ((isCentr) || (ic != i) || (ir != j)) {
								if ((fMoore)
										|| ((Math.abs(ic - 10) + Math
												.abs(ir - 10)) <= iRng)) {
									if (crrState[xVector[ic]][yVector[ir]] != 0) {
										iCnt++;
									}
								}
							}
						}
					}
					// determine the cell status
					if (bOldVal == 0) // was dead
					{
						if ((iCnt >= iBMin) && (iCnt <= iBMax)) // rules for
																// birth
							bNewVal = ColoringMethod == 1
									? 1
									: (short) (mjb.Cycle
											% (mjb.StatesCount - 1) + 1);
					} else // was alive
					{
						if ((iCnt >= iSMin) && (iCnt <= iSMax)) // rules for
																// surviving
						{
							if (ColoringMethod == 1) // standard
							{
								bNewVal = bOldVal < (mjb.StatesCount - 1)
										? (short) (bOldVal + 1)
										: (short) (mjb.StatesCount - 1);
							} else {
								// alternate coloring - cells remain not changed
							}
						} else
							bNewVal = 0; // isolation or overpopulation
					}
				}
				tmpState[i][j] = bNewVal;
				if (bNewVal != bOldVal) // change detected
				{
					modCnt++; // one more modified cell
				}
			} // for j
		} // for i

		return modCnt;
	}
	// ----------------------------------------------------------------
}