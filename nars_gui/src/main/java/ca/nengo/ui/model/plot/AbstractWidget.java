package ca.nengo.ui.model.plot;

import ca.nengo.model.SimulationException;
import ca.nengo.model.impl.AbstractNode;
import ca.nengo.ui.lib.object.model.ModelObject;
import ca.nengo.ui.lib.world.PaintContext;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.lib.world.piccolo.object.BoundsHandle;
import ca.nengo.ui.model.UIBuilder;
import ca.nengo.ui.model.UINeoNode;
import ca.nengo.ui.model.icon.EmptyIcon;
import org.piccolo2d.util.PBounds;

import java.awt.geom.Rectangle2D;

/**
 * Created by me on 3/3/15.
 */
public abstract class AbstractWidget extends AbstractNode implements UIBuilder {

    protected AbstractWidgetUI ui;

    public AbstractWidget(String name) {
        super(name);

        newUI();
    }

    public boolean isResizable() {
        return true;
    }

    public WorldObject newIcon(ModelObject UI) {
        EmptyIcon ei = new EmptyIcon(UI);
        ei.setLabelVisible(false);
        return ei;
    }

    public Rectangle2D getInitialBounds() {
        return new Rectangle2D.Double(0, 0, 64, 64);
    }

    public PBounds getBounds() { return ui.getBounds(); }
    public PBounds setBounds(PBounds p) { ui.setBounds(p); return p; }


    protected abstract void paint(PaintContext paintContext, double width, double height);

    public String getTypeName() {
        return getClass().getName();
    }

    @Override
    public UINeoNode newUI() {
        if (ui==null)
            ui = new AbstractWidgetUI();
        return ui;
    }

    @Override
    public abstract void run(float startTime, float endTime) throws SimulationException;

    @Override
    public void reset(boolean randomize) {


    }



    public class AbstractWidgetUI extends UINeoNode<AbstractWidget> {

        public AbstractWidgetUI() {
            super(AbstractWidget.this);

            if (isResizable())
                BoundsHandle.addBoundsHandlesTo(this);

            setIcon(newIcon(this));

            setBounds(AbstractWidget.this.getInitialBounds());



            repaint();
        }


        @Override
        public void dragOffset(double dx, double dy) {
            super.dragOffset(dx, dy);
        }

        @Override
        public String getTypeName() {
            return AbstractWidget.this.getTypeName();
        }


        @Override
        public void paint(PaintContext paintContext) {
            super.paint(paintContext);

            AbstractWidget.this.paint(paintContext, getWidth(), getHeight());
        }

    }

    public void move(double dx, double dy) {
        ui.dragOffset(dx, dy);
    }
}
