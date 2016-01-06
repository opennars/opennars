package nars.ca;// Mirek's Java Cellebration
// http://www.mirekw.com
//
// Favourite patterns list

import java.util.StringTokenizer;

public class MJDiversities {
	public static final int DIV_NONE = 0;
	public static final int DIV_SYSTEM = 1;
	public static final int DIV_NOISE = 2;
	public static final int DIV_BHOLE = 3;
	public static final int DIV_SNOVA = 4;
	public static final int DIV_STRIN = 5;

	public boolean m_Enabled; // enabled?

	public boolean m_BHoleActive;
	public int m_BHoleCtrX, m_BHoleCtrY;
	public int m_BHoleSize;

	public boolean m_SNovaActive;
	public int m_SNovaCtrX, m_SNovaCtrY;
	public int m_SNovaSize;
	public int m_SNovaState;

	public boolean m_NoiseActive;
	public int m_NoiseCycles;
	public int m_NoiseCells;
	public int m_NoiseState;

	public final MJDiv_StrIn StrIn;

	// ----------------------------------------------------------------
	// Constructor
	MJDiversities() {
		m_Enabled = false;

		StrIn = new MJDiv_StrIn();
		ResetItem(DIV_NOISE);
		ResetItem(DIV_BHOLE);
		ResetItem(DIV_SNOVA);
		ResetItem(DIV_STRIN);
	}

	// ------------------------------------------------------------------------------
	// Reset the specified item; defaults
	public void ResetItem(int itm) {
		switch (itm) {
			case DIV_NOISE :
				m_NoiseActive = false;
				m_NoiseCycles = 1;
				m_NoiseCells = 0;
				m_NoiseState = 1;
				break;
			case DIV_BHOLE :
				m_BHoleActive = false;
				m_BHoleCtrX = -30;
				m_BHoleCtrY = -30;
				m_BHoleSize = 10;
				m_SNovaState = 1;
				break;
			case DIV_SNOVA :
				m_SNovaActive = false;
				m_SNovaCtrX = 30;
				m_SNovaCtrY = 30;
				m_SNovaSize = 10;
				break;
			case DIV_STRIN :
				StrIn.Reset();
				break;
		}
	}

	// ------------------------------------------------------------------------------
	// Initialize the specified item from the string
	public void ItemFromString(String sStr, int sizX, int sizY) {
		int itm = DIV_NONE;
		// noinspection UseOfStringTokenizer
		StringTokenizer st;
		String sTok;
		String sBff;
		int iTmp;

		try {
			// noinspection IfStatementWithTooManyBranches
			if (sStr.startsWith("#SYSTEM"))
				itm = DIV_SYSTEM;
			else if (sStr.startsWith("#NOISE"))
				itm = DIV_NOISE;
			else if (sStr.startsWith("#BHOLE"))
				itm = DIV_BHOLE;
			else if (sStr.startsWith("#SNOVA"))
				itm = DIV_SNOVA;
			else if (sStr.startsWith("#STRIN"))
				itm = DIV_STRIN;

			if (itm != DIV_NONE) {
				ResetItem(itm); // first reset it to defaults
				st = new StringTokenizer(sStr, ",", false);
				while (st.hasMoreTokens()) {
					sTok = st.nextToken().toUpperCase();
					switch (itm) {
						case DIV_SYSTEM :
							if (sTok.startsWith("ACT="))
								m_Enabled = Integer.valueOf(sTok.substring(4)) != 0;
							break;
						case DIV_NOISE :
							// noinspection IfStatementWithTooManyBranches
							if (sTok.startsWith("ACT="))
								m_NoiseActive = Integer.valueOf(sTok
										.substring(4)) != 0;
							else if (sTok.startsWith("CYCL="))
								m_NoiseCycles = Integer.valueOf(sTok
										.substring(5));
							else if (sTok.startsWith("CELL="))
								m_NoiseCells = Integer.valueOf(sTok
										.substring(5));
							else if (sTok.startsWith("STT="))
								m_NoiseState = Integer.valueOf(sTok
										.substring(4));
							break;
						case DIV_BHOLE :
							// noinspection IfStatementWithTooManyBranches
							if (sTok.startsWith("ACT="))
								m_BHoleActive = Integer.valueOf(sTok
										.substring(4)) != 0;
							else if (sTok.startsWith("X=")) {
								m_BHoleCtrX = Integer
										.valueOf(sTok.substring(2));
								m_BHoleCtrX += sizX / 2;
							} else if (sTok.startsWith("Y=")) {
								m_BHoleCtrY = Integer
										.valueOf(sTok.substring(2));
								m_BHoleCtrY += sizY / 2;
							} else if (sTok.startsWith("SIZE="))
								m_BHoleSize = Integer
										.valueOf(sTok.substring(5));
							break;
						case DIV_SNOVA :
							// noinspection IfStatementWithTooManyBranches
							if (sTok.startsWith("ACT="))
								m_SNovaActive = Integer.valueOf(sTok
										.substring(4)) != 0;
							else if (sTok.startsWith("X=")) {
								m_SNovaCtrX = Integer
										.valueOf(sTok.substring(2));
								m_SNovaCtrX += sizX / 2;
							} else if (sTok.startsWith("Y=")) {
								m_SNovaCtrY = Integer
										.valueOf(sTok.substring(2));
								m_SNovaCtrY += sizY / 2;
							} else if (sTok.startsWith("SIZE="))
								m_SNovaSize = Integer
										.valueOf(sTok.substring(5));
							else if (sTok.startsWith("STT="))
								m_SNovaState = Integer.valueOf(sTok
										.substring(4));
							break;
						case DIV_STRIN :
							StrIn.SetFromString(sStr);
							StrIn.m_X += sizX / 2;
							StrIn.m_Y += sizY / 2;
							break;
					}
				}
			}
		} catch (Exception e) {
		}
	}

	// ------------------------------------------------------------------------------
	// Get the specified item as a string
	public String ItemAsString(int itm) {
		String sRet = "";

		switch (itm) {
			case DIV_SYSTEM :
				sRet = "#SYSTEM";
				sRet = sRet + ",act=" + (m_Enabled ? "1" : "0");
				break;

			case DIV_NOISE :
				sRet = "#NOISE";
				sRet = sRet + ",act=" + (m_NoiseActive ? "1" : "0");
				sRet = sRet + ",cycl=" + m_NoiseCycles;
				sRet = sRet + ",cell=" + m_NoiseCells;
				sRet = sRet + ",stt=" + m_NoiseState;
				break;

			case DIV_BHOLE :
				sRet = "#BHOLE";
				sRet = sRet + ",act=" + (m_BHoleActive ? "1" : "0");
				sRet = sRet + ",x=" + m_BHoleCtrX;
				sRet = sRet + ",y=" + m_BHoleCtrY;
				sRet = sRet + ",size=" + m_BHoleSize;
				break;

			case DIV_SNOVA :
				sRet = "#SNOVA";
				sRet = sRet + ",act=" + (m_SNovaActive ? "1" : "0");
				sRet = sRet + ",x=" + m_SNovaCtrX;
				sRet = sRet + ",y=" + m_SNovaCtrY;
				sRet = sRet + ",size=" + m_SNovaSize;
				sRet = sRet + ",stt=" + m_SNovaState;
				break;

			case DIV_STRIN :
				sRet = StrIn.GetAsString();
				break;
		}

		return (sRet);
	}

	// ------------------------------------------------------------------------------
	// Check, if the specified item is active
	public boolean ItemActive(int itm) {
		boolean bRet = false;
		switch (itm) {
			case DIV_SYSTEM :
				bRet = m_Enabled;
				break;
			case DIV_NOISE :
				bRet = m_NoiseActive;
				break;
			case DIV_BHOLE :
				bRet = m_BHoleActive;
				break;
			case DIV_SNOVA :
				bRet = m_SNovaActive;
				break;
			case DIV_STRIN :
				bRet = StrIn.m_Active;
				break;
		}
		return (bRet);
	}

	// ------------------------------------------------------------------------------
	// Perform diversities
	// "BeforePass" is True if the procedure is invoked before the cycle,
	// and False after.
	public void Perform(boolean BeforePass, MJBoard mjb) {
		int i, iCol, iRow;
		int minX, minY;
		boolean ifRedraw = false;

		// Black hole active?
		if (m_BHoleActive) {
			ifRedraw = true; // need for redrawing
			minX = m_BHoleCtrX - m_BHoleSize / 2;
			minY = m_BHoleCtrY - m_BHoleSize / 2;
			for (iCol = minX; iCol <= minX + m_BHoleSize - 1; iCol++)
				for (iRow = minY; iRow <= minY + m_BHoleSize - 1; iRow++)
					mjb.SetCell(iCol, iRow, (short) 0);
		}

		// SuperNova active?
		if (m_SNovaActive) {
			ifRedraw = true; // need for redrawing
			minX = m_SNovaCtrX - m_SNovaSize / 2;
			minY = m_SNovaCtrY - m_SNovaSize / 2;
			for (iCol = minX; iCol <= minX + m_SNovaSize - 1; iCol++)
				for (iRow = minY; iRow <= minY + m_SNovaSize - 1; iRow++)
					mjb.SetCell(iCol, iRow, (short) 1);
		}

		// noise active?
		if (m_NoiseActive && BeforePass && (m_NoiseCycles > 0)
				&& (m_NoiseCells > 0)) {
			if ((mjb.Cycle % m_NoiseCycles) == 0) {
				ifRedraw = true; // need for redrawing
				for (i = 1; i <= m_NoiseCells; i++) {
					iCol = (int) Math
							.ceil(Math.random() * (mjb.UnivSize.x - 1));
					iRow = (int) Math
							.ceil(Math.random() * (mjb.UnivSize.y - 1));
					mjb.SetCell(iCol, iRow, (short) m_NoiseState);
				}
			}
		}

		// string injection active?
		if (StrIn.m_Active && BeforePass) {
			int iSiz = StrIn.m_Vals.size();
			if (iSiz > 0) // any items exist?
			{
				ifRedraw = true; // need for redrawing
				if ((StrIn.m_Pos < 0) || (StrIn.m_Pos >= iSiz))
					StrIn.m_Pos = 0;

				mjb.SetCell(StrIn.m_X, StrIn.m_Y, ((Integer) (StrIn.m_Vals
						.elementAt(StrIn.m_Pos))).shortValue());
				StrIn.m_Pos++;
			}
		}

		if (ifRedraw)
			mjb.RedrawBoard(true);
	}

}