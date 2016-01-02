/*
 * Java Arcade Learning Environment (A.L.E) Agent
 *  Copyright (C) 2011-2012 Marc G. Bellemare <mgbellemare@ualberta.ca>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jurls.reinforcementlearning.domains.arcade.gui;

import java.awt.image.BufferedImage;

/** An empty UI for running console experiments. All abstract methods are implemented
 *   to do nothing.
 *
 * @author Marc G. Bellemare
 */
public class NullUI implements AbstractUI {
    @Override
    public void die() {
    }

    @Override
    public void setImage(BufferedImage img) {
    }

    @Override
    public void setCenterString(String s) {
    }

    @Override
    public void addMessage(String s) {
    }

    @Override
    public int getKeyboardAction() {
        return 0;
    }

    @Override
    public void updateFrameCount() {
    }

    @Override
    public boolean quitRequested() {
        return false;
    }
    
    @Override
    public void refresh() {
    }
}
