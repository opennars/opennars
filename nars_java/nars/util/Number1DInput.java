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

package nars.util;

import java.text.NumberFormat;
import nars.core.NAR;
import nars.io.Input;
import nars.io.TextInput;


//TODO do not create new TextInput's, instead buffer inputs and return them through next() only
public class Number1DInput implements Input {
    /*
    
    %%%%%%%%%%%%%%%%%%%% Understanding rational numbers if needed %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    qadd([AZ,AN],[BZ,BN],C) :-  AN#\=0, BN#\=0, CN1#=AN*BN,    CZ1#=AZ*BN+BZ*AN, qequals([CZ1,CN1],C).
    qmul([AZ,AN],[BZ,BN],C) :-  AN#\=0, BN#\=0, CN1#=AN*BN,    CZ1#=AZ*BZ,       qequals([CZ1,CN1],C).
    qbigger([AZ,AN],[BZ,BN]) :- AN#\=0, BN#\=0, AZ*BN#>=BZ*AN.
    qequals([AZ,AN],[BZ,BN]):-  AN#\=0, BN#\=0, AZ*BN#=BZ*AN.
    qrats([]).
    qrats([LH|LT]):- LH in -100..100, qrats(LT). %bound rationals to finite domain
    %example query: http://upload.wikimedia.org/math/8/e/5/8e50b51a3fc429e753d82c099b38f06a.png
    %Rats=[X3Z,X3N,LeftZ,LeftN,XZ,XN], qrats(Rats), qmul([3,1],[XZ,XN],[X3Z,X3N]), qmul([LeftZ,LeftN],[X3Z,X3N],[2,1]), qequals([LeftZ,LeftN],[6,1]), label(Rats).
    %will result in XZ=-11 and XN=-99 which is 1/9
    
    */
    
    private double[] data;
    //TODO subclasses: NormalizedArray1DInput, Synchronous, Asynch, ..
    private boolean changed;
    private final String id;
    private final NAR nar;
    private static NumberFormat nf = NumberFormat.getInstance();
    boolean finished = false;
    
    /**
     * 
     * @param id (used for concept prefixes)
     */
    public Number1DInput(NAR n, String id, double[] data) {
        this.nar = n;
        this.id = id;
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);        
        
        initPredicates(n, data.length);
        set(data);
        
        nar.addInput(this);
        
    }
    
    public static void initPredicates(NAR N, int n) {
        
        StringBuffer s = new StringBuffer(32);
        
        //relative sequence
        for (int i = 1; i < n; i++) {
            s.append("<(*,ELEMENT_" + (i-1) + ",ELEMENT_" + (i) +") --> NEXT>. %1.00;1.00%\n");
        }
        
        //absolute position inheriting proportionally from ELEMENT_FIRST and ELEMENT_LAST
        for (int i = 0; i < n; i++) {
            double di = (double)i;
            String fp = nf.format( di / ((double)n-1) );
            String lp = nf.format( 1.0 - (di / ((double)n-1)) );
            s.append("<ELEMENT_" + i + " --> ELEMENT_FIRST>. %" + lp + ";1.00%\n");
            s.append("<ELEMENT_" + i + " --> ELEMENT_LAST>. %" + fp + ";1.00%\n");
        }
        
        String in = nf.format( 1.0 / ((double)n) );
        s.append("<(*,ELEMENT_FIRST,ELEMENT_LAST) --> NEXT>. %" + in + ";1.00%\n");
        
        s.append("<ZERO <-> ONE>. %0.00;1.00%\n"); //ONE and ZERO are inequal
        s.append("<(*,ZERO,ONE) --> DISTINCT_INTEGERS>. %0.00;1.00%\n");
        s.append("<ELEMENT_FIRST <-> ELEMENT_LAST>. %0.00;1.00%\n"); //first and last are inequal
        
        new TextInput(N, s.toString());
    }

    
    
    @Override
    public Object next() {
        if (changed) {
            if (data==null) {
                //erase existing statements?
            }
            else {                
               new TextInput(nar, getStatements());
            }
            
            changed = false;
        }
        return null;
    }
    
    final String cert = "1.00"; //default certainty
    
    public String getStatements() {

        StringBuffer sb = new StringBuffer();        

        String product = "<(*,";
        for (int i = 0; i < data.length; i++) {
            
            product += id + "_" + i;
            if (i < data.length-1)
                product += ",";
        }
        product += ") --> " + id + ">. %0.99;" + cert + "%\n";
        sb.append(product);
        
        
        for (int i = 0; i < data.length; i++) {
            String[] s = getStatementsInheritsMinMaxProportionally(i, data[i]);
            for (String t : s)
                sb.append(t + "\n");
        }
        
        return sb.toString();
    }
    
    @Deprecated public String[] getStatementsFrequencyEncoded(int i, double value) {
        String freq = nf.format(value);
            //<(*, id_i, id) --> array_value_i>. %1.00;0.79%                    
        String s = "<(*," + id + "_" + i + "," + id + ") --> ELEMENT_" + i + ">. %" + freq + ";" + cert + "%";
        return new String[] { s };
    }

   public String[] getStatementsInheritsMinMaxProportionally(int i, double value) {
        double zp = value;
        double op = 1.0 - value;
        return new String[] { 
            //"<(*," + id + "_" + i + ",ELEMENT_" + id + ") --> ELEMENT>. %0.99;" + cert + "%",
            "<" + id + "_" + i + " --> ELEMENT_" + i + ">. %0.99;" + cert + "%",
            "<" + id + "_" + i + " --> ZERO>. %" + zp + ";" + cert + "%",
            "<" + id + "_" + i + " --> ONE>. %" + op + ";" + cert + "%",
        };
    }    
    
    @Override
    public boolean finished(boolean force) {
        if (force)
            finished = true;
        return finished;
    }

    private void set(double[] data) {
        this.data = data;
        changed = true;
    }
    
    
}
