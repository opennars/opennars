/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
 *
 * @author patha
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
