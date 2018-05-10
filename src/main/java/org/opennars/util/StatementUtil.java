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
