package nars.ca;// Mirek's Java Cellebration
// http://www.mirekw.com
//
// Diversities - string injection

import java.util.StringTokenizer;
import java.util.Vector;

public class MJDiv_StrIn {

	private String m_Str;
	public boolean m_Active;
	public int m_X, m_Y;
	public boolean m_Repeat;
	public final Vector m_Vals = new Vector();
	public int m_Pos; // current position in the array - the value to be
						// injected

	private void AddVal(Integer n) {
		if ((n >= 0) && (n <= MJBoard.MAX_CLO)) {
			m_Vals.addElement(n); // grow when necessary
		}
	}

	// Initialize the array of values from the given string
	// Example: 1,2,1,0,10(1),5(0)
	private void SetStr(String sStr) {
		String sTok, sBff;
		int i, iPos, iCnt;
		// noinspection UseOfStringTokenizer
		StringTokenizer st;

		m_Str = sStr;

		m_Pos = 0;
		st = new StringTokenizer(sStr, ",", false);
		while (st.hasMoreTokens()) {
			sTok = st.nextToken();
			sTok = sTok.trim();
			if (sTok.contains("(")) // multiple items
			{
				iPos = sTok.indexOf('(');
				sBff = sTok.substring(0, iPos);
				iCnt = Integer.valueOf(sBff); // count
				sBff = sTok.substring(iPos + 1); // value
				iPos = sBff.indexOf(')');
				if (iPos >= 0)
					sBff = sBff.substring(0, iPos);
				while (iCnt > 0) {
					AddVal(Integer.valueOf(sBff));
					iCnt--;
				}
			} else // single item
			{
				AddVal(Integer.valueOf(sTok));
			}
		}
	}

	// ------------------------------------------------------------------------------
	public void Reset() {
		m_Active = false;
		m_Repeat = true;
		m_X = 0;
		m_Y = 0;
		m_Str = "";
		m_Vals.removeAllElements();
	}

	// ------------------------------------------------------------------------------
	// Example: #STRIN,act=1,rep=1,x=0,y=-40,str=1,2,1,0,10(1),5(0)
	public void SetFromString(String sStr) {
		// noinspection UseOfStringTokenizer
		StringTokenizer st;
		String sTok;
		String sBff;
		int iPos;

		Reset(); // first reset it to defaults

		st = new StringTokenizer(sStr, ",", false);
		while (st.hasMoreTokens()) {
			sTok = st.nextToken().toUpperCase();
			// noinspection IfStatementWithTooManyBranches
			if (sTok.startsWith("ACT="))
				m_Active = Integer.valueOf(sTok.substring(4)) != 0;
			else if (sTok.startsWith("REP="))
				m_Repeat = Integer.valueOf(sTok.substring(4)) != 0;
			else if (sTok.startsWith("X="))
				m_X = Integer.valueOf(sTok.substring(2));
			else if (sTok.startsWith("Y="))
				m_Y = Integer.valueOf(sTok.substring(2));
		}

		iPos = sStr.indexOf("str=");
		if (iPos >= 0) {
			sBff = sStr.substring(iPos + 4);
			SetStr(sBff);
		}
	}

	// ------------------------------------------------------------------------------
	public String GetAsString() {
		String sRet = "#STRIN";

		sRet = sRet + ",act=" + (m_Active ? "1" : "0");
		sRet = sRet + ",rep=" + (m_Repeat ? "1" : "0");
		sRet = sRet + ",x=" + m_X;
		sRet = sRet + ",y=" + m_Y;
		sRet = sRet + ",str=" + m_Str;

		return sRet;
	}
	// ------------------------------------------------------------------------------

}
