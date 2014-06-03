package com.googlecode.opennars.parser.loan.Loan;
import com.googlecode.opennars.parser.loan.Loan.Absyn.*;
/** BNFC-Generated Composition Visitor
*/

public class ComposVisitor<A> implements
  com.googlecode.opennars.parser.loan.Loan.Absyn.Document.Visitor<com.googlecode.opennars.parser.loan.Loan.Absyn.Document,A>,
  com.googlecode.opennars.parser.loan.Loan.Absyn.BaseRule.Visitor<com.googlecode.opennars.parser.loan.Loan.Absyn.BaseRule,A>,
  com.googlecode.opennars.parser.loan.Loan.Absyn.Sentence.Visitor<com.googlecode.opennars.parser.loan.Loan.Absyn.Sentence,A>,
  com.googlecode.opennars.parser.loan.Loan.Absyn.Budget.Visitor<com.googlecode.opennars.parser.loan.Loan.Absyn.Budget,A>,
  com.googlecode.opennars.parser.loan.Loan.Absyn.Stm.Visitor<com.googlecode.opennars.parser.loan.Loan.Absyn.Stm,A>,
  com.googlecode.opennars.parser.loan.Loan.Absyn.Term.Visitor<com.googlecode.opennars.parser.loan.Loan.Absyn.Term,A>,
  com.googlecode.opennars.parser.loan.Loan.Absyn.URIRef.Visitor<com.googlecode.opennars.parser.loan.Loan.Absyn.URIRef,A>,
  com.googlecode.opennars.parser.loan.Loan.Absyn.Literal.Visitor<com.googlecode.opennars.parser.loan.Loan.Absyn.Literal,A>,
  com.googlecode.opennars.parser.loan.Loan.Absyn.NSPrefix.Visitor<com.googlecode.opennars.parser.loan.Loan.Absyn.NSPrefix,A>,
  com.googlecode.opennars.parser.loan.Loan.Absyn.TruthValue.Visitor<com.googlecode.opennars.parser.loan.Loan.Absyn.TruthValue,A>
{
/* Document */
    public Document visit(com.googlecode.opennars.parser.loan.Loan.Absyn.DocBR p, A arg)
    {
      BaseRule baserule_ = p.baserule_.accept(this, arg);
      ListSentence listsentence_ = new ListSentence();
      for (Sentence x : p.listsentence_) {
        listsentence_.add(x.accept(this,arg));
      }

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.DocBR(baserule_, listsentence_);
    }
    public Document visit(com.googlecode.opennars.parser.loan.Loan.Absyn.Doc p, A arg)
    {
      ListSentence listsentence_ = new ListSentence();
      for (Sentence x : p.listsentence_) {
        listsentence_.add(x.accept(this,arg));
      }

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.Doc(listsentence_);
    }

/* BaseRule */
    public BaseRule visit(com.googlecode.opennars.parser.loan.Loan.Absyn.BaseR p, A arg)
    {
      String urilit_ = p.urilit_;

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.BaseR(urilit_);
    }

/* Sentence */
    public Sentence visit(com.googlecode.opennars.parser.loan.Loan.Absyn.SentPrefix p, A arg)
    {
      NSPrefix nsprefix_ = p.nsprefix_.accept(this, arg);
      String urilit_ = p.urilit_;

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.SentPrefix(nsprefix_, urilit_);
    }
    public Sentence visit(com.googlecode.opennars.parser.loan.Loan.Absyn.SentImport p, A arg)
    {
      String urilit_ = p.urilit_;

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.SentImport(urilit_);
    }
    public Sentence visit(com.googlecode.opennars.parser.loan.Loan.Absyn.SentDelay p, A arg)
    {
      Integer integer_ = p.integer_;

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.SentDelay(integer_);
    }
    public Sentence visit(com.googlecode.opennars.parser.loan.Loan.Absyn.SentOp p, A arg)
    {
      URIRef uriref_ = p.uriref_.accept(this, arg);

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.SentOp(uriref_);
    }
    public Sentence visit(com.googlecode.opennars.parser.loan.Loan.Absyn.SentJudge p, A arg)
    {
      Stm stm_ = p.stm_.accept(this, arg);
      TruthValue truthvalue_ = p.truthvalue_.accept(this, arg);
      Budget budget_ = p.budget_.accept(this, arg);

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.SentJudge(stm_, truthvalue_, budget_);
    }
    public Sentence visit(com.googlecode.opennars.parser.loan.Loan.Absyn.SentQuest p, A arg)
    {
      Stm stm_ = p.stm_.accept(this, arg);
      Budget budget_ = p.budget_.accept(this, arg);

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.SentQuest(stm_, budget_);
    }
    public Sentence visit(com.googlecode.opennars.parser.loan.Loan.Absyn.SentGoal p, A arg)
    {
      Stm stm_ = p.stm_.accept(this, arg);
      TruthValue truthvalue_ = p.truthvalue_.accept(this, arg);
      Budget budget_ = p.budget_.accept(this, arg);

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.SentGoal(stm_, truthvalue_, budget_);
    }

/* Budget */
    public Budget visit(com.googlecode.opennars.parser.loan.Loan.Absyn.BudgetE p, A arg)
    {

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.BudgetE();
    }
    public Budget visit(com.googlecode.opennars.parser.loan.Loan.Absyn.BudgetP p, A arg)
    {
      Double double_ = p.double_;

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.BudgetP(double_);
    }
    public Budget visit(com.googlecode.opennars.parser.loan.Loan.Absyn.BudgetPD p, A arg)
    {
      Double double_1 = p.double_1;
      Double double_2 = p.double_2;

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.BudgetPD(double_1, double_2);
    }

/* Stm */
    public Stm visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpl p, A arg)
    {
      Stm stm_1 = p.stm_1.accept(this, arg);
      Stm stm_2 = p.stm_2.accept(this, arg);

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpl(stm_1, stm_2);
    }
    public Stm visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmEquiv p, A arg)
    {
      Stm stm_1 = p.stm_1.accept(this, arg);
      Stm stm_2 = p.stm_2.accept(this, arg);

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.StmEquiv(stm_1, stm_2);
    }
    public Stm visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpPred p, A arg)
    {
      Stm stm_1 = p.stm_1.accept(this, arg);
      Stm stm_2 = p.stm_2.accept(this, arg);

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpPred(stm_1, stm_2);
    }
    public Stm visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpRet p, A arg)
    {
      Stm stm_1 = p.stm_1.accept(this, arg);
      Stm stm_2 = p.stm_2.accept(this, arg);

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpRet(stm_1, stm_2);
    }
    public Stm visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpConc p, A arg)
    {
      Stm stm_1 = p.stm_1.accept(this, arg);
      Stm stm_2 = p.stm_2.accept(this, arg);

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpConc(stm_1, stm_2);
    }
    public Stm visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmEqvPred p, A arg)
    {
      Stm stm_1 = p.stm_1.accept(this, arg);
      Stm stm_2 = p.stm_2.accept(this, arg);

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.StmEqvPred(stm_1, stm_2);
    }
    public Stm visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmEqvConc p, A arg)
    {
      Stm stm_1 = p.stm_1.accept(this, arg);
      Stm stm_2 = p.stm_2.accept(this, arg);

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.StmEqvConc(stm_1, stm_2);
    }
    public Stm visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmConj p, A arg)
    {
      Stm stm_1 = p.stm_1.accept(this, arg);
      Stm stm_2 = p.stm_2.accept(this, arg);

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.StmConj(stm_1, stm_2);
    }
    public Stm visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmDisj p, A arg)
    {
      Stm stm_1 = p.stm_1.accept(this, arg);
      Stm stm_2 = p.stm_2.accept(this, arg);

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.StmDisj(stm_1, stm_2);
    }
    public Stm visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmPar p, A arg)
    {
      Stm stm_1 = p.stm_1.accept(this, arg);
      Stm stm_2 = p.stm_2.accept(this, arg);

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.StmPar(stm_1, stm_2);
    }
    public Stm visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmSeq p, A arg)
    {
      Stm stm_1 = p.stm_1.accept(this, arg);
      Stm stm_2 = p.stm_2.accept(this, arg);

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.StmSeq(stm_1, stm_2);
    }
    public Stm visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmNot p, A arg)
    {
      Stm stm_ = p.stm_.accept(this, arg);

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.StmNot(stm_);
    }
    public Stm visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmPst p, A arg)
    {
      Stm stm_ = p.stm_.accept(this, arg);

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.StmPst(stm_);
    }
    public Stm visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmPres p, A arg)
    {
      Stm stm_ = p.stm_.accept(this, arg);

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.StmPres(stm_);
    }
    public Stm visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmFut p, A arg)
    {
      Stm stm_ = p.stm_.accept(this, arg);

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.StmFut(stm_);
    }
    public Stm visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmInher p, A arg)
    {
      Term term_1 = p.term_1.accept(this, arg);
      Term term_2 = p.term_2.accept(this, arg);

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.StmInher(term_1, term_2);
    }
    public Stm visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmSim p, A arg)
    {
      Term term_1 = p.term_1.accept(this, arg);
      Term term_2 = p.term_2.accept(this, arg);

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.StmSim(term_1, term_2);
    }
    public Stm visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmInst p, A arg)
    {
      Term term_1 = p.term_1.accept(this, arg);
      Term term_2 = p.term_2.accept(this, arg);

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.StmInst(term_1, term_2);
    }
    public Stm visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmProp p, A arg)
    {
      Term term_1 = p.term_1.accept(this, arg);
      Term term_2 = p.term_2.accept(this, arg);

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.StmProp(term_1, term_2);
    }
    public Stm visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmInPp p, A arg)
    {
      Term term_1 = p.term_1.accept(this, arg);
      Term term_2 = p.term_2.accept(this, arg);

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.StmInPp(term_1, term_2);
    }
    public Stm visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmOp p, A arg)
    {
      Term term_ = p.term_.accept(this, arg);
      ListTerm listterm_ = new ListTerm();
      for (Term x : p.listterm_) {
        listterm_.add(x.accept(this,arg));
      }

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.StmOp(term_, listterm_);
    }
    public Stm visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmTrm p, A arg)
    {
      Term term_ = p.term_.accept(this, arg);

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.StmTrm(term_);
    }

/* Term */
    public Term visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExInt p, A arg)
    {
      Term term_1 = p.term_1.accept(this, arg);
      Term term_2 = p.term_2.accept(this, arg);

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExInt(term_1, term_2);
    }
    public Term visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInInt p, A arg)
    {
      Term term_1 = p.term_1.accept(this, arg);
      Term term_2 = p.term_2.accept(this, arg);

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInInt(term_1, term_2);
    }
    public Term visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExDif p, A arg)
    {
      Term term_1 = p.term_1.accept(this, arg);
      Term term_2 = p.term_2.accept(this, arg);

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExDif(term_1, term_2);
    }
    public Term visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInDif p, A arg)
    {
      Term term_1 = p.term_1.accept(this, arg);
      Term term_2 = p.term_2.accept(this, arg);

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInDif(term_1, term_2);
    }
    public Term visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExImg p, A arg)
    {
      Term term_ = p.term_.accept(this, arg);
      ListTerm listterm_1 = new ListTerm();
      for (Term x : p.listterm_1) {
        listterm_1.add(x.accept(this,arg));
      }
      ListTerm listterm_2 = new ListTerm();
      for (Term x : p.listterm_2) {
        listterm_2.add(x.accept(this,arg));
      }

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExImg(term_, listterm_1, listterm_2);
    }
    public Term visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInImg p, A arg)
    {
      Term term_ = p.term_.accept(this, arg);
      ListTerm listterm_1 = new ListTerm();
      for (Term x : p.listterm_1) {
        listterm_1.add(x.accept(this,arg));
      }
      ListTerm listterm_2 = new ListTerm();
      for (Term x : p.listterm_2) {
        listterm_2.add(x.accept(this,arg));
      }

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInImg(term_, listterm_1, listterm_2);
    }
    public Term visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExSet p, A arg)
    {
      ListTerm listterm_ = new ListTerm();
      for (Term x : p.listterm_) {
        listterm_.add(x.accept(this,arg));
      }

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExSet(listterm_);
    }
    public Term visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInSet p, A arg)
    {
      ListTerm listterm_ = new ListTerm();
      for (Term x : p.listterm_) {
        listterm_.add(x.accept(this,arg));
      }

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInSet(listterm_);
    }
    public Term visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmProd p, A arg)
    {
      ListTerm listterm_ = new ListTerm();
      for (Term x : p.listterm_) {
        listterm_.add(x.accept(this,arg));
      }

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.TrmProd(listterm_);
    }
    public Term visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmLit p, A arg)
    {
      Literal literal_ = p.literal_.accept(this, arg);

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.TrmLit(literal_);
    }
    public Term visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmStm p, A arg)
    {
      Stm stm_ = p.stm_.accept(this, arg);

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.TrmStm(stm_);
    }

/* URIRef */
    public URIRef visit(com.googlecode.opennars.parser.loan.Loan.Absyn.URIFul p, A arg)
    {
      String urilit_ = p.urilit_;

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.URIFul(urilit_);
    }
    public URIRef visit(com.googlecode.opennars.parser.loan.Loan.Absyn.URICur p, A arg)
    {
      NSPrefix nsprefix_ = p.nsprefix_.accept(this, arg);
      String ident_ = p.ident_;

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.URICur(nsprefix_, ident_);
    }

/* Literal */
    public Literal visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitQVar p, A arg)
    {
      String ident_ = p.ident_;

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.LitQVar(ident_);
    }
    public Literal visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitQVarAn p, A arg)
    {

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.LitQVarAn();
    }
    public Literal visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitSVarD p, A arg)
    {
      String ident_ = p.ident_;
      ListIdent listident_ = p.listident_;

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.LitSVarD(ident_, listident_);
    }
    public Literal visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitSVarI p, A arg)
    {
      String ident_ = p.ident_;

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.LitSVarI(ident_);
    }
    public Literal visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitURI p, A arg)
    {
      URIRef uriref_ = p.uriref_.accept(this, arg);

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.LitURI(uriref_);
    }
    public Literal visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitInt p, A arg)
    {
      Integer integer_ = p.integer_;

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.LitInt(integer_);
    }
    public Literal visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitDbl p, A arg)
    {
      Double double_ = p.double_;

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.LitDbl(double_);
    }
    public Literal visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitString p, A arg)
    {
      String string_ = p.string_;

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.LitString(string_);
    }
    public Literal visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitTrue p, A arg)
    {

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.LitTrue();
    }
    public Literal visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitFalse p, A arg)
    {

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.LitFalse();
    }

/* NSPrefix */
    public NSPrefix visit(com.googlecode.opennars.parser.loan.Loan.Absyn.NSPrefix1 p, A arg)
    {
      String ident_ = p.ident_;

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.NSPrefix1(ident_);
    }
    public NSPrefix visit(com.googlecode.opennars.parser.loan.Loan.Absyn.NSPrefix2 p, A arg)
    {

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.NSPrefix2();
    }

/* TruthValue */
    public TruthValue visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TruthE p, A arg)
    {

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.TruthE();
    }
    public TruthValue visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TruthF p, A arg)
    {
      Double double_ = p.double_;

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.TruthF(double_);
    }
    public TruthValue visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TruthFC p, A arg)
    {
      Double double_1 = p.double_1;
      Double double_2 = p.double_2;

      return new com.googlecode.opennars.parser.loan.Loan.Absyn.TruthFC(double_1, double_2);
    }

}