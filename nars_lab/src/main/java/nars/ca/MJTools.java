package nars.ca;// Mirek's Java Cellebration
// http://www.mirekw.com
//
// Various tools

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

public class MJTools {
	// ----------------------------------------------------------------
	public MJTools() {
	}

	// ----------------------------------------------------------------
	// Read the specified text file, return its lines in vLines
	public boolean LoadTextFile(String sPath, Vector vLines) {
		boolean fRetVal = false;
		URL theUrl;
		DataInputStream theFile;
		String sBff;
		try {
			theUrl = new URL(sPath);
		} catch (MalformedURLException mue) {
			System.out.println("Malformed URL: " + sPath);
			return false;
		} catch (SecurityException se) {
			System.out.println("Security exception: " + sPath);
			return false;
		}

		try {
			vLines.removeAllElements();

			theFile = new DataInputStream(theUrl.openStream());
			BufferedReader br = new BufferedReader(new InputStreamReader(
					theFile));

			// read-in the whole file
			while ((sBff = br.readLine()) != null) {
				if (!sBff.isEmpty()) {
					vLines.addElement(sBff.trim());
				}
			}
			br.close();
			fRetVal = true;
		} catch (IOException e) {
			System.out.println("IOException:" + e);
		}

		return fRetVal;
	}

	// ----------------------------------------------------------------
	// Read the specified resource file, return its lines in vLines
	// Resource file MUST be given as a relative path.
	public boolean LoadResTextFile(String sPath, Vector vLines) {
		boolean fRetVal = false;
		String sBff;

		try {
			InputStream in = MJCell.class.getResourceAsStream(sPath);
			if (in != null) {
				BufferedReader br = new BufferedReader(
						new InputStreamReader(in));

				// read-in the whole file
				while ((sBff = br.readLine()) != null) {
					if (!sBff.isEmpty()) {
						vLines.addElement(sBff.trim());
					}
				}
				br.close();
				fRetVal = true;
			}
		} catch (IOException e) {
			System.out.println("IOException:" + e);
		}

		return fRetVal;
	}
	// ----------------------------------------------------------------
	// ----------------------------------------------------------------
	// ----------------------------------------------------------------
}