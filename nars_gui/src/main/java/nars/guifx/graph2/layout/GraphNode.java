package nars.guifx.graph2.layout;

import javafx.scene.Group;
import nars.guifx.graph2.TermEdge;


public class GraphNode extends Group {

	/**
	 * cached from last set
	 */
	private double scaled = 0.0;
	private double tx = 0.0;
	private double ty = 0.0;

	public GraphNode() {
		super();
		setManaged(false);
		setPickOnBounds(true);
	}

	public boolean visible() {
		return isVisible() && getParent()!=null;
	}

	public void scale(double scale) {
		this.scaled = scale;


		setScaleX(scale);
		setScaleY(scale);

		//float conf = c != null ? c.getBeliefs().getConfidenceMax(0, 1) : 0;
            /*base.setFill(NARGraph.vis.get().getVertexColor(priNorm, conf));*/

		//setOpacity(0.75f + 0.25f * vertexScale);

		//System.out.println(scale + " " + vertexScale + " " + (int)(priorityDisplayedResolution * vertexScale));
	}


	public final void getPosition(final double[] v) {
		v[0] = tx;
		v[1] = ty;
	}

	//Point2D sceneCoord;// = new Point2D(0,0);

	final public GraphNode move(final double x, final double y) {
		setTranslateX(this.tx = x);
		setTranslateY(this.ty = y);

		//sceneCoord = null;
		return this;
	}

	final public void move(final double[] v, final double speed, final double threshold) {
		move(v[0], v[1], speed, threshold);
	}
	final public void move(final double v0, final double v1, final double speed, final double threshold) {
		final double px = tx;
		final double py = ty;
		final double momentum = 1f - speed;
		final double nx = v0 * speed + px * momentum;
		final double ny = v1 * speed + py * momentum;
		final double dx = Math.abs(px - nx);
		final double dy = Math.abs(py - ny);
		if ((dx > threshold) || (dy > threshold)) {
			move(nx, ny);
		}
	}

	final public boolean move(final double[] v, final double threshold) {
		final double x = tx;
		final double y = ty;
		final double nx = v[0];
		final double ny = v[1];
		if (!((Math.abs(x - nx) < threshold) && (Math.abs(y - ny) < threshold))) {
			move(nx, ny);
			return true;
		}
		return false;
	}

	public final double width() {
		return scaled; //getScaleX();
	}

	public final double height() {
		return scaled; //getScaleY();
	}

//    public double sx() {
//        if (sceneCoord == null) sceneCoord = localToParent(0, 0);
//        return sceneCoord.getX();
//    }
//
//    public double sy() {
//        if (sceneCoord == null) sceneCoord = localToParent(0, 0);
//        return sceneCoord.getY();
//    }

	public final double x() {
		return tx;
	}

	public final double y() {
		return ty;
	}

	/** TODO generalize to EdgeNode's  */
	@Deprecated public TermEdge[] getEdges() {
		return TermEdge.empty;
	}
}
