/*
 * Copyright (C) 2014 peiwang
 *
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
package nars.inference;


import nars.core.Parameters;
import nars.entity.*;
import nars.language.*;
import nars.storage.*;

/**
 *
 * @author peiwang
 */
public class TemporalRules {

    public static final int ORDER_NONE = 2;
    public static final int ORDER_FORWARD = 1;
    public static final int ORDER_CONCURRENT = 0;
    public static final int ORDER_BACKWARD = -1;
    public static final int ORDER_INVALID = -2;

    public static int reverseOrder(int order) {
        if (order == ORDER_NONE) {
            return ORDER_NONE;
        } else {
            return -order;
        }
    }

    public static boolean matchingOrder(int order1, int order2) {
        return (order1 == order2) || (order1 == ORDER_NONE) || (order2 == ORDER_NONE);
    }

    public static int dedExeOrder(int order1, int order2) {
        int order = ORDER_INVALID;
        if ((order1 == order2) || (order2 == TemporalRules.ORDER_NONE)) {
            order = order1;
        } else if ((order1 == TemporalRules.ORDER_NONE) || (order1 == TemporalRules.ORDER_CONCURRENT)) {
            order = order2;
        } else if (order2 == TemporalRules.ORDER_CONCURRENT) {
            order = order1;
        }
        return order;
    }

    public static int abdIndComOrder(int order1, int order2) {
        int order = ORDER_INVALID;
        if (order2 == TemporalRules.ORDER_NONE) {
            order = order1;
        } else if ((order1 == TemporalRules.ORDER_NONE) || (order1 == TemporalRules.ORDER_CONCURRENT)) {
            order = reverseOrder(order2);
        } else if ((order2 == TemporalRules.ORDER_CONCURRENT) || (order1 == -order2)) {
            order = order1;
        }
        return order;
    }

    public static int analogyOrder(int order1, int order2, int figure) {
        int order = ORDER_INVALID;
        if ((order2 == TemporalRules.ORDER_NONE) || (order2 == TemporalRules.ORDER_CONCURRENT)) {
            order = order1;
        } else if ((order1 == TemporalRules.ORDER_NONE) || (order1 == TemporalRules.ORDER_CONCURRENT)) {
            order = (figure < 20) ? order2 : reverseOrder(order2);
        } else if (order1 == order2) {
            if ((figure == 12) || (figure == 21)) {
                order = order1;
            }
        } else if ((order1 == -order2)) {
            if ((figure == 11) || (figure == 22)) {
                order = order1;
            }
        }
        return order;
    }

    public static int resemblanceOrder(int order1, int order2, int figure) {
        int order = ORDER_INVALID;
        if ((order2 == TemporalRules.ORDER_NONE)) {
            order = (figure > 20) ? order1 : reverseOrder(order1); // switch when 11 or 12
        } else if ((order1 == TemporalRules.ORDER_NONE) || (order1 == TemporalRules.ORDER_CONCURRENT)) {
            order = (figure % 10 == 1) ? order2 : reverseOrder(order2); // switch when 12 or 22
        } else if (order2 == TemporalRules.ORDER_CONCURRENT) {
            order = (figure > 20) ? order1 : reverseOrder(order1); // switch when 11 or 12
        } else if (order1 == order2) {
            order = (figure == 21) ? order1 : -order1;
        }
        return order;
    }

    public static int composeOrder(int order1, int order2) {
        int order = ORDER_INVALID;
        if (order2 == TemporalRules.ORDER_NONE) {
            order = order1;
        } else if (order1 == TemporalRules.ORDER_NONE) {
            order = order2;
        } else if (order1 == order2) {
            order = order1;
        }
        return order;
    }

    public static void temporalInduction(Sentence s1, Sentence s2, Memory memory) {
        Term t1 = s1.cloneContent();
        Term t2 = s2.cloneContent();
        if (Statement.invalidStatement(t1, t2)) {
            return;
        }
        long time1 = s1.getOccurenceTime();
        long time2 = s2.getOccurenceTime();
        long timeDiff = time2 - time1;
        Interval interval = null;
        if (Math.abs(timeDiff) > Parameters.DURATION) {
            interval = new Interval(Math.abs(timeDiff));
            if (timeDiff > 0) {
                t1 = Conjunction.make(t1, interval, ORDER_FORWARD, memory);
            } else {
                t2 = Conjunction.make(t2, interval, ORDER_FORWARD, memory);
            }
        }
        int order;
        if (timeDiff > Parameters.DURATION) {
            order = TemporalRules.ORDER_FORWARD;
        } else if (timeDiff < -Parameters.DURATION) {
            order = TemporalRules.ORDER_BACKWARD;
        } else {
            order = TemporalRules.ORDER_CONCURRENT;
        }
        TruthValue givenTruth1 = s1.truth;
        TruthValue givenTruth2 = s2.truth;
        TruthValue truth1 = TruthFunctions.abduction(givenTruth1, givenTruth2);
        TruthValue truth2 = TruthFunctions.abduction(givenTruth2, givenTruth1);
        TruthValue truth3 = TruthFunctions.comparison(givenTruth1, givenTruth2);
        BudgetValue budget1 = BudgetFunctions.forward(truth1, memory);
        BudgetValue budget2 = BudgetFunctions.forward(truth2, memory);
        BudgetValue budget3 = BudgetFunctions.forward(truth3, memory);
        Statement statement1 = Implication.make(t1, t2, order, memory);
        Statement statement2 = Implication.make(t2, t1, reverseOrder(order), memory);
        Statement statement3 = Equivalence.make(t1, t2, order, memory);
        memory.doublePremiseTask(statement1, truth1, budget1);
        memory.doublePremiseTask(statement2, truth2, budget2);
        memory.doublePremiseTask(statement3, truth3, budget3);
    }
}
