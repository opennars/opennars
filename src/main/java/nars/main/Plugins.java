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
package nars.main;

import nars.operator.mental.Anticipate;
import nars.plugin.mental.FullInternalExperience;
import nars.plugin.mental.InternalExperience;
import nars.plugin.misc.RuntimeNARSettings;
import nars.plugin.mental.Emotions;
import nars.plugin.mental.Counting;
import nars.plugin.mental.Abbreviation;
import nars.language.SetInt;
import nars.language.Term;
import nars.plugin.perception.VisionChannel;

/**
 * Default set of NAR parameters which have been classically used for development.
 */
public class Plugins {

    public NAR init(NAR n) {         
        n.addPlugin(new RuntimeNARSettings());
        n.addPlugin(new Emotions());
        n.addPlugin(new Anticipate());      // expect an event 
        Term label = SetInt.make(new Term("bright"));
        //n.addSensoryChannel(label.toString(),
        //                    new VisionChannel(label, n, n, 30, 30));
        boolean full_internal_experience = false;
        if(!full_internal_experience) {
            n.addPlugin(new InternalExperience());
        }
        else {
            n.addPlugin(new FullInternalExperience());
            n.addPlugin(new Abbreviation());
            n.addPlugin(new Counting());
        }
        
        return n;
    }
}
