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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.Test;
import org.opennars.io.Narsese;
import org.opennars.io.Symbols;
import org.opennars.language.CompoundTerm;
import org.opennars.language.Term;
import org.opennars.language.Variables;
import org.opennars.main.Nar;
import org.xml.sax.SAXException;

/**
 * Tests the correct functionality of the unifier
 *
 * @author Patrick Hammer
 */
public class UnificationTest {
    @Test
    public void testUnificationTermination() {
        try {
            Nar nar = new Nar();
            Narsese parser = new Narsese(nar);
            CompoundTerm t1 = (CompoundTerm) parser.parseTerm("<(&&,$1#1$,$2,$4$3$,(#,$2,$1#1$,$4$3$)) ==> <(*,$1#1$,(*,(/,REPRESENT,$2,_),(/,REPRESENT,$4$3$,_))) --> REPRESENT>>");
            CompoundTerm t2 = (CompoundTerm) parser.parseTerm("<(&&,#1,$2,$3,(#,$2,#1,$3)) ==> <(*,#1,(*,(/,REPRESENT,$2,_),(/,REPRESENT,$3,_))) --> REPRESENT>>");
            Map[] unifier = new HashMap[]{new HashMap<Term,Term>(), new HashMap<Term,Term>()};
            Variables.findSubstitute(Symbols.VAR_DEPENDENT, t1, t2, unifier);
            //Variables.unify(0, t1, t2, compound)
            //findSubstitute(final char type, final Term term1, final Term term2, final Map<Term, Term>[] map, final boolean allowPartial)
            Variables.findSubstitute(Symbols.VAR_INDEPENDENT, t1, t2, unifier, true);
        } catch (Exception ex) {
            assert(false); //test failed, no matter what happened
        }
    }
}
