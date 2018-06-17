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
package org.opennars.util.test;

import org.opennars.entity.Concept;
import org.opennars.entity.Sentence;
import org.opennars.entity.Task;
import org.opennars.io.Narsese;
import org.opennars.io.Symbols;
import org.opennars.language.CompoundTerm;
import org.opennars.language.Term;
import org.opennars.main.Nar;

/**
 *
 * @author patrick.hammer
 */
public class ConceptMonitor {
    
    public static Term stringToTerm(final Nar nar, final String s) {
        final Narsese narsese = new Narsese(nar.memory);
        final Task ret;
        try {
            ret = narsese.parseTask(s + Symbols.JUDGMENT_MARK);
        } catch (final Narsese.InvalidInputException ex) {
            throw new IllegalStateException("Could not parse task", ex);
        }
        if(ret == null) {
            return null;
        }
        return ret.getTerm();
    }
    
    public static Concept concept(final Nar nar, final String s) {
        final Term ts = stringToTerm(nar, s);
        if(ts == null) {
            return null;
        }
        return nar.memory.concept(ts);
    }
    
    public static Sentence strongestProjectedInputEventBelief(final Nar nar, final String st) {
        final Concept c = ConceptMonitor.concept(nar, st);
        if(c != null) {
            for(final Task t : c.beliefs) {
                if(t.isInput() && !t.sentence.isEternal()) {
                    final Sentence s = t.sentence;
                    final Sentence projected = s.projection(nar.time(), nar.time(), nar.memory);
                    if(!projected.isEternal()) {
                        return projected;
                    }
                }
            }
        }
        return null;
    }
    
    public static Sentence strongestProjectedEternalizedBelief(final Nar nar, final String st) {
        final Concept c = ConceptMonitor.concept(nar, st);
        if(c != null) {
            for(final Task t : c.beliefs) {
                final Sentence s = t.sentence;
                final Sentence projected = s.projection(nar.time(), nar.time(), nar.memory);
                return projected;
            }
        }
        return null;
    }
    
    public static Sentence strongestPrecondition(final Nar nar, final String conc, final String statement) {
        final Concept c = ConceptMonitor.concept(nar, conc);
        final Term st = stringToTerm(nar, statement);
        if(c != null && st != null) {
            for(final Task t : c.executable_preconditions) {
                if(CompoundTerm.replaceIntervals(t.getTerm()).equals(
                        CompoundTerm.replaceIntervals(st))) {
                    return t.sentence;
                }
            }
        }
        return null;
    }
    
    public static Sentence strongestPrecondition2(final Nar nar, final String conc, final String statement) { //test to compare with previous
        return strongestProjectedEternalizedBelief(nar, statement);
    }
}
