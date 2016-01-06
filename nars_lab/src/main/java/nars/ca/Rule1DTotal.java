package nars.ca;// Mirek's Java Cellebration
// http://www.mirekw.com
//
// 1D totalistic rules

import java.util.StringTokenizer;

public class Rule1DTotal {
	public static final int MAX_RANGE = 10;

	public boolean isHist; // with history?
	public boolean isCentr; // use the center (middle) cell?
	public int iClo; // count of states
	public int iRng; // range, 1..10
	public boolean[] rulesS = new boolean[MAX_RANGE * 2 + 2]; // rules for
																// surviving
	public boolean[] rulesB = new boolean[MAX_RANGE * 2 + 2]; // rules for birth

	// ----------------------------------------------------------------
	public Rule1DTotal() {
		ResetToDefaults();
	}

	// ----------------------------------------------------------------
	// Set default parameters
	public void ResetToDefaults() {
		int i;
		isHist = false; // no history
		iClo = 2; // count of states
		isCentr = true; // use the center cell
		iRng = 2; // range, 1..10
		for (i = 0; i <= MAX_RANGE * 2 + 1; i++) {
			rulesS[i] = false; // rules for surviving
			rulesB[i] = false; // rules for birth
		}
	}

	// ----------------------------------------------------------------
	// Parse the rule string
	// Example: "R6,C25,M1,S1,S4,S7,S8,B0,B3,B5"
	public void InitFromString(String sStr) {
		// noinspection UseOfStringTokenizer
		StringTokenizer st;
		String sTok;
		int iTmp;
		ResetToDefaults();

		st = new StringTokenizer(sStr, ",", true);
		while (st.hasMoreTokens()) {
			sTok = st.nextToken().toUpperCase();
			// System.out.println(sTok);
			// noinspection IfStatementWithTooManyBranches
			if (sTok.length() > 0 && sTok.charAt(0) == 'R')
				iRng = Integer.valueOf(sTok.substring(1));
			else if (sTok.length() > 0 && sTok.charAt(0) == 'C') {
				iTmp = Integer.valueOf(sTok.substring(1));
				if (iTmp >= 3) {
					isHist = true; // history, get the states count
					iClo = iTmp;
				} else
					isHist = false; // states count is meaningless
			} else if (sTok.length() > 0 && sTok.charAt(0) == 'M') // center
																	// cell?
			{
				isCentr = (Integer.valueOf(sTok.substring(1)) > 0);
			} else if (sTok.length() > 0 && sTok.charAt(0) == 'S') // surviving
																	// rules
			{
				iTmp = Integer.valueOf(sTok.substring(1));
				if ((iTmp >= 0) && (iTmp <= MAX_RANGE * 2 + 1)) {
					rulesS[iTmp] = true;
				}
			} else if (sTok.length() > 0 && sTok.charAt(0) == 'B') // birth
																	// rules
			{
				iTmp = Integer.valueOf(sTok.substring(1));
				if ((iTmp >= 0) && (iTmp <= MAX_RANGE * 2 + 1)) {
					rulesB[iTmp] = true;
				}
			}
			if (!isHist)
				iClo = 8;
			Validate(); // now correct parameters
		}
	}

	// ----------------------------------------------------------------
	//
	public void InitFromPrm(int i_Clo, int i_Rng, boolean is_Hist,
			boolean is_Centr, boolean[] rules_S, boolean[] rules_B) {
		isHist = is_Hist; // history
		iClo = i_Clo; // states
		isCentr = is_Centr; // use the center (middle) cell
		iRng = i_Rng; // range, 1..10
		rulesS = rules_S; // surviving rules
		rulesB = rules_B; // birth rules

		Validate(); // now correct parameters
	}

	// ----------------------------------------------------------------
	// Create the rule string
	// Example: "R6,C25,M1,S1,S4,S7,S8,B0,B3,B5"
	public String GetAsString() {
		String sBff;
		int i, ih;

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
		for (i = 0; i <= MAX_RANGE * 2 + 1; i++)
			if (rulesS[i])
				sBff = sBff + ",S" + i;

		// B rules
		for (i = 0; i <= MAX_RANGE * 2 + 1; i++)
			if (rulesB[i])
				sBff = sBff + ",B" + i;

		return sBff;
	}

	// ----------------------------------------------------------------
	// Check the validity of the Cyclic CA parameters, correct
	// them if necessary.
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
	}

	// ----------------------------------------------------------------
	// Perform one pass of the rule
	public int OnePass(int sizX, int sizY, boolean isWrap, int ColoringMethod,
			short[][] crrState, short[][] tmpState, MJBoard mjb) {
		short bOldVal, bNewVal;
		int modCnt = 0;
		int i, iCnt;
		short[] OneRow;
		int[] xVector = new int[21]; // 0..9, 10, 11..20
		int ary1DOfs; // margins, used for wrapping
		int ic;

		ary1DOfs = iRng;
		OneRow = new short[sizX + 1 + 2 * ary1DOfs];
		int i1DNextRow; // next row
		// the next row
		i1DNextRow = mjb.i1DLastRow + 1;
		if (i1DNextRow >= sizY)
			i1DNextRow = 0;

		for (ic = 0; ic < sizX; ic++)
			OneRow[ic + ary1DOfs] = crrState[ic][mjb.i1DLastRow]; // original
																	// line
		if (isWrap) {
			for (ic = 1; ic <= ary1DOfs; ic++) {
				OneRow[ary1DOfs - ic] = OneRow[sizX - 1 - ic + 1];
				OneRow[sizX - 1 + ic] = OneRow[ary1DOfs + ic - 1];
			}
		}

		for (ic = 0; ic < sizX; ic++) // for the whole row
		{
			bOldVal = OneRow[ic + ary1DOfs];
			iCnt = 0; // count of neighbours
			if (isHist) // with history
			{
				if (bOldVal <= 1) // can survive or be born
				{
					if (isCentr) // the center cell
						if (OneRow[ic + ary1DOfs] == 1)
							iCnt++;
					for (i = 1; i <= iRng; i++) // neighbours
					{
						if (OneRow[ic - i + ary1DOfs] == 1)
							iCnt++;
						if (OneRow[ic + i + ary1DOfs] == 1)
							iCnt++;
					}

					bNewVal = bOldVal; // default - no change

					// determine the cell status
					if (bOldVal == 0) // was dead
					{
						if (rulesB[iCnt]) // in rules for birth
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
					bNewVal = bOldVal < (iClo - 1) ? (short) (bOldVal + 1) : 0;
				}
			} else // no history
			{
				if (isCentr) // the center cell
					if (OneRow[ic + ary1DOfs] > 0)
						iCnt++;
				for (i = 1; i <= iRng; i++) // neighbours
				{
					if (OneRow[ic - i + ary1DOfs] > 0)
						iCnt++;
					if (OneRow[ic + i + ary1DOfs] > 0)
						iCnt++;
				}

				bNewVal = bOldVal; // default - no change

				// determine the cell status
				if (bOldVal == 0) // was dead
				{
					if (rulesB[iCnt]) // rules for birth
						bNewVal = ColoringMethod == 1 ? 1 : (short) (mjb.Cycle
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
			tmpState[ic][i1DNextRow] = bNewVal;
		} // for

		modCnt = 1; // run forever
		mjb.i1DLastRow = i1DNextRow; // Done. Advance the last generated row

		return modCnt;
	}
}
