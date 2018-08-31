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
package org.opennars.core;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;


/**
 * runs a subset of the test cases, selected by the boolean include(filename) function
 */
@RunWith(Parameterized.class)
public class NALTestSome extends NALTest {

    static {
        showOutput = true;
        showSuccess = showOutput;
    }
    
   public static boolean include(final String filename) {
       //return true; //filename.startsWith("nal6.8.nal");
       return filename.startsWith("nal1.0.nal");
   }

   
    @Parameterized.Parameters
    public static Collection params() {
        final List l = new LinkedList();

        File folder = null;
        try {
            folder = new File(NALTestSome.class.getResource("/nal/single_step").toURI());
        } catch (final URISyntaxException e) {
            throw new IllegalStateException("Could not parse URI to nal test files.", e);
        }

        for (final File file : folder.listFiles()) {
            if (file.getName().equals("README.txt") || file.getName().contains(".png"))
                continue;
            if (include(file.getName()))
                l.add(new Object[] { file.getAbsolutePath() } );
        }
                  
        return l;
    }
   
   public static void main(final String[] args) {
        org.junit.runner.JUnitCore.runClasses(NALTestSome.class);
   }    

   public NALTestSome(final String scriptPath) {
       super(scriptPath);//, true);

   }

}
