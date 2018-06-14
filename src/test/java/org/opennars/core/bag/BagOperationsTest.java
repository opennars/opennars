/**
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
/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
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
        assertEquals(0.1f, b.getMinPriority(),0.001f);
        assertEquals(0.2f, b.getMaxPriority(),0.001f);
        
        //if (b instanceof GearBag()) return;
        
        
        b.putIn(makeConcept("b", 0.4f));
        
        
        assertEquals(2, b.size());
        assertEquals(0.2f, b.getMinPriority(),0.001f);
        assertEquals(0.4f, b.getMaxPriority(),0.001f);
        
        
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
