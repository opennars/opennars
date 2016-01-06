package nars.ca;// Mirek's Java Cellebration
// http://www.mirekw.com
//
// General binary rules

import java.util.StringTokenizer;

public class RuleGenBin {
	public int iClo; // actual count of states
	public boolean isHist; // with history?
	public int iNgh; // neighborhood type, NGHTYP_MOOR or NGHTYP_NEUM
	public boolean[] rulesS = new boolean[256];
	public boolean[] rulesB = new boolean[256];

	// ----------------------------------------------------------------
	public RuleGenBin() {
		ResetToDefaults();
	}

	// ----------------------------------------------------------------
	// Set default parameters
	public void ResetToDefaults() {
		iClo = 2; // count of states
		isHist = false;
		iNgh = MJRules.NGHTYP_MOOR; // default - Moore neighbourhood
		for (int i = 0; i <= 255; i++) {
			rulesS[i] = rulesB[i] = false;
		}
	}

	// ----------------------------------------------------------------
	// Expand the compressed rule string to a string like 0011101001000
	private String ExpandIt(String sStr) {
		int i, j, iNum;
		String sRetString = "";
		char cChar;
		int iCharVal;

		iNum = 0;
		sStr = sStr.trim();
		for (i = 0; i < sStr.length(); i++) {
			cChar = sStr.charAt(i);
			if (Character.isDigit(cChar)) {
				iCharVal = cChar - '0';
				iNum = iNum * 10 + iCharVal;
			} else {
				if (iNum == 0)
					iNum = 1;
				if ((sStr.charAt(i) == 'a') || (sStr.charAt(i) == 'b')) {
					for (j = 0; j < iNum; j++) {
						sRetString = sRetString
								+ ((sStr.charAt(i) == 'a') ? "0" : "1");
					}
					iNum = 0;
				}
			}
		}
		return sRetString;
	}

	// ----------------------------------------------------------------
	// Parse the rule string
	// Example: "C0,NN,S3babbabbabba3b,B7ab3aba3b"
	public void InitFromString(String sStr) {
		int i, iTmp;
		String sTok;
		// noinspection UseOfStringTokenizer
		StringTokenizer st;

		ResetToDefaults();

		st = new StringTokenizer(sStr, ",", true);
		while (st.hasMoreTokens()) {
			sTok = st.nextToken();
			// noinspection IfStatementWithTooManyBranches
			if (sTok.length() > 0 && sTok.charAt(0) == 'S') // survivals
			{
				sTok = ExpandIt(sTok.substring(1));
				for (i = 0; (i < sTok.length()) && (i < 256); i++)
					rulesS[i] = (sTok.charAt(i) == '1');
			} else if (sTok.length() > 0 && sTok.charAt(0) == 'B') // births
			{
				sTok = ExpandIt(sTok.substring(1));
				for (i = 0; (i < sTok.length()) && (i < 256); i++)
					rulesB[i] = (sTok.charAt(i) == '1');
			} else if (sTok.length() > 0 && sTok.charAt(0) == 'C') {
				i = Integer.valueOf(sTok.substring(1));
				if (i >= 3) {
					isHist = true; // history, get the states count
					iClo = i;
				} else
					isHist = false; // states count is meaningless
			} else if (sTok.startsWith("NM")) // Moore neighbourhood
			{
				iNgh = MJRules.NGHTYP_MOOR;
			} else if (sTok.startsWith("NN")) // von Neumann neighbourhood
			{
				iNgh = MJRules.NGHTYP_NEUM;
			}
		}
		Validate(); // now correct parameters
	}

	// ----------------------------------------------------------------
	// Initialize from separate parameters
	public void InitFromPrm(int i_Clo, boolean is_Hist, int i_Ngh,
			boolean[] rules_S, boolean[] rules_B) {
		iClo = i_Clo; // count of colors
		iNgh = i_Ngh; // neighbourhood
		isHist = is_Hist; // with history?
		rulesS = rules_S;
		rulesB = rules_B;

		Validate(); // now correct parameters
	}

	// ----------------------------------------------------------------
	private String OneToken(int iVal, int iCnt) {
		String sChr, sRetStr;

		sRetStr = "";
		if (iCnt > 0) {
			sChr = iVal == 0 ? "a" : "b";

			if (iCnt == 1)
				sRetStr = sChr;
			else if (iCnt == 2)
				sRetStr = sChr + sChr;
			else
				sRetStr = iCnt + sChr;
		}
		return sRetStr;
	}

	// ----------------------------------------------------------------
	private String CompactIt(String sStr) {
		int i, iCnt, iThis, iLast;
		String sResult = "";

		iLast = -1;
		iCnt = 0;
		for (i = 0; i < sStr.length(); i++) {
			iThis = Integer.valueOf(sStr.substring(i, i + 1));
			if ((iThis != 0) && (iThis != 1))
				iThis = 0;
			if (iThis != iLast) {
				sResult = sResult + OneToken(iLast, iCnt);
				iLast = iThis;
				iCnt = 1;
			} else
				iCnt++;
		}
		return sResult + OneToken(iLast, iCnt);
	}

	// ----------------------------------------------------------------
	// Create the General binary rule string
	// Example: C0,NN,S3babbabbabba3b,B7ab3aba3b
	public String GetAsString() {
		String sBff, sTmp;
		int i, ih, maxIdx;

		// correct parameters first
		Validate();

		// make the string
		// states
		ih = isHist ? iClo : 0;
		sBff = 'C' + String.valueOf(ih);

		// neighbourhood
		if (iNgh == MJRules.NGHTYP_NEUM) // von Neumann neighbourhood
		{
			sBff = sBff + ",NN";
			maxIdx = 15;
		} else // Moore neighbourhood
		{
			sBff = sBff + ",NM";
			maxIdx = 255;
		}

		// survivals
		sTmp = "";
		for (i = 0; i < maxIdx; i++) {
			sTmp = rulesS[i] ? sTmp + '1' : sTmp + '0';
		}
		sBff = sBff + ",S" + CompactIt(sTmp);

		// births
		sTmp = "";
		for (i = 0; i < maxIdx; i++) {
			sTmp = rulesB[i] ? sTmp + '1' : sTmp + '0';
		}
		sBff = sBff + ",B" + CompactIt(sTmp);

		return sBff;
	}

	// ----------------------------------------------------------------
	// Check the validity of the Neumann binary rule parameters, correct
	// them if necessary.
	public void Validate() {
		if (iClo < 2)
			iClo = 2;
		else if (iClo > MJBoard.MAX_CLO)
			iClo = MJBoard.MAX_CLO;

		if ((iNgh != MJRules.NGHTYP_MOOR) && (iNgh != MJRules.NGHTYP_NEUM))
			iNgh = MJRules.NGHTYP_MOOR; // default - Moore neighbourhood
	}

	// ----------------------------------------------------------------
	// Perform one pass of the rule
	public int OnePass(int sizX, int sizY, boolean isWrap, int ColoringMethod,
			short[][] crrState, short[][] tmpState, MJBoard mjb) {
		int modCnt = 0;
		int i, j, iCnt;
		short bOldVal, bNewVal; // old and new value of the cell
		int[] lurd = new int[4]; // 0-left, 1-up, 2-right, 3-down

		for (i = 0; i < sizX; ++i) {
			// determine left and right cells
			lurd[0] = (i > 0) ? i - 1 : (isWrap) ? sizX - 1 : sizX;
			lurd[2] = (i < sizX - 1) ? i + 1 : (isWrap) ? 0 : sizX;
			for (j = 0; j < sizY; ++j) {
				// determine up and down cells
				lurd[1] = (j > 0) ? j - 1 : (isWrap) ? sizY - 1 : sizY;
				lurd[3] = (j < sizY - 1) ? j + 1 : (isWrap) ? 0 : sizY;
				bOldVal = crrState[i][j];
				bNewVal = bOldVal; // default - no change

				iCnt = 0; // count of neighbours
				if (isHist) // with history
				{
					if (bOldVal <= 1) // can survive or be born
					{
						if (iNgh == MJRules.NGHTYP_MOOR) {
							if (crrState[i][lurd[1]] == 1)
								iCnt += 1;
							if (crrState[lurd[2]][lurd[1]] == 1)
								iCnt += 2;
							if (crrState[lurd[2]][j] == 1)
								iCnt += 4;
							if (crrState[lurd[2]][lurd[3]] == 1)
								iCnt += 8;
							if (crrState[i][lurd[3]] == 1)
								iCnt += 16;
							if (crrState[lurd[0]][lurd[3]] == 1)
								iCnt += 32;
							if (crrState[lurd[0]][j] == 1)
								iCnt += 64;
							if (crrState[lurd[0]][lurd[1]] == 1)
								iCnt += 128;
						} else {
							if (crrState[i][lurd[1]] == 1)
								iCnt += 1;
							if (crrState[lurd[2]][j] == 1)
								iCnt += 2;
							if (crrState[i][lurd[3]] == 1)
								iCnt += 4;
							if (crrState[lurd[0]][j] == 1)
								iCnt += 8;
						}

						// determine the cell status
						if (bOldVal == 0) // was dead
						{
							if (rulesB[iCnt]) // rules for birth
								bNewVal = 1; // birth
						} else // was 1 - alive
						{
							if (rulesS[iCnt]) // in rules for surviving
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
					if (iNgh == MJRules.NGHTYP_MOOR) {
						if (crrState[i][lurd[1]] != 0)
							iCnt += 1;
						if (crrState[lurd[2]][lurd[1]] != 0)
							iCnt += 2;
						if (crrState[lurd[2]][j] != 0)
							iCnt += 4;
						if (crrState[lurd[2]][lurd[3]] != 0)
							iCnt += 8;
						if (crrState[i][lurd[3]] != 0)
							iCnt += 16;
						if (crrState[lurd[0]][lurd[3]] != 0)
							iCnt += 32;
						if (crrState[lurd[0]][j] != 0)
							iCnt += 64;
						if (crrState[lurd[0]][lurd[1]] != 0)
							iCnt += 128;
					} else {
						if (crrState[i][lurd[1]] != 0)
							iCnt += 1;
						if (crrState[lurd[2]][j] != 0)
							iCnt += 2;
						if (crrState[i][lurd[3]] != 0)
							iCnt += 4;
						if (crrState[lurd[0]][j] != 0)
							iCnt += 8;
					}

					// determine the cell status
					if (bOldVal == 0) // was dead
					{
						if (rulesB[iCnt]) // rules for birth
							bNewVal = ColoringMethod == 1
									? 1
									: (short) (mjb.Cycle
											% (mjb.StatesCount - 1) + 1);
					} else // was alive
					{
						if (rulesS[iCnt]) // rules for surviving
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
				if (bNewVal != bOldVal) {
					modCnt++; // one more modified cell
				}
			}
			// for j
		}
		// for i

		return modCnt;
	}
	// ----------------------------------------------------------------
}