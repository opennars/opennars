/*
 * Java Arcade Learning Environment (A.L.E) Agent
 *  Copyright (C) 2011-2012 Marc G. Bellemare <mgbellemare@ualberta.ca>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jurls.reinforcementlearning.domains.arcade.rl;

import jurls.reinforcementlearning.domains.arcade.screen.ScreenMatrix;

import java.util.Arrays;

/**
 * A simple RL feature set for Atari agents. The screen is divided into blocks.
 * Within each block, we encode the presence or absence of a color. The number
 * of colors is restricted to reduce the number of active features.
 * 
 * @author Marc G. Bellemare <mgbellemare@ualberta.ca>
 */
public class FeatureMap {
	/** The number of colors used in our feature set */
	protected final int numColors;
	/** The number of columns (y bins) in the quantization */
	protected final int numColumns;
	/** The number of rows (x bins) in the quantization */
	protected final int numRows;
	private final double[] features;
	private final boolean[] hasColor;

	/**
	 * Create a new FeatureMap with fixed parameter settings: 16 columns, 21
	 * rows and 8 colors (SECAM).
	 */
	public FeatureMap(int columns, int rows, int colors) {
		// Some hardcoded feature set parameters
		numColumns = columns;
		numRows = rows;
		numColors = colors;
		hasColor = new boolean[numColors];
		features = new double[numFeatures()];
		System.out.println(this + " " + numFeatures());

	}
	public FeatureMap() {
		this(18, 21, 8);
	}

	/**
	 * Returns a quantized version of the last screen.
	 * 
	 * @param history
	 * @return
	 */
	public double[] getFeatures(FrameHistory history) {
		// Obtain the last screen
		ScreenMatrix screen = history.getLastFrame(0);

		int blockWidth = screen.width / numColumns;
		int blockHeight = screen.height / numRows;

		int featuresPerBlock = numColors;

		int blockIndex = 0;

		int numColors = this.numColors;

		double[] features = this.features;
		int[][] matrix = screen.matrix;

		int rr = numRows;
		int cc = numColumns;

		// For each pixel block
		boolean[] hasColor = this.hasColor;

		for (int by = 0; by < rr; by++) {

			int yo = by * blockHeight;

			for (int bx = 0; bx < cc; bx++) {
				Arrays.fill(hasColor, false);
				int xo = bx * blockWidth;

				// Determine which colors are present
				for (int x = xo; x < xo + blockWidth; x++) {
					int[] sm = matrix[x];
					for (int y = yo; y < yo + blockHeight; y++) {
						int pixelColor = sm[y];
						hasColor[encode(pixelColor)] = true;
					}
				}

				// Add all colors present to our feature set
				for (int c = 0; c < numColors; c++)
					if (hasColor[c])
						features[c + blockIndex] = 1.0;

				// Increment the feature offset in the big feature vector
				blockIndex += featuresPerBlock;
			}
		}

		return features;
	}

	/**
	 * SECAM encoding of colors; we end up with 8 possible colors.
	 * 
	 * @param color
	 * @return
	 */
	protected static int encode(int color) {
		return (color & 0xF) >> 1;
	}

	/**
	 * Returns the number of features in this FeatureMap.
	 * 
	 * @return
	 */
	public int numFeatures() {
		return numColumns * numRows * numColors;
	}

	/**
	 * Returns the length of history required to compute features.
	 * 
	 * @return
	 */
	public int historyLength() {
		return 1;
	}
}
