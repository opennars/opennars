/*
 * tuProlog - Copyright (C) 2001-2004  aliCE team at deis.unibo.it
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package nars.tuprolog.gui.ide;

import java.util.ArrayList;

/**
 * A repository for history of goals which resolution has been asked for.
 * 
 * @author    <a href="mailto:giulio.piancastelli@studio.unibo.it">Giulio Piancastelli</a>
 * @version    1.0 - Tuesday 6th July, 2004
 */
class History {
    
    private ArrayList<String> history;
    private int index;
    
    public History() {
        history = new ArrayList<>(1);
        history.add("");
        index = 0;
    }
    
    public void add(String item) {
        index = history.size() - 1;
        history.add(index, item);
    }
    
    /**
     * Get the previous element in history. If the lower bound of the
     * history is crossed, the first element in history is returned.
     * 
     * @return The previous element in history, or the first element
     * in history if its lower bound is crossed.
     */
    public String previous() {
        if (history.isEmpty()) return "";
        index--;
        if (index < 0)
            index = 0;
        return history.get(index);
    }

    /**
     * Get the next element in history. If the upper bound of
     * the history is crossed, an empty element is returned.
     * 
     * @return The next element in history, or an empty element
     * if history's upper bound is crossed.
     */
    public String next() {
        if (history.isEmpty()) return "";
        index++;
        if (index >= history.size())
            index = history.size() - 1;
        return history.get(index);
    }

} // end History class
