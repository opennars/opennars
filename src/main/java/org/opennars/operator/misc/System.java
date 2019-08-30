/* 
 * The MIT License
 *
 * Copyright 2018 The OpenNARS authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.opennars.operator.misc;

import org.opennars.language.Term;
import org.opennars.operator.FunctionOperator;
import org.opennars.storage.Memory;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Count the number of elements in a set
 */
public class System extends FunctionOperator {

    public System() {
        super("^system");
    }

    @Override
    protected Term function(final Memory memory, final Term[] x) {
        String cmd="";
        for(int i=0; i<x.length;++i)
        {
            cmd += x[i].name().toString() + " ";
        }
        String s;
        String ret="";
        String[] cmds={"bash","-c",cmd};
        Runtime r;
        Process p;
        try {
            r = Runtime.getRuntime();
            p=r.exec(cmds);
            BufferedReader br = new BufferedReader(
                new InputStreamReader(p.getInputStream()));
            while ((s = br.readLine()) != null)
                ret += s;
                //System.out.println("line: " + s);
            p.waitFor();
            //System.out.println ("exit: " + p.exitValue());
            p.destroy();
        } catch (Exception e) {}
        return new Term(ret);
    }

    @Override
    protected Term getRange() {
        return Term.get("system_called");
    }
    
}
