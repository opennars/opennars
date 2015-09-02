package nars.process.concept;

import nars.Events;
import nars.Memory;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.nal.nal1.Inheritance;
import nars.nal.nal4.ImageExt;
import nars.nal.nal4.ImageInt;
import nars.nal.nal4.Product;
import nars.nal.nal5.Conjunction;
import nars.nal.nal5.Equivalence;
import nars.nal.nal5.Implication;
import nars.premise.Premise;
import nars.process.ConceptProcess;
import nars.process.ConceptTaskLinkProcess;
import nars.task.Task;
import nars.term.Compound;
import nars.term.Statement;
import nars.term.Term;
import nars.truth.Truth;

import java.util.Arrays;


/**
 * ----- logic with one TaskLink only -----
 * The TaskLink is of type TRANSFORM, and the conclusion is an equivalent
 * transformation
 **/
public class TransformTask extends ConceptFireTask<ConceptTaskLinkProcess> {

    @Override
    public final boolean apply(ConceptTaskLinkProcess f, TaskLink t) {

        if (t.type == TermLink.TRANSFORM) {


            if (f instanceof ConceptProcess)
                f.setBelief(null);


            // to turn this into structural logic as below?

            Compound content = f.getTask().getTerm();
            short[] indices = t.index;
            Term inh = null;

            if ((indices.length == 2) || (content instanceof Inheritance)) {          // <(*, term, #) --> #>
                inh = content;
            } else if (indices.length == 3) {   // <<(*, term, #) --> #> ==> #>
                inh = content.term[indices[0]];
            } else if (indices.length == 4) {   // <(&&, <(*, term, #) --> #>, #) ==> #>
                Term component = content.term[indices[0]];
                if ((component instanceof Conjunction) && (((content instanceof Implication) && (indices[0] == 0)) || (content instanceof Equivalence))) {

                    Term[] cterms = ((Compound) component).term;
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


            f.emit(Events.TermLinkTransformed.class, t, f.getConcept(), this);
            f.getMemory().logic.TERM_LINK_TRANSFORM.hit();
        }

        return true;
    }


    /* -------------------- products and images transform -------------------- */
    /**
     * Equivalent transformation between products and images {<(*, S, M) --> P>,
     * S@(*, S, M)} |- <S --> (/, P, _, M)> {<S --> (/, P, _, M)>, P@(/, P, _,
     * M)} |- <(*, S, M) --> P> {<S --> (/, P, _, M)>, M@(/, P, _, M)} |- <M -->
     * (/, P, S, _)>
     *  @param inh An Inheritance statement
     * @param oldContent The whole content
     * @param indices The indices of the TaskLink
     */
    public static Task transformProductImage(final Inheritance inh, final Compound oldContent, final short[] indices, final Premise nal) {
        Term subject = inh.getSubject();
        Term predicate = inh.getPredicate();
        if (inh.equals(oldContent)) {
            if (subject instanceof Compound) {
                transformSubjectPI((Compound) subject, predicate, nal);
            }
            if (predicate instanceof Compound) {
                transformPredicatePI(subject, (Compound) predicate, nal);
            }
            return null;
        }
        short index = indices[indices.length - 1];
        short side = indices[indices.length - 2];

        Term compT = inh.term[side];
        if (!(compT instanceof Compound))
            return null;
        Compound comp = (Compound)compT;

        if (comp.term.length <= index) {
            throw new RuntimeException("Invalid term index: " + index + ", term=" + comp + ", indices="  + Arrays.toString(indices));
        }

        Term cti = comp.term[index];

        if (comp instanceof Product) {
            if (side == 0) {
                subject = cti;
                predicate = ImageExt.make((Product) comp, inh.getPredicate(), index);
            } else {
                subject = ImageInt.make((Product) comp, inh.getSubject(), index);
                predicate =cti;
            }
        } else if ((comp instanceof ImageExt) && (side == 1)) {
            if (index == ((ImageExt) comp).relationIndex) {
                subject = Product.make(comp, inh.getSubject(), index);
                predicate = cti;
            } else {
                subject = cti;
                predicate = ImageExt.make((ImageExt) comp, inh.getSubject(), index);
            }
        } else if ((comp instanceof ImageInt) && (side == 0)) {
            if (index == ((ImageInt) comp).relationIndex) {
                subject = cti;
                predicate = Product.make(comp, inh.getPredicate(), index);
            } else {
                subject = ImageInt.make((ImageInt) comp, inh.getPredicate(), index);
                predicate = cti;
            }
        } else {
            return null;
        }

        Inheritance newInh = Inheritance.make(subject, predicate);
        if (newInh == null)
            return null;

        Compound content = null;
        if (indices.length == 2) {
            content = newInh;
        } else if ((oldContent instanceof Statement) && (indices[0] == 1)) {
            content = Statement.make(oldContent.op(), oldContent.term[0], newInh, oldContent.getTemporalOrder());
        } else {
            Term[] componentList;
            Term condition = oldContent.term[0];
            if (((oldContent instanceof Implication) || (oldContent instanceof Equivalence)) && (condition instanceof Conjunction)) {
                componentList = ((Compound) condition).cloneTermsDeepIfContainingVariables(); //cloneTerms();
                componentList[indices[1]] = newInh;
                Term newCond = Memory.term((Compound) condition, componentList);
                content = Statement.make(oldContent.op(), newCond, ((Statement) oldContent).getPredicate(), oldContent.getTemporalOrder());
            } else {

                componentList = oldContent.cloneTermsDeepIfContainingVariables(); //oldContent.cloneTerms();
                componentList[indices[0]] = newInh;
                if (oldContent instanceof Conjunction) {
                    Term newContent = Memory.term(oldContent, componentList);
                    if (!(newContent instanceof Compound))
                        return null;
                    content = (Compound)newContent;
                } else if ((oldContent instanceof Implication) || (oldContent instanceof Equivalence)) {
                    content = Statement.make(oldContent.op(), componentList[0], componentList[1], oldContent.getTemporalOrder());
                }
            }
        }

        if (content == null)
            return null;

        final Task currentTask = nal.getTask();
        final Truth truth = currentTask.getTruth();
        final Budget budget;
        if (currentTask.isQuestOrQuestion()) {
            budget = BudgetFunctions.compoundBackward(content, nal);
        } else {
            budget = BudgetFunctions.compoundForward(truth, content, nal);
        }

        return nal.deriveSingle(content, truth, budget);
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
    private static void transformSubjectPI(Compound subject, Term predicate, Premise nal) {
        Truth truth = nal.getTask().getTruth();
        Budget budget;
        Inheritance inheritance;
        Term newSubj, newPred;
        if (subject instanceof Product) {
            Product product = (Product) subject;
            for (short i = 0; i < product.length(); i++) {
                newSubj = product.term(i);
                newPred = ImageExt.make(product, predicate, i);
                inheritance = Inheritance.make(newSubj, newPred);
                if (inheritance != null) {
                    if (truth == null) {
                        budget = BudgetFunctions.compoundBackward(inheritance, nal);
                    } else {
                        budget = BudgetFunctions.compoundForward(truth, inheritance, nal);
                    }
                    nal.deriveSingle(inheritance, truth, budget);
                }
            }
        } else if (subject instanceof ImageInt) {
            ImageInt image = (ImageInt) subject;
            int relationIndex = image.relationIndex;
            for (short i = 0; i < image.length(); i++) {
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
                    nal.deriveSingle(inheritance, truth, budget);
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
    private static void transformPredicatePI(Term subject, Compound predicate, Premise nal) {
        Truth truth = nal.getTask().getTruth();
        Budget budget;
        Inheritance inheritance;
        Term newSubj, newPred;
        if (predicate instanceof Product) {
            Product product = (Product) predicate;
            for (short i = 0; i < product.length(); i++) {
                newSubj = ImageInt.make(product, subject, i);
                newPred = product.term(i);
                inheritance = Inheritance.make(newSubj, newPred);
                if (inheritance != null) {
                    if (truth == null) {
                        budget = BudgetFunctions.compoundBackward(inheritance, nal);
                    } else {
                        budget = BudgetFunctions.compoundForward(truth, inheritance, nal);
                    }
                    nal.deriveSingle(inheritance, truth, budget);
                }
            }
        } else if (predicate instanceof ImageExt) {
            ImageExt image = (ImageExt) predicate;
            int relationIndex = image.relationIndex;
            for (short i = 0; i < image.length(); i++) {
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
                    nal.deriveSingle(inheritance, truth, budget);
                }
            }
        }
    }

}
