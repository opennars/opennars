package nars.ca;// Mirek's Java Cellebration
// http://www.mirekw.com
//
// 1D binary rules

import java.util.StringTokenizer;

public class Rule1DBin {
	public static final int MAX_RANGE = 4;

	public final byte[] iAry = new byte[512]; // the rule
	public String sHex; // Wolfram's code
	public int iRng; // range, 1..4

	// ----------------------------------------------------------------
	public Rule1DBin() {
		ResetToDefaults();
		SetArray(); // prepare the rule array
	}

	// ----------------------------------------------------------------
	// Set default parameters
	public void ResetToDefaults() {
		iRng = 1;
		sHex = "6E";
	}

	// ----------------------------------------------------------------
	// Parse the rule string
	// Example: "R2,W23AC2"
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
			if (sTok.length() > 0 && sTok.charAt(0) == 'R') // range
			{
				iRng = Integer.valueOf(sTok.substring(1));
			} else if (sTok.length() > 0 && sTok.charAt(0) == 'W') // Wolfram's
																	// code
			{
				sHex = sTok.substring(1);
			}
		}
		// done
		SetArray(); // prepare the rule array
	}

	// ----------------------------------------------------------------
	// Initialize from separate parameters
	public void InitFromPrm(int i_Rng, String sBinStr) {
		iRng = i_Rng;
		sHex = CvtBinStr2HexStr(sBinStr);
		SetArray(); // prepare the rule array
	}

	// ----------------------------------------------------------------
	// Create the rule string
	// Example: "R2,W23AC2"
	public String GetAsString() {
		String sBff;
		int i, ih;

		// correct parameters first
		Validate();
		// range
		sBff = 'R' + String.valueOf(iRng);

		// rule
		sBff = sBff + ",R" + sHex;

		return sBff;
	}

	// ----------------------------------------------------------------
	// Check the validity of the Cyclic CA parameters, correct
	// them if necessary.
	public void Validate() {
		if (iRng < 1)
			iRng = 1;
		else if (iRng > MAX_RANGE)
			iRng = MAX_RANGE;

		sHex.toUpperCase();
		if ((!sHex.isEmpty()) && (sHex.charAt(0) == 'W'))
			sHex = sHex.substring(1); // skip 'W' prefix
	}

	// ----------------------------------------------------------------
	// Prepare the rule array
	private void SetArray() {
		String sBinStr;
		int i, iCnt;

		Validate(); // first correct parameters

		sBinStr = CvtHexStr2BinStr(sHex);
		iCnt = 1;

		// fill with '0' to the full length
		for (i = 1; i <= 2 * iRng + 1; i++)
			iCnt = iCnt * 2;
		sBinStr = LPad(sBinStr, iCnt, '0');

		// set the rule array
		for (i = 0; i < iCnt; i++)
			iAry[iCnt - i - 1] = (byte) (sBinStr.charAt(i) == '1' ? 1 : 0);
	}

	// ----------------------------------------------------------------
	// If 'sStr' is shorter than 'num', pad it left with the 'chPad'
	private String LPad(String sStr, int num, char chPad) {
		int i, iLen;
		iLen = sStr.length();
		if (iLen < num) {
			for (i = 1; i <= num - iLen; i++)
				sStr = chPad + sStr;
		}
		return sStr;
	}

	// ----------------------------------------------------------------
	// Convert the binary string to hex string
	private String CvtBinStr2HexStr(String sBin) {
		int i, iVal;
		String sTok, sHexStr;

		i = sBin.length();
		if ((i % 4) != 0)
			LPad(sBin, 4 - (i % 4), '0');

		sHexStr = "";
		for (i = 1; i <= (sBin.length() / 4); i++) {
			sTok = sBin.substring(sBin.length() - i * 4, sBin.length() - i * 4
					+ 3);
			iVal = 0;
			if (sTok.charAt(1) == '1')
				iVal += 8;
			if (sTok.charAt(2) == '1')
				iVal += 4;
			if (sTok.charAt(3) == '1')
				iVal += 2;
			if (sTok.charAt(4) == '1')
				iVal += 1;
			sHexStr = Integer.toHexString(iVal) + sHexStr;
		}
		sHexStr = DelLedChr(sHexStr, '0');

		return sHexStr;
	}

	// ----------------------------------------------------------------
	// Convert the binary string to hex string
	private String CvtHexStr2BinStr(String sHex) {
		String sBinBff;
		int i;

		sBinBff = "";
		sHex.toUpperCase();
		for (i = 0; i < sHex.length(); i++) {
			switch (sHex.charAt(i)) {
				case '0' :
					sBinBff = sBinBff + "0000";
					break;
				case '1' :
					sBinBff = sBinBff + "0001";
					break;
				case '2' :
					sBinBff = sBinBff + "0010";
					break;
				case '3' :
					sBinBff = sBinBff + "0011";
					break;
				case '4' :
					sBinBff = sBinBff + "0100";
					break;
				case '5' :
					sBinBff = sBinBff + "0101";
					break;
				case '6' :
					sBinBff = sBinBff + "0110";
					break;
				case '7' :
					sBinBff = sBinBff + "0111";
					break;
				case '8' :
					sBinBff = sBinBff + "1000";
					break;
				case '9' :
					sBinBff = sBinBff + "1001";
					break;
				case 'A' :
					sBinBff = sBinBff + "1010";
					break;
				case 'B' :
					sBinBff = sBinBff + "1011";
					break;
				case 'C' :
					sBinBff = sBinBff + "1100";
					break;
				case 'D' :
					sBinBff = sBinBff + "1101";
					break;
				case 'E' :
					sBinBff = sBinBff + "1110";
					break;
				case 'F' :
					sBinBff = sBinBff + "1111";
					break;
			}
		}
		sBinBff = DelLedChr(sBinBff, '0');
		return sBinBff;
	}

	// ----------------------------------------------------------------
	// Remove all leading characters 'cChar' from the string
	private String DelLedChr(String sStr, char cChar) {
		while ((!sStr.isEmpty()) && (sStr.charAt(0) == cChar))
			sStr = sStr.substring(1);

		return sStr;
	}

	// ----------------------------------------------------------------
	// Perform one pass of the rule
	public int OnePass(int sizX, int sizY, boolean isWrap, int ColoringMethod,
			short[][] crrState, short[][] tmpState, MJBoard mjb) {
		short bOldVal, bNewVal;
		int modCnt = 0;
		int i;
		short[] OneRow;
		int[] xVector;
		int ary1DOfs; // margins, used for wrapping
		int ic, iPow, iIdx;
		int iClo = mjb.StatesCount;

		ary1DOfs = iRng;
		OneRow = new short[sizX + 1 + 2 * ary1DOfs];
		xVector = new int[21]; // 0..9, 10, 11..20

		int i1DNextRow; // next row
		i1DNextRow = mjb.i1DLastRow + 1;
		if (i1DNextRow >= sizY)
			i1DNextRow = 0;

		for (ic = 0; ic < sizX; ic++)
			OneRow[ic + ary1DOfs] = crrState[ic][mjb.i1DLastRow]; // original
																	// row
		if (isWrap) {
			for (ic = 1; ic <= ary1DOfs; ic++) {
				OneRow[ary1DOfs - ic] = OneRow[sizX - 1 - ic + 1];
				OneRow[sizX - 1 + ic] = OneRow[ary1DOfs + ic - 1];
			}
		}

		for (ic = 0; ic < sizX; ic++) // for the whole row
		{
			bOldVal = OneRow[ic + ary1DOfs];

			iPow = 1;
			iIdx = 0;
			for (i = iRng; i >= -iRng; i--) // neighbours
			{
				if (OneRow[ic + i + ary1DOfs] > 0) // alive cell
					iIdx = iIdx + iPow;
				iPow = iPow * 2;
			}

			// determine the cell status
			bNewVal = iAry[iIdx]; // default - no change
			if (bNewVal > 0)
				if (ColoringMethod == 2) // alternate
					bNewVal = (short) (mjb.Cycle % (iClo - 1) + 1); // birth

			tmpState[ic][i1DNextRow] = bNewVal;
		} // for

		modCnt = 1; // run forever
		mjb.i1DLastRow = i1DNextRow; // Done. Advance the last generated row

		return modCnt;
	}
}
