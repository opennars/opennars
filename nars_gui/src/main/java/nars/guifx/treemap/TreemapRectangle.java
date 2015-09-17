package nars.guifx.treemap;

import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;

/**
 * @author Tadas Subonis <tadas.subonis@gmail.com>
 */
class TreemapRectangle extends Label {

    //private static final Logger LOG = Logger.getLogger(TreemapRectangle.class.getName());
    //private Random random = new Random();
    private final Color rectangleColor;
    //private final Rectangle rectangle = new Rectangle();

    public TreemapRectangle(final TreemapDtoElement child, Color color) {
        super(child.getLabel());

        minHeight(child.getHeight());
        minWidth(child.getWidth());
        //rectangle.addEventHandler(MouseEvent.MOUSE_ENTERED, new HoverOnEventHandler());
        //rectangle.addEventHandler(MouseEvent.MOUSE_EXITED, new HoverOffEventHandler());
        rectangleColor = color;
        //rectangle.setFill(rectangleColor);



        setBackground(new Background(
                new BackgroundFill(color,null,null)
        ));

        layout();

        /*this.getChildren().setAll(
                //rectangle,
                label
        );*/

        //label.setCenterShape(true);
        //label.setMouseTransparent(true);



        /*NodeTooltip nodeTooltip = new NodeTooltip(child);
        Tooltip.install(this, nodeTooltip);*/
    }

    private class NodeTooltip extends Tooltip {

        public NodeTooltip(final TreemapDtoElement child) {
            setText(child.getLabel());
        }
    }

//    private class HoverOnEventHandler implements EventHandler<Event> {
//
//        @Override
//        public void handle(Event t) {
//            Color brighter = rectangleColor.brighter();
//            TreemapRectangle.this.rectangle.setFill(brighter);
//        }
//    }
//
//    private class HoverOffEventHandler implements EventHandler<Event> {
//
//        @Override
//        public void handle(Event t) {
//            TreemapRectangle.this.rectangle.setFill(rectangleColor);
//        }
//    }
}
