package nars.ca;// Mirek's Java Cellebration
// http://www.mirekw.com
//
// User DLL rules

import java.util.StringTokenizer;

public class RuleUser {
	public boolean isHist; // with history?
	public int iClo; // count of states

	private static final int RIDX_RUG = 1; // Rug
	private static final int RIDX_DIB = 2; // Digital Inkblots
	private static final int RIDX_HOD = 3; // Hodge
	private static final int RIDX_GRH = 4; // GreenHast

	private int RuleIdx;
	private int Increment; // used in Rug, Digital Ink., Aurora, etc.

	// ----------------------------------------------------------------
	public RuleUser() {
		ResetToDefaults();
	}

	// ----------------------------------------------------------------
	// Set default parameters
	public void ResetToDefaults() {
		isHist = true; // with history?
		iClo = 16; // count of colors
		RuleIdx = RIDX_HOD; // default rule
		Increment = 3; // increment
	}

	// ----------------------------------------------------------------
	// Parse the rule string
	// Example: RUG,C128,I1
	public void InitFromString(String sStr) {
		// noinspection UseOfStringTokenizer
		StringTokenizer st;
		String sTok;
		int i;
		ResetToDefaults();
		if (sStr.length() < 3)
			return;

		// noinspection IfStatementWithTooManyBranches
		if (sStr.compareTo("Rug") == 0)
			sStr = "RUG,C64,I1";
		else if (sStr.compareTo("Digital_Inkblots") == 0)
			sStr = "DIB,C256,I3";
		else if (sStr.compareTo("Hodge") == 0)
			sStr = "HOD,C32,I5";
		else if (sStr.compareTo("GreenHast") == 0)
			sStr = "GRH";

		// noinspection IfStatementWithTooManyBranches
		if (sStr.startsWith("RUG"))
			RuleIdx = RIDX_RUG;
		else if (sStr.startsWith("DIB"))
			RuleIdx = RIDX_DIB;
		else if (sStr.startsWith("HOD"))
			RuleIdx = RIDX_HOD;
		else if (sStr.startsWith("GRH"))
			RuleIdx = RIDX_GRH;

		st = new StringTokenizer(sStr, " ,", true);
		while (st.hasMoreTokens()) {
			sTok = st.nextToken().toUpperCase();
			if (sTok.length() > 0 && sTok.charAt(0) == 'I')
				Increment = Integer.valueOf(sTok.substring(1));
			else if (sTok.length() > 0 && sTok.charAt(0) == 'C')
				iClo = Integer.valueOf(sTok.substring(1));
		}

		Validate(); // now correct parameters
	}

	// ----------------------------------------------------------------
	// Create the Rules table string
	public String GetAsString() {
		String sBff = "";

		// correct parameters first
		Validate();

		switch (RuleIdx) {
			case RIDX_RUG : // Rug
				sBff = "RUG,C" + iClo;
				sBff = sBff + ",I" + Increment;
				break;
			case RIDX_DIB : // Digital Inkblots
				sBff = "DIB,C" + iClo;
				sBff = sBff + ",I" + Increment;
				break;
			case RIDX_HOD : // Hodge
				sBff = "HOD,C" + iClo;
				sBff = sBff + ",I" + Increment;
				break;
			case RIDX_GRH : // GreenHast
				sBff = "GRH";
				break;
		}

		return sBff;
	}

	// ----------------------------------------------------------------
	// Check the validity of the parameters, correct them if necessary.
	public void Validate() {
		if (iClo < 2)
			iClo = 2;
		else if (iClo > MJBoard.MAX_CLO)
			iClo = MJBoard.MAX_CLO;
	}

	// ----------------------------------------------------------------
	// Perform one pass of the rule
	public int OnePass(int sizX, int sizY, boolean isWrap, int ColoringMethod,
			short[][] crrState, short[][] tmpState) {
		short bOldVal, bNewVal;
		int modCnt = 0;
		int i, j, iCnt;
		int[] lurd = new int[4]; // 0-left, 1-up, 2-right, 3-down

		for (i = 0; i < sizX; ++i) {
			// determine left and right cells
			lurd[0] = (i > 0) ? i - 1 : (isWrap) ? sizX - 1 : sizX;
			lurd[2] = (i < sizX - 1) ? i + 1 : (isWrap) ? 0 : sizX;
			for (j = 0; j < sizY; ++j) {
				// determine up and down cells
				lurd[1] = j > 0 ? j - 1 : (isWrap) ? sizY - 1 : sizY;
				lurd[3] = (j < sizY - 1) ? j + 1 : (isWrap) ? 0 : sizY;
				bOldVal = crrState[i][j];

				bNewVal = bOldVal;
				switch (RuleIdx) {
					case RIDX_RUG : // Rug
						// total of neighbours
						iCnt = crrState[lurd[0]][lurd[1]]
								+ crrState[lurd[0]][j]
								+ crrState[lurd[0]][lurd[3]]
								+ crrState[i][lurd[1]] + crrState[i][lurd[3]]
								+ crrState[lurd[2]][lurd[1]]
								+ crrState[lurd[2]][j]
								+ crrState[lurd[2]][lurd[3]];
						bNewVal = (short) (((iCnt / 8) + Increment) % iClo); // new
																				// cell
																				// status
						break;

					case RIDX_DIB : // Digital Inkblots
						// total of neighbours
						iCnt = crrState[lurd[0]][lurd[1]]
								+ crrState[lurd[0]][j]
								+ crrState[lurd[0]][lurd[3]]
								+ crrState[i][lurd[1]] + crrState[i][j]
								+ crrState[i][lurd[3]]
								+ crrState[lurd[2]][lurd[1]]
								+ crrState[lurd[2]][j]
								+ crrState[lurd[2]][lurd[3]];
						bNewVal = (short) (((iCnt / 9) + Increment) % iClo); // new
																				// cell
																				// status
						break;

					case RIDX_HOD : // Hodge
						int sum8 = crrState[lurd[0]][lurd[1]]
								+ crrState[lurd[0]][j]
								+ crrState[lurd[0]][lurd[3]]
								+ crrState[i][lurd[1]] + crrState[i][lurd[3]]
								+ crrState[lurd[2]][lurd[1]]
								+ crrState[lurd[2]][j]
								+ crrState[lurd[2]][lurd[3]];
						bNewVal = 0;

						// CelLab's version
						if (bOldVal == 0) {
							if (sum8 < Increment) {
								bNewVal = 0;
							} else
								bNewVal = (short) (sum8 < 100 ? 2 : 3);
						} else if ((bOldVal > 0) && (bOldVal < (iClo - 1))) {
							bNewVal = (short) (((sum8 >> 3) + Increment) & 255);
						}

						if (bNewVal > (iClo - 1)) {
							bNewVal = (short) (iClo - 1);
						}

						if (bOldVal == (iClo - 1)) {
							bNewVal = 0;
						}
						break;

					case RIDX_GRH : // GreenHast
						int r = 0,
						d = 0;
						int prevState;

						prevState = (bOldVal >> 2) & 3; // get the previous
														// state
						bOldVal = (short) (bOldVal & 3); // throw the previous
															// state away

						switch (bOldVal) {
							case 0 : // dead
								int i4Sum = 0;
								i4Sum = (((crrState[lurd[0]][j] & 3) == 1)
										? 1
										: 0)
										+ (((crrState[i][lurd[1]] & 3) == 1)
												? 1
												: 0)
										+ (((crrState[i][lurd[3]] & 3) == 1)
												? 1
												: 0)
										+ (((crrState[lurd[2]][j] & 3) == 1)
												? 1
												: 0);
								r = 0;
								d = (i4Sum > 0) ? 1 : 0;
								break;
							case 1 : // alive
								r = 2;
								d = 0;
								break;
							case 2 : // dying
								r = 0;
								d = 0;
								break;
						}
						bNewVal = (short) ((r + d - prevState + 3) % 3 + (bOldVal << 2)); // store
																							// Me
																							// for
																							// next
																							// calls
						break;
				} // switch

				tmpState[i][j] = bNewVal;
				if (bNewVal != bOldVal) {
					modCnt++; // one more modified cell
				}
			}
		}
		return modCnt;
	}
	// ----------------------------------------------------------------
}