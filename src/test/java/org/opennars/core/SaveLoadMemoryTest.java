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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.Test;
import org.opennars.entity.Concept;
import org.opennars.io.Narsese;
import org.opennars.main.Nar;
import org.xml.sax.SAXException;

/**
 * Tests for the correct functionality of the serialization fo the state
 *
 * @author Patrick Hammer
 */
public class SaveLoadMemoryTest {
    @Test
    public void testloadSaveMem() throws IOException, InstantiationException, NoSuchMethodException, SAXException, ParseException, 
            IllegalAccessException, InvocationTargetException, ParserConfigurationException, ClassNotFoundException, Narsese.InvalidInputException, Exception {
        Nar nar = new Nar();
        nar.addInput("<a --> b>.");
        nar.cycles(1);
        String fname = "test1.nars";
        nar.SaveToFile(fname);
        Nar nar2 = Nar.LoadFromFile(fname);
        Concept c2 = nar2.concept("<a --> b>");
        assert(c2 != null);
    }
}
