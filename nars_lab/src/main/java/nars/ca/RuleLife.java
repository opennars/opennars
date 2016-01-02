package nars.ca;// Mirek's Java Cellebration
// http://www.mirekw.com
//
// Life rules

import java.util.StringTokenizer;

public class RuleLife {
	private final boolean[] RulesS = new boolean[9]; // rules for surviving
	private final boolean[] RulesB = new boolean[9]; // rules for birth

	// ----------------------------------------------------------------
	public RuleLife() {
		ResetToDefaults();
	}

	// ----------------------------------------------------------------
	// Set default parameters
	public void ResetToDefaults() {
		for (int i = 0; i <= 8; i++) {
			RulesS[i] = false;
			RulesB[i] = false;
		}
	}

	// ----------------------------------------------------------------
	// Parse the rule string
	// Example: '23/3'
	@SuppressWarnings("HardcodedFileSeparator")
	public void InitFromString(String sStr) {
		//noinspection UseOfStringTokenizer
		StringTokenizer st;
		String sTok;
		int i, iNum = 1;
		char cChar;
		int iCharVal;
		ResetToDefaults();

		st = new StringTokenizer(sStr, ",/", true);
		while (st.hasMoreTokens()) {
			sTok = st.nextToken();
			if ((sTok.compareTo("/") == 0) || (sTok.compareTo(",") == 0)) {
				iNum++;
				continue;
			}

			for (i = 0; i < sTok.length(); i++) {
				cChar = sTok.charAt(i);
				if (Character.isDigit(cChar)) {
					iCharVal = cChar - '0';
					if ((iCharVal >= 0) && (iCharVal <= 8)) {
						//noinspection IfStatementWithTooManyBranches
						if ((sTok.charAt(0) == 'S') || (sTok.charAt(0) == 's'))
							RulesS[iCharVal] = true;
						else if ((sTok.charAt(0) == 'B')
								|| (sTok.charAt(0) == 'b'))
							RulesB[iCharVal] = true;
						else if (iNum == 1)
							RulesS[iCharVal] = true;
						else
							RulesB[iCharVal] = true;
					}
				}
			}
		}

		Validate(); // now correct parameters
	}

	// ----------------------------------------------------------------
	//
	public void InitFromPrm(boolean[] rulS, boolean[] rulB) {
		for (int i = 0; i <= 8; i++) {
			RulesS[i] = rulS[i];
			RulesB[i] = rulB[i];
		}
		Validate(); // now correct parameters
	}

	// ----------------------------------------------------------------
	// Create the rule string
	// Example: '23/3'
	@SuppressWarnings("HardcodedFileSeparator")
	public String GetAsString() {
		String sBff = "";
		int i;

		// correct parameters first
		Validate();

		// make the string
		for (i = 0; i <= 8; i++)
			// S
			if (RulesS[i])
				sBff = sBff + i;
		sBff = sBff + '/';

		for (i = 0; i <= 8; i++)
			// B
			if (RulesB[i])
				sBff = sBff + i;

		return sBff;
	}

	// ----------------------------------------------------------------
	// Check the validity of the parameters, correct
	// them if necessary.
	public void Validate() {
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
				bNewVal = bOldVal;
				iCnt = 0;
				if (crrState[lurd[0]][lurd[1]] != 0)
					++iCnt;
				if (crrState[i][lurd[1]] != 0)
					++iCnt;
				if (crrState[lurd[2]][lurd[1]] != 0)
					++iCnt;
				if (crrState[lurd[0]][j] != 0)
					++iCnt;
				if (crrState[lurd[2]][j] != 0)
					++iCnt;
				if (crrState[lurd[0]][lurd[3]] != 0)
					++iCnt;
				if (crrState[i][lurd[3]] != 0)
					++iCnt;
				if (crrState[lurd[2]][lurd[3]] != 0)
					++iCnt;

				// determine the cell status
				if (bOldVal == 0) // was dead
				{
					if (RulesB[iCnt]) // rules for birth
						bNewVal = ColoringMethod == 1 ? 1 : (short) (mjb.Cycle
								% (mjb.StatesCount - 1) + 1);
				} else // was alive
				{
					if (RulesS[iCnt]) // rules for surviving
					{
						if (ColoringMethod == 1) // standard
						{
							bNewVal = bOldVal < (mjb.StatesCount - 1) ? (short) (bOldVal + 1) : (short) (mjb.StatesCount - 1);
						} else {
							// alternate coloring - cells remain not changed
						}
					} else
						bNewVal = 0; // isolation or overpopulation
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