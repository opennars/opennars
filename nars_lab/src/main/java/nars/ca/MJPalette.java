package nars.ca;// Mirek's Java Cellebration
// http://www.mirekw.com
//
// Color palette class

import java.awt.*;

public class MJPalette {
	public final int[] Palette = new int[MJBoard.MAX_CLO + 1]; // color palette
	public final int[] GridColor = new int[2]; // normal, bold
	public String PalName = "MJCell Standard";

	// ----------------------------------------------------------------
	// Constructor
	MJPalette() {
		ActivatePalette("MJCell Standard", MJBoard.MAX_CLO + 1);
	}

	// ----------------------------------------------------------------
	// Make RGB value out of its 3 components
	private int MakeRGB(int r, int g, int b) {
		return b + (g << 8) + (r << 16) + (0xff << 24);
	}

	// ----------------------------------------------------------------
	// Activate the given palette with the 'iSttCnt' states
	public void ActivatePalette(String palNam, int iSttCnt) {
		int i, j;
		// noinspection IfStatementWithTooManyBranches
		if ("Red & blue".equalsIgnoreCase(palNam)) {
			GeneratePalette(Color.red, Color.blue, iSttCnt);
			Palette[0] = Color.white.getRGB();
			// gray
			GridColor[0] = MakeRGB(208, 208, 208); // normal
			GridColor[1] = MakeRGB(160, 160, 160); // bold
		} else if ("Dolphin".equalsIgnoreCase(palNam)) {
			GeneratePalette(new Color(0, 0, 255), Color.cyan, iSttCnt);
			Palette[0] = Color.white.getRGB();
			// gray
			GridColor[0] = MakeRGB(208, 208, 208); // normal
			GridColor[1] = MakeRGB(160, 160, 160); // bold
		} else if ("Milky way".equalsIgnoreCase(palNam)) {
			GeneratePalette(Color.white, new Color(16, 16, 255), iSttCnt);
			Palette[0] = MakeRGB(0, 0, 80);
			// gray
			GridColor[0] = MakeRGB(0, 0, 128); // normal
			GridColor[1] = MakeRGB(0, 0, 160); // bold
		} else if ("8 colors".equalsIgnoreCase(palNam)) {
			GridColor[0] = 4194304 + (0xff << 24); // normal
			GridColor[1] = 6488833 + (0xff << 24); // bold
			Palette[0] = 0;
			Palette[1] = 16711680;
			Palette[2] = 255;
			Palette[3] = 16776960;
			Palette[4] = 32768;
			Palette[5] = 12632256;
			Palette[6] = 16744448;
			Palette[7] = 65535;
			Palette[8] = 65280;

			Palette[9] = 10289152;
			Palette[10] = 42496;
			Palette[11] = 16711935;
			Palette[12] = 8421504;
			Palette[13] = 8388736;
			Palette[14] = 16777215;
			Palette[15] = 128;
			Palette[16] = 244939;
			Palette[17] = 16711680;
			Palette[18] = 255;
			Palette[19] = 16776960;
			Palette[20] = 32768;
			Palette[21] = 12632256;
			Palette[22] = 16744448;
			Palette[23] = 65535;
			Palette[24] = 65280;

			// copy it 14x, up to 248
			for (i = 0; i <= 13; i++) {
				for (j = 1; j <= 16; j++) {
					Palette[24 + i * 16 + j] = Palette[8 + j];
				}
			}

			// the rest
			for (i = 249; i <= 255; i++)
				Palette[i] = Palette[i - 240];

			// make it Java RGB
			for (i = 0; i < 256; i++)
				Palette[i] += (0xff << 24);
		} else // "MJCell Standard"
		{
			if (iSttCnt <= 16) {
				GeneratePalette(Color.yellow, Color.red, iSttCnt);
				Palette[0] = Color.black.getRGB();
				// brown
				GridColor[0] = 4194304 + (0xff << 24); // normal
				GridColor[1] = 6488833 + (0xff << 24); // bold
			} else {
				GridColor[0] = 4194304 + (0xff << 24); // normal
				GridColor[1] = 6488833 + (0xff << 24); // bold
				Palette[0] = 0;
				Palette[1] = 16776960;
				Palette[2] = 16767744;
				Palette[3] = 16758528;
				Palette[4] = 16749312;
				Palette[5] = 16740096;
				Palette[6] = 16730880;
				Palette[7] = 16721664;
				Palette[8] = 16711680;
				Palette[9] = 15728640;
				Palette[10] = 14745600;
				Palette[11] = 13762560;
				Palette[12] = 12779520;
				Palette[13] = 11796480;
				Palette[14] = 10813440;
				Palette[15] = 9830400;
				Palette[16] = 8388608;
				Palette[17] = 8060928;
				Palette[18] = 7733248;
				Palette[19] = 7405568;
				Palette[20] = 7077888;
				Palette[21] = 6750208;
				Palette[22] = 6422528;
				Palette[23] = 6094848;
				Palette[24] = 5308416;
				Palette[25] = 4660992;
				Palette[26] = 4013568;
				Palette[27] = 3366144;
				Palette[28] = 2718720;
				Palette[29] = 2071296;
				Palette[30] = 1423872;
				Palette[31] = 776448;
				Palette[32] = 65280;
				Palette[33] = 65311;
				Palette[34] = 65342;
				Palette[35] = 65373;
				Palette[36] = 65404;
				Palette[37] = 65435;
				Palette[38] = 65466;
				Palette[39] = 65497;
				Palette[40] = 65535;
				Palette[41] = 57599;
				Palette[42] = 49663;
				Palette[43] = 41727;
				Palette[44] = 33791;
				Palette[45] = 25855;
				Palette[46] = 17919;
				Palette[47] = 9983;
				Palette[48] = 255;
				Palette[49] = 1376490;
				Palette[50] = 2752725;
				Palette[51] = 4128960;
				Palette[52] = 5505195;
				Palette[53] = 6881430;
				Palette[54] = 8388736;
				Palette[55] = 9180536;
				Palette[56] = 9972336;
				Palette[57] = 10764136;
				Palette[58] = 11555936;
				Palette[59] = 12347736;
				Palette[60] = 13139536;
				Palette[61] = 13931336;
				Palette[62] = 14723136;
				Palette[63] = 15514936;

				for (i = 1; i <= 3; i++) // copy colors to states 64..255
				{
					for (j = 0; j < 64; j++) {
						Palette[64 * i + j] = Palette[j];
					}
				}
				Palette[64] = Palette[66];
				Palette[128] = Palette[66];
				Palette[192] = Palette[66];

				// make it Java RGB
				for (i = 0; i < 256; i++)
					Palette[i] += (0xff << 24);
			}
		}
	}

	// ----------------------------------------------------------------
	// Generate the color palette
	public void GeneratePalette(Color c1, Color c2, int iSttCnt) {
		int r, dr, r1, r2;
		int g, dg, g1, g2;
		int b, db, b1, b2;
		int i;
		r1 = c1.getRed();
		r2 = c2.getRed();
		g1 = c1.getGreen();
		g2 = c2.getGreen();
		b1 = c1.getBlue();
		b2 = c2.getBlue();
		dr = (r2 - r1) / (iSttCnt - 1);
		dg = (g2 - g1) / (iSttCnt - 1);
		db = (b2 - b1) / (iSttCnt - 1);

		// Palette[0] = Color.black;
		for (i = 1; i < iSttCnt; i++) {
			Palette[i] = (i == iSttCnt - 1) && (iSttCnt > 2) ? MakeRGB(r2, g2,
					b2) : MakeRGB(r1 + (i - 1) * dr, g1 + (i - 1) * dg, b1
					+ (i - 1) * db);
		}
	}
	// ----------------------------------------------------------------
	// ----------------------------------------------------------------
	// ----------------------------------------------------------------

}