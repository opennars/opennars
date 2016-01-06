package nars.ca;// Mirek's Java Cellebration
// http://www.mirekw.com
//
// Weighted Life

import java.util.StringTokenizer;

public class RuleWLife {
	public static final int IMAXWLIFEVAL = 256;
	public static final int IMAXWLIFERUL = 8 * IMAXWLIFEVAL;
	public int iClo; // count of states
	public final int[] wgtAry = new int[10]; // weights of neighbours
	public final boolean[] rulesS = new boolean[IMAXWLIFERUL + 1]; // rules for
																	// surviving
	public final boolean[] rulesB = new boolean[IMAXWLIFERUL + 1]; // rules for
																	// birth
	public boolean isHist; // with history?

	// ----------------------------------------------------------------
	public RuleWLife() {
		ResetToDefaults();
	}

	// ----------------------------------------------------------------
	// Set default parameters
	public void ResetToDefaults() {
		int i;

		for (i = 1; i <= 9; i++)
			wgtAry[i] = 1;
		wgtAry[5] = 0; // me
		isHist = true; // with history?

		for (i = 0; i < IMAXWLIFERUL; i++) {
			rulesS[i] = false;
			rulesB[i] = false;
		}
	}

	// ----------------------------------------------------------------
	// Parse the rule string
	// Example: #RULE NW0,NN1,NE0,WW1,ME0,EE1,SW0,SS1,SE0,HI7,RS2,RB1,RB2,RB3
	public void InitFromString(String sStr) {
		// noinspection UseOfStringTokenizer
		StringTokenizer st;
		String sTok;
		int i;
		ResetToDefaults();

		st = new StringTokenizer(sStr, " ,", true);
		while (st.hasMoreTokens()) {
			sTok = st.nextToken().toUpperCase();
			// System.out.println(sTok);
			// noinspection IfStatementWithTooManyBranches
			if (sTok.startsWith("NW"))
				wgtAry[1] = Integer.valueOf(sTok.substring(2));
			else if (sTok.startsWith("NN"))
				wgtAry[2] = Integer.valueOf(sTok.substring(2));
			else if (sTok.startsWith("NE"))
				wgtAry[3] = Integer.valueOf(sTok.substring(2));

			else if (sTok.startsWith("WW"))
				wgtAry[4] = Integer.valueOf(sTok.substring(2));
			else if (sTok.startsWith("ME"))
				wgtAry[5] = Integer.valueOf(sTok.substring(2));
			else if (sTok.startsWith("EE"))
				wgtAry[6] = Integer.valueOf(sTok.substring(2));

			else if (sTok.startsWith("SW"))
				wgtAry[7] = Integer.valueOf(sTok.substring(2));
			else if (sTok.startsWith("SS"))
				wgtAry[8] = Integer.valueOf(sTok.substring(2));
			else if (sTok.startsWith("SE"))
				wgtAry[9] = Integer.valueOf(sTok.substring(2));

			else if (sTok.startsWith("HI")) {
				i = Integer.valueOf(sTok.substring(2));
				if (i >= 3) {
					isHist = true; // history, get the states count
					iClo = i;
				} else
					isHist = false; // states count is meaningless
			} else if (sTok.startsWith("RS")) // survival
			{
				i = Integer.valueOf(sTok.substring(2));
				if ((i >= 0) && (i <= IMAXWLIFERUL))
					rulesS[i] = true;
			} else if (sTok.startsWith("RB")) // birth
			{
				i = Integer.valueOf(sTok.substring(2));
				if ((i > 0) && (i <= IMAXWLIFERUL))
					rulesB[i] = true;
			}
		}

		Validate(); // now correct parameters
	}

	// ----------------------------------------------------------------
	// Create the Rules table string
	public String GetAsString() {
		String sBff = "";
		int i, ih;

		// correct parameters first
		Validate();

		ih = isHist ? iClo : 0;

		sBff = "NW" + wgtAry[1] + ",NN" + wgtAry[2] + ",NE" + wgtAry[3] + ",WW"
				+ wgtAry[4] + ",ME" + wgtAry[5] + ",EE" + wgtAry[6] + ",SW"
				+ wgtAry[7] + ",SS" + wgtAry[8] + ",SE" + wgtAry[9] + ",HI"
				+ ih;

		for (i = 0; i < IMAXWLIFERUL; i++)
			if (rulesS[i])
				sBff = sBff + ",RS" + i;

		for (i = 0; i < IMAXWLIFERUL; i++)
			if (rulesB[i])
				sBff = sBff + ",RB" + i;

		return sBff;
	}

	// ----------------------------------------------------------------
	// Check the validity of the Rules table parameters, correct
	// them if necessary.
	public void Validate() {
		if (iClo < 2)
			iClo = 2;
		else if (iClo > MJBoard.MAX_CLO)
			iClo = MJBoard.MAX_CLO;
	}

	// ----------------------------------------------------------------
	// Perform one pass of the rule
	public int OnePass(int sizX, int sizY, boolean isWrap, int ColoringMethod,
			short[][] crrState, short[][] tmpState, MJBoard mjb) {
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
				lurd[1] = (j > 0) ? j - 1 : (isWrap) ? sizY - 1 : sizY;
				lurd[3] = (j < sizY - 1) ? j + 1 : (isWrap) ? 0 : sizY;
				bOldVal = crrState[i][j];
				bNewVal = bOldVal; // default - no change

				iCnt = 0; // count of neighbours
				if (isHist) // with history
				{
					if (bOldVal <= 1) // can survive or born
					{
						if (crrState[lurd[0]][lurd[1]] == 1)
							iCnt += wgtAry[1];
						if (crrState[i][lurd[1]] == 1)
							iCnt += wgtAry[2];
						if (crrState[lurd[2]][lurd[1]] == 1)
							iCnt += wgtAry[3];
						if (crrState[lurd[0]][j] == 1)
							iCnt += wgtAry[4];
						if (crrState[i][j] == 1)
							iCnt += wgtAry[5];
						if (crrState[lurd[2]][j] == 1)
							iCnt += wgtAry[6];
						if (crrState[lurd[0]][lurd[3]] == 1)
							iCnt += wgtAry[7];
						if (crrState[i][lurd[3]] == 1)
							iCnt += wgtAry[8];
						if (crrState[lurd[2]][lurd[3]] == 1)
							iCnt += wgtAry[9];
						if (iCnt < 0)
							iCnt = 0; // safety check

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
					if (crrState[lurd[0]][lurd[1]] != 0)
						iCnt += wgtAry[1];
					if (crrState[i][lurd[1]] != 0)
						iCnt += wgtAry[2];
					if (crrState[lurd[2]][lurd[1]] != 0)
						iCnt += wgtAry[3];
					if (crrState[lurd[0]][j] != 0)
						iCnt += wgtAry[4];
					if (crrState[i][j] != 0)
						iCnt += wgtAry[5];
					if (crrState[lurd[2]][j] != 0)
						iCnt += wgtAry[6];
					if (crrState[lurd[0]][lurd[3]] != 0)
						iCnt += wgtAry[7];
					if (crrState[i][lurd[3]] != 0)
						iCnt += wgtAry[8];
					if (crrState[lurd[2]][lurd[3]] != 0)
						iCnt += wgtAry[9];
					if (iCnt < 0)
						iCnt = 0; // safety check

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
			} // closes the main for j loop
		} // closes the main for i loop

		return modCnt;
	}
	// ----------------------------------------------------------------
}