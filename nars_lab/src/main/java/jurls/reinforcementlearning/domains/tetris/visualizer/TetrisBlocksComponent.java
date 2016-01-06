/*
 Copyright 2007 Brian Tanner
 http://rl-library.googlecode.com/
 brian@tannerpages.com
 http://brian.tannerpages.com

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package jurls.reinforcementlearning.domains.tetris.visualizer;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class TetrisBlocksComponent {

	private TetrisVisualizer tetVis = null;
	private final int lastUpdateTimeStep = -1;

	public TetrisBlocksComponent(TetrisVisualizer ev) {
		// TODO Write Constructor
		tetVis = ev;
	}

	float motionBlur = 0.6f;
	final Color alphaBlack = new Color(0, 0, 0, 1.0f - motionBlur);
	final Color alphaWhite = new Color(1.0f, 1.0f, 1.0f, 1.0f - motionBlur);

	public void render(Graphics2D g, int DABS) {

		Rectangle2D agentRect;
		int numCols = tetVis.getWorldWidth();
		int numRows = tetVis.getWorldHeight();
		double[] tempWorld = tetVis.getWorld();

		// Desired abstract block size
		int scaleFactorX = numCols * DABS;
		int scaleFactorY = numRows * DABS;

		int w = DABS;
		int h = DABS;
		int x = 0;
		int y = 0;

		g.setColor(alphaBlack);
		g.fillRect(0, 0, w * numCols, h * numRows);

		for (int i = 0; i < numRows; i++) {
			y = i * DABS;

			for (int j = 0; j < numCols; j++) {

				x = j * DABS;

				int in = i * numCols + j;

				double bc = tempWorld[in];

				Color c = null;
				if ((bc < 1.0) && (bc > 0)) {
					c = alphaWhite; // falling block, ~0.5
				} else if (bc > 0) {
					int thisBlockColor = (int) bc;
					if (thisBlockColor != 0) {
						switch (thisBlockColor) {
							case 1 :
								c = (Color.PINK);
								break;
							case 2 :
								c = (Color.RED);
								break;
							case 3 :
								c = (Color.GREEN);
								break;
							case 4 :
								c = (Color.YELLOW);
								break;
							case 5 :
								c = new Color(0.3f, 0.3f, 1.0f); // blue
								break;
							case 6 :
								c = (Color.ORANGE);
								break;
							case 7 :
								c = (Color.MAGENTA);
								break;
						}
					}
				}

				if (c != null) {
					g.setColor(c);
					g.fillRect(x, y, w, h);
				}

			}
		}
		// g.setColor(Color.GRAY);
		// g.drawRect(0, 0, DABS * numCols, DABS * numRows);
		// g.setTransform(saveAT);
	}

}
