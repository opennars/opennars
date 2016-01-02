package nars.ca;// Mirek's Java Cellebration
// http://www.mirekw.com
//
// Cyclic CA rules

import java.util.StringTokenizer;

public class RuleCyclic {
	public int iClo; // count of states
	public int iRng; // range
	public int iThr; // threshold
	public int iNgh; // neighbourhood type
	public boolean fGH; // Greenberg-Hastings Model?

	public static final int MAX_RANGE = 10;

	// ----------------------------------------------------------------
	public RuleCyclic() {
		ResetToDefaults();
	}

	// ----------------------------------------------------------------
	// Set default parameters
	public void ResetToDefaults() {
		iClo = 3; // count of states
		iRng = 1; // range
		iThr = 3; // threshold
		iNgh = MJRules.NGHTYP_MOOR; // neighbourhood type
		fGH = false; // Greenberg-Hastings Model?
	}

	// ----------------------------------------------------------------
	// Parse the rule string
	@SuppressWarnings("HardcodedFileSeparator")
	public void InitFromString(String sStr) {
		//noinspection UseOfStringTokenizer
		StringTokenizer st;
		String sTok;
		ResetToDefaults();

		st = new StringTokenizer(sStr, ",/", true);
		while (st.hasMoreTokens()) {
			sTok = st.nextToken().toUpperCase();
			//noinspection IfStatementWithTooManyBranches
			if (sTok.length() > 0 && sTok.charAt(0) == 'R')
				iRng = Integer.valueOf(sTok.substring(1));
			else if (sTok.length() > 0 && sTok.charAt(0) == 'T')
				iThr = Integer.valueOf(sTok.substring(1));
			else if (sTok.length() > 0 && sTok.charAt(0) == 'C')
				iClo = Integer.valueOf(sTok.substring(1));
			else if (sTok.startsWith("NM"))
				iNgh = MJRules.NGHTYP_MOOR;
			else if (sTok.startsWith("NN"))
				iNgh = MJRules.NGHTYP_NEUM;
			else if (sTok.startsWith("GH"))
				fGH = true;
		}
		Validate(); // now correct parameters
	}

	// ----------------------------------------------------------------
	//
	public void InitFromPrm(int i_Clo, int i_Rng, int i_Thr, int i_Ngh,
			boolean f_GH) {
		iClo = i_Clo;
		iRng = i_Rng;
		iThr = i_Thr;
		iNgh = i_Ngh;
		fGH = f_GH;

		Validate(); // now correct parameters
	}

	// ----------------------------------------------------------------
	// Create the Cyclic CA table string
	// Example: 'R1/T3/C5/NM'
	@SuppressWarnings("HardcodedFileSeparator")
	public String GetAsString() {
		String sBff;

		// correct parameters first
		Validate();

		// make the string
		sBff = 'R' + String.valueOf(iRng) + "/T" + iThr + "/C"
				+ iClo;

		sBff = iNgh == MJRules.NGHTYP_NEUM ? sBff + "/NN" : sBff + "/NM";

		if (fGH)
			sBff = sBff + "/GH"; // Greenberg-Hastings Model

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

		iMax = 0;
		for (i = 1; i <= iRng; i++)
			// calculate the max. threshold
			iMax = iMax + i * 8;

		if (iThr < 1)
			iThr = 1;
		else if (iThr > iMax)
			iThr = iMax;

		if (iNgh != MJRules.NGHTYP_NEUM)
			iNgh = MJRules.NGHTYP_MOOR; // default - Moore neighbourhood
	}

	// ----------------------------------------------------------------
	// Perform one pass of the rule
	public int OnePass(int sizX, int sizY, boolean isWrap, int ColoringMethod,
					   short[][] crrState, short[][] tmpState) {
		short bOldVal, bNewVal;
		int modCnt = 0;
		int i, j, iCnt;
		int[] xVector = new int[21]; // 0..9, 10, 11..20
		int[] yVector = new int[21]; // 0..9, 10, 11..20
		int colL, colR, rowT, rowB;
		int ic, ir, iTmp;
		short nxtStt;
		boolean fMoore; // Moore neighbourhood? Else von Neumann.

		fMoore = (iNgh == MJRules.NGHTYP_MOOR); // Moore neighbourhood? Else von Neumann.

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
				nxtStt = bOldVal >= (iClo - 1) ? 0 : (short) (bOldVal + 1);

				if ((!fGH) || (bOldVal == 0)) {
					bNewVal = bOldVal; // default - no change
					if (bNewVal >= iClo)
						bNewVal = (short) (iClo - 1);

					iCnt = 0; // count of neighbours with the next state
					for (ic = 10 - iRng; ic <= 10 + iRng; ic++) {
						for (ir = 10 - iRng; ir <= 10 + iRng; ir++) {
							if ((fMoore)
									|| ((Math.abs(ic - 10) + Math.abs(ir - 10)) <= iRng)) {
								if (crrState[xVector[ic]][yVector[ir]] == nxtStt) {
									iCnt++;
								}
							}
						}
					}
					if (iCnt >= iThr)
						bNewVal = nxtStt; // new cell status
				} else {
					bNewVal = nxtStt; // in GH all > 0 automatically advance
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
}