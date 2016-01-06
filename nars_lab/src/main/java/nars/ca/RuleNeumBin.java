package nars.ca;// Mirek's Java Cellebration
// http://www.mirekw.com
//
// Neumann binary rules

public class RuleNeumBin {
	public static final int MAX_STATES = 3;
	public int iClo; // actual count of states
	public int[][][][][] states = new int[MAX_STATES][MAX_STATES][MAX_STATES][MAX_STATES][MAX_STATES];

	// ----------------------------------------------------------------
	public RuleNeumBin() {
		ResetToDefaults();
	}

	// ----------------------------------------------------------------
	// Set default parameters
	public void ResetToDefaults() {
		iClo = 3; // count of states
		for (int i = 0; i < MAX_STATES; i++)
			for (int j = 0; j < MAX_STATES; j++)
				for (int k = 0; k < MAX_STATES; k++)
					for (int l = 0; l < MAX_STATES; l++)
						for (int m = 0; m < MAX_STATES; m++)
							states[i][j][k][l][m] = 0;
	}

	// ----------------------------------------------------------------
	// Parse the rule string
	public void InitFromString(String sStr) {
		int iPos = 0;
		int iStt;
		String sOneChar;
		ResetToDefaults();

		iClo = Integer.valueOf(sStr.substring(iPos, iPos + 1));
		iPos++;
		if (iClo < 2)
			iClo = 2;
		if (iClo > MAX_STATES)
			iClo = MAX_STATES;
		for (int i = 0; i < iClo; i++)
			for (int j = 0; j < iClo; j++)
				for (int k = 0; k < iClo; k++)
					for (int l = 0; l < iClo; l++)
						for (int m = 0; m < iClo; m++) {
							sOneChar = sStr.substring(iPos, iPos + 1);
							iPos++;
							iStt = Integer.valueOf(sOneChar);
							if (iStt < 0)
								iStt = 0;
							if (iStt >= MAX_STATES)
								iStt = MAX_STATES - 1;
							states[i][j][k][l][m] = iStt;
						}

		Validate(); // now correct parameters
	}

	// ----------------------------------------------------------------
	//
	public void InitFromPrm(int i_Clo, int[][][][][] sttAry) {
		iClo = i_Clo;
		states = sttAry;

		Validate(); // now correct parameters
	}

	// ----------------------------------------------------------------
	// Create the Neumann binary rule string
	// Example: '30010202100...'
	public String GetAsString() {
		String sBff;

		// correct parameters first
		Validate();

		// make the string
		sBff = String.valueOf(iClo);
		for (int i = 0; i < iClo; i++)
			for (int j = 0; j < iClo; j++)
				for (int k = 0; k < iClo; k++)
					for (int l = 0; l < iClo; l++)
						for (int m = 0; m < iClo; m++) {
							sBff = sBff + states[i][j][k][l][m];
						}

		return sBff;
	}

	// ----------------------------------------------------------------
	// Check the validity of the Neumann binary rule parameters, correct
	// them if necessary.
	public void Validate() {
		int i, iMax;

		if (iClo < 2)
			iClo = 2;
		else if (iClo > MAX_STATES)
			iClo = MAX_STATES;
	}

	// ----------------------------------------------------------------
	// Perform one pass of the rule
	public int OnePass(int sizX, int sizY, boolean isWrap, int ColoringMethod,
			short[][] crrState, short[][] tmpState) {
		short bOldVal, bNewVal;
		int modCnt = 0;
		int i, j, iCnt;
		int[] lurd = new int[4]; // 0-left, 1-up, 2-right, 3-down
		int[] iCntAry = new int[iClo];
		int iTmp;
		int l, u, r, d;

		for (i = 0; i < sizX; ++i) {
			// determine left and right cells
			lurd[0] = (i > 0) ? i - 1 : (isWrap) ? sizX - 1 : sizX;
			lurd[2] = (i < sizX - 1) ? i + 1 : (isWrap) ? 0 : sizX;

			for (j = 0; j < sizY; ++j) {
				// determine up and down cells
				lurd[1] = (j > 0) ? j - 1 : (isWrap) ? sizY - 1 : sizY;
				lurd[3] = (j < sizY - 1) ? j + 1 : (isWrap) ? 0 : sizY;
				bOldVal = crrState[i][j];
				bOldVal = (short) ((bOldVal < 0) ? 0 : (bOldVal >= iClo)
						? (iClo - 1)
						: bOldVal);

				l = crrState[lurd[0]][j];
				u = crrState[i][lurd[1]];
				r = crrState[lurd[2]][j];
				d = crrState[i][lurd[3]];
				l = (l < 0) ? 0 : (l >= iClo) ? (iClo - 1) : l;
				u = (u < 0) ? 0 : (u >= iClo) ? (iClo - 1) : u;
				r = (r < 0) ? 0 : (r >= iClo) ? (iClo - 1) : r;
				d = (d < 0) ? 0 : (d >= iClo) ? (iClo - 1) : d;
				bNewVal = (short) states[bOldVal][u][r][d][l];

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