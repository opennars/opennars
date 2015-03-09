/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "BrainViewer.java". Description:
""

The Initial Developer of the Original Code is Bryan Tripp & Centre for Theoretical Neuroscience, University of Waterloo. Copyright (C) 2006-2008. All Rights Reserved.

Alternatively, the contents of this file may be used under the terms of the GNU
Public License license (the GPL License), in which case the provisions of GPL
License are applicable  instead of those above. If you wish to allow use of your
version of this file only under the terms of the GPL License and not to allow
others to use your version of this file under the MPL, indicate your decision
by deleting the provisions above and replace  them with the notice and other
provisions required by the GPL License.  If you do not delete the provisions above,
a recipient may use your version of this file under either the MPL or the GPL License.
 */

package ca.nengo.ui.model.brain;

import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.lib.world.handler.AbstractStatusHandler;
import ca.nengo.ui.lib.world.handler.EventConsumer;
import ca.nengo.ui.lib.world.piccolo.WorldGroundImpl;
import ca.nengo.ui.lib.world.piccolo.WorldImpl;
import ca.nengo.ui.lib.world.piccolo.WorldObjectImpl;
import ca.nengo.ui.lib.world.piccolo.primitive.PXImage;
import ca.nengo.ui.lib.world.piccolo.primitive.Text;
import org.piccolo2d.event.PDragSequenceEventHandler;
import org.piccolo2d.event.PInputEvent;

/**
 * TODO
 * 
 * @author TODO
 */
public class BrainViewer extends WorldImpl {

    AbstractBrainImage2D topView, sideView, frontView;

    /**
     * TODO
     */
    public BrainViewer() {
        super("Brain View", createGround());

        setStatusBarHandler(null);
        init();

        // getSky().setScale(2);
        // setBounds(parentToLocal(getFullBounds()));
        // addInputEventListener(new EventConsumer());

    }

    private void init() {
        sideView = new BrainSideImage();
        frontView = new BrainFrontImage();

        topView = new BrainTopImage();

        getGround().addChild(new BrainImageWrapper(sideView));
        getGround().addChild(new BrainImageWrapper(frontView));
        getGround().addChild(new BrainImageWrapper(topView));

    }

    /**
     * @return TODO
     */
    public int getZCoord() {
        return topView.getCoord();
    }

    /**
     * @return TODO
     */
    public int getYCoord() {
        return frontView.getCoord();
    }

    private static WorldGroundImpl createGround() {
        return new WorldGroundImpl() {

            @Override
            public void layoutChildren() {

                int x = 0;
                double maxHeight = 0;

                for (WorldObject wo : getChildren()) {

                    if (wo.getHeight() > maxHeight) {
                        maxHeight = wo.getHeight();
                    }

                }

                for (WorldObject wo : getChildren()) {

                    wo.setOffset(x, maxHeight - wo.getHeight());
                    x += wo.getWidth() + 10;

                }
            }
        };
    }
}

class BrainImageWrapper extends WorldObjectImpl {

    final AbstractBrainImage2D myBrainImage;
    final Text myLabel;

    public BrainImageWrapper(AbstractBrainImage2D brainImage) {
        super();
        myBrainImage = brainImage;
        addChild(new WorldObjectImpl(new PXImage(brainImage)));

        myLabel = new Text();
        addChild(myLabel);
        updateLabel();

        addInputEventListener(new EventConsumer());
        addInputEventListener(new BrainImageMouseHandler());

        layoutChildren();
        setBounds(parentToLocal(getFullBoundsClone()));
        // addChild(new Border(this, style.COLOR_FOREGROUND));

    }

    private void updateLabel() {
        myLabel.setText(myBrainImage.getViewName() + " (" + myBrainImage.getCoordName()
                + " coord: " + myBrainImage.getCoord() + ')');
    }

    @Override
    public void layoutChildren() {
        super.layoutChildren();

        myLabel.setOffset(0, myBrainImage.getHeight() + 10);
    }

    class BrainImageMouseHandler extends PDragSequenceEventHandler {

        double roundingError = 0;

        @Override
        protected void dragActivityStep(PInputEvent aEvent) {

            double dx = (aEvent.getCanvasPosition().getX() - getMousePressedCanvasPoint().getX()) / 30;
            int dxInteger = (int) dx;

            roundingError += dx - (int) dx;

            if (roundingError > 1) {
                dxInteger++;
                roundingError--;
            } else if (roundingError < -1) {
                roundingError++;
                dxInteger--;
            }

            myBrainImage.setCoord(myBrainImage.getCoord() + dxInteger);
            updateLabel();
            repaint();

        }
    }
}

class BrainViewStatusHandler extends AbstractStatusHandler {

    public BrainViewStatusHandler(BrainViewer world) {
        super(world);
    }

    @Override
    protected String getStatusMessage(PInputEvent event) {
        return "Z Coord: " + getWorld().getZCoord();

    }

    @Override
    protected BrainViewer getWorld() {
        return (BrainViewer) super.getWorld();
    }
}
