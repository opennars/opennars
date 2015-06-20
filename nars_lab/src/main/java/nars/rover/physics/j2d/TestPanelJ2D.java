/**
 * *****************************************************************************
 * Copyright (c) 2013, Daniel Murphy All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. * Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *****************************************************************************
 */
package nars.rover.physics.j2d;

import nars.rover.physics.PhysicsController;
import nars.rover.physics.TestbedPanel;
import nars.rover.physics.TestbedState;

import javax.swing.*;
import java.awt.*;

/**
 * @author Daniel Murphy
 */
@SuppressWarnings("serial")
public class TestPanelJ2D extends JPanel implements TestbedPanel {

    public static final int SCREEN_DRAG_BUTTON = 3;

    public static final int INIT_WIDTH = 600;
    public static final int INIT_HEIGHT = 600;

    private Graphics2D dbg = null;
    private Image dbImage = null;

    private int panelWidth;
    private int panelHeight;

    private final PhysicsController controller;

    public TestPanelJ2D(final TestbedState model, final PhysicsController controller) {
        this.controller = controller;
        setBackground(Color.black);
        setPreferredSize(new Dimension(INIT_WIDTH, INIT_HEIGHT));
        setIgnoreRepaint(true);
        updateSize(INIT_WIDTH, INIT_HEIGHT);

        AWTPanelHelper.addHelpAndPanelListeners(this, model, controller, SCREEN_DRAG_BUTTON);
//        addComponentListener(new ComponentAdapter() {
//            @Override
//            public void componentResized(ComponentEvent e) {
//                updateSize(getWidth(), getHeight());
//                dbImage = null;
//            }
//        });

    }

    public final Graphics2D getDBGraphics() {
        return dbg;
    }

    private void updateSize(int width, int height) {
        panelWidth = width;
        panelHeight = height;
        controller.updateExtents(width / 2, height / 2);
    }

    public boolean render() {
        if (dbImage == null) {
            //System.out.println("dbImage is null, creating a new one");
            if (panelWidth <= 0 || panelHeight <= 0) {
                return false;
            }
            dbImage = createImage(panelWidth, panelHeight);
            if (dbImage == null) {
                System.err.println("dbImage is still null, ignoring render call");
                return false;
            }
            dbg = (Graphics2D) dbImage.getGraphics();            
            dbg.setRenderingHints(new RenderingHints(
                     RenderingHints.KEY_COLOR_RENDERING,
                     RenderingHints.VALUE_COLOR_RENDER_SPEED));
            dbg.setRenderingHints(new RenderingHints(
                     RenderingHints.KEY_RENDERING,
                     RenderingHints.VALUE_RENDER_SPEED));
        }
        dbg.setColor(Color.black);
        dbg.fillRect(0, 0, panelWidth, panelHeight);
        return true;
    }

    public void paintScreen() {
        try {
            Graphics g = this.getGraphics();
            if ((g != null) && dbImage != null) {
                g.drawImage(dbImage, 0, 0, null);
                //Toolkit.getDefaultToolkit().sync();
                g.dispose();
            }
        } catch (AWTError e) {
            System.err.println("Graphics context error" + e);
        }
    }
}
