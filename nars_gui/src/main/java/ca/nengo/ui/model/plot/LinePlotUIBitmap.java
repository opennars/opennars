package ca.nengo.ui.model.plot;


import ca.nengo.model.Node;
import ca.nengo.model.SimulationException;
import ca.nengo.model.impl.AbstractNode;
import ca.nengo.ui.lib.world.PaintContext;
import ca.nengo.ui.lib.world.piccolo.WorldObjectImpl;
import ca.nengo.ui.lib.world.piccolo.primitive.PXImage;
import ca.nengo.ui.model.UINeoNode;
import ca.nengo.util.ScriptGenException;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.HashMap;

public class LinePlotUIBitmap extends UINeoNode {

    private final BufferedImage img;

    public LinePlotUIBitmap() {
        this(new AbstractNode("plot1", Collections.EMPTY_LIST, Collections.EMPTY_LIST) {
            @Override
            public void run(float startTime, float endTime) throws SimulationException {

            }

            @Override
            public void reset(boolean randomize) {

            }


            @Override
            public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException {
                return "";
            }
        });
    }
    public LinePlotUIBitmap(Node plot) {
        super(plot);
        img = new BufferedImage(400, 200, BufferedImage.TYPE_4BYTE_ABGR);
        setIcon(new WorldObjectImpl(new PXImage(img)));
        setBounds(0, 0,251,91);

        update();
    }

    @Override
    public String getTypeName() {
        return "X";
    }

    public void update() {
        float w = 10;
        float h = 10;
        float x = (float)(Math.random() * getBounds().getWidth());
        float y = (float)(Math.random() * getBounds().getHeight());
        Graphics g = img.getGraphics();
        g.setColor(Color.getHSBColor((float)Math.random(), (float)Math.random(), (float)Math.random()));
        g.drawOval((int)x,(int)y,(int)w,(int)h);
        g.dispose();
    }

    @Override
    public void paint(PaintContext paintContext) {
        super.paint(paintContext);
        update();
    }
}
