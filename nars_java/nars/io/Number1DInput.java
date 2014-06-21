/*
 * Copyright (C) 2014 me
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nars.io;

import nars.core.NAR;
import org.python.icu.text.NumberFormat;

/**
 * 
 * @author me
 */
public class Number1DInput implements InputChannel {
    private double[] data;
    //TODO subclasses: NormalizedArray1DInput, Synchronous, Asynch, ..
    private boolean changed;
    private final String id;
    private final NAR nar;
    private NumberFormat nf;
    /**
     * 
     * @param id (used for concept prefixes)
     */
    public Number1DInput(NAR n, String id, double[] data) {
        this.nar = n;
        this.id = id;
        nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);        
        
        set(data);
        
        nar.addInputChannel(this);
    }

    
    
    @Override
    public boolean nextInput() {
        if (changed) {
            if (data==null) {
                //erase existing statements?
            }
            else {
                final String cert = "0.99"; //default certainty
                
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < data.length; i++) {
                    //<(*, id_i, id) --> array_value_i>. %1.00;0.79%                    
                    String freq = nf.format(data[i]);
                    
                    
                    String s = 
                            "<(*," + id + "_" + i + "," + id + ") --> array_value_" + i + ">. %" + freq + ";" + cert + "%\n";
                    sb.append(s);
                }
                new TextInput(nar, sb.toString());
            }
            
            changed = false;
        }
        return true;
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    private void set(double[] data) {
        this.data = data;
        changed = true;
    }
    
    
}
