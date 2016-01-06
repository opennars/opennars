package nars.ca;// Mirek's Java Cellebration
// http://www.mirekw.com
//
// Margolus rules

import java.util.StringTokenizer;

public class RuleMarg {
	public static final int TYPE_MS = 1; // Simple Margolus
	public int iClo; // count of states, not used yet
	public int iTyp; // Margolus rule type, 1-simple
	public boolean isHist; // with history? not used yet
	public int[] swapArray = new int[16];

	// ----------------------------------------------------------------
	public RuleMarg() {
		ResetToDefaults();
	}

	// ----------------------------------------------------------------
	// Set default parameters
	public void ResetToDefaults() {
		iClo = 2; // count of states
		isHist = true;
		iTyp = TYPE_MS; // simple
		for (int i = 0; i <= 15; i++)
			swapArray[i] = i;
	}

	// ----------------------------------------------------------------
	// Parse the rule string
	// Example: "MS,D0;8;4;3;2;5;9;7;1;6;10;11;12;13;14;15"
	public void InitFromString(String sStr) {
		String sTok, sSwaps;
		int i, iNum, iVal;
		// noinspection UseOfStringTokenizer
		StringTokenizer st, std;

		ResetToDefaults();

		st = new StringTokenizer(sStr, ",", true);
		while (st.hasMoreTokens()) {
			sTok = st.nextToken();
			if (sTok.length() > 0 && sTok.charAt(0) == 'M') // Margholus rule
															// type
			{
				iTyp = TYPE_MS; // simple - the only one available now
			} else if (sTok.length() > 0 && sTok.charAt(0) == 'D') // definition
			{
				std = new StringTokenizer(sTok.substring(1), ";", false);
				iNum = 0;
				while (std.hasMoreTokens() && (iNum <= 15)) {
					sSwaps = std.nextToken();
					iVal = Integer.valueOf(sSwaps);
					if ((iVal >= 0) && (iVal <= 15))
						swapArray[iNum] = iVal;
					iNum++;
				}
			}
		}

		Validate(); // now correct parameters
	}

	// ----------------------------------------------------------------
	// Initialize from separate parameters
	public void InitFromPrm(int i_Clo, boolean is_Hist, int[] ary) {
		ResetToDefaults();
		iClo = i_Clo; // count of colors
		isHist = is_Hist; // with history?
		swapArray = ary;
		Validate(); // now correct parameters
	}

	// ----------------------------------------------------------------
	// Create the rule string
	// Example: 'MS,D0;8;4;3;2;5;9;7;1;6;10;11;12;13;14;15'
	public String GetAsString() {
		// correct parameters first
		Validate();

		// make the string
		String sBff = "MS,D";

		for (int i = 0; i <= 14; i++)
			sBff = sBff + swapArray[i] + ';';
		sBff = sBff + swapArray[15];

		return sBff;
	}

	// ----------------------------------------------------------------
	// Check the validity of the parameters, correct
	// them if necessary.
	public void Validate() {
		if (iClo < 2)
			iClo = 2;
		else if (iClo > MJBoard.MAX_CLO)
			iClo = MJBoard.MAX_CLO;

		for (int i = 0; i <= 15; i++)
			if ((swapArray[i] < 0) || (swapArray[i] > 15))
				swapArray[i] = i;
	}

	// ----------------------------------------------------------------
	// swap four cells according to the rule
	private void SwapMargCells(int[] mgCells) {
		int iCnt, iNewCnt;

		// if at least 1 cell is > 1 than the location is locked
		if ((mgCells[0] < 2) && (mgCells[1] < 2) && (mgCells[2] < 2)
				&& (mgCells[3] < 2)) {
			iCnt = 0;
			if (mgCells[0] > 0)
				iCnt += 1;
			if (mgCells[1] > 0)
				iCnt += 2;
			if (mgCells[2] > 0)
				iCnt += 4;
			if (mgCells[3] > 0)
				iCnt += 8;
			iNewCnt = swapArray[iCnt];

			mgCells[0] = (1 & iNewCnt) > 0 ? 1 : 0;
			mgCells[1] = (2 & iNewCnt) > 0 ? 1 : 0;
			mgCells[2] = (4 & iNewCnt) > 0 ? 1 : 0;
			mgCells[3] = (8 & iNewCnt) > 0 ? 1 : 0;
		}
	}

	// ----------------------------------------------------------------
	// Perform one pass of the rule
	public int OnePass(int sizX, int sizY, boolean isWrap, int ColoringMethod,
			short[][] crrState, short[][] tmpState, MJBoard mjb) {
		int modCnt = 0;
		int i, j, ic;
		int c1, c2, r1, r2;
		int[] mgCells = new int[4]; // Margolus neighbourhood 2x2 block
		int[] mgCellsOld = new int[4]; // a copy for changes detection
		boolean isOdd; // odd pass?

		isOdd = ((mjb.Cycle % 2) != 0);
		i = 0;
		if (isOdd)
			i--; // odd pass, shift blocks left
		while (i < sizX) {
			c1 = i;
			if (c1 < 0)
				c1 = (isWrap) ? sizX - 1 : sizX; // wrapping
			c2 = i + 1;
			if (c2 >= sizX)
				c2 = (isWrap) ? 0 : sizX;
			j = 0;
			if (isOdd)
				j--; // odd pass, shift blocks up
			while (j < sizY) {
				r1 = j;
				if (r1 < 0)
					r1 = (isWrap) ? sizY - 1 : sizY; // wrapping
				r2 = j + 1;
				if (r2 >= sizY)
					r2 = (isWrap) ? 0 : sizY;
				mgCellsOld[0] = mgCells[0] = tmpState[c1][r1] = crrState[c1][r1]; // ul
				mgCellsOld[1] = mgCells[1] = tmpState[c2][r1] = crrState[c2][r1]; // ur
				mgCellsOld[2] = mgCells[2] = tmpState[c1][r2] = crrState[c1][r2]; // ll
				mgCellsOld[3] = mgCells[3] = tmpState[c2][r2] = crrState[c2][r2]; // lr

				if ((mgCells[0] + mgCells[1] + mgCells[2] + mgCells[3] > 0)
						|| (swapArray[0] > 0)) {
					SwapMargCells(mgCells); // apply the rule

					for (ic = 0; ic <= 3; ic++) // check if any of 4 cells was
												// modified
					{
						if (mgCellsOld[ic] != mgCells[ic]) // change detected
						{
							modCnt++; // one more changed cell
							switch (ic) {
								case 0 :
									tmpState[c1][r1] = (short) mgCells[ic];
									break; // ul
								case 1 :
									tmpState[c2][r1] = (short) mgCells[ic];
									break; // ur
								case 2 :
									tmpState[c1][r2] = (short) mgCells[ic];
									break; // ll
								case 3 :
									tmpState[c2][r2] = (short) mgCells[ic];
									break; // lr
							}
						}
					}
				}
				j += 2;
			}
			i += 2;
		}
		if ((modCnt == 0) && (mjb.Population > 0))
			modCnt = 1;

		return modCnt;
	}
	// ----------------------------------------------------------------
}