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
package org.opennars.main;

import org.opennars.language.SetInt;
import org.opennars.language.Term;
import org.opennars.operator.mental.Anticipate;
import org.opennars.plugin.mental.*;
import org.opennars.plugin.misc.RuntimeNARSettings;
import org.opennars.plugin.perception.VisionChannel;

/**
 * Default set of Nar parameters which have been classically used for development.
 */
public class Plugins {

    public Nar init(final Nar n) {
        n.addPlugin(new RuntimeNARSettings());
        n.addPlugin(new Emotions());
        n.addPlugin(new Anticipate());      // expect an event 
        final Term label = SetInt.make(new Term("BRIGHT"));
        //Add a vision channel:
        final int sensor_W = 5;
        final int sensor_H = 5;
        n.addSensoryChannel(label.toString(),
                            new VisionChannel(label, n, n, sensor_H, sensor_W, sensor_W*sensor_H));
        //allow NAL9 capabilities:
        final boolean full_internal_experience = false;
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
