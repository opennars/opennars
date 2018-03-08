/*
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
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */

package nars.io.ports;

import java.io.IOException;

/**
 * 
 * Provides input for the next moment from an input channel that delivers input asynchronously
 */
public interface Input<X> {
   
    /** returns next input if available, null if none */
    public X next() throws IOException;
    
    /**
     * 
     * @param stop - if true, this Input should terminate (ex: close connections) because 
     * it has been removed from NAR.
     * @return whether this input is finished
     */      
    default public boolean finished(boolean stop) {
        return false;
    }
}
