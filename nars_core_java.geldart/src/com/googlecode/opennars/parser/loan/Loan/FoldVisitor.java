package com.googlecode.opennars.parser.loan.Loan;

import com.googlecode.opennars.parser.loan.Loan.Absyn.*;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

/** BNFC-Generated Fold Visitor */
public abstract class FoldVisitor<R,A> implements AllVisitor<R,A> {
    public abstract R leaf(A arg);
    public abstract R combine(R x, R y, A arg);

/* Document */
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.DocBR p, A arg) {
      R r = leaf(arg);
      r = combine(p.baserule_.accept(this, arg), r, arg);
      for (Sentence x : p.listsentence_) {
        r = combine(x.accept(this,arg), r, arg);
      }
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.Doc p, A arg) {
      R r = leaf(arg);
      for (Sentence x : p.listsentence_) {
        r = combine(x.accept(this,arg), r, arg);
      }
      return r;
    }

/* BaseRule */
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.BaseR p, A arg) {
      R r = leaf(arg);
      return r;
    }

/* Sentence */
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.SentPrefix p, A arg) {
      R r = leaf(arg);
      r = combine(p.nsprefix_.accept(this, arg), r, arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.SentImport p, A arg) {
      R r = leaf(arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.SentDelay p, A arg) {
      R r = leaf(arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.SentOp p, A arg) {
      R r = leaf(arg);
      r = combine(p.uriref_.accept(this, arg), r, arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.SentJudge p, A arg) {
      R r = leaf(arg);
      r = combine(p.stm_.accept(this, arg), r, arg);
      r = combine(p.truthvalue_.accept(this, arg), r, arg);
      r = combine(p.budget_.accept(this, arg), r, arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.SentQuest p, A arg) {
      R r = leaf(arg);
      r = combine(p.stm_.accept(this, arg), r, arg);
      r = combine(p.budget_.accept(this, arg), r, arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.SentGoal p, A arg) {
      R r = leaf(arg);
      r = combine(p.stm_.accept(this, arg), r, arg);
      r = combine(p.truthvalue_.accept(this, arg), r, arg);
      r = combine(p.budget_.accept(this, arg), r, arg);
      return r;
    }

/* Budget */
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.BudgetE p, A arg) {
      R r = leaf(arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.BudgetP p, A arg) {
      R r = leaf(arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.BudgetPD p, A arg) {
      R r = leaf(arg);
      return r;
    }

/* Stm */
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpl p, A arg) {
      R r = leaf(arg);
      r = combine(p.stm_1.accept(this, arg), r, arg);
      r = combine(p.stm_2.accept(this, arg), r, arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmEquiv p, A arg) {
      R r = leaf(arg);
      r = combine(p.stm_1.accept(this, arg), r, arg);
      r = combine(p.stm_2.accept(this, arg), r, arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpPred p, A arg) {
      R r = leaf(arg);
      r = combine(p.stm_1.accept(this, arg), r, arg);
      r = combine(p.stm_2.accept(this, arg), r, arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpRet p, A arg) {
      R r = leaf(arg);
      r = combine(p.stm_1.accept(this, arg), r, arg);
      r = combine(p.stm_2.accept(this, arg), r, arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpConc p, A arg) {
      R r = leaf(arg);
      r = combine(p.stm_1.accept(this, arg), r, arg);
      r = combine(p.stm_2.accept(this, arg), r, arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmEqvPred p, A arg) {
      R r = leaf(arg);
      r = combine(p.stm_1.accept(this, arg), r, arg);
      r = combine(p.stm_2.accept(this, arg), r, arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmEqvConc p, A arg) {
      R r = leaf(arg);
      r = combine(p.stm_1.accept(this, arg), r, arg);
      r = combine(p.stm_2.accept(this, arg), r, arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmConj p, A arg) {
      R r = leaf(arg);
      r = combine(p.stm_1.accept(this, arg), r, arg);
      r = combine(p.stm_2.accept(this, arg), r, arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmDisj p, A arg) {
      R r = leaf(arg);
      r = combine(p.stm_1.accept(this, arg), r, arg);
      r = combine(p.stm_2.accept(this, arg), r, arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmPar p, A arg) {
      R r = leaf(arg);
      r = combine(p.stm_1.accept(this, arg), r, arg);
      r = combine(p.stm_2.accept(this, arg), r, arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmSeq p, A arg) {
      R r = leaf(arg);
      r = combine(p.stm_1.accept(this, arg), r, arg);
      r = combine(p.stm_2.accept(this, arg), r, arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmNot p, A arg) {
      R r = leaf(arg);
      r = combine(p.stm_.accept(this, arg), r, arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmPst p, A arg) {
      R r = leaf(arg);
      r = combine(p.stm_.accept(this, arg), r, arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmPres p, A arg) {
      R r = leaf(arg);
      r = combine(p.stm_.accept(this, arg), r, arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmFut p, A arg) {
      R r = leaf(arg);
      r = combine(p.stm_.accept(this, arg), r, arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmInher p, A arg) {
      R r = leaf(arg);
      r = combine(p.term_1.accept(this, arg), r, arg);
      r = combine(p.term_2.accept(this, arg), r, arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmSim p, A arg) {
      R r = leaf(arg);
      r = combine(p.term_1.accept(this, arg), r, arg);
      r = combine(p.term_2.accept(this, arg), r, arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmInst p, A arg) {
      R r = leaf(arg);
      r = combine(p.term_1.accept(this, arg), r, arg);
      r = combine(p.term_2.accept(this, arg), r, arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmProp p, A arg) {
      R r = leaf(arg);
      r = combine(p.term_1.accept(this, arg), r, arg);
      r = combine(p.term_2.accept(this, arg), r, arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmInPp p, A arg) {
      R r = leaf(arg);
      r = combine(p.term_1.accept(this, arg), r, arg);
      r = combine(p.term_2.accept(this, arg), r, arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmOp p, A arg) {
      R r = leaf(arg);
      r = combine(p.term_.accept(this, arg), r, arg);
      for (Term x : p.listterm_) {
        r = combine(x.accept(this,arg), r, arg);
      }
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmTrm p, A arg) {
      R r = leaf(arg);
      r = combine(p.term_.accept(this, arg), r, arg);
      return r;
    }

/* Term */
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExInt p, A arg) {
      R r = leaf(arg);
      r = combine(p.term_1.accept(this, arg), r, arg);
      r = combine(p.term_2.accept(this, arg), r, arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInInt p, A arg) {
      R r = leaf(arg);
      r = combine(p.term_1.accept(this, arg), r, arg);
      r = combine(p.term_2.accept(this, arg), r, arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExDif p, A arg) {
      R r = leaf(arg);
      r = combine(p.term_1.accept(this, arg), r, arg);
      r = combine(p.term_2.accept(this, arg), r, arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInDif p, A arg) {
      R r = leaf(arg);
      r = combine(p.term_1.accept(this, arg), r, arg);
      r = combine(p.term_2.accept(this, arg), r, arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExImg p, A arg) {
      R r = leaf(arg);
      r = combine(p.term_.accept(this, arg), r, arg);
      for (Term x : p.listterm_1) {
        r = combine(x.accept(this,arg), r, arg);
      }
      for (Term x : p.listterm_2) {
        r = combine(x.accept(this,arg), r, arg);
      }
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInImg p, A arg) {
      R r = leaf(arg);
      r = combine(p.term_.accept(this, arg), r, arg);
      for (Term x : p.listterm_1) {
        r = combine(x.accept(this,arg), r, arg);
      }
      for (Term x : p.listterm_2) {
        r = combine(x.accept(this,arg), r, arg);
      }
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExSet p, A arg) {
      R r = leaf(arg);
      for (Term x : p.listterm_) {
        r = combine(x.accept(this,arg), r, arg);
      }
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInSet p, A arg) {
      R r = leaf(arg);
      for (Term x : p.listterm_) {
        r = combine(x.accept(this,arg), r, arg);
      }
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmProd p, A arg) {
      R r = leaf(arg);
      for (Term x : p.listterm_) {
        r = combine(x.accept(this,arg), r, arg);
      }
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmLit p, A arg) {
      R r = leaf(arg);
      r = combine(p.literal_.accept(this, arg), r, arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmStm p, A arg) {
      R r = leaf(arg);
      r = combine(p.stm_.accept(this, arg), r, arg);
      return r;
    }

/* URIRef */
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.URIFul p, A arg) {
      R r = leaf(arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.URICur p, A arg) {
      R r = leaf(arg);
      r = combine(p.nsprefix_.accept(this, arg), r, arg);
      return r;
    }

/* Literal */
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitQVar p, A arg) {
      R r = leaf(arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitQVarAn p, A arg) {
      R r = leaf(arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitSVarD p, A arg) {
      R r = leaf(arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitSVarI p, A arg) {
      R r = leaf(arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitURI p, A arg) {
      R r = leaf(arg);
      r = combine(p.uriref_.accept(this, arg), r, arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitInt p, A arg) {
      R r = leaf(arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitDbl p, A arg) {
      R r = leaf(arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitString p, A arg) {
      R r = leaf(arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitTrue p, A arg) {
      R r = leaf(arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitFalse p, A arg) {
      R r = leaf(arg);
      return r;
    }

/* NSPrefix */
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.NSPrefix1 p, A arg) {
      R r = leaf(arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.NSPrefix2 p, A arg) {
      R r = leaf(arg);
      return r;
    }

/* TruthValue */
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TruthE p, A arg) {
      R r = leaf(arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TruthF p, A arg) {
      R r = leaf(arg);
      return r;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TruthFC p, A arg) {
      R r = leaf(arg);
      return r;
    }


}
