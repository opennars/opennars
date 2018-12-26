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
package org.opennars.core.bag;

import org.junit.Test;
import org.opennars.entity.BudgetValue;
import org.opennars.entity.Concept;
import org.opennars.entity.Item;
import org.opennars.language.Term;
import org.opennars.main.Nar;
import org.opennars.storage.Bag;
import org.opennars.storage.LevelBag;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.opennars.main.Parameters;

/**
 *
 * @author me
 */
public class BagOperationsTest {

    private static Parameters narParameters;
    static Nar nar;

    static {
        try {
            nar = new Nar();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    static Concept makeConcept(final String name, final float priority) {
        final BudgetValue budg = new BudgetValue(priority,priority,priority,narParameters);
        final Concept s = new Concept(budg,new Term(name),nar.memory);
        return s;
    }  
    
    @Test
    public void testConcept() throws Exception {
        Nar nar = new Nar();
        this.narParameters = nar.narParameters;
        testBagSequence(new LevelBag(2, 2, nar.narParameters));    
    }

    public static float getMinPriority(Bag<Concept,Term> bag) {
        float min = 1.0f;
        for (final Item e : bag) {
            final float p = e.getPriority();
            if (p < min) min = p;
        }
        return min;            
    }
    public static float getMaxPriority(Bag<Concept,Term> bag) {
        float max = 0.0f;
        for (final Item e : bag) {
            final float p = e.getPriority();
            if (p > max) max = p;
        }
        return max;
    }
    
    public static void testBagSequence(final Bag b) {

        //different id, different priority
        b.putIn(makeConcept("a", 0.1f));
        b.putIn(makeConcept("b", 0.15f));
        assertEquals(2, b.size());
        b.clear();
        
        //same priority, different id
        b.putIn(makeConcept("a", 0.1f));
        b.putIn(makeConcept("b", 0.1f));
        assertEquals(2, b.size());
        
        b.putIn(makeConcept("c", 0.2f));
        assertEquals(2, b.size());
        assertEquals(0.1f, getMinPriority(b),0.001f);
        assertEquals(0.2f, getMaxPriority(b),0.001f);
        
        //if (b instanceof GearBag()) return;
        
        
        b.putIn(makeConcept("b", 0.4f));
        
        
        assertEquals(2, b.size());
        assertEquals(0.2f, getMinPriority(b),0.001f);
        assertEquals(0.4f, getMaxPriority(b),0.001f);
        
        
        final Item tb = b.take(new Term("b"));
        assertTrue(tb!=null);
        assertEquals(1, b.size());
        assertEquals(0.4f, tb.getPriority(), 0.001f);
        
        final Item tc = b.takeNext();
        assertEquals(0, b.size());
        assertEquals(0.2f, tc.getPriority(), 0.001f);
        
        
        
        assertEquals(null, b.putIn(makeConcept("a", 0.2f)));
        assertEquals(null, b.putIn(makeConcept("b", 0.3f)));
        
        if (b instanceof LevelBag) {
            assertEquals("a", b.putIn(makeConcept("c", 0.1f)).name().toString()); //replaces item on level
        }
        
    }
}
