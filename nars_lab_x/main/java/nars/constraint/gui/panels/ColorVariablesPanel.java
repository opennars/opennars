/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nars.constraint.gui.panels;

import nars.constraint.gui.GUI;
import org.chocosolver.solver.search.loop.monitors.IMonitorOpenNode;

import java.awt.image.BufferedImage;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 05/06/2014
 */
public class ColorVariablesPanel extends APanel implements IMonitorOpenNode {

    int size;
    int psize = 10;

    BufferedImage image;
    JLabel theLabel;
    ImageIcon icon;


    public ColorVariablesPanel(GUI frame) {
        super(frame);
        solver.plugMonitor(this);
        size = (int) Math.ceil(Math.sqrt(solver.getNbVars()));
        psize = 600 / size;
        image = new BufferedImage(size * psize, size * psize, BufferedImage.TYPE_INT_ARGB);
        solver.plugMonitor(this);
        icon = new ImageIcon(image);
        theLabel = new JLabel(new ImageIcon(image));
        add(theLabel);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int i = 0; i < solver.getNbVars(); i++) {
            int x = i / size;
            int y = i % size;
            Color color = solver.getVar(i).isInstantiated() ? Color.GREEN : Color.BLUE;
            for (int j = 0; j < psize; j++)
                for (int k = 0; k < psize; k++)
                    image.setRGB(x * psize + j, y * psize + k, color.getRGB());
        }
    }

    @Override
    public void plug(JTabbedPane tabbedpanel) {
        super.plug(tabbedpanel);
        tabbedpanel.addTab("Color map", this);
    }

    @Override
    public void beforeOpenNode() {
    }


    @Override
    public void afterOpenNode() {
        if (frame.canUpdate() && activate) {
            repaint();
        }
    }
}