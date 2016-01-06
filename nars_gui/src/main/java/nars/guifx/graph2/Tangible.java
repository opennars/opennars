package nars.guifx.graph2;

import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import nars.guifx.Spacegraph;
import nars.guifx.util.Animate;

/**
 * Created by me on 12/6/15.
 */
public class Tangible {

	private final Node node;
	Spacegraph parent = null;
	private Overlay ww;
	private Bounds currentBounds;

	// public String getName();
	// public Term getDescription();

	public class Overlay extends Group {
		private final Node target;
		private final Animate boundsTracker;

		public Overlay(Node target) {

            this.target = target;
            setMouseTransparent(true);

            Circle c = new Circle(-0.5, -0.5, 0.5);
            //Polygon c = JFX.newPoly(6, 1);
            c.setStrokeType(StrokeType.INSIDE);

//            c.setFill(new Color(0.25, 0.75, 0.25, 0.5));
            c.setFill(Color.TRANSPARENT);
            c.setStroke(new Color(0.25, 0.75, 0.25, 0.5));
            c.setStrokeWidth(0.05);
            c.setMouseTransparent(true);

            getChildren().add(c);

            boundsTracker = new Animate(30, (a)-> trackBounds());
            boundsTracker.start();

//            parentProperty().addListener((c) -> {
//
//            });
        }
		protected void trackBounds() {
			if (getParent() == null) {
				boundsTracker.stop();
				currentBounds = null;
				return;
			}

			Bounds sb = currentBounds = target.localToScene(target
					.getBoundsInLocal());
			setLayoutX(0.5 * (sb.getMinX() + sb.getMaxX()));
			setLayoutY(0.5 * (sb.getMinY() + sb.getMaxY()));
			setScaleX(sb.getWidth());
			setScaleY(sb.getHeight());

		}
	}

	public Tangible(Node n) {
        node = n;
        n.setOnMouseEntered(e -> hover(true));
        n.setOnMouseExited(e -> hover(false));
        n.setOnMousePressed( e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                e.consume();
                zoomTo();
            }
        });
    }
	private void zoomTo() {
		System.out.println("zoom to: " + currentBounds);
		if (parent != null) {
			double w = currentBounds.getWidth();
			double h = currentBounds.getHeight();
			// parent.panX.set(currentBounds.getMinX()+w/2);
			// parent.panY.set(currentBounds.getMinY()+h/2);

			double z = w / 500;
			// parent.zoomFactor.set(z);
		}
	}

	protected void hover(boolean b) {

		if (parent == null) {
			findSpace();
			if (parent == null)
				return;
		}

		if (b) {
			ww = new Overlay(node);
			parent.hud.getChildren().add(ww);
		} else {
			if (ww != null) {
				parent.hud.getChildren().remove(ww);
				ww = null;
			}
		}

	}

	private void findSpace() {
		Node p = node.getParent();
		while (p != null && !(p instanceof Spacegraph)) {
			p = p.getParent();
		}

		parent = (p != null) ? (Spacegraph) p : null;
	}

}
