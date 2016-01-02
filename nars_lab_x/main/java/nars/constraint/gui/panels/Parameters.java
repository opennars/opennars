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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.LinkedList;
import java.util.List;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 06/06/2014
 */
public class Parameters extends APanel {


    List<APanel> panels = new LinkedList<APanel>();
    List<JCheckBox> tpanels = new LinkedList<JCheckBox>();

    public Parameters(GUI frame) {
        super(frame);
    }

    @Override
    public void plug(JTabbedPane tabbedpanel) {
        panels.add(new FreeVarsPanel(frame));
        tpanels.add(addCheckbox("Free variables", 0, tabbedpanel));

        panels.add(new DepthPanel(frame));
        tpanels.add(addCheckbox("Depth", 1, tabbedpanel));

        panels.add(new ObjectivePanel(frame));
        tpanels.add(addCheckbox("Objective", 2, tabbedpanel));

        panels.add(new LeftRightBranchPanel(frame));
        tpanels.add(addCheckbox("LR decisions", 3, tabbedpanel));

        panels.add(new ColorVariablesPanel(frame));
        tpanels.add(addCheckbox("Domain state", 4, tabbedpanel));

        panels.add(new BinaryTreePanel(frame));
        tpanels.add(addCheckbox("Tree", 5, tabbedpanel));

        for (APanel panel : panels) {
            panel.plug(tabbedpanel);
        }

        if (!((ObjectivePanel) panels.get(2)).isOpt) {
            tpanels.get(2).setEnabled(false);
            panels.get(2).unplug(tabbedpanel);
        }

        tabbedpanel.addTab("Parameters", this);
    }


    private JCheckBox addCheckbox(String text, final int idx, final JTabbedPane tabbedpanel) {
        JCheckBox cbox = new JCheckBox(text);
        cbox.setSelected(true);
        cbox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    panels.get(idx).plug(tabbedpanel);
                } else {
                    panels.get(idx).unplug(tabbedpanel);
                }
            }
        });
        add(cbox);
        return cbox;
    }

    @Override
    public void unplug(JTabbedPane tabbedpanel) {
    }

    public void flushNow() {
        for (APanel panel : panels) {
            panel.flushData();
        }
    }
}
