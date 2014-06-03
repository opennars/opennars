/*
 * TermLinkBag.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.googlecode.opennars.storage;

import com.googlecode.opennars.entity.*;
import com.googlecode.opennars.main.Parameters;

/**
 * Contains CompositionLinks to relevant Terms.
 */
public class TermLinkBag extends Bag<TermLink> {

    private static final int maxTakeOut = Parameters.MAX_TAKE_OUT_K_LINK;
    
    protected int capacity() {
        return Parameters.BELIEF_BAG_SIZE;
    }
    
    protected int forgetRate() {
        return Parameters.BELIEF_DEFAULT_FORGETTING_CYCLE;
    }
    
    // replace defualt to prevent repeated inference
    public TermLink takeOut(TaskLink tLink) {
        for (int i = 0; i < maxTakeOut; i++) {
            TermLink bLink = takeOut();
            if (bLink == null)
                return null;
            if (tLink.novel(bLink)) {
                return bLink;
            }
            putBack(bLink);
        }
        return null;
    }
}

