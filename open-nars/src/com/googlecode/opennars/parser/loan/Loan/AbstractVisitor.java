package com.googlecode.opennars.parser.loan.Loan;
import com.googlecode.opennars.parser.loan.Loan.Absyn.*;
/** BNFC-Generated Abstract Visitor */
public class AbstractVisitor<R,A> implements AllVisitor<R,A> {
/* Document */
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.DocBR p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.Doc p, A arg) { return visitDefault(p, arg); }
    public R visitDefault(com.googlecode.opennars.parser.loan.Loan.Absyn.Document p, A arg) {
      throw new IllegalArgumentException(this.getClass().getName() + ": " + p);
    }
/* BaseRule */
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.BaseR p, A arg) { return visitDefault(p, arg); }
    public R visitDefault(com.googlecode.opennars.parser.loan.Loan.Absyn.BaseRule p, A arg) {
      throw new IllegalArgumentException(this.getClass().getName() + ": " + p);
    }
/* Sentence */
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.SentPrefix p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.SentImport p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.SentDelay p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.SentOp p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.SentJudge p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.SentQuest p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.SentGoal p, A arg) { return visitDefault(p, arg); }
    public R visitDefault(com.googlecode.opennars.parser.loan.Loan.Absyn.Sentence p, A arg) {
      throw new IllegalArgumentException(this.getClass().getName() + ": " + p);
    }
/* Budget */
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.BudgetE p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.BudgetP p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.BudgetPD p, A arg) { return visitDefault(p, arg); }
    public R visitDefault(com.googlecode.opennars.parser.loan.Loan.Absyn.Budget p, A arg) {
      throw new IllegalArgumentException(this.getClass().getName() + ": " + p);
    }
/* Stm */
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpl p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmEquiv p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpPred p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpRet p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpConc p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmEqvPred p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmEqvConc p, A arg) { return visitDefault(p, arg); }

    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmConj p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmDisj p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmPar p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmSeq p, A arg) { return visitDefault(p, arg); }

    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmNot p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmPst p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmPres p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmFut p, A arg) { return visitDefault(p, arg); }

    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmInher p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmSim p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmInst p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmProp p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmInPp p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmOp p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmTrm p, A arg) { return visitDefault(p, arg); }

    public R visitDefault(com.googlecode.opennars.parser.loan.Loan.Absyn.Stm p, A arg) {
      throw new IllegalArgumentException(this.getClass().getName() + ": " + p);
    }
/* Term */
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExInt p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInInt p, A arg) { return visitDefault(p, arg); }

    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExDif p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInDif p, A arg) { return visitDefault(p, arg); }

    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExImg p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInImg p, A arg) { return visitDefault(p, arg); }

    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExSet p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInSet p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmProd p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmLit p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmStm p, A arg) { return visitDefault(p, arg); }

    public R visitDefault(com.googlecode.opennars.parser.loan.Loan.Absyn.Term p, A arg) {
      throw new IllegalArgumentException(this.getClass().getName() + ": " + p);
    }
/* URIRef */
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.URIFul p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.URICur p, A arg) { return visitDefault(p, arg); }
    public R visitDefault(com.googlecode.opennars.parser.loan.Loan.Absyn.URIRef p, A arg) {
      throw new IllegalArgumentException(this.getClass().getName() + ": " + p);
    }
/* Literal */
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitQVar p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitQVarAn p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitSVarD p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitSVarI p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitURI p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitInt p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitDbl p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitString p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitTrue p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitFalse p, A arg) { return visitDefault(p, arg); }
    public R visitDefault(com.googlecode.opennars.parser.loan.Loan.Absyn.Literal p, A arg) {
      throw new IllegalArgumentException(this.getClass().getName() + ": " + p);
    }
/* NSPrefix */
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.NSPrefix1 p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.NSPrefix2 p, A arg) { return visitDefault(p, arg); }
    public R visitDefault(com.googlecode.opennars.parser.loan.Loan.Absyn.NSPrefix p, A arg) {
      throw new IllegalArgumentException(this.getClass().getName() + ": " + p);
    }
/* TruthValue */
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TruthE p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TruthF p, A arg) { return visitDefault(p, arg); }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TruthFC p, A arg) { return visitDefault(p, arg); }
    public R visitDefault(com.googlecode.opennars.parser.loan.Loan.Absyn.TruthValue p, A arg) {
      throw new IllegalArgumentException(this.getClass().getName() + ": " + p);
    }

}
