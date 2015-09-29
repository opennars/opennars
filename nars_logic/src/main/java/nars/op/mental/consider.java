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

package nars.op.mental;

import nars.concept.Concept;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.nal.Deriver;
import nars.nal.nal8.Operation;
import nars.nal.nal8.Operator;
import nars.nal.nal8.operator.SyncOperator;
import nars.process.ConceptTaskTermLinkProcess;
import nars.task.Task;
import nars.term.Term;

import java.util.ArrayList;

/**
 * Operator that activates a concept
 */
public class consider extends SyncOperator implements Mental {

    public static Operator consider = Operator.the("consider");

    final Deriver deriver = null; //new SimpleDeriver();

    /**
     * To activate a concept as if a question has been asked about it
     * Arguments, a Statement followed by an optional tense
     * @return Immediate results as Tasks
     */
    @Override
    public ArrayList<Task> apply(Task<Operation> operation) {
        Term term = operation.getTerm().arg(0);
        
        Concept concept = nar.conceptualize(term, operation.getBudget());

        TaskLink taskLink = concept.getTaskLinks().peekNext();
        TermLink termLink = concept.getTermLinks().peekNext();
        if ((taskLink!=null) && (termLink!=null)) {

            nar.input(
                new ConceptTaskTermLinkProcess(nar, concept, taskLink, termLink).derive(deriver)
            );

        }
        
        return null;
    }

}
