package nars.nario;

import nars.nario.level.Level;

import java.awt.*;
import java.util.Random;

public class BgRenderer {
	private int xCam;
	private int yCam;
	private final Image image;
	private final Graphics2D g;
	private static final Color transparent = new Color(0, 0, 0, 0);
	private Level level;

	private final Random random = new Random();
	public boolean renderBehaviors = false;

	private final int width;
	private final int height;
	private final int distance;

	public BgRenderer(Level level, GraphicsConfiguration graphicsConfiguration,
			int width, int height, int distance) {
		this.distance = distance;
		this.width = width;
		this.height = height;

		this.level = level;
		image = graphicsConfiguration.createCompatibleImage(width, height,
				Transparency.BITMASK);
		g = (Graphics2D) image.getGraphics();
		g.setComposite(AlphaComposite.Src);

		updateArea(0, 0, width, height);
	}

	public void setCam(int xCam, int yCam) {
		xCam /= distance;
		yCam /= distance;
		int xCamD = this.xCam - xCam;
		int yCamD = this.yCam - yCam;
		this.xCam = xCam;
		this.yCam = yCam;

		g.setComposite(AlphaComposite.Src);
		g.copyArea(0, 0, width, height, xCamD, yCamD);

		if (xCamD < 0) {
			if (xCamD < -width)
				xCamD = -width;
			updateArea(width + xCamD, 0, -xCamD, height);
		} else if (xCamD > 0) {
			if (xCamD > width)
				xCamD = width;
			updateArea(0, 0, xCamD, height);
		}

		if (yCamD < 0) {
			if (yCamD < -width)
				yCamD = -width;
			updateArea(0, height + yCamD, width, -yCamD);
		} else if (yCamD > 0) {
			if (yCamD > width)
				yCamD = width;
			updateArea(0, 0, width, yCamD);
		}
	}

	private void updateArea(int x0, int y0, int w, int h) {
		g.setBackground(transparent);
		g.clearRect(x0, y0, w, h);
		int xTileStart = (x0 + xCam) / 32;
		int yTileStart = (y0 + yCam) / 32;
		int xTileEnd = (x0 + xCam + w) / 32;
		int yTileEnd = (y0 + yCam + h) / 32;
		for (int x = xTileStart; x <= xTileEnd; x++) {
			for (int y = yTileStart; y <= yTileEnd; y++) {
				int b = level.getBlock(x, y) & 0xff;
				g.drawImage(Art.bg[b % 8][b / 8], (x << 5) - xCam, (y << 5)
						- yCam - 16, null);
			}
		}
	}

	public void render(Graphics g, int tick, float alpha) {
		g.drawImage(image, 0, 0, null);
	}

	public void setLevel(Level level) {
		this.level = level;
		updateArea(0, 0, width, height);
	}
}