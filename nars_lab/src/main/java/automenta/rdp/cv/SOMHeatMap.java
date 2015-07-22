package automenta.rdp.cv;

import automenta.rdp.RdesktopFrame;
import automenta.rdp.WrappedImage;
import io.undertow.util.FastConcurrentDirectDeque;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import nars.rl.gng.NeuralGasNet;
import nars.rl.gng.Node;

import java.awt.image.BufferedImage;
import java.util.Deque;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Created by me on 7/22/15.
 */
public class SOMHeatMap implements RDPVis {


    private RdesktopFrame rdesktopFrame;
    public boolean updated = false;
    public Canvas vc;
    final Random rng = new Random();

    BufferedImage w;

    final NeuralGasNet n = new NeuralGasNet(2, 64);

    final Deque<Consumer<GraphicsContext>> redrawn = new FastConcurrentDirectDeque();

    public SOMHeatMap(RdesktopFrame rdesktopFrame) {
        this.rdesktopFrame = rdesktopFrame;
    }

	public SOMHeatMap() {
		n.setAlpha(0.5f);

	}
    @Override
	public void redrawn(WrappedImage backstore, int x, int y, int wx, int wy) {
		if (w == null || w.getWidth() != backstore.getWidth() || w.getHeight() != backstore.getHeight()) {
			w = new BufferedImage(backstore.getWidth(), backstore.getHeight(), BufferedImage.TYPE_INT_ARGB);
		}


		//g.setColor(new Color(1f, 0, 0, 0.25f));
		//g.fillRect(x, y, wx, wy);


		redrawn.addLast(g -> {
			//synchronized(n) {
			n.learn(x + wx / 2, y + wy / 2);
			n.learn(x, y);
			n.learn(x + wx, y);
			n.learn(x + wx, y + wy);
			n.learn(x, y + wy);
			//}

			g.setFill(javafx.scene.paint.Color.rgb(255, 0, 0, 0.35));
			g.fillRect(x, y, wx, wy);
		});

		updated = true;
	}

    @Override
	public void update() {
		if (w == null) return;


		//if (!updated) return;

		updated = false;


		GraphicsContext g = vc.getGraphicsContext2D();


		g.setFill(javafx.scene.paint.Color.color(0, 0, 0, 0.1));
		g.fillRect(0, 0, vc.getWidth(), vc.getHeight());

		while (!redrawn.isEmpty()) {
			redrawn.pollFirst().accept(g);
		}

		g.setFill(javafx.scene.paint.Color.GREEN);

		for (Node nn : n.vertexSet()) {
			double[] d = nn.getDataRef();

			final int t = 8;
			int x = (int) d[0] - t / 2;
			int y = (int) d[1] - t / 2;
			g.fillRect(x, y, t, t);
		}


//					float alpha = 0.1f;
//					AlphaComposite alcom = AlphaComposite.getInstance(
//							AlphaComposite.SRC_OVER, alpha);
//					g.setComposite(alcom);
//

//				}


	}

    @Override
	public javafx.scene.Node getNode() {

		vc = new Canvas(rdesktopFrame.canvas.width, rdesktopFrame.canvas.height);
		vc.setBlendMode(BlendMode.EXCLUSION);
		return vc;

	}


}
