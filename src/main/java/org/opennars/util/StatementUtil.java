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
package org.opennars.util;

import org.opennars.language.Statement;
import org.opennars.language.Term;

public class StatementUtil {
    /**
     * returns the subject (0) or predicate(1)
     * @param statement statement for which the side has to be returned
     * @param side subject(0) or predicate(1)
     * @return the term of the side
     */
    public static Term retBySide(final Statement statement, final EnumStatementSide side) {
        return side == EnumStatementSide.SUBJECT ? statement.getSubject() : statement.getPredicate();
    }

    public static EnumStatementSide retOppositeSide(final EnumStatementSide side) {
        return side == EnumStatementSide.SUBJECT ? EnumStatementSide.PREDICATE : EnumStatementSide.SUBJECT;
    }

    public enum EnumStatementSide {
        SUBJECT,
        PREDICATE,
    }
}
