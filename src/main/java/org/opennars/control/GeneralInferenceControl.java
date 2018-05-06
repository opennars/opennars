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
package org.opennars.control;

import org.opennars.main.Parameters;
import org.opennars.entity.Concept;
import org.opennars.io.events.Events;
import org.opennars.entity.Task;
import org.opennars.entity.TermLink;
import org.opennars.inference.BudgetFunctions;
import org.opennars.inference.RuleTables;
import org.opennars.storage.Memory;

/** Concept reasoning context - a concept is "fired" or activated by applying the reasoner */
public class GeneralInferenceControl {
    
    public static void selectConceptForInference(Memory mem) {
        Concept currentConcept = mem.concepts.takeNext();
        if (currentConcept==null) {
            return;
        }
        
        if(currentConcept.taskLinks.size() == 0) { //remove concepts without tasklinks and without termlinks
            mem.concepts.take(currentConcept.getTerm());
            mem.conceptRemoved(currentConcept);
            return;
        }
        if(currentConcept.termLinks.size() == 0) {  //remove concepts without tasklinks and without termlinks
            mem.concepts.take(currentConcept.getTerm());
            mem.conceptRemoved(currentConcept);
            return;
        }
        
        DerivationContext nal = new DerivationContext(mem);
        nal.setCurrentConcept(currentConcept);

        boolean putBackConcept = fireConcept(nal, 1);

        if(putBackConcept) { // put back
            float forgetCycles = nal.memory.cycles(nal.memory.param.conceptForgetDurations);
            nal.currentConcept.setQuality(BudgetFunctions.or(nal.currentConcept.getQuality(),nal.memory.emotion.happy()));
            nal.memory.concepts.putBack(nal.currentConcept, forgetCycles, nal.memory);
        }
    }

    // /return true if concept must be put back
    public static boolean fireConcept(DerivationContext nal, int numTaskLinks) {
        for (int i = 0; i < numTaskLinks; i++) {
            if (nal.currentConcept.taskLinks.size() == 0) {
                return false;
            }

            nal.currentTaskLink = nal.currentConcept.taskLinks.takeNext();                    
            if (nal.currentTaskLink == null) {
                return false;
            }

            if (nal.currentTaskLink.budget.aboveThreshold()) {
                fireTaskLink(nal, Parameters.TERMLINK_MAX_REASONED);                    
            }

            nal.currentConcept.taskLinks.putBack(nal.currentTaskLink, nal.memory.cycles(nal.memory.param.taskLinkForgetDurations), nal.memory);
        }
        return true;
    }
    
    protected static void fireTaskLink(DerivationContext nal, int termLinks) {
        final Task task = nal.currentTaskLink.getTarget();
        nal.setCurrentTerm(nal.currentConcept.term);
        nal.setCurrentTaskLink(nal.currentTaskLink);
        nal.setCurrentBeliefLink(null);
        nal.setCurrentTask(task); // one of the two places where this variable is set
        
        nal.memory.emotion.adjustBusy(nal.currentTaskLink.getPriority(),nal.currentTaskLink.getDurability(),nal);
        
        if (nal.currentTaskLink.type == TermLink.TRANSFORM) {
            nal.setCurrentBelief(null);
            //TermLink tasklink_as_termlink = new TermLink(nal.currentTaskLink.getTerm(), TermLink.TRANSFORM, nal.getCurrentTaskLink().index);
            //if(nal.currentTaskLink.novel(tasklink_as_termlink, nal.memory.time(), true)) { //then record yourself, but also here novelty counts
                RuleTables.transformTask(nal.currentTaskLink, nal); // to turn this into structural inference as below?
            //}
            
        } else {            
            while (termLinks > 0) {
                final TermLink termLink = nal.currentConcept.selectTermLink(nal.currentTaskLink, nal.memory.time());
                if (termLink == null) {
                    break;
                }
                fireTermlink(termLink, nal);
                nal.currentConcept.returnTermLink(termLink);
                termLinks--;
            }
        }
                
        nal.memory.emit(Events.ConceptFire.class, nal);
        //memory.logic.TASKLINK_FIRE.commit(currentTaskLink.budget.getPriority());
    }

    public static boolean fireTermlink(final TermLink termLink, DerivationContext nal) {
        nal.setCurrentBeliefLink(termLink);
        RuleTables.reason(nal.currentTaskLink, termLink, nal);

        nal.memory.emit(Events.TermLinkSelect.class, termLink, nal.currentConcept, nal);
        //memory.logic.REASON.commit(termLink.getPriority());                    
        return true;
    }
}
