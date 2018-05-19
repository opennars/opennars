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
package org.opennars.inference;

import org.opennars.language.Statement;
import org.opennars.language.Term;

/**
 * Provides tools for AST/Term transformations
 */
// TODO< may get called by a Interpreter or JIT to make it flexible without loosing a lot of performance >
public class InferenceCommands {
    public static class Context {
        public Term[] termRegisters;

        public static Context make() {
            Context createdContext = new Context();
            createdContext.termRegisters = new Term[8];
            return createdContext;
        }

        public void appendSuccess(boolean success) {
            privateSuccess &= success;
        }

        public boolean retSuccess() {
            return privateSuccess;
        }

        private boolean privateSuccess = true;
    }

    public static class PathElement {
        int index; // inside in the term

        Class<?> termType; // expected term type
    }


    // gathers (reads) from the source term by a path the term and stores it into a termRegister in the context
    public static void gather(Context context, int destinationTermRegister, int sourceTermRegister, PathElement... path) {
        Term currentTerm = context.termRegisters[sourceTermRegister];

        for( int pathIdx = 0; pathIdx <= path.length; pathIdx++ ) {
            boolean isLastPathElement = pathIdx == path.length;
            if( isLastPathElement ) {
                context.termRegisters[destinationTermRegister] = currentTerm;
                context.appendSuccess(true); // successful by default
                return;
            }
            else {
                PathElement currentPathElement = path[pathIdx];

                if( currentTerm.getClass() != currentPathElement.termType ) {
                    context.appendSuccess(false);
                    return;
                }

                if( currentTerm instanceof Statement ) {
                    Statement currentStatement = (Statement)currentTerm;
                    if( currentPathElement.index == 0 ) {
                        currentTerm = currentStatement.getSubject();
                    }
                    else if( currentPathElement.index == 1 ) {
                        currentTerm = currentStatement.getPredicate();
                    }
                    else {
                        // invalid index - this is a soft error which has to be propagated to the caller(which can be native code or interpreted/JIT'ed code)
                        context.appendSuccess(false);
                        return;
                    }
                }
            }
        }

    }

    // DEPRECATED because copulaSourceStatement is a hack around a issue with the creation of a Statement using a copula from a enumeration
    /**
     *
     * @param context
     * @param destinationTermRegister
     * @param copulaSourceStatement   statement used to retrieve the copula  DEPRECATED!
     * @param sourceTermRegisterOfSubject term register
     * @param sourceTermRegisterOfPredicate
     * @param order the temporal order of the statement
     */
    public static void makeStatementWithOrderDeprecatedOrFail(final Context context, final int destinationTermRegister, Statement copulaSourceStatement, int sourceTermRegisterOfSubject, int sourceTermRegisterOfPredicate, final int order) {
        Term subject = context.termRegisters[sourceTermRegisterOfSubject];
        Term predicate = context.termRegisters[sourceTermRegisterOfPredicate];

        Term resultTerm = Statement.make(copulaSourceStatement, subject, predicate, order);
        context.appendSuccess(resultTerm != null);

        context.termRegisters[destinationTermRegister] = resultTerm;
    }

}
