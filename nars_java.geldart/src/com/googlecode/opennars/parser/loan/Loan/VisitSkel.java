package com.googlecode.opennars.parser.loan.Loan;
import com.googlecode.opennars.parser.loan.Loan.Absyn.*;
/*** BNFC-Generated Visitor Design Pattern Skeleton. ***/
/* This implements the common visitor design pattern.
   Tests show it to be slightly less efficient than the
   instanceof method, but easier to use. 
   Replace the R and A parameters with the desired return
   and context types.*/

public class VisitSkel
{
  public class DocumentVisitor<R,A> implements Document.Visitor<R,A>
  {
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.DocBR p, A arg)
    {
      /* Code For DocBR Goes Here */

      p.baserule_.accept(new BaseRuleVisitor<R,A>(), arg);
      for (Sentence x : p.listsentence_) {
      }

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.Doc p, A arg)
    {
      /* Code For Doc Goes Here */

      for (Sentence x : p.listsentence_) {
      }

      return null;
    }

  }
  public class BaseRuleVisitor<R,A> implements BaseRule.Visitor<R,A>
  {
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.BaseR p, A arg)
    {
      /* Code For BaseR Goes Here */

      //p.urilit_;

      return null;
    }

  }
  public class SentenceVisitor<R,A> implements Sentence.Visitor<R,A>
  {
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.SentPrefix p, A arg)
    {
      /* Code For SentPrefix Goes Here */

      p.nsprefix_.accept(new NSPrefixVisitor<R,A>(), arg);
      //p.urilit_;

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.SentImport p, A arg)
    {
      /* Code For SentImport Goes Here */

      //p.urilit_;

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.SentDelay p, A arg)
    {
      /* Code For SentDelay Goes Here */

      //p.integer_;

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.SentOp p, A arg)
    {
      /* Code For SentOp Goes Here */

      p.uriref_.accept(new URIRefVisitor<R,A>(), arg);

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.SentJudge p, A arg)
    {
      /* Code For SentJudge Goes Here */

      p.stm_.accept(new StmVisitor<R,A>(), arg);
      p.truthvalue_.accept(new TruthValueVisitor<R,A>(), arg);
      p.budget_.accept(new BudgetVisitor<R,A>(), arg);

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.SentQuest p, A arg)
    {
      /* Code For SentQuest Goes Here */

      p.stm_.accept(new StmVisitor<R,A>(), arg);
      p.budget_.accept(new BudgetVisitor<R,A>(), arg);

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.SentGoal p, A arg)
    {
      /* Code For SentGoal Goes Here */

      p.stm_.accept(new StmVisitor<R,A>(), arg);
      p.truthvalue_.accept(new TruthValueVisitor<R,A>(), arg);
      p.budget_.accept(new BudgetVisitor<R,A>(), arg);

      return null;
    }

  }
  public class BudgetVisitor<R,A> implements Budget.Visitor<R,A>
  {
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.BudgetE p, A arg)
    {
      /* Code For BudgetE Goes Here */


      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.BudgetP p, A arg)
    {
      /* Code For BudgetP Goes Here */

      //p.double_;

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.BudgetPD p, A arg)
    {
      /* Code For BudgetPD Goes Here */

      //p.double_1;
      //p.double_2;

      return null;
    }

  }
  public class StmVisitor<R,A> implements Stm.Visitor<R,A>
  {
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpl p, A arg)
    {
      /* Code For StmImpl Goes Here */

      p.stm_1.accept(new StmVisitor<R,A>(), arg);
      p.stm_2.accept(new StmVisitor<R,A>(), arg);

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmEquiv p, A arg)
    {
      /* Code For StmEquiv Goes Here */

      p.stm_1.accept(new StmVisitor<R,A>(), arg);
      p.stm_2.accept(new StmVisitor<R,A>(), arg);

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpPred p, A arg)
    {
      /* Code For StmImpPred Goes Here */

      p.stm_1.accept(new StmVisitor<R,A>(), arg);
      p.stm_2.accept(new StmVisitor<R,A>(), arg);

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpRet p, A arg)
    {
      /* Code For StmImpRet Goes Here */

      p.stm_1.accept(new StmVisitor<R,A>(), arg);
      p.stm_2.accept(new StmVisitor<R,A>(), arg);

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpConc p, A arg)
    {
      /* Code For StmImpConc Goes Here */

      p.stm_1.accept(new StmVisitor<R,A>(), arg);
      p.stm_2.accept(new StmVisitor<R,A>(), arg);

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmEqvPred p, A arg)
    {
      /* Code For StmEqvPred Goes Here */

      p.stm_1.accept(new StmVisitor<R,A>(), arg);
      p.stm_2.accept(new StmVisitor<R,A>(), arg);

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmEqvConc p, A arg)
    {
      /* Code For StmEqvConc Goes Here */

      p.stm_1.accept(new StmVisitor<R,A>(), arg);
      p.stm_2.accept(new StmVisitor<R,A>(), arg);

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmConj p, A arg)
    {
      /* Code For StmConj Goes Here */

      p.stm_1.accept(new StmVisitor<R,A>(), arg);
      p.stm_2.accept(new StmVisitor<R,A>(), arg);

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmDisj p, A arg)
    {
      /* Code For StmDisj Goes Here */

      p.stm_1.accept(new StmVisitor<R,A>(), arg);
      p.stm_2.accept(new StmVisitor<R,A>(), arg);

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmPar p, A arg)
    {
      /* Code For StmPar Goes Here */

      p.stm_1.accept(new StmVisitor<R,A>(), arg);
      p.stm_2.accept(new StmVisitor<R,A>(), arg);

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmSeq p, A arg)
    {
      /* Code For StmSeq Goes Here */

      p.stm_1.accept(new StmVisitor<R,A>(), arg);
      p.stm_2.accept(new StmVisitor<R,A>(), arg);

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmNot p, A arg)
    {
      /* Code For StmNot Goes Here */

      p.stm_.accept(new StmVisitor<R,A>(), arg);

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmPst p, A arg)
    {
      /* Code For StmPst Goes Here */

      p.stm_.accept(new StmVisitor<R,A>(), arg);

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmPres p, A arg)
    {
      /* Code For StmPres Goes Here */

      p.stm_.accept(new StmVisitor<R,A>(), arg);

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmFut p, A arg)
    {
      /* Code For StmFut Goes Here */

      p.stm_.accept(new StmVisitor<R,A>(), arg);

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmInher p, A arg)
    {
      /* Code For StmInher Goes Here */

      p.term_1.accept(new TermVisitor<R,A>(), arg);
      p.term_2.accept(new TermVisitor<R,A>(), arg);

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmSim p, A arg)
    {
      /* Code For StmSim Goes Here */

      p.term_1.accept(new TermVisitor<R,A>(), arg);
      p.term_2.accept(new TermVisitor<R,A>(), arg);

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmInst p, A arg)
    {
      /* Code For StmInst Goes Here */

      p.term_1.accept(new TermVisitor<R,A>(), arg);
      p.term_2.accept(new TermVisitor<R,A>(), arg);

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmProp p, A arg)
    {
      /* Code For StmProp Goes Here */

      p.term_1.accept(new TermVisitor<R,A>(), arg);
      p.term_2.accept(new TermVisitor<R,A>(), arg);

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmInPp p, A arg)
    {
      /* Code For StmInPp Goes Here */

      p.term_1.accept(new TermVisitor<R,A>(), arg);
      p.term_2.accept(new TermVisitor<R,A>(), arg);

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmOp p, A arg)
    {
      /* Code For StmOp Goes Here */

      p.term_.accept(new TermVisitor<R,A>(), arg);
      for (Term x : p.listterm_) {
      }

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmTrm p, A arg)
    {
      /* Code For StmTrm Goes Here */

      p.term_.accept(new TermVisitor<R,A>(), arg);

      return null;
    }

  }
  public class TermVisitor<R,A> implements Term.Visitor<R,A>
  {
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExInt p, A arg)
    {
      /* Code For TrmExInt Goes Here */

      p.term_1.accept(new TermVisitor<R,A>(), arg);
      p.term_2.accept(new TermVisitor<R,A>(), arg);

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInInt p, A arg)
    {
      /* Code For TrmInInt Goes Here */

      p.term_1.accept(new TermVisitor<R,A>(), arg);
      p.term_2.accept(new TermVisitor<R,A>(), arg);

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExDif p, A arg)
    {
      /* Code For TrmExDif Goes Here */

      p.term_1.accept(new TermVisitor<R,A>(), arg);
      p.term_2.accept(new TermVisitor<R,A>(), arg);

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInDif p, A arg)
    {
      /* Code For TrmInDif Goes Here */

      p.term_1.accept(new TermVisitor<R,A>(), arg);
      p.term_2.accept(new TermVisitor<R,A>(), arg);

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExImg p, A arg)
    {
      /* Code For TrmExImg Goes Here */

      p.term_.accept(new TermVisitor<R,A>(), arg);
      for (Term x : p.listterm_1) {
      }
      for (Term x : p.listterm_2) {
      }

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInImg p, A arg)
    {
      /* Code For TrmInImg Goes Here */

      p.term_.accept(new TermVisitor<R,A>(), arg);
      for (Term x : p.listterm_1) {
      }
      for (Term x : p.listterm_2) {
      }

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExSet p, A arg)
    {
      /* Code For TrmExSet Goes Here */

      for (Term x : p.listterm_) {
      }

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInSet p, A arg)
    {
      /* Code For TrmInSet Goes Here */

      for (Term x : p.listterm_) {
      }

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmProd p, A arg)
    {
      /* Code For TrmProd Goes Here */

      for (Term x : p.listterm_) {
      }

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmLit p, A arg)
    {
      /* Code For TrmLit Goes Here */

      p.literal_.accept(new LiteralVisitor<R,A>(), arg);

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmStm p, A arg)
    {
      /* Code For TrmStm Goes Here */

      p.stm_.accept(new StmVisitor<R,A>(), arg);

      return null;
    }

  }
  public class URIRefVisitor<R,A> implements URIRef.Visitor<R,A>
  {
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.URIFul p, A arg)
    {
      /* Code For URIFul Goes Here */

      //p.urilit_;

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.URICur p, A arg)
    {
      /* Code For URICur Goes Here */

      p.nsprefix_.accept(new NSPrefixVisitor<R,A>(), arg);
      //p.ident_;

      return null;
    }

  }
  public class LiteralVisitor<R,A> implements Literal.Visitor<R,A>
  {
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitQVar p, A arg)
    {
      /* Code For LitQVar Goes Here */

      //p.ident_;

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitQVarAn p, A arg)
    {
      /* Code For LitQVarAn Goes Here */


      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitSVarD p, A arg)
    {
      /* Code For LitSVarD Goes Here */

      //p.ident_;
      for (String x : p.listident_) {
      }

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitSVarI p, A arg)
    {
      /* Code For LitSVarI Goes Here */

      //p.ident_;

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitURI p, A arg)
    {
      /* Code For LitURI Goes Here */

      p.uriref_.accept(new URIRefVisitor<R,A>(), arg);

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitInt p, A arg)
    {
      /* Code For LitInt Goes Here */

      //p.integer_;

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitDbl p, A arg)
    {
      /* Code For LitDbl Goes Here */

      //p.double_;

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitString p, A arg)
    {
      /* Code For LitString Goes Here */

      //p.string_;

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitTrue p, A arg)
    {
      /* Code For LitTrue Goes Here */


      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitFalse p, A arg)
    {
      /* Code For LitFalse Goes Here */


      return null;
    }

  }
  public class NSPrefixVisitor<R,A> implements NSPrefix.Visitor<R,A>
  {
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.NSPrefix1 p, A arg)
    {
      /* Code For NSPrefix1 Goes Here */

      //p.ident_;

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.NSPrefix2 p, A arg)
    {
      /* Code For NSPrefix2 Goes Here */


      return null;
    }

  }
  public class TruthValueVisitor<R,A> implements TruthValue.Visitor<R,A>
  {
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TruthE p, A arg)
    {
      /* Code For TruthE Goes Here */


      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TruthF p, A arg)
    {
      /* Code For TruthF Goes Here */

      //p.double_;

      return null;
    }
    public R visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TruthFC p, A arg)
    {
      /* Code For TruthFC Goes Here */

      //p.double_1;
      //p.double_2;

      return null;
    }

  }
}