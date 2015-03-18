package nars.logic.reason.concept;

import nars.core.Events;
import nars.core.Memory;
import nars.logic.BudgetFunctions;
import nars.logic.NAL;
import nars.logic.entity.*;
import nars.logic.nal1.Inheritance;
import nars.logic.nal4.ImageExt;
import nars.logic.nal4.ImageInt;
import nars.logic.nal4.Product;
import nars.logic.nal5.Conjunction;
import nars.logic.nal5.Equivalence;
import nars.logic.nal5.Implication;
import nars.logic.reason.ConceptProcess;


/**
 * ----- logic with one TaskLink only -----
 * The TaskLink is of type TRANSFORM, and the conclusion is an equivalent
 * transformation
 **/
public class TransformTask extends ConceptFireTask {

    @Override
    public boolean apply(ConceptProcess f, TaskLink t) {

        if (t.type == TermLink.TRANSFORM) {

            // to turn this into structural logic as below?
            CompoundTerm content = f.getCurrentTask().getTerm();
            short[] indices = t.index;
            Term inh = null;

            if ((indices.length == 2) || (content instanceof Inheritance)) {          // <(*, term, #) --> #>
                inh = content;
            } else if (indices.length == 3) {   // <<(*, term, #) --> #> ==> #>
                inh = content.term[indices[0]];
            } else if (indices.length == 4) {   // <(&&, <(*, term, #) --> #>, #) ==> #>
                Term component = content.term[indices[0]];
                if ((component instanceof Conjunction) && (((content instanceof Implication) && (indices[0] == 0)) || (content instanceof Equivalence))) {

                    Term[] cterms = ((CompoundTerm) component).term;
                    if (indices[1] < cterms.length-1)
                        inh = cterms[indices[1]];
                    else
                        return true;

                } else {
                    return true;
                }
            }

            if (inh instanceof Inheritance) {
                transformProductImage((Inheritance) inh, content, indices, f);
            }


            f.emit(Events.TermLinkTransformed.class, t, f.getCurrentConcept(), this);
            f.memory.logic.TERM_LINK_TRANSFORM.hit();
        }

        return true;
    }


    /* -------------------- products and images transform -------------------- */
    /**
     * Equivalent transformation between products and images {<(*, S, M) --> P>,
     * S@(*, S, M)} |- <S --> (/, P, _, M)> {<S --> (/, P, _, M)>, P@(/, P, _,
     * M)} |- <(*, S, M) --> P> {<S --> (/, P, _, M)>, M@(/, P, _, M)} |- <M -->
     * (/, P, S, _)>
     *
     * @param inh An Inheritance statement
     * @param oldContent The whole content
     * @param indices The indices of the TaskLink
     */
    public static void transformProductImage(Inheritance inh, CompoundTerm oldContent, short[] indices, NAL nal) {
        Term subject = inh.getSubject();
        Term predicate = inh.getPredicate();
        if (inh.equals(oldContent)) {
            if (subject instanceof CompoundTerm) {
                transformSubjectPI((CompoundTerm) subject, predicate, nal);
            }
            if (predicate instanceof CompoundTerm) {
                transformPredicatePI(subject, (CompoundTerm) predicate, nal);
            }
            return;
        }
        short index = indices[indices.length - 1];
        short side = indices[indices.length - 2];

        Term compT = inh.term[side];
        if (!(compT instanceof CompoundTerm))
            return;
        CompoundTerm comp = (CompoundTerm)compT;

        if (comp instanceof Product) {
            if (side == 0) {
                subject = comp.term[index];
                predicate = ImageExt.make((Product) comp, inh.getPredicate(), index);
            } else {
                subject = ImageInt.make((Product) comp, inh.getSubject(), index);
                predicate = comp.term[index];
            }
        } else if ((comp instanceof ImageExt) && (side == 1)) {
            if (index == ((ImageExt) comp).relationIndex) {
                subject = Product.make(comp, inh.getSubject(), index);
                predicate = comp.term[index];
            } else {
                subject = comp.term[index];
                predicate = ImageExt.make((ImageExt) comp, inh.getSubject(), index);
            }
        } else if ((comp instanceof ImageInt) && (side == 0)) {
            if (index == ((ImageInt) comp).relationIndex) {
                subject = comp.term[index];
                predicate = Product.make(comp, inh.getPredicate(), index);
            } else {
                subject = ImageInt.make((ImageInt) comp, inh.getPredicate(), index);
                predicate = comp.term[index];
            }
        } else {
            return;
        }

        Inheritance newInh = Inheritance.make(subject, predicate);
        if (newInh == null)
            return;

        CompoundTerm content = null;
        if (indices.length == 2) {
            content = newInh;
        } else if ((oldContent instanceof Statement) && (indices[0] == 1)) {
            content = Statement.make((Statement) oldContent, oldContent.term[0], newInh, oldContent.getTemporalOrder());
        } else {
            Term[] componentList;
            Term condition = oldContent.term[0];
            if (((oldContent instanceof Implication) || (oldContent instanceof Equivalence)) && (condition instanceof Conjunction)) {
                componentList = ((CompoundTerm) condition).cloneVariableTermsDeep(); //cloneTerms();
                componentList[indices[1]] = newInh;
                Term newCond = Memory.term((CompoundTerm) condition, componentList);
                content = Statement.make((Statement) oldContent, newCond, ((Statement) oldContent).getPredicate(), oldContent.getTemporalOrder());
            } else {

                componentList = oldContent.cloneVariableTermsDeep(); //oldContent.cloneTerms();
                componentList[indices[0]] = newInh;
                if (oldContent instanceof Conjunction) {
                    Term newContent = Memory.term(oldContent, componentList);
                    if (!(newContent instanceof CompoundTerm))
                        return;
                    content = (CompoundTerm)newContent;
                } else if ((oldContent instanceof Implication) || (oldContent instanceof Equivalence)) {
                    content = Statement.make((Statement) oldContent, componentList[0], componentList[1], oldContent.getTemporalOrder());
                }
            }
        }

        if (content == null)
            return;

        Sentence sentence = nal.getCurrentTask().sentence;
        TruthValue truth = sentence.truth;
        BudgetValue budget;
        if (sentence.isQuestion() || sentence.isQuest()) {
            budget = BudgetFunctions.compoundBackward(content, nal);
        } else {
            budget = BudgetFunctions.compoundForward(truth, content, nal);
        }

        nal.singlePremiseTask(content, truth, budget);
    }

    /**
     * Equivalent transformation between products and images when the subject is
     * a compound {<(*, S, M) --> P>, S@(*, S, M)} |- <S --> (/, P, _, M)> {<S
     * --> (/, P, _, M)>, P@(/, P, _, M)} |- <(*, S, M) --> P> {<S --> (/, P, _,
     * M)>, M@(/, P, _, M)} |- <M --> (/, P, S, _)>
     *
     * @param subject The subject term
     * @param predicate The predicate term
     * @param nal Reference to the memory
     */
    private static void transformSubjectPI(CompoundTerm subject, Term predicate, NAL nal) {
        TruthValue truth = nal.getCurrentTask().sentence.truth;
        BudgetValue budget;
        Inheritance inheritance;
        Term newSubj, newPred;
        if (subject instanceof Product) {
            Product product = (Product) subject;
            for (short i = 0; i < product.size(); i++) {
                newSubj = product.term[i];
                newPred = ImageExt.make(product, predicate, i);
                inheritance = Inheritance.make(newSubj, newPred);
                if (inheritance != null) {
                    if (truth == null) {
                        budget = BudgetFunctions.compoundBackward(inheritance, nal);
                    } else {
                        budget = BudgetFunctions.compoundForward(truth, inheritance, nal);
                    }
                    nal.singlePremiseTask(inheritance, truth, budget);
                }
            }
        } else if (subject instanceof ImageInt) {
            ImageInt image = (ImageInt) subject;
            int relationIndex = image.relationIndex;
            for (short i = 0; i < image.size(); i++) {
                Term iti = image.term[i];
                if (i == relationIndex) {
                    newSubj = iti;
                    newPred = Product.make(image, predicate, relationIndex);
                } else {
                    newSubj = ImageInt.make(image, predicate, i);
                    newPred = iti;
                }
                inheritance = Inheritance.make(newSubj, newPred);
                if (inheritance != null) {
                    if (truth == null) {
                        budget = BudgetFunctions.compoundBackward(inheritance, nal);
                    } else {
                        budget = BudgetFunctions.compoundForward(truth, inheritance, nal);
                    }
                    nal.singlePremiseTask(inheritance, truth, budget);
                }
            }
        }
    }

    /**
     * Equivalent transformation between products and images when the predicate
     * is a compound {<(*, S, M) --> P>, S@(*, S, M)} |- <S --> (/, P, _, M)>
     * {<S --> (/, P, _, M)>, P@(/, P, _, M)} |- <(*, S, M) --> P> {<S --> (/,
     * P, _, M)>, M@(/, P, _, M)} |- <M --> (/, P, S, _)>
     *
     * @param subject The subject term
     * @param predicate The predicate term
     * @param nal Reference to the memory
     */
    private static void transformPredicatePI(Term subject, CompoundTerm predicate, NAL nal) {
        TruthValue truth = nal.getCurrentTask().sentence.truth;
        BudgetValue budget;
        Inheritance inheritance;
        Term newSubj, newPred;
        if (predicate instanceof Product) {
            Product product = (Product) predicate;
            for (short i = 0; i < product.size(); i++) {
                newSubj = ImageInt.make(product, subject, i);
                newPred = product.term[i];
                inheritance = Inheritance.make(newSubj, newPred);
                if (inheritance != null) {
                    if (truth == null) {
                        budget = BudgetFunctions.compoundBackward(inheritance, nal);
                    } else {
                        budget = BudgetFunctions.compoundForward(truth, inheritance, nal);
                    }
                    nal.singlePremiseTask(inheritance, truth, budget);
                }
            }
        } else if (predicate instanceof ImageExt) {
            ImageExt image = (ImageExt) predicate;
            int relationIndex = image.relationIndex;
            for (short i = 0; i < image.size(); i++) {
                if (i == relationIndex) {
                    newSubj = Product.make(image, subject, relationIndex);
                    newPred = image.term[relationIndex];
                } else {
                    newSubj = image.term[i];
                    newPred = ImageExt.make(image, subject, i);
                }
                inheritance = Inheritance.make(newSubj, newPred);
                if (inheritance != null) { // jmv <<<<<
                    if (truth == null) {
                        budget = BudgetFunctions.compoundBackward(inheritance, nal);
                    } else {
                        budget = BudgetFunctions.compoundForward(truth, inheritance, nal);
                    }
                    nal.singlePremiseTask(inheritance, truth, budget);
                }
            }
        }
    }

}
