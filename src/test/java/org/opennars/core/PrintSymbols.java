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

import org.opennars.io.Symbols;
import org.opennars.io.Symbols.NativeOperator;/**
 *
 * @author me
 */


public class PrintSymbols {

    public static void main(String[] args) {
        int relations = 0;
        int innates = 0;
        int symbols = 0;
        
        System.out.println("string" + "\t\t" + "rel?" + "\t\t" + "innate?" + "\t\t" + "opener?" + "\t\t" + "closer?");
        for (NativeOperator i : Symbols.NativeOperator.values()) {
            System.out.println(i.symbol + "\t\t" + i.relation + "\t\t" + i.isNative + "\t\t" + i.opener + "\t\t" + i.closer); 
            if (i.relation) relations++;
            if (i.isNative) innates++;
            symbols++;
        }
        System.out.println("symbols=" + symbols + ", relations=" + relations + ", innates=" + innates);
    }
}
