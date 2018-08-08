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
 *
 * @author patham9
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
